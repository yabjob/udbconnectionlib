/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) UDB Connection Library contributors.
 * See the LICENSE file in the project root for license information.
 */
package udblib.sql;

import udblib.IDatabaseAdapter;
import udblib.IDatabaseConnParams;
import udblib.IDatabaseConnection;
import udblib.IDatabaseQuery;
import util.Util;

import java.util.List;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;



/**
 * Base implementation for JDBC adapters. It owns the legacy connection pool and supplies shared connection, execution, and transaction behavior.
 *
 * <p><strong>Compatibility note:</strong> Public names follow the original API,
 * including its historical naming conventions. Renaming them would be a breaking change.</p>
 *
 * @since 2.1.0
 */
public abstract class sqlAdapter implements IDatabaseAdapter  {
	
	private   int			dbType;
    private   String			driverClassName;
    private   int			connMode;
    private   long			connMaxID;
    private   boolean		sqlToLowerCase = false;
    private   boolean	debugMode		 =	false; 

    private   List <sqlConnParams> connData 	= null;
    private   List  <sqlConnection>  connPool 	= null;

    private   long			poolLastClearTime;
    private   int			poolCleanInterval;    		// minutes
    private   int			poolMinConnCount;		// min conncount for uniq key: server + db + login
    private   int			poolMaxConnCount;		// max conncount
    private   int			poolConnTimeout;			// timeout when inactive connection will be returned to pool, minutes
    private   boolean	poolUseOldOwnExistingSessionConnection = false;

    private   Logger    	logger = null; 

    
    
    
  public sqlAdapter( int aDbType, String aDriverClassName, int aConnMode,
                     					int aPoolCleanInterval, int aPoolMinConnCount, int aPoolMaxConnCount, int aPoolConnTimeout, 
                     					boolean aNeedLogger, String aLogFileName ) {

    dbType						= aDbType;
    driverClassName			= aDriverClassName;
    connMode					= aConnMode;
    poolCleanInterval		= aPoolCleanInterval;
    poolMinConnCount	= aPoolMinConnCount <= aPoolMaxConnCount ? aPoolMinConnCount : (aPoolMinConnCount > 0 ? aPoolMinConnCount : 1);
    poolMaxConnCount	= aPoolMaxConnCount;
    poolConnTimeout		= aPoolConnTimeout;

    connMaxID         	 	= 0;
    poolLastClearTime  	= Util.CurrentTime();

    connData	= new ArrayList<sqlConnParams>(); 
    connPool	= new ArrayList<sqlConnection>();
    
    // Initialize logger only when requested. Avoid NullPointerException when aNeedLogger==false.
    if (aNeedLogger) {
        try {
            logger = Logger.getLogger(aLogFileName);
            logger.setLevel(Level.ALL);
            Handler fh2 = new FileHandler(aLogFileName, true);
            fh2.setFormatter(new SimpleFormatter());
            logger.addHandler(fh2);
        }
        catch (Exception e) {
            // If logging setup fails, disable logging but don't crash the library.
            logger = null;
        }
    } else {
        logger = null;
    }
    
  }

  public int getDbType() { return dbType; }
  
  public void setDebugMode(boolean v) { debugMode = v; }
  public boolean isDebugMode() { return debugMode; }
  
  

// ----------------------------------------------------------------------------
//        conn Data        *for sqlsrv - dbname,login,passw..
// ----------------------------------------------------------------------------
   private sqlConnParams dataGetParams(String key) {
     sqlConnParams res = null;
     sqlConnParams cp = null;
     for (int i = 0; i < connData.size(); i++) {
        cp = (sqlConnParams) connData.get(i);
        if (cp == null) continue;
        if ( key.equalsIgnoreCase(cp.getKey()) ) { res = cp; break; }
     }
     return res;
   }

   
private IDatabaseConnParams dataAddParamsIfNotExists(String key, String url, String login, String passw,
                                                   						boolean sqlToLowerCase, Properties connProps) {
     sqlConnParams cp = dataGetParams(key);
     if (cp == null) {
       cp = new sqlConnParams(key, dbType, driverClassName, url, login, passw, sqlToLowerCase, connProps);
       connData.add(cp);
     }
     return cp;
   }

// ----------------------------------------------------------------------------
//                            conn Pool
// ----------------------------------------------------------------------------
   private void poolCreateConnections(String key, String url, String login, String passw,
                                      boolean sqlToLowerCase, Properties connProps)  throws Exception {
     IDatabaseConnParams cp = dataAddParamsIfNotExists(key,url,login,passw,sqlToLowerCase,connProps);
     for (int i = 1; i <= poolMinConnCount; i++) { poolAddConn(key, null, cp); }
   }


