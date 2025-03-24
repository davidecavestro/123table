
@Grab('org.spockframework:spock-core')
@Grab('org.jacoco:org.jacoco.core:0.8.12')

import spock.lang.Specification

class LibSpec extends Specification {
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
        def driver = lib.getDriverClassName("jdbc:sqlserver://localhost:1433;databaseName=AdventureWorks;user=MyUserName;password=<password>;encrypt=false;")

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

}