import groovy.sql.Sql
import java.sql.Driver
import java.sql.DriverManager
import java.util.ServiceLoader

def getDriverClassName (String url){
    if (!url) return null
    
    def drivers

    // Register drivers and get the first one that accepts the URL
    if (!drivers){
        drivers = ServiceLoader.load(Driver)
    }

    def driver = drivers.find { driver ->
        try {
            driver.acceptsURL(url)
        } catch (e) {
            false
        }
    }
    println driver
    driver?.getClass()?.name
}

def execute(def cliOptions){
    def sourceDbUrl = cliOptions['source-db-url']
    def sourceDbDriver = cliOptions['source-db-driver'] ?: getDriverClassName(sourceDbUrl)
    Sql.withInstance(
        sourceDbUrl,
        cliOptions['source-db-user'] ?: null,
        cliOptions['source-db-password'] ?: null,
        sourceDbDriver ?: null
    ) { def sourceSql ->
        println 'ciao'
    }
}