   private IDatabaseConnection poolGetConnByKey(String key) throws Exception{
     IDatabaseConnection res = null;  IDatabaseConnection c = null;
     for (int i = 0; i < connPool.size(); i++) {
       c = (IDatabaseConnection) connPool.get(i);
       if (c != null) { if (key.equalsIgnoreCase(c.getKey())) {res = c; break;} }
     }
     return res;
   }

   private IDatabaseConnection poolGetFreeConn(String key, String aOwnerID, boolean NeedPingConnection) throws Exception{
     IDatabaseConnection res = null;   IDatabaseConnection c;
     for (int i = 0; i < connPool.size(); i++) {
         c = (IDatabaseConnection) connPool.get(i);
         if (c != null) {
             if ( key.equalsIgnoreCase(c.getKey()) && 
              	  (!c.HasOwner() || (poolUseOldOwnExistingSessionConnection && c.IsOwner(aOwnerID)))  ) {
                  if (c.isExists()) {
                       if (NeedPingConnection) {
                           if (PingConnection(c)) {res = c; break;}
                           else { poolDelConn(c); }
                       }
                       else {res = c; break;}
                  }
                  else { poolDelConn(c); }
             }
         }
     }
     return res;
   }

   private IDatabaseConnection poolAddConn(String key, String ownerID, IDatabaseConnParams cp) throws Exception {
	 sqlConnection c = null; boolean allowCreate = true;  
	 if (poolMaxConnCount > 0) {
		 if (connPool.size() >= poolMaxConnCount) {
			 poolDeleteUnusedConn();
			 allowCreate = connPool.size() < poolMaxConnCount; 
		 }
	 }
	 if (allowCreate) {
	     connMaxID++;
	     c = new sqlConnection(ownerID, key, connMaxID, cp, this, logger);
	     c.Connect();
	     connPool.add(c);
	 }
	 else {
		 Util.RaiseUException("Error! Application exceeds the maximum number of connections to database!");
	 }
     return c;
   }

   private void poolDelConn(IDatabaseConnection conn) throws Exception {
    try {
      conn.Disconnect();
      connPool.remove(conn);
    }
    catch(Exception e) { }
   }

   private synchronized IDatabaseConnection poolReservConn(String aConnKey, String aOwnerID) throws Exception {
     IDatabaseConnection c = poolGetFreeConn(aConnKey, aOwnerID, false);
     if (c != null) { c.setOwnerID(aOwnerID); }
     else {
       IDatabaseConnParams cp = dataGetParams(aConnKey);
       c = poolAddConn(aConnKey,aOwnerID,cp);
     }
     return c;
  }

   private synchronized void poolReservConn(IDatabaseConnection conn, String aOwnerID) throws Exception {
     conn.setOwnerID(aOwnerID);
   }

   private synchronized void poolFreeConn(IDatabaseConnection conn) throws Exception {
      if (conn  != null)  conn.setOwnerID(null);
   }

   private long poolGetPeriodFromLastClearTime() {
      return (Util.CurrentTime() - poolLastClearTime) / 60000;  // minutes
   }

