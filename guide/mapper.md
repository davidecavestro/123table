# Configuring the field mapper

Though *123table* is not an ETL tool, it supports remapping
fields to a different name or transforming the original
value to a different one in the written rows.

This can be done defining a mapper, that is a list of field
mappings, where each field mapping possibly specifies
- the source field name
- the target field name
- the field type
- how the target value is computed from the original one.


## Mapper format

The mapper is specified as a JSON list of objects. Each object
supports the following properties:

<dl>
<dt>from</dt>
<dd>
    Name of the source field
    (Mandatory unless using <code>name</code>)
</dd>
<dt>to</dt>
<dd>
    Name of the target field.
    (Mandatory unless using <code>name</code>)
</dd>
<dt>name</dt>
<dd>
    Common name for both source and target field.
    An alternative to <code>from</code>/<code>to</code> when
    the name is the same but the value should be computed.
</dd>
<dt>expr</dt>
<dd>
    A simple groovy expression (aka formula) deriving a value
    from the original one (available as variable <code>orig</code>).
    <br>
    i.e.
<pre>
orig.toLowerCase()
</pre>
</dd>
<dt>calc</dt>
<dd>
    A calculator (a groovy closure) deriving a value
    from the original one
    (available as <code>orig</code> along with the whole
    <code>row</code> map).
    <br>
    i.e.
<pre>
{ def orig, def row ->
    "${orig.toLowerCase()} (${row.surname})"
}
</pre>
</dd>
</dl>

## How to specify a mapper

A mapper may be passed as
<ul>
<li><code>--mapper</code> flag or <code>MAPPER</code> env var a json
value (useful mainly for small mappers)</li>
<li><code>--mapper-file</code> flag or <code>MAPPER_FILE</code> env
var: the path to a json file (provided that it is available to the
container via bind mount or a volume)</li>
</ul>

Example:

```bash
docker run ... \
  --mapper '[{ "name": "surname", "expr": "orig.capitalize()" }]
```
