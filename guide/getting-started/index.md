# Getting started with 123table

_123table_ is distributed as a container image, so the easiest
way to use it is through [docker](https://docs.docker.com/get-started/)
or [podman](https://podman.io/get-started).


## Concepts

_123table_ offers a command line interface, so that it can be launched
from a terminal or a script.

The simplest run of _123table_ does one simple thing:
inserts into the target db table every row it reads from the source table.

_123table_ can use any data source as the db, provided that you have the
appropriate JDBC driver.

A special data source is CSV, because the CSV JDBC driver can read any
CSV file as a db table.

See [Drivers](/123table/guide/drivers.html) for more details about JDBC drivers. 


## Copy CSV contents to a new table

Given a CSV file named _foo.csv_ in the current directory, run

```bash
docker run --rm -it \
  -v $(pwd):/data ghcr.io/davidecavestro/123table:fast-latest \
  -stable foo \
  -create \
  -url jdbc:sqlite:/data/foo.db
```
to load its rows into a newly created _foo_ table of a sqlite db.
<br>
Replace the `-url` value with the proper JDBC url for your target db. 
<br>
Use the `--help` flag to get the full list of options.

## Copy CSV rows to a table transforming values

The following command loads a CSV capitalizing both the _firstname_ and
_surname_ fields

```bash
docker run --rm -it \
  -v $(pwd):/data ghcr.io/davidecavestro/123table:fast-latest \
  -stable foo \
  -url jdbc:sqlite:/data/foo.db \
  --mapper '[{ "name": "firstname", "expr": "orig.capitalize()" }, \
    { "name": "surname", "expr": "orig.capitalize()" }]'
```

Consider using a file for complex mappings.
See [Mapping fields](/123table/guide/mapper.html) for more details.


## Truncate a table and add rows from another db

The following command truncates the _foo_ table on _target_ db, 
then loads the rows of the _bar_ table from _source_ db

```bash
docker run --rm -it \
  -v $(pwd):/data ghcr.io/davidecavestro/123table:fast-latest \
  -surl jdbc:sqlite:/data/source.db \
  -stable bar \
  -url jdbc:sqlite:/data/target.db \
  -table foo \
  -trunc
```