/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) UDB Connection Library contributors.
 * See the LICENSE file in the project root for license information.
 */
package udblib;

import java.util.List;
import java.util.Locale;
import java.util.Properties;


/**
 * Database-independent contract implemented by JDBC adapters. It manages connection creation and pooling, delegates query execution, and exposes database-specific SQL formatting rules.
 *
 * <p><strong>Compatibility note:</strong> Public names follow the original API,
 * including its historical naming conventions. Renaming them would be a breaking change.</p>
 *
 * @since 2.1.0
 */
public interface IDatabaseAdapter {

	public static final int CM_PerQuery    	= 	1;
	public static final int CM_PerSession	=	2;

    // sql server types
	public static final int DB_EmptyTest		=   5;
    public static final int DB_MySQL     		=  10;
    public static final int DB_Mariadb    		=  15;
    public static final int DB_MSSQL     		=  20;
    public static final int DB_HSQLDB    		=  30;

    
    public static final int SHRINK_DATABASE	=   100;
    
       
    
	
    
    public abstract IDatabaseQuery PrepareConnect( boolean aAutoCreatePool,
																					String aOwnerID, String aDbName,
																					String aLogin, String aPassword,
									                                                boolean useConnProps, int aTimeout,  Locale aLocale, 
									                                                long aTimeZoneOffset) throws Exception;
	
	public abstract IDatabaseConnection ReservConnect(IDatabaseQuery aQuery) throws Exception;

	public abstract void FreeConnect(IDatabaseConnection conn) throws Exception;

	public abstract void DestroyConnect(IDatabaseConnection aConn) throws Exception;
	
	public abstract IDatabaseQuery ReCreateConnect(IDatabaseConnection aConn, String aOwnerID, boolean useConnProps, 
																					 int aTimeout, Locale aLocale, long aTimeZoneOffset) throws Exception;
	
	public abstract void DestroyConnections(String aDbUrl) throws Exception;

	public abstract int getDefaultTransactionIsolation();
	
	public abstract String getVersion();
	
	public abstract int getDbType();

	public abstract void setDebugMode(boolean v);

	public abstract boolean isDebugMode();
	
	public abstract String getDbUrl(String aDbName);
	
	public abstract boolean getIsAutoCommit();
	
	public abstract boolean getIsReadOnly();
	
	public abstract void ExecQuery( boolean aIsStoredProcCall, IDatabaseQuery aQuery, Class aResultRowClass,
													   List aResults, Locale aLocale, long aTimeZoneOffset ) throws Exception;
    
    public abstract void BeginTran(IDatabaseConnection conn, String aSQL) throws Exception;

	public abstract void CommitTran(IDatabaseConnection conn) throws Exception;

	public abstract void RollbackTran(IDatabaseConnection conn) throws Exception;

	public abstract boolean PingConnection(IDatabaseConnection conn);

	public abstract boolean getSQLToLowerCase();
	
	public abstract boolean getUseOldOwnExistingSessionConnection();
	public abstract void setUseOldOwnExistingSessionConnection(boolean value);

	public abstract String getPingQueryText();

	public abstract boolean getPingQueryIsSP();

	public abstract char getQuoteChar();

	public abstract String BuildStoredProcExecSQL(String aSPName, Properties params, String[] paramNames);

	public abstract int getMaxSPParamsCount();

	public abstract String getNullWord();

	public abstract String replaceSpecialSymbols(String str);

	public abstract String getDateTimeStr(long Time);

	public abstract String getDateStr(long Time);

	public abstract String getTimeStr(long Time);
	
	public abstract String ConvertDateStrToSQLDateStr(String str, String formatStr);

	public abstract String getShutdownText(int mode);
	
	public abstract String getCheckDatabaseText(int mode);
	
	public abstract void destroy();
}