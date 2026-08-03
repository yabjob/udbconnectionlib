/*
 * SPDX-License-Identifier: Apache-2.0
 */
package examples;

import java.util.Locale;

import udblib.IDatabaseAdapter;
import udblib.IDatabaseQuery;
import udblib.sql.mysql.mysqlAdapter;

/** Demonstrates transaction handling and rollback-on-failure. */
public final class TransactionExample {
    private TransactionExample() {
    }

    public static void main(String[] args) throws Exception {
        IDatabaseAdapter adapter = new mysqlAdapter(
            ".", "localhost", "3306", false,
            IDatabaseAdapter.CM_PerSession,
            60_000, 1, 10, 30_000, false, null
        );

        IDatabaseQuery query = null;
        try {
            query = adapter.PrepareConnect(
                true, "transaction-example", "example_db",
                "example_user", "change-me",
                true, 10_000, Locale.US, 0L
            );

            query.BeginTran();
            try {
                query.setParam("accountId", 42L);
                query.setParam("amount", 25.00d);
                query.Exec("UPDATE accounts SET balance = balance - :amount "
                    + "WHERE id = :accountId");
                query.CommitTran();
            } catch (Exception failure) {
                query.RollbackTran(false);
                throw failure;
            }
        } finally {
            if (query != null) {
                query.FreeConnect();
            }
            adapter.destroy();
        }
    }
}
