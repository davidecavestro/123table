# 123table

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/davidecavestro/123table?logo=GitHub)](https://github.com/davidecavestro/123table/releases)
[![build](https://github.com/davidecavestro/123table/actions/workflows/build.yml/badge.svg)](https://github.com/davidecavestro/123table/actions/workflows/build.yml)
[![coverage](https://raw.githubusercontent.com/davidecavestro/123table/badges/jacoco.svg)](https://github.com/davidecavestro/123table/actions/workflows/build.yml)


123table is a containerized command line tool that makes it easy to load rows into a database table.

_123table_ reads the rows from a db table or from a CSV (using a JDBC driver)
and insert them into a table in any JDBC compatible database.

## Project status

Beta testing: the implemented features seem to work as expected.
<br>
See the roadmap below or open a PR for missing features.

## Image flavours

_123table_ is packaged into flavours for specific needs

| Flavour       | Suffix      | Pre-warmed | Startup | JDBC drivers | Weight |
| ------------- | ----------- |:----------:| ------- | ------------ | ------ |
| Generic       |             | No         | Slow    | Included     | Heavy  |
| Slim          | `slim`      | No         | Slow    | -            | Light  |
| Fast          | `fast`      | Yes        | Fast    | Included     | Heavy  |
| Fast and slim | `fast-slim` | Yes        | Fast    | -            | Light  |


Where the *fast* flavours leverage features from the https://crac.org project
in order to speed-up the container boot.

The included drivers are available within the `/drivers` folder.
<br>
Currently packaged drivers cover the following data sources:
- csv
- h2
- MS SQLserver
- Oracle
- PostgreSQL
- sqlite

If other drivers become needed, just mount a volume or bind-mount
a local folder containing the appropriate jars.


## Motivation

Beyond the obvious need to load rows into a db table, this project began
to **experiment as a developer** with the feasibility and limitations - in
2025 - of using only Groovy scripts, a Dockerfile and some bash as **a
minimalist approach to developing a small CLI tool** with some automated
test coverage and contnuous integration, but without the bells and whistles
of build tools.


## HOWTOs


### Basic usage

Given a CSV file named _foo.csv_ in the current directory, run

```bash
docker run --rm -it \
  -v $(pwd):/data ghcr.io/davidecavestro/123table:main-fast \
  -stable foo \
  -create \
  -url jdbc:sqlite:/data/foo.db
```
to load its rows into a newly created *foo* table of a sqlite db.
<br>
Replace the `-url` value with a proper JDBC url for your target db. 
<br>
Use the `--help` flag to get the full list of options.

### How to build locally

```bash
docker build -t 123table .
```


### How to run tests locally

#### Containerized tests

```bash
docker build --target tests -t 123table-tests . && \
docker run --rm -it -v ./target:/target 123table-tests
```

## Roadmap (sort of)

- [X] Feat: copy from CSV
- [X] Feat: plain copy - same name and type
- [ ] Feat: field name remapping
- [ ] Feat: field type remapping (value computed by target type)
- [ ] Feat: field value remapping (value computed by custom code)
- [ ] Feat: support for additional/custom JDBC drivers
- [ ] Feat: copy from db
- [ ] CI: code coverage > 90%
- [ ] CI: automatic tests on push
- [ ] CI: show test coverage
- [ ] CI: release automation
