# 123table CLI

The easiest way to get the full list of options supported by _123table_
command line interface is launching it with the `--help` flag.


## CLI syntax

The following table sums up the options exposed by _123table_ CLI.

|  *Short / Long*              | *Default value*                       | *Description*                       |
|  :-------------------------- |:------------------------------------- | :---------------------------------- |
|  `-surl`/`--source-db-url`   | `jdbc:relique:csv:/data`              | Source JDBC url                     |
|  `-stable`/`--source-table`  | `table`                               | Source table name                   |
|  `-url`/`--target-db-url`    | -                                     | **Mandatory** - Target JDBC url     |
|  `-table`/`--target-table`   | The value of _source-table_ flag      | Target table name                   |
|  `-query`/`--source-query`   | `SELECT * FROM <<source-table>>`      | Source query                        |
|  `-batch`/`--batch-size`     | `100`                                 | Batch size                          |
|  `-create`/`--create-table`  | -                                     | Create the target table             |
|  `-trunc`/`--truncate-table` | -                                     | Truncate the target table           |
|  `-dry`/`--dry-run`          | -                                     | Avoid applying any changes          |
|  `-h`/`--help`               | -                                     | Show usage information              |
|  `-mapper`/`--mapper`        | -                                     | See [Mapping fields](mapper.html)   |
|  `-mfile`/`--mapper-file`    | -                                     | See the _mapper_ flag               |
|  `-w`/`--warm-up`            | -                                     | Generate a CRaC checkpoint.         |


Please note that the `-w`/`--warm-up` flags are meant for generating a
CRaC checkpoint to reduce _123table_ startup time.
For more details Check [_fast_ images](flavours.html).