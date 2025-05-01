#!/usr/bin/env groovy

import groovy.cli.commons.CliBuilder

def defaultSourceUrl = 'jdbc:relique:csv:/data'
def defaultSourceTable = 'table'
def defaultSourceQuery = 'SELECT * FROM <<source-table>>'
def defaultBatchSize = 100

// println "PID = ${ProcessHandle.current().pid()}"

def cli = new CliBuilder(
    header: '123Table is a command line tool that makes it easy to load rows into a database table.',
    usage:'123t [options] -url jdbc:sqlite:/data/foo.db -stable foo -create',
    width: -1
).tap {
    surl(
        longOpt: 'source-db-url', "Source JDBC url. [defaults to '${defaultSourceUrl}']",
        args: 1,
        defaultValue: defaultSourceUrl
    )
    url(
        longOpt: 'target-db-url', 'Target JDBC url', args: 1, required: true
    )
    stable(
        longOpt: 'source-table', "Source table name. [defaults to '${defaultSourceTable}']",
        args: 1,
        defaultValue: defaultSourceTable
    )
    table(longOpt: 'target-table', 'Target table name. [defaults to <<source-table>>]', args: 1)
    query(
        longOpt: 'source-query', "Source query. [defaults to '${defaultSourceQuery}']",
        args: 1
    )
    batch(
        longOpt: 'batch-size', "Batch size. [defaults to '${defaultBatchSize}']",
        args: 1
    )
    create(longOpt: 'create-table', 'Create the target table')
    trunc(longOpt: 'truncate-table', 'Truncate the target table')
    dry(longOpt: 'dry-run', 'Just mimic write actions, avoid making any changes to the target db')
    h(longOpt: 'help', 'Usage Information')

    mapper(
        longOpt: 'mapper', 'Field.mapper: json list of objects structured as: ' +
        'from: <source field> '+
        'to: <target field> ' +
        'name: <alternative to from/to> ' +
        'type: used for table auto create ' +
        'expr: simple expression returning the value (a groovy expression with access to "orig" value)' +
        'calc: calculator returning the value. A groovy closure i.e. { def orig, def row -> orig?.toLowerCase() }',
        args: 1
    )
    mfile(
        longOpt: 'mapper-file', 'Json file for field.mapping: see the "mapper" flag',
        args: 1
    )
    w(
        longOpt: 'warm-up', 'Generate a CRaC checkpoint that at restore reads args from STDIN',
        args: 1
    )
}

def processArgs = {def args ->
    if (['-h', '--help'].intersect(args as List)) {
        cli.usage()
    } else {
        def cliOptions = cli.parse(args)

        if (cliOptions) {
            new lib().execute(new Opts().fromCli(cliOptions))
        } else {
            /* groovylint-disable-next-line SystemExit */
            System.exit(-1)
        }
    }
}

if (['-w', '--warm-up'].intersect(args as List)) {
    org.crac.Core.globalContext.register([
        beforeCheckpoint: { },
        afterRestore: { def ctx ->
            def loader = new Loader()
            if (System.getenv('SIDELOAD_DRIVERS') == 'true') {
                loader.sideLoadDrivers(System.getenv('DRIVERS_DIR') ?: '/drivers')
            }
            def stdinArgs = loader.getArgs(System.in)
            processArgs stdinArgs
        },
    ] as org.crac.Resource)

    org.crac.Core.checkpointRestore()
} else {
    processArgs args
}
