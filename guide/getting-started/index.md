[Home](/123table)
-
[Up](./)

# Getting started with 123table

## Copy CSV contents to a new table

Given a CSV file named _foo.csv_ in the current directory, run

<pre>
docker run --rm -it \
  -v $(pwd):/data ghcr.io/davidecavestro/123table:main-fast \
  -stable foo \
  -create \
  -url jdbc:sqlite:/data/foo.db
</pre>
to load its rows into a newly created <i>foo</i> table of a sqlite db.
<br>
Replace the <code>-url</code> value with the proper JDBC url for your target db. 
<br>
Use the <code>--help</code> flag to get the full list of options.

## Copy CSV rows to a table transforming values

The following command loads a CSV capitalizing both the <em>firstname</em> and
<em>surname</rm> fields

<pre>
docker run --rm -it \
  -v $(pwd):/data ghcr.io/davidecavestro/123table:main-fast \
  -stable foo \
  -url jdbc:sqlite:/data/foo.db
  --mapper '[{ "name": "firstname", "expr": "orig.capitalize()" }, \
    { "name": "surname", "expr": "orig.capitalize()" }]'
</pre>

Consider using a file for complex mappings.
See [Mapping fields](/123table/guide/getting-started/mapper.html) for more details.

[Home](/123table)