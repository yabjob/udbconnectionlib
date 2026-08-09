/*
 * SPDX-License-Identifier: Apache-2.0
 */
package examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import udblib.IDatabaseAdapter;
import udblib.IDatabaseQuery;
import udblib.sql.mysql.mysqlAdapter;

/**
 * Demonstrates the primary way this library is actually used: calling stored
 * procedures rather than issuing ad-hoc SELECT/CRUD statements. Covers all
 * four {@code ExecSP*} variants exposed by {@link IDatabaseQuery}.
 */
public final class StoredProcedureExample {
    private StoredProcedureExample() {
    }

    public static void main(String[] args) throws Exception {
        IDatabaseAdapter adapter = new mysqlAdapter(
            ".", env("UDB_HOST", "localhost"), env("UDB_PORT", "3306"), false,
            IDatabaseAdapter.CM_PerQuery,
            60_000, 1, 10, 30_000, false, null
        );

        IDatabaseQuery query = null;
        try {
            query = adapter.PrepareConnect(
                true, "sp-example", env("UDB_DATABASE", "example_db"),
                env("UDB_USER", "example_user"), env("UDB_PASSWORD", "change-me"),
                true, 10_000, Locale.US, 0L
            );

            fireAndForget(query);
            callReturningRows(query);
            callReturningSingleRow(query);
            callInsertReturningId(query);

        } finally {
            if (query != null) {
                query.FreeConnect();
            }
            adapter.destroy();
        }
    }

    /**
     * ExecSP(name): call a stored procedure that performs an action and
     * returns no result set. Parameters are bound positionally, in the order
     * they were set with setParam(...), against the procedure's declared
     * parameter list.
     *
     * Equivalent generated call: {@code call sp_touch_last_login(:userId)}
     */
    private static void fireAndForget(IDatabaseQuery query) throws Exception {
        query.ClearParams();
        query.setParam("userId", 42L);
        query.ExecSP("sp_touch_last_login");
    }

    /**
     * ExecSP(name, resultRowClass, results): call a stored procedure whose
     * SELECT produces a result set, mapped onto a caller-provided class the
     * same way a plain SELECT would be (matching public fields, ignoring case).
     *
     * Equivalent generated call: {@code call sp_get_users_by_status(:status)}
     */
    private static void callReturningRows(IDatabaseQuery query) throws Exception {
        query.ClearParams();
        query.setParam("status", "ACTIVE");

        List<UserRow> results = new ArrayList<>();
        query.ExecSP("sp_get_users_by_status", UserRow.class, (List) results);

        for (UserRow user : results) {
            System.out.println(user.id + ": " + user.name);
        }
    }

    /**
     * ExecSP_Get(name, resultRowClass): convenience wrapper around ExecSP
     * that returns only the first row, or null if the procedure produced no
     * rows. Useful for lookup-style procedures expected to return 0 or 1 rows.
     *
     * Equivalent generated call: {@code call sp_get_user_by_id(:userId)}
     */
    private static void callReturningSingleRow(IDatabaseQuery query) throws Exception {
        query.ClearParams();
        query.setParam("userId", 42L);

        UserRow user = (UserRow) query.ExecSP_Get("sp_get_user_by_id", UserRow.class);
        if (user != null) {
            System.out.println("Found: " + user.name + " <" + user.email + ">");
        }
    }

    /**
     * ExecSP_Ins(name, resultSetIdColumnName): calls an insert-style
     * procedure and reads a generated ID back from its result set by column
     * name. The procedure itself is expected to end with a SELECT that
     * exposes the new ID under that column, for example:
     *
     * <pre>
     * CREATE PROCEDURE sp_insert_user(IN p_name VARCHAR(100), IN p_email VARCHAR(200))
     * BEGIN
     *     INSERT INTO users (name, email) VALUES (p_name, p_email);
     *     SELECT LAST_INSERT_ID() AS newId;
     * END
     * </pre>
     *
     * This is not an OUT parameter — the value is read from the first row of
     * the procedure's own result set, not from a JDBC OUT binding.
     *
     * Equivalent generated call: {@code call sp_insert_user(:name, :email)}
     */
    private static void callInsertReturningId(IDatabaseQuery query) throws Exception {
        query.ClearParams();
        query.setParam("name", "Jane Doe");
        query.setParam("email", "jane@example.com");

        long newId = query.ExecSP_Ins("sp_insert_user", "newId");
        System.out.println("Inserted user id=" + newId);
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
