
@Grab('org.spockframework:spock-core:2.3-groovy-4.0')
@Grab('org.junit.platform:junit-platform-reporting:1.11.4')

import groovy.sql.*
import java.time.*
import java.time.format.*
import spock.lang.Specification

class OmniSpec extends Specification {
    def "should get correct driver class name for postgres"() {
        given: "a Lib instance"
        def lib = new lib()

        when: "getting driver class name for postgresql"
        def driver = lib.getDriverClassName("jdbc:postgresql://localhost/mydb")

        then: "should return postgresql driver"
        driver == "org.postgresql.Driver"
    }

    def "should get correct driver class name for mssql"() {
        given: "a Lib instance"
        def lib = new lib()

        when: "getting driver class name for mssql"
        def driver = lib.getDriverClassName(
            /* groovylint-disable-next-line LineLength */
            "jdbc:sqlserver://localhost:1433;databaseName=AdventureWorks;user=MyUserName;password=<password>;encrypt=false;"
        )

        then: "should return mssql driver"
        driver == "com.microsoft.sqlserver.jdbc.SQLServerDriver"
    }

    def "should get correct driver class name for oracle"() {
        given: "a Lib instance"
        def lib = new lib()

        when: "getting driver class name for oracle"
        def driver = lib.getDriverClassName("jdbc:oracle:thin:@//myhost.mydomain.com:1521/mydb")

        then: "should return oracle driver"
        driver == "oracle.jdbc.OracleDriver"
    }

    def "should get correct driver class name for H2"() {
        given: "a Lib instance"
        def lib = new lib()

        when: "getting driver class name for H2"
        def driver = lib.getDriverClassName("jdbc:h2:mem:testdb")

        then: "should return H2 driver"
        driver == "org.h2.Driver"
    }

    def "should insert a csv to h2"() {
        given: "a csv file and opts to load it"
        def lib = new lib()
        def csvFolder = new File('data')
        csvFolder.mkdirs()
        def csvFile = new File(csvFolder, 'foo.csv')
        csvFile.text = ['id,name', '1,foo', '2,bar'].join('\n')

        def opts = new Opts().tap{
            sourceDbUrl = "jdbc:relique:csv:${csvFolder.absolutePath}"
            sourceTable = 'foo'
            sourceQuery = 'SELECT * FROM foo'
            targetDbUrl = 'jdbc:h2:mem:testdb'
            targetTable = 'foo'
            createTable = true
        }

        when: "execute is called with opts"
        lib.execute(opts)

        then: "the H2 table should be populated"
        def expected = [
            [id: 1, name: 'foo'],
            [id: 2, name: 'bar']
        ]
        Sql.withInstance(opts.sourceDbUrl) { def sql ->
            def actual = sql.rows('SELECT id, name FROM foo')
            actual.size() == 2
            actual.collect { [id: it.id, name: it.name] } == expected
        }
    }

    def "should be able to remap fields to datetime"(){
        given: "a mapper instance translating a field to local datetime"
        def lib = new lib()
        def mapper = [
            [
                from: 'start',
                to: 'START',
                type: 'TIMESTAMP',
                calc: '''
                    import java.time.*
                    import java.time.format.*

                    { def orig, def row ->
                        LocalDateTime.parse(orig, DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm'))
                    }
                '''
            ],
            [
                name: 'code',
                calc: '''
                    import java.time.*
                    import java.time.format.*

                    { def orig, def row ->
                        LocalDateTime.parse(orig, DateTimeFormatter.ofPattern('yyyy-MM-dd HH:mm'))
                    }
                '''
            ],
        ]

        when: "remaping fields"
        def targetFields = lib.mapTargetFields(mapper)

        then: "should return a datetime given a closure doing so"
        targetFields[0].toValue([start: '2025-01-01 00:00']) == LocalDateTime.parse(
            '2025-01-01 00:00',
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        )
    }

