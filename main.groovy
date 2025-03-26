#!/usr/bin/env groovy

import groovy.cli.commons.CliBuilder

def defaultSourceUrl = 'jdbc:relique:csv:/data'
def defaultSourceTable = '123table'
def defaultSourceQuery = 'SELECT * FROM <<source-table>>'
def defaultBatchSize = 100

def cli = new CliBuilder(
    header: '123table is a command line tool making it easy to load rows into a database.',
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
    table(longOpt: 'target-table', 'Target table name', args: 1, required: true)
    query(
        longOpt: 'source-query', "Source query. [defaults to '${defaultSourceQuery}']",
        args: 1,
        defaultValue: defaultSourceQuery
    )
    batch(
        longOpt: 'batch-size', "Batch size. [defaults to '${defaultBatchSize}']",
        args: 1,
        type: Integer,
        defaultValue: defaultBatchSize
    )
    h(longOpt: 'help', 'Usage Information')
}

def cliOptions = cli.parse(args)

if (!cliOptions) {
    /* groovylint-disable-next-line SystemExit */
    System.exit(-1)
}

if (cliOptions.help) {
    cli.usage()
    /* groovylint-disable-next-line SystemExit */
    System.exit(0)
}

new lib().execute(cliOptions)
