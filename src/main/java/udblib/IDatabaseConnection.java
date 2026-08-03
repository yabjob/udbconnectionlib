/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) UDB Connection Library contributors.
 * See the LICENSE file in the project root for license information.
 */
package udblib;

import java.sql.Connection;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Represents one managed JDBC connection and its ownership, transaction, and execution lifecycle.
 *
 * <p><strong>Compatibility note:</strong> Public names follow the original API,
 * including its historical naming conventions. Renaming them would be a breaking change.</p>
 *
 * @since 2.1.0
 */
public interface IDatabaseConnection {

	public abstract Connection Connect() throws Exception;

	public abstract void Disconnect() throws Exception;

	public abstract boolean isExists();

	public abstract long getID();

	public abstract String getKey();

	public abstract Connection getConnection();

	public abstract IDatabaseConnParams getConnParams();

	public abstract IDatabaseAdapter getAdapter();

	public abstract String getOwnerID();

	public abstract void setOwnerID(String aOwnerID);

	public abstract boolean HasOwner();
	
	public abstract boolean IsOwner(String aOwnerID);	

	public abstract long getInactivePeriod();

	public abstract void doExec(boolean aIsStoredProcCall, String aSQL, Properties aParams, Class aResultRowClass, List aResultArray, Locale aLocale, long aTimeZoneOffset) throws Exception;

	public abstract void BeginTran(String aSQL) throws Exception;

	public abstract void CommitTran() throws Exception;

	public abstract void RollbackTran() throws Exception;

}