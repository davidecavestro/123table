class Opts {
    def sourceDbUrl
    def sourceDbUser
    def sourceDbPassword
    def sourceTable
    def sourceQuery

    def targetDbUrl
    def targetDbUser
    def targetDbPassword
    def targetTable

    def batchSize = 100

    boolean createTable
    boolean truncateTable
    boolean dryRun
    boolean verbose

    def mapper = []

    def fromCli(def cliOptions) {
        sourceDbUrl = cliOptions.'source-db-url'
        sourceDbUser = cliOptions.'source-db-user' ?: null
        sourceDbPassword = cliOptions.'source-db-password' ?: null

        sourceTable = cliOptions.'source-table'
        sourceQuery = cliOptions.'source-query' ?: "SELECT * FROM ${sourceTable}" as String

        targetDbUrl = cliOptions.'target-db-url'
        targetDbUser = cliOptions.'target-db-user' ?: null
        targetDbPassword = cliOptions.'target-db-password' ?: null

        targetTable = cliOptions.'target-table' ?: sourceTable

        batchSize = cliOptions.'batch-size' ?: batchSize

        createTable = cliOptions.create
        truncateTable = cliOptions.trunc
        dryRun = cliOptions.dryRun
        verbose = cliOptions.verbose

        mapper = parseMapper(cliOptions)
        this
    }

    def parseMapper(def cliOptions) {
        def retVal = cliOptions.'mapper' ?: System.getenv('MAPPER')
        if (!retVal) {
            def mapperFile = cliOptions.'mapper-file' ?: System.getenv('MAPPER_FILE')?.with { new File(it) }
            mapperFile?.with {
                retVal = new groovy.json.JsonSlurper().parseFile(it)
            }
        }
        retVal
    }
}