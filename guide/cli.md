# 123table CLI


## CLI syntax

The following table sums up the options exposed by _123table_ CLI.

|  Short / Long                | Default value                         | Description                         |
|  :-------------------------- |:------------------------------------- | :---------------------------------- |
|  `-surl`/`--source-db-url`   | `jdbc:relique:csv:/data`              | Source JDBC url                     |
|  `-stable`/`--source-table`  | `table`                               | Source table name                   |
|  `-url`/`--target-db-url`    | -                                     | **(Mandatory)** Target JDBC url     |
|  `-table`/`--target-table`   | The value of _source-table_ value     | Target table name                   |
|  `-query`/`--source-query`   | <tt>SELECT * FROM _source-table_</tt> | Source query                        |
|  `-batch`/`--batch-size`     | `100`                                 | Batch size                          |
|  `-create`/`--create-table`  | -                                     | Create the target table             |
|  `-trunc`/`--truncate-table` | -                                     | Truncate the target table           |
|  `-dry`/`--dry-run`          | -                                     | Avoid making any changes            |
|  `-h`  /`--help`             | -                                     | Show usage information              |
|  `-mapper`/`--mapper`        | -                                     | See [Mapping Fields](mapper.html)   |
|  `-mfile`/`--mapper-file`    | -                                     | See the _mapper_ flag               |
|  `-w`  /`--warm-up`          | -                                     | Generate a CRaC checkpoint. See [_fast_ images](flavours.html) |
