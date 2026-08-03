/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) UDB Connection Library contributors.
 * See the LICENSE file in the project root for license information.
 */
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