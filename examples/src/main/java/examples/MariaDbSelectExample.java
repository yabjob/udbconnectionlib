/*
 * SPDX-License-Identifier: Apache-2.0
 */
package examples;

import java.util.List;
import java.util.Locale;

import udblib.IDatabaseAdapter;
import udblib.IDatabaseQuery;
import udblib.sql.mariadb.mariadbAdapter;

/** Demonstrates a parameterized SELECT with MariaDB. */
public final class MariaDbSelectExample {
    private MariaDbSelectExample() {
    }

    public static void main(String[] args) throws Exception {
        IDatabaseAdapter adapter = createAdapter();
        IDatabaseQuery query = null;
        try {
            query = adapter.PrepareConnect(
                true,
                "example-session",
                env("UDB_DATABASE", "example_db"),
                env("UDB_USER", "example_user"),
                env("UDB_PASSWORD", "change-me"),
                true,
                10_000,
                Locale.US,
                0L
            );

            query.setParam("minimumId", 100L);
            List rows = query.Exec(
                false,
                "SELECT id, name, email FROM users WHERE id >= :minimumId",
                UserRow.class
            );

            for (Object value : rows) {
                UserRow user = (UserRow) value;
                System.out.println(user.id + ": " + user.name);
            }
        } finally {
            if (query != null) {
                query.FreeConnect();
            }
            adapter.destroy();
        }
    }

    private static IDatabaseAdapter createAdapter() {
        return new mariadbAdapter(
            ".",
            env("UDB_HOST", "localhost"),
            env("UDB_PORT", "3306"),
            false,
            IDatabaseAdapter.CM_PerQuery,
            60_000,
            1,
            10,
            30_000,
            false,
            null
        );
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
