# Code Review

## High priority

1. **SQL parameter rendering is not true prepared-statement binding.** `ParseSQL` expands values into SQL text, then prepares the already-rendered SQL. This makes escaping correctness security-critical and prevents normal JDBC type binding.
2. **Custom connection pool.** Pool concurrency, validation, cleanup, ownership, and timeout behavior require comprehensive stress tests. Mature pools such as HikariCP should be considered.
3. **Sensitive connection data.** Passwords are retained in connection-parameter objects and `Properties`. Avoid logging these objects and minimize their lifetime.
4. **Reflection mapping.** Mapping targets public fields and has many manual numeric/date conversions. Null handling and narrowing conversions may surprise callers.

## Medium priority

- Public API uses raw `Class` and `List` types.
- Naming does not follow Java conventions (`sqlQuery`, `PrepareConnect`, `ReservConnect`).
- Date/time logic uses legacy `Date`, `Calendar`, locale formatting, and manual offsets.
- Exceptions are broadly declared as `Exception`.
- Debug output writes directly to standard output.
- Global/static version state in the MySQL hierarchy can be changed by the MariaDB adapter.
- Utility packages are generic (`util`) and may conflict with consumers.

## Fresh-version-specific fixes applied

- Preserved the new MariaDB adapter and maximum pool-size constructor argument.
- Updated the MySQL driver class to `com.mysql.cj.jdbc.Driver`.
- Prevented a null dereference when `aTimeout > 0` but connection properties are disabled in MySQL/MariaDB adapters.
- Closed statements and result sets with try-with-resources.
- Replaced deprecated `Class.newInstance()` with constructor reflection.
- Removed compiled classes, Eclipse metadata, obfuscator binaries, and bundled JDBC JARs from the release package.

## Verification limits

The sources were compiled directly with `javac --release 8`. Maven was not installed in the preparation environment, so the complete Maven lifecycle is delegated to GitHub Actions.
