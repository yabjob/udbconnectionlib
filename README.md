# UDB Connection Library

A small JDBC abstraction and custom connection-pooling library for **MySQL**, **MariaDB**, and **HSQLDB**.

> **Project status:** legacy code being prepared for community maintenance. Review `docs/CODE_REVIEW.md` before production use.

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
  <groupId>io.github.your-github-username</groupId>
  <artifactId>udb-connection-lib</artifactId>
  <version>2.1.0</version>
</dependency>
```

Replace `your-github-username` in this file and `pom.xml` before publishing.

## Minimal example

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

See [`examples/`](examples/) for complete SELECT and transaction examples. Credentials
in those examples are read from environment variables.

## Generate API documentation

```bash
mvn javadoc:javadoc
```

The generated reference is written to `target/site/apidocs`. Normal Maven packages
also attach source and Javadoc JARs. See [`docs/JAVADOC.md`](docs/JAVADOC.md).

## Security warning

The current named-parameter engine renders values into SQL text instead of binding them through JDBC placeholders. Do not accept raw SQL fragments, identifiers, or untrusted values without reviewing the escaping behavior. See `SECURITY.md` and `docs/CODE_REVIEW.md`.

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
