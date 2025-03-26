/* groovylint-disable UnusedVariable */
def sourceDbUrl
def sourceDbUser
def sourceDbPassword

def sourceQuery
def batchSize
def sourceTable
def targetTable

boolean createTable
boolean truncateTable
boolean dryRun
boolean verbose

def fromCli(cliOptions) {
    sourceDbUrl = cliOptions.'source-db-url'
    sourceDbUser = cliOptions.'source-db-user' ?: null
    sourceDbPassword = cliOptions.'source-db-password' ?: null

    sourceTable = cliOptions.'source-table'
    sourceQuery = cliOptions.'source-query' ?: "SELECT * FROM ${sourceTable}" as String

    targetDbUrl = cliOptions.'target-db-url'
    targetDbUser = cliOptions.'target-db-user' ?: null
    targetDbPassword = cliOptions.'target-db-password' ?: null

    targetTable = cliOptions.'target-table' ?: sourceTable

    batchSize = cliOptions.'batch-size' ?: 100

    createTable = cliOptions.create
    truncateTable = cliOptions.truncate
    dryRun = cliOptions.dryRun
    verbose = cliOptions.verbose

    this
}