   private synchronized void poolDeleteUnusedConn() throws Exception {
       IDatabaseConnection c = null; IDatabaseConnParams cp = null;
       for (int n = 0; n < connData.size(); n++) {
           cp = (IDatabaseConnParams) connData.get(n);
           String key = cp.getKey();

           int unusedCount = 0;
           for (int i = 0; i < connPool.size(); i++) {
               c = (IDatabaseConnection) connPool.get(i);
               if (c == null)  continue;
               if ( ! key.equalsIgnoreCase(c.getKey())) continue;
               if (c.getInactivePeriod() > poolConnTimeout) c.setOwnerID(null);
               if ( ! c.HasOwner()) unusedCount++;
           }
           if ((unusedCount > 0) && (connPool.size() > poolMinConnCount)) {
               for (int i = 0; i < connPool.size(); i++) {
                   c = (IDatabaseConnection) connPool.get(i);
                   if (c == null)  continue;
                   if ( ! key.equalsIgnoreCase(c.getKey())) continue;
                   if (!c.HasOwner()) {
                      if ((unusedCount > 0) && (connPool.size() > poolMinConnCount)) {
                        poolDelConn(c);
                        unusedCount--;
                      }
                      else break;
                   }
               }
           }
       }
   }

   private synchronized void poolDeleteAllConn(String aDbName) throws Exception {
	  if (aDbName == null) return; IDatabaseConnection c = null;
	  String DbUrl = getDbUrl(aDbName);
	  IDatabaseConnParams cp = null;
	  for (int n = 0; n < connData.size(); n++) {
		  cp = (IDatabaseConnParams) connData.get(n);
		  if (!DbUrl.equalsIgnoreCase(cp.getDbUrl())) continue; 
		  String key = cp.getKey();
		  for (int i = 0; i < connPool.size(); i++) {
			  c = (IDatabaseConnection) connPool.get(i);
		       if (c == null)  continue;
		       if (key.equalsIgnoreCase(c.getKey())) poolDelConn(c);
		  }
	  }
   }


// ----------------------------------------------------------------------------
//                            public methods
// ----------------------------------------------------------------------------

   protected synchronized IDatabaseQuery PrepareConnect(boolean aAutoCreatePool,
                                                  String aOwnerID, String aUrl,
                                                  String aLogin, String aPassw,
                                                  boolean aSQLToLowerCase,
                                                  Properties aConnProps,
                                                  Locale aLocale, long aTimeZoneOffset)  throws Exception {
    sqlToLowerCase =  aSQLToLowerCase;
    IDatabaseConnection c = null;
    String key = "key_" + aUrl + aLogin;

    if (aAutoCreatePool) {
       if (poolGetConnByKey(key) == null) poolCreateConnections(key,aUrl,aLogin,aPassw,aSQLToLowerCase,aConnProps);
    }

    if (connMode == CM_PerSession) {
      c = poolGetFreeConn(key, aOwnerID, true);
      if (c != null) { poolReservConn(c, aOwnerID); }
      else {
     	 IDatabaseConnParams cp = dataAddParamsIfNotExists(key,aUrl,aLogin,aPassw,aSQLToLowerCase,aConnProps);
         c = poolAddConn(key, aOwnerID, cp);
      }
    }
      else
    if (connMode == CM_PerQuery) {
      c = poolGetConnByKey(key);
      if (c == null) dataAddParamsIfNotExists(key,aUrl,aLogin,aPassw,aSQLToLowerCase,aConnProps);
      c = null;
    }

    IDatabaseQuery aQuery = new sqlQuery(this, c, key, aOwnerID, aLocale, aTimeZoneOffset);


    if (poolGetPeriodFromLastClearTime() >= poolCleanInterval) {
          poolDeleteUnusedConn();
          poolLastClearTime = Util.CurrentTime();
    }


    return aQuery;
   }


 public synchronized IDatabaseConnection ReservConnect(IDatabaseQuery aQuery) throws Exception {
     String aOwnerID     = aQuery.getOwnerID();
     String aConnKey     = aQuery.getConnKey();
     IDatabaseConnection aConn = aQuery.getConn();
     if (aConn == null) aConn = poolReservConn(aConnKey,aOwnerID);
     if (aConn == null) Util.RaiseUException("Reserv connect operation aborted!");
     return aConn;
   }

 public synchronized void FreeConnect(IDatabaseConnection conn) throws Exception {
       poolFreeConn(conn);
   }

