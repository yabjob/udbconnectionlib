# UDB Connection Library

A small JDBC abstraction and custom connection-pooling library for **MySQL**, **MariaDB**, and **HSQLDB**.

> **Project status:** legacy code being prepared for community maintenance. Review `docs/CODE_REVIEW.md` before production use.

## Maintainer

This repository is maintained by:

- Yuri Boltovski — <yabjob@gmail.com> ([yabjob](https://github.com/yabjob))

Contributions are welcome — see CONTRIBUTING.md for guidelines.

## Features

- Database-specific adapters for MySQL, MariaDB, and HSQLDB
- Per-query and per-session connection modes
- Configurable connection-pool limits and idle timeouts
- Named SQL parameter expansion
- Reflection-based result mapping into public Java fields
- Basic transaction and stored-procedure support

## Requirements

- Java 8 or newer
- Maven 3.9+
- A supported JDBC database

## Build

```bash
mvn clean verify
```

## Maven dependency

After publishing a release, consumers can use:

```xml
<dependency>
  <groupId>io.github.yabjob</groupId>
  <artifactId>udb-connection-lib</artifactId>
  <version>2.1.0</version>
</dependency>
```

## Minimal example

> **Note:** in practice this library is used almost exclusively to call
> **stored procedures**, not ad-hoc SELECT/CRUD statements. See
> [Calling stored procedures](docs/API_GUIDE.md#calling-stored-procedures) in
> the API guide for the primary usage pattern, with examples for each
> `ExecSP*` variant. The SELECT example below still applies (and works the
> same way for procedures that return rows), but most real call sites look
> like the stored-procedure examples, not this one.

```java
import java.util.List;
import java.util.Locale;

import udblib.IDatabaseAdapter;
import udblib.IDatabaseQuery;
import udblib.sql.mariadb.mariadbAdapter;

IDatabaseAdapter adapter = new mariadbAdapter(
    ".", "localhost", "3306", false,
    IDatabaseAdapter.CM_PerQuery,
    60_000, 1, 10, 30_000,
    false, null
);

IDatabaseQuery query = null;
try {
    query = adapter.PrepareConnect(
        true, "example-owner", "example_db",
        "db_user", "db_password",
        true, 10_000, Locale.US, 0L
    );

    query.setParam("minimumId", 100L);
    List rows = query.Exec(
        false,
        "SELECT id, name, email FROM users WHERE id >= :minimumId",
        UserRow.class
    );
} finally {
    if (query != null) {
        query.FreeConnect();
    }
    adapter.destroy();
}
```

See [`examples/`](examples/) for complete SELECT, stored-procedure, and
transaction examples. Credentials in those examples are read from environment
variables.

## Generate API documentation

```bash
mvn javadoc:javadoc
```

The generated reference is written to `target/site/apidocs`. Normal Maven packages
also attach source and Javadoc JARs. See [`docs/JAVADOC.md`](docs/JAVADOC.md).

## Security warning

The current named-parameter engine renders values into SQL text instead of binding them through JDBC placeholders in older versions. The yuri/prepare-oss branch migrates the library to bind parameters using JDBC placeholders to reduce SQL injection risk. Avoid using untrusted raw SQL fragments or identifiers.

## Documentation

- [API guide](docs/API_GUIDE.md)
- [Javadoc guide](docs/JAVADOC.md)
- [Runnable examples](examples/)
- [Architecture](docs/ARCHITECTURE.md)
- [Code review](docs/CODE_REVIEW.md)
- [Modernization roadmap](docs/ROADMAP.md)
- [Contributing](CONTRIBUTING.md)

## License

Apache License 2.0. Confirm that every contributor/copyright holder has approved this license before publishing.
