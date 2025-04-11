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
        System.exit(0)
    }

    def cliOptions = cli.parse(args)

    if (!cliOptions) {
        System.exit(-1)
    }

    new lib().execute(new opts().fromCli(cliOptions))
}

if (['-w', '--warm-up'].intersect(args as List)) {
    org.crac.Core.globalContext.register([
        beforeCheckpoint: {},
        afterRestore: { def ctx ->
            if (System.getenv('SIDELOAD_DRIVERS') == 'true') {
                def drivers = new File(System.getenv('DRIVERS_DIR') ?: '/drivers').listFiles(
                    [accept: { it.name.endsWith('.jar') }] as FileFilter
                ).with {
                    it.collect { def jar ->
                        def jarFile = new java.util.jar.JarFile(jar)
                        def driverClassName = jarFile.getEntry('META-INF/services/java.sql.Driver')?.with {
                            // pick the first non empty line
                            /* groovylint-disable-next-line NestedBlockDepth */
                            def s = jarFile.getInputStream(it)
                            s.readLines().find { it.trim() }?.trim()
                        }
                        [driver: driverClassName, url: jar.toURI().toURL()]
                    }
                }

                def driversClassLoader = new URLClassLoader(
                    drivers*.url as URL[],
                    this.class.classLoader
                )
                Thread.currentThread().setContextClassLoader(driversClassLoader)

                drivers.findAll { it.driver }.collect { it.driver }.each {
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
