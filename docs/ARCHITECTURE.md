# Architecture

## Main abstractions

- `IDatabaseAdapter`: database behavior, pool lifecycle, URL creation, SQL formatting, transaction delegation.
- `IDatabaseConnection`: wraps a JDBC `Connection` and tracks ownership/activity.
- `IDatabaseQuery`: query-oriented facade used by clients.
- `sqlAdapter`: shared custom pool and execution orchestration.
- `sqlConnection`: JDBC execution, SQL rendering, and reflection-based row mapping.
- `mysqlAdapter`, `mariadbAdapter`, `hsqldbAdapter`: vendor-specific defaults and URL/property construction.

## Execution flow

1. Create an adapter.
2. `PrepareConnect` creates connection parameters and a query facade.
3. The adapter reserves or creates a pooled connection.
4. `sqlConnection` renders parameters and executes JDBC calls.
5. Results are mapped to `sqlResultRow` or to public fields on a caller-provided class.
6. The adapter returns or destroys the connection according to connection mode and health.

## Compatibility policy

The initial open-source release keeps package names, class names, and method signatures. Breaking renames and a safer parameter-binding API should be introduced only in a future major version.
