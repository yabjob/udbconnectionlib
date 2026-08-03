# API Guide

## Choosing an adapter

- `udblib.sql.mysql.mysqlAdapter`
- `udblib.sql.mariadb.mariadbAdapter`
- `udblib.sql.hsqldb.hsqldbAdapter`

Constructor pool arguments are: cleanup period, minimum connections, maximum connections, and connection timeout.

## Connection modes

- `IDatabaseAdapter.CM_PerQuery`: reserve/release around query execution.
- `IDatabaseAdapter.CM_PerSession`: retain ownership using an owner ID.

## Result mapping

Passing no result class returns `sqlResultRow` objects. Passing a class maps result columns to matching **public fields**, ignoring case. This is intentionally legacy behavior and does not use setters or records.

## Transactions

Use `BeginTran`, `CommitTran`, and `RollbackTran` on an explicitly reserved connection. Always roll back in error handling and release/destroy the connection afterward.

## Lifecycle

Close query objects when finished and call `adapter.destroy()` during application shutdown.
