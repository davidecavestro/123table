#!/usr/bin/env groovy

import groovy.cli.commons.CliBuilder

def defaultSourceUrl = 'jdbc:relique:csv:/data'
def defaultSourceTable = 'table'
def defaultSourceQuery = 'SELECT * FROM <<source-table>>'
def defaultBatchSize = 100

println "PID = ${ProcessHandle.current().pid()}"

def cli = new CliBuilder(
    header: '123Table is a command line tool that makes it easy to load rows into a database table.',
    usage:'123t [options] -url jdbc:h2:mem:testdb -table mytable',
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
    truncate(longOpt: 'truncate-table', 'Truncate the target table')
    dry(longOpt: 'dry-run', 'Just mimic write actions, avoid making any changes to the target db')
    h(longOpt: 'help', 'Usage Information')
}

def processArgs = {def args ->
    if (['-h', '--help'].intersect(args as List)) {
        cli.usage()
        /* groovylint-disable-next-line SystemExit */
        System.exit(0)
    }

    def cliOptions = cli.parse(args)

    if (!cliOptions) {
        /* groovylint-disable-next-line SystemExit */
        System.exit(-1)
    }

    new lib().execute(new opts().fromCli(cliOptions))
// new lib().execute(cliOptions)
}

if (['-w', '--warm-up'].intersect(args as List)) {
    // Thread.currentThread().setContextClassLoader(
    //     new URLClassLoader(
    //         new File(System.getenv('DRIVERS_DIR') ?: '/drivers').listFiles(
    //             [accept: { it.name.endsWith('.jar') }] as FileFilter
    //         ).with {
    //             it.collect { it.toURI().toURL() }
    //         } as URL[],
    //         getClass().getClassLoader()
    //     )
    // )

    org.crac.Core.getGlobalContext().register([
        beforeCheckpoint: { def ctx ->
println 'beforeCheckpoint called'
        },
        afterRestore: { def ctx ->
            def input = System.in.newReader().readLines().first
            /* groovylint-disable-next-line UnnecessaryCollectCall */
            def stdinArgs = (input =~ /"[^"]*"|'[^']*'|[^\s]+/).collect { 
                it.replaceAll(/^["']|["']$/,'')
            }.toArray()
println "afterRestore called for ${stdinArgs.size()} args"
            processArgs stdinArgs
        },
    ] as org.crac.Resource)

    org.crac.Core.checkpointRestore()
} else {
    processArgs args
}
