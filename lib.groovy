import groovy.sql.Sql
import java.sql.Driver
import java.time.*
import java.time.format.*

def getDriverClassName(String url) {
    // Register drivers and get the first one that accepts the URL
    def drivers = ServiceLoader.load(Driver)

    def driver = drivers.find { driver ->
        try {
            driver.acceptsURL(url)
        } catch (e) {
            false
        }
    }

    driver?.getClass()?.name
}

def execute(def opts) {
    opts.tap {
// println "sourceDbUrl: ${sourceDbUrl}"

        Sql.withInstance(
            sourceDbUrl,
            sourceDbUser,
            sourceDbPassword,
        ) { def sourceDb ->
            def targetFields
            if (opts.mapper){
                targetFields = mapTargetFields(opts.mapper)
            } else { // no mapping info
                def aRow = sourceDb.firstRow(sourceQuery)
                targetFields = aRow.collect { def name, def value ->
                    [name: name, type: 'VARCHAR', toValue: { def row -> row[name] }]
                }
            }

            println "Using targetFields: ${targetFields}" 
            def targetQuery = """
                INSERT INTO ${targetTable}
                (${ targetFields*.name.join(', ') })
                VALUES
                (${ targetFields.collect { '?' }.join(', ') })
            """ as String

            Sql.withInstance(
                targetDbUrl,
                targetDbUser,
                targetDbPassword,
            ) { def targetDb ->
                if (createTable) {
                    if (!targetFields) {
                        throw new RuntimeException('Cannot create a table without any knowledge of the fields.')
                    }
                    println 'Creating table...'
                    targetDb.execute """
                        CREATE TABLE ${targetTable}
                        (${ targetFields.collect { it.name + ' ' + it.type }.join(', ') })
                    """ as String
                    println 'Table created'
                } else if (truncateTable){
                    println 'Truncating table...'
                    targetDb.execute """
                        TRUNCATE TABLE ${targetTable}
                    """ as String
                    println 'Table truncated'
                }
                def counter = 0
                targetDb.withBatch(batchSize, targetQuery) { ps ->
                    sourceDb.eachRow(sourceQuery) { row ->
                        println "${++counter}. Adding row ${row}"
                        ps.addBatch(toBatchParams(targetFields, row))
                    }
                }
                println "${counter} rows added to ${targetTable}"
            }
        }
    }
}

def toBatchParams(def targetFields, def row) {
    targetFields*.toValue(row)
}

def mapTargetFields(def mapper) {
    def shell = new GroovyShell()
    def closures = [:]

    mapper.collect { def mapping ->
        def srcField = mapping.from ?: mapping.name
        def toValue = { def row ->
            def orig = row[srcField]
            def expr = mapping.expr
            def calc = mapping.calc
            if (expr || calc) {
                // cache the closure
                def closure = closures[srcField]
                if (!closure) {
                    if (expr) { // wrap the parsed expression into a closure
                        def script = new GroovyShell(new Binding(orig: orig, row: row)).parse(expr)
                        closure = { _, __ -> script.run() }
                    } else { // parse the closure
                        closure = shell.evaluate(calc)
                    }
                    closures[srcField] = closure
                }
                closure(orig, row)
            } else {
                orig
            }
        }

        [
            name: mapping.to ?: mapping.name,
            type: mapping.type ?: 'VARCHAR',
            toValue: toValue
        ]
    }

}