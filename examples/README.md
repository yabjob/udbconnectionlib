# Usage examples

These examples are intentionally kept outside the main artifact so the library has no
runtime dependency on a particular database installation.

## Available examples

- [`StoredProcedureExample.java`](src/main/java/examples/StoredProcedureExample.java) —
  the primary usage pattern: calling stored procedures via `ExecSP`,
  `ExecSP_Get`, and `ExecSP_Ins`. Start here.
- [`MariaDbSelectExample.java`](src/main/java/examples/MariaDbSelectExample.java) —
  a parameterized SELECT.
- [`TransactionExample.java`](src/main/java/examples/TransactionExample.java) —
  `BeginTran`/`CommitTran`/`RollbackTran` around multiple statements.

## Run an example

1. Build and install the library locally:

   ```bash
   mvn clean install
   ```

2. Copy the desired example into an application, update the connection settings, and
   add the matching JDBC driver dependency.

The examples use environment variables for credentials:

- `UDB_HOST`
- `UDB_PORT`
- `UDB_DATABASE`
- `UDB_USER`
- `UDB_PASSWORD`

Never commit real credentials to source control.
