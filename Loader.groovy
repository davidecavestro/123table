class Loader {

    def sideLoadDrivers (def driversDir) {
        def drivers = new File(driversDir).listFiles(
            [accept: { it.name.endsWith('.jar') }] as FileFilter
        ).collect { def jar ->
        println "Processing jar: ${jar}"
            def jarFile = new java.util.jar.JarFile(jar)
            def svcFile = jarFile.getEntry('META-INF/services/java.sql.Driver')
            // pick the first non empty line
            def driverClassName = svcFile ? jarFile.getInputStream(svcFile).readLines().find { it.trim() }?.trim() : null

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

    /**
     * Parse first input line as if thewy were args from main
     */
    def getArgs(InputStream input) {
        def line = input.newReader().readLines().first
        return (line =~ /"[^"]*"|'[^']*'|[^\s]+/).collect { 
            it.replaceAll(/^["']|["']$/,'')
        }.toArray()
    }
}