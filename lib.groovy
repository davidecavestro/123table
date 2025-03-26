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

def execute(def cliOptions) {
    def sourceDbUrl = cliOptions['source-db-url']
    def sourceDbDriver = cliOptions['source-db-driver'] ?: getDriverClassName(sourceDbUrl)
    Sql.withInstance(
        sourceDbUrl,
        cliOptions['source-db-user'] ?: null,
        cliOptions['source-db-password'] ?: null,
        sourceDbDriver ?: null
    ) { def sourceSql ->
        def sourceQuery = cliOptions['source-query']
        def batchSize = cliOptions['batch-size']
        def sourceTable = cliOptions['source-table']
        def targetTable = cliOptions['target-table']

        if (!sourceQuery) {
            sourceQuery = "SELECT * FROM ${sourceTable}"
        }
        def targetQuery = """
            INSERT INTO '${targetTable}'
            (${ targetFields*.name.join(', ') })
            VALUES
            (${ targetFields.collect { '?' }.join(', ') })
        """
        sql.withBatch(batchSize, targetQuery) { ps ->
            sql.eachRow(sourceQuery) { row ->
                ps.addBatch(targetFields*.value)
            }
        }
    }
}
