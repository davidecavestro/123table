#!/usr/bin/env groovy
//@Grab('net.sourceforge.csvjdbc:csvjdbc:1.0.46')
import groovy.cli.commons.CliBuilder

def defaultSourceUrl = 'jdbc:relique:csv:/data'
def defaultSourceTable = '123table'
def defaultSourceQuery = 'SELECT * FROM 123table'
def defaultDoubleThing = '1.09'
def defaultBatchSize = "1000"

def cli = new CliBuilder(header: '123table', usage:'123table', width: -1)
cli.sd(longOpt: 'source-db-url', "Source JDBC url. [defaults to '${defaultSourceUrl}']", args: 1, defaultValue: defaultSourceUrl)
cli.td(longOpt: 'target-db-url', "Target JDBC url", args: 1)
// cli.st(longOpt: 'source-table', "Source Table. [defaults to '${defaultSourceTable}']", args: 1, defaultValue: defaultSourceTable)
// cli.tt(longOpt: 'target-table', "Target Table", args: 1)
cli.sq(longOpt: 'source-query', "Source query. [defaults to '${defaultSourceQuery}']", args: 1, defaultValue: defaultSourceQuery)
cli.tq(longOpt: 'target-query', "Target query", args: 1)

cli.bs(longOpt: 'batch-size', "Batch size. [defaults to '${defaultBatchSize}']", args: 1, defaultValue: defaultBatchSize)

// cli.dt(longOpt: 'doubleThing', "Some double. [defaults to '${defaultDoubleThing}']", args: 1, type: Double, defaultValue: defaultDoubleThing)
// cli.nt(longOpt: 'numberThing', "Some number. [defaults to '${defaultNumberThing}']", args: 1, defaultValue: defaultNumberThing)
cli.h(longOpt: 'help', 'Usage Information')

def cliOptions = cli.parse(args)

if (!cliOptions) {
  cli.usage()
  System.exit(-1)
}

// if (cliOptions.help) {
//   cli.usage()
//   System.exit(0)
// }

new lib().execute(cliOptions)