 public synchronized void DestroyConnect(IDatabaseConnection aConn) throws Exception {
       if (aConn != null) { poolDelConn(aConn); }
   } 

 public synchronized IDatabaseQuery ReCreateConnect(IDatabaseConnection aConn, String aOwnerID, boolean useConnProps, 
 										 int aTimeout, Locale aLocale, long aTimeZoneOffset) throws Exception {
 	IDatabaseQuery aQuery = null;
     if (aConn != null) {  
    	 	IDatabaseConnParams cp = aConn.getConnParams();
    	 		String dbUrl = cp.getDbUrl();  String dbLogin = cp.getLogin();  String dbPassw = cp.getPassw();
    	 	DestroyConnect(aConn);
    	 	aQuery = PrepareConnect(true, aOwnerID, dbUrl, dbLogin, dbPassw,  useConnProps, aTimeout, aLocale, aTimeZoneOffset);      
     }
     return aQuery;
 }


 public void DestroyConnections(String aDbName) throws Exception {
 	poolDeleteAllConn(aDbName);
 }


 public void ExecQuery(boolean aIsStoredProcCall, IDatabaseQuery aQuery, Class aResultRowClass, List aResults, Locale aLocale, long aTimeZoneOffset) throws Exception {
     IDatabaseConnection aConn = aQuery.getConn();
     boolean ConnectWasReserved = false;

     if (connMode == CM_PerQuery && aConn == null) {
         aConn = ReservConnect(aQuery);
         ConnectWasReserved = true;
     }
     
     aConn.doExec(aIsStoredProcCall, aQuery.getSQL(), aQuery.getParams(), aResultRowClass, aResults, aLocale, aTimeZoneOffset);

     if (connMode == CM_PerQuery && ConnectWasReserved) poolFreeConn(aConn);
   }


 public void BeginTran(IDatabaseConnection conn, String aSQL) throws Exception {
 	_BeginTran(conn, aSQL);
 }

 public abstract boolean getIsAutoCommit();

 public abstract boolean getIsReadOnly();

 public abstract  int getDefaultTransactionIsolation();

 protected void _BeginTran(IDatabaseConnection conn, String aSQL) throws Exception {
     if (conn != null) conn.BeginTran(aSQL);
     else Util.RaiseUException("sqlConnection object is null!");
 }

 public void CommitTran(IDatabaseConnection conn) throws Exception {
      if (conn != null) conn.CommitTran();
      else Util.RaiseUException("sqlConnection object is null!");
    }

 public void RollbackTran(IDatabaseConnection conn) throws Exception {
      if (conn != null) conn.RollbackTran();
      else Util.RaiseUException("sqlConnection object is null!");
    }


 public boolean PingConnection(IDatabaseConnection conn) {
      boolean res = false;
      try {
    	  if (conn != null) {
    		         List lst = new ArrayList();
    		         String aSQL = getPingQueryText();
    		         conn.doExec(getPingQueryIsSP(), aSQL, null, null, lst, java.util.Locale.US, 0);
    		         res = ! lst.isEmpty();
    	  }
      }
      catch(Exception e)  {}
      return res;
   }

 public boolean getSQLToLowerCase() { return sqlToLowerCase; }

 public boolean getUseOldOwnExistingSessionConnection() { return poolUseOldOwnExistingSessionConnection; }
 public void setUseOldOwnExistingSessionConnection(boolean value) { poolUseOldOwnExistingSessionConnection = value; }

 public abstract String getPingQueryText();

 public abstract char   getQuoteChar();

 public abstract String BuildStoredProcExecSQL( String aSPName, Properties params, String[] paramNames );

 public abstract int      getMaxSPParamsCount();

 public abstract String getNullWord();

 public abstract String replaceSpecialSymbols(String str);

 public abstract String getDateTimeStr(long Time);

 public abstract String getDateStr(long Time);

 public abstract String getTimeStr(long Time);

 public abstract String getShutdownText(int mode);

 public void destroy() {
 	
 }

}
