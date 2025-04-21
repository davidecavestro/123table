# 123table and JDBC drivers

Since _123table_ is written in the Groovy language and the access to the
db is delegated to JDBC drivers, you can use any data source as the db,
provided that you have the appropriate JDBC driver.

A special data source is CSV, because the CSV JDBC driver can read any
CSV file as a db table.


## Packaged drivers

The included drivers are available within the `/drivers` folder.
<br>
Currently packaged drivers cover the following data sources:
- csv
- duckdb
- h2
- MS SQLserver
- MySQL
- Oracle
- PostgreSQL
- sqlite

If other drivers become needed, just mount a volume or bind-mount
a local folder containing the appropriate jars.

See [Image Flavours](flavours.html) for insights about image flavours
w/o packaged drivers.


## _Fast_ images JDBC drivers sideloading

Since _fast_ images under the hood restore a JVM image with the
JDBC drivers loaded at image build time, this would prevent _123table_
to find any new JDBC driver mounted at runtime.
That's why the two flavours of _fast_ images differ on the following items:

- `SIDELOAD_DRIVERS` env var: it's `true` for the _slim_ one,
to enable the discovery and load of JDBC drivers, while it is `false`
for the image with packaged drivers
- the classpath includes the jar files available within the `/drivers`
folder at image build time for the image with packaged drivers, while
the _slim_ image doesn't have any prepackaged driver

You can find more details about CRaC packaging at
[Image Flavours](flavours.html).
