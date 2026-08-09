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

## Calling stored procedures

**This is the primary way the library is used in practice.** The `Exec(...)`
family (plain SELECT/UPDATE/etc.) exists on `IDatabaseQuery`, but real
applications built on this library call named stored procedures through the
`ExecSP*` methods almost exclusively, rather than issuing ad-hoc SQL.

Parameters are set once per call with `setParam(name, value)`, then bound
**positionally**, in the order they were set, against the target procedure's
declared parameter list — the adapter builds the `call proc_name(:p1, :p2, ...)`
text via `BuildStoredProcExecSQL`, and the `:name` tokens are resolved to safe
`?` placeholders and bound through `PreparedStatement`/`CallableStatement`
(see the Security section in the top-level README) rather than rendered into
the SQL string.

Always call `query.ClearParams()` before setting parameters for a new call on
a reused `IDatabaseQuery` instance — parameters are not cleared automatically
between calls.

### `ExecSP(name)` — action procedure, no result set

For procedures that perform an action and return nothing:

```java
query.ClearParams();
query.setParam("userId", 42L);
query.ExecSP("sp_touch_last_login");
// generated call: call sp_touch_last_login(:userId)
```

### `ExecSP(name, resultRowClass, results)` — procedure returning rows

For procedures whose body ends in a `SELECT`, mapped onto a result class the
same way a plain SELECT is (matching public fields, case-insensitive):

```java
query.ClearParams();
query.setParam("status", "ACTIVE");

List<UserRow> results = new ArrayList<>();
query.ExecSP("sp_get_users_by_status", UserRow.class, (List) results);
// generated call: call sp_get_users_by_status(:status)
```

### `ExecSP_Get(name, resultRowClass)` — single-row convenience call

Returns the first row only, or `null` if the procedure produced none. Useful
for lookup-style procedures expected to return zero or one row:

```java
query.ClearParams();
query.setParam("userId", 42L);

UserRow user = (UserRow) query.ExecSP_Get("sp_get_user_by_id", UserRow.class);
// generated call: call sp_get_user_by_id(:userId)
```

### `ExecSP_Ins(name, resultSetIdColumnName)` — insert procedure returning a generated ID

Calls an insert-style procedure and reads a generated ID back **from the
procedure's own result set**, by column name — this is not a JDBC `OUT`
parameter binding. The procedure must end with a `SELECT` that exposes the
new ID under the given column name:

```sql
CREATE PROCEDURE sp_insert_user(IN p_name VARCHAR(100), IN p_email VARCHAR(200))
BEGIN
    INSERT INTO users (name, email) VALUES (p_name, p_email);
    SELECT LAST_INSERT_ID() AS newId;
END
```

```java
query.ClearParams();
query.setParam("name", "Jane Doe");
query.setParam("email", "jane@example.com");

long newId = query.ExecSP_Ins("sp_insert_user", "newId");
// generated call: call sp_insert_user(:name, :email)
```

If the procedure's result set doesn't include a row, or doesn't contain the
named column, this throws — there is currently no `OUT`-parameter-based
variant of `ExecSP_Ins`; if your procedure signals the new ID via an `OUT`
parameter rather than a trailing `SELECT`, it is not yet supported by this
method and would need to be called through the lower-level `Exec(...)` path
instead.

See [`StoredProcedureExample.java`](../examples/src/main/java/examples/StoredProcedureExample.java)
for a complete, runnable version of all four variants.

## Transactions

Use `BeginTran`, `CommitTran`, and `RollbackTran` on an explicitly reserved connection. Always roll back in error handling and release/destroy the connection afterward.

## Lifecycle

Close query objects when finished and call `adapter.destroy()` during application shutdown.
