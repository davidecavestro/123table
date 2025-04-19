#!/usr/bin/env groovy

import groovy.cli.commons.CliBuilder

def defaultSourceUrl = 'jdbc:relique:csv:/data'
def defaultSourceTable = 'table'
def defaultSourceQuery = 'SELECT * FROM <<source-table>>'
def defaultBatchSize = 100

println "PID = ${ProcessHandle.current().pid()}"

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
    truncate(longOpt: 'truncate-table', 'Truncate the target table')
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
        /* groovylint-disable-next-line SystemExit */
        System.exit(0)
    }

    def cliOptions = cli.parse(args)

    if (!cliOptions) {
        /* groovylint-disable-next-line SystemExit */
        System.exit(-1)
    }

    new lib().execute(new Opts().fromCli(cliOptions))
}

if (['-w', '--warm-up'].intersect(args as List)) {
    org.crac.Core.globalContext.register([
        beforeCheckpoint: { },
        afterRestore: { def ctx ->
            if (System.getenv('SIDELOAD_DRIVERS') == 'true') {
                def drivers = new File(System.getenv('DRIVERS_DIR') ?: '/drivers').listFiles(
                    [accept: { it.name.endsWith('.jar') }] as FileFilter
                ).collect { def jar ->
                    def jarFile = new java.util.jar.JarFile(jar)
                    def svcFile = jarFile.getEntry('META-INF/services/java.sql.Driver')
                    // pick the first non empty line
                    def driverClassName = jarFile.getInputStream(svcFile).readLines().find { it.trim() }?.trim()

                    [driver: driverClassName, url: jar.toURI().toURL()]
                }

                def driversClassLoader = new URLClassLoader(
                    drivers*.url as URL[],
                    this.class.classLoader
                )
                Thread.currentThread().setContextClassLoader(driversClassLoader)

                drivers.findAll { it.driver }*.driver.each {
                    def driverClass = driversClassLoader.loadClass(it)
                    println "Registering driver: ${ it }"
                    def driverShim = new Wrapper(wrapped: driverClass.newInstance())

                    java.sql.DriverManager.registerDriver(driverShim)
                }
            }
            def input = System.in.newReader().readLines().first
            /* groovylint-disable-next-line UnnecessaryCollectCall */
            def stdinArgs = (input =~ /"[^"]*"|'[^']*'|[^\s]+/).collect { 
                it.replaceAll(/^["']|["']$/,'')
            }.toArray()
// println "afterRestore called for ${stdinArgs.size()} args"
            processArgs stdinArgs
        },
    ] as org.crac.Resource)

    org.crac.Core.checkpointRestore()
} else {
    processArgs args
}
