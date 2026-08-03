/*
SPDX-License-Identifier: Apache-2.0
Copyright (c) 2026 Yuri Boltovski
Author: Yuri Boltovski yabjob@gmail.com
This file is part of the UDB Connection Library.
See the project LICENSE file for license terms.
NOTE: This header was added/verified by Yuri Boltovski. */
package udblib;

import java.util.Properties;

/**
 * Read-only connection configuration used when creating or recreating a JDBC connection.
 *
 * <p><strong>Compatibility note:</strong> Public names follow the original API,
 * including its historical naming conventions. Renaming them would be a breaking change.</p>
 *
 * @since 2.1.0
 */
public interface IDatabaseConnParams {

    public abstract String getKey();

    public abstract int getType();

    public abstract String getDriverClassName();

    public abstract String getDbUrl();

    public abstract String getLogin();

    public abstract String getPassw();

    public abstract boolean getSqlToLowerCase();

    public abstract Properties getConnProps();

}
