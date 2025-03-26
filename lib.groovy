// @Grab('net.sourceforge.csvjdbc:csvjdbc:1.0.46')

import groovy.sql.Sql
import java.sql.Driver

def getDriverClassName(String url) {
    if (!url) return null

    def drivers

    // Register drivers and get the first one that accepts the URL
    if (!drivers) {
        drivers = ServiceLoader.load(Driver)
    }

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
    // def sourceDbUrl = cliOptions.'source-db-url'
    // if (sourceDbUrl.startsWith('jdbc:relique:csv:')){
    //     println 'Loading CSV JDBC driver'
    //     Class.forName('org.relique.jdbc.csv.CsvDriver')
    // }
    // def sourceDbDriver = cliOptions.'source-db-driver' ?: getDriverClassName(sourceDbUrl)
    opts.tap {
println "sourceDbUrl: ${sourceDbUrl}"

        Sql.withInstance(
            sourceDbUrl,
            sourceDbUser,
            sourceDbPassword,
        ) { def sourceDb ->
            def targetFields = sourceDb.firstRow(sourceQuery)?.collect { def name, def value ->
                [name: name, type: 'VARCHAR']
            }
            // def targetFields = sourceDb.query(sourceQuery) {def rs ->
            //     def ret
            //     if (rs.next()){
            //         ret = rs.getMetaData()?.with { def md ->
            //             (1..md.columnCount).collectEntries { [name: md.getColumnName(it)] }
            //         }
            //         if (!ret){
            //             ret = 
            //         }
            //     }
            //     ret
            // }
            if (!targetFields) {
                println 'No source row found (empty table?)'
                return
            }

            println "Using targetFields: ${targetFields}" 
            def targetQuery = """
                INSERT INTO ${targetTable}
                (${ targetFields.collect { it.name }.join(', ') })
                VALUES
                (${ targetFields.collect { '?' }.join(', ') })
            """ as String

            Sql.withInstance(
                targetDbUrl,
                targetDbUser,
                targetDbPassword,
            ) { def targetDb ->
                if (createTable) {
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
    targetFields.collect {
        row[it.name]
    }
}
