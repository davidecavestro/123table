import java.sql.*

class Wrapper implements Driver {
    def wrapped

	boolean acceptsURL(String u) throws SQLException {
		wrapped.acceptsURL(u)
	}
	Connection connect(String u, Properties p) throws SQLException {
		wrapped.connect(u, p)
	}
	int getMajorVersion() {
		wrapped.getMajorVersion()
	}
	int getMinorVersion() {
		wrapped.getMinorVersion()
	}
	DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
		wrapped.getPropertyInfo(u, p)
	}
	boolean jdbcCompliant() {
		wrapped.jdbcCompliant()
	}
	java.util.logging.Logger getParentLogger() {
		wrapped.getParentLogger()
	}
}