    def "should be able to remap fields using an expression"(){
        given: "a mapper instance translating a field to lowecase"
        def lib = new lib()
        def mapper = [
            [
                name: 'code',
                expr: 'orig.toLowerCase()'
            ],
        ]
        def row = [
            code: 'ABC'
        ]

        when: "remaping fields"
        def targetFields = lib.mapTargetFields(mapper)

        then: "should return a lowercase value given an expression doing so"
        targetFields[0].toValue(row) == 'abc'
    }

    def "the cli should show help"() {
        given: "a main instance"
        def main = new main()

        when: "invoked passing -h"
        main.run(new File('main.groovy'), '-h')

        then: "it doesn't fail"
    }

    def "cli opts should be parsed into Opts"() {
        given: "cli opts"
        def cliOptions = [
            'source-db-url': 'jdbc:my//bar',
            'source-db-user': 'foo',
            'source-db-password': 'bar',
            'source-table': 'foo',
            'source-query': 'SELECT foo',
            'target-db-url': 'jdbc:your//baz',
            'target-db-user': 'bat',
            'target-db-password': 'man',
            'target-table': 'tbl',
            'batch-size': '1',
        ]
        def opts = new Opts()
        def extracted

        when: "cli opts are extracted"
        extracted = opts.fromCli(cliOptions)

        then: "extracted opts match cli ones"
        extracted.sourceDbUrl == cliOptions.'source-db-url'
    }

    def "cli opts should be parsed into Opts with defaults"() {
        given: "cli opts"
        def cliOptions = [
            'source-db-url': 'jdbc:my//bar',
            'source-table': 'foo',
            'target-db-url': 'jdbc:your//baz'
        ]
        def opts = new Opts()
        def extracted

        when: "cli opts are extracted"
        extracted = opts.fromCli(cliOptions)

        then: "extracted opts match cli ones"
        extracted.sourceDbUrl == cliOptions.'source-db-url'
    }

    def "cli opts should parse empty mapper"() {
        given: "cli opts"
        def cliOptions = [
            'mapper': '{}'
        ]
        def opts = new Opts()
        def extracted

        when: "mapper is extracted from cliOps"
        extracted = opts.parseMapper(cliOptions)

        then: "extracted opts match cli ones"
        extracted.mapper == [:]
    }

    def "drivers should be side-loadable"() {
        given: "a driver Loader"
        def lib = new lib()
        def loader = new Loader()

        when: "the loader side-loads the drivers"
        loader.sideLoadDrivers('/drivers')
        def driver = lib.getDriverClassName("jdbc:postgresql://localhost/mydb")

        then: "the driver should be available"
        driver == "org.postgresql.Driver"
    }


    def "drivers should be wrappable"() {
        given: "a wrapped driver"
        def driver = Mock(java.sql.Driver)
        def wrapper = new Wrapper(wrapped: driver)

        when: "the wrapped is called"
        wrapper.getMinorVersion()
        wrapper.getMajorVersion()
        wrapper.jdbcCompliant()
        wrapper.acceptsURL('jdbc.foo//bar')
        wrapper.connect(null, null)
        wrapper.getPropertyInfo(null, null)
        wrapper.getParentLogger()

        then: "the wrapper should turn calls to the wrapped driver"
        1 * driver.getMinorVersion()
        1 * driver.getMajorVersion()
        1 * driver.jdbcCompliant()
        1 * driver.acceptsURL('jdbc.foo//bar')
        1 * driver.connect(null, null)
        1 * driver.getPropertyInfo(null, null)
        1 * driver.getParentLogger()
    }

    def "gets args from stdin"() {
        given: "a Loader"
        def loader = new Loader()

        when: "args are extracted from an inputstream"
        def args = loader.getArgs(new ByteArrayInputStream('-foo --bar'.getBytes( 'UTF-8' )))

        then: "args are available"
        args[0] == '-foo'
        args[1] == '--bar'
    }
}
