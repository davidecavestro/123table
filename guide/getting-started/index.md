[Home](/123table) - [Up](./)

# Getting started with 123table

## Copy CSV contents to a new table

Given a CSV file named _foo.csv_ in the current directory, run

```bash
docker run --rm -it \
  -v $(pwd):/data ghcr.io/davidecavestro/123table:main-fast \
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
  -v $(pwd):/data ghcr.io/davidecavestro/123table:main-fast \
  -stable foo \
  -url jdbc:sqlite:/data/foo.db
  --mapper '[{ "name": "firstname", "expr": "orig.capitalize()" }, \
    { "name": "surname", "expr": "orig.capitalize()" }]'
```

Consider using a file for complex mappings.
See [Mapping fields](/123table/guide/getting-started/mapper.html) for more details.
