# 123Table

## Project status

Early alpha, not working yet.


## Motivation

Beyond the obvious need to load rows into a db table, this project began
to experiment with the feasibility and limitations - in 2025 - of using only
Groovy scripts, a Dockerfile and some bash as a minimalist approach to
developing a small CLI tool with some automated test coverage without the
bells and whistles of build tools.


## HOWTOs

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