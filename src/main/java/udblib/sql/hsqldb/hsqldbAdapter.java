/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) UDB Connection Library contributors.
 * See the LICENSE file in the project root for license information.
 */
package udblib.sql.hsqldb;

	import udblib.IDatabaseAdapter;
	import udblib.IDatabaseConnection;
	import udblib.IDatabaseQuery;
	import udblib.sql.sqlAdapter;
	import util.Util;

	import java.io.File;
	import java.sql.Connection;
	import java.sql.SQLException;
	import java.util.Properties;
	import java.util.Locale;
	import java.util.Date;
	import java.util.List;
	import java.util.Calendar;


/**
 * HSQLDB-specific adapter providing file/server URL handling and database-specific SQL formatting.
 *
 * <p><strong>Compatibility note:</strong> Public names follow the original API,
 * including its historical naming conventions. Renaming them would be a breaking change.</p>
 *
 * @since 2.1.0
 */
public class hsqldbAdapter extends sqlAdapter {
	
	private static  final String		version							=	"2.01"; 
	
	private static final String 		driverClassName       	= "org.hsqldb.jdbcDriver";
    private static final boolean 	defIsAutoCommit			=  true;
    private static final boolean 	defIsReadOnly				=  false;
    private static final int      		defTransactionIsolation =  Connection.TRANSACTION_REPEATABLE_READ; //; TRANSACTION_READ_COMMITTED
    private static final String 		quoteChar          	     	=  "'";
    private static final String 		beginTranStatement 		= "START TRANSACTION";
    private static final String 		nullWord           	     	= "null";
    private static final int      		maxSPparamsCount 		= 100;
            


    private String   	appPath;
    private String   	dbHost;
    private String   	dbPort;
    private boolean  	isEmbeded;


    public hsqldbAdapter(String aAppPath, String aDbHost, String aDbPort,
                        			  boolean aIsEmbeded, int aConnMode,
                        			  int aPoolClearPeriod, int aPoolMinConnCount, int aPoolMaxConnCount, int aPoolConnTimeout, 
                        			  boolean aNeedLogger, String aLogFileName ) {

       super (IDatabaseAdapter.DB_HSQLDB, driverClassName, aConnMode, aPoolClearPeriod, aPoolMinConnCount, aPoolMaxConnCount, aPoolConnTimeout, 
    		   	  aNeedLogger, aLogFileName);
       appPath   	= aAppPath;
       isEmbeded = aIsEmbeded;
       dbHost    	= aDbHost;
       dbPort    	= aDbPort;
    }
    

	public String getVersion() { return version; };
    
    public void setDbHost(String value) {	dbHost = value; }
    public String getDbHost() { return dbHost;   }
    
    public void setDbPort(String value) { 	dbPort = value;  }
    public String getDbPort() {  return dbPort;   }
    

    public synchronized IDatabaseQuery PrepareConnect( boolean aAutoCreatePool,
                                                 String aOwnerID, String aDbName,  String aLogin, String aPassword,
                                                 boolean useConnProps, int aTimeout,  Locale aLocale, long aTimeZoneOffset) throws Exception {
        
    	Properties props =  useConnProps ?  new Properties() : null;

        boolean sqlToLowerCase = true;
        
        String url = getDbUrl(aDbName);
        
        if (aTimeout > 0) {
//          props.setProperty("connectTimeout",String.valueOf(aTimeout));
           // props.setProperty("socketTimeout",String.valueOf(aTimeout));
           // props.setProperty("interactiveClient","true");
        }
        if (props != null) {
          props.setProperty("user",aLogin);
          props.setProperty("password",aPassword);
          props.setProperty("sql.ignore_case", "true"); 
//          props.setProperty("characterEncoding", "UTF-8");
//          props.setProperty("connectionCollation","utf8_general_ci");
        }
        
        IDatabaseQuery dbQuery = super.PrepareConnect(aAutoCreatePool, aOwnerID, url, aLogin, aPassword, sqlToLowerCase, props, aLocale, aTimeZoneOffset);
        
        dbQuery.getConn().getConnection().setAutoCommit(false);
        
        // NO_BACKSLASH_ESCAPES to sql_mode if this option is not exists by default
//        dbQuery.Exec("SET SESSION sql_mode = if(locate('NO_BACKSLASH_ESCAPES',@@GLOBAL.sql_mode)<=0,concat('NO_BACKSLASH_ESCAPES',',',@@GLOBAL.sql_mode),@@GLOBAL.sql_mode)");
        
        		//ArrayList tmpLst = new ArrayList();
        		//dbQuery.Exec(false, "select @@SESSION.sql_mode as SqlMode", tmpLst);
        
        return dbQuery;
    }

    public boolean getIsAutoCommit() { return defIsAutoCommit; }

    public boolean getIsReadOnly()  { return defIsReadOnly; }

    public int getDefaultTransactionIsolation() {return defTransactionIsolation; } 

	public String getDbUrl(String aDbName) {
        String url = "jdbc:hsqldb:";
        if (! isEmbeded) {
        		url = url + "hsql://" + dbHost;
        		if (dbPort != null) { url = url + ":" + dbPort; }
                if (aDbName != null) {
            		if (aDbName.indexOf("jdbc:hsqldb:hsql://") < 0)  url = url + "/" + aDbName; else url = aDbName;
                }
        }
        else {
        	File baseDir = !Util.StrIsEmpty(dbHost) ? new File(dbHost) : new File(appPath, "hsqldb");
        	if (!Util.StrIsEmpty(aDbName)) { 
        			if (aDbName.indexOf(System.getProperty("file.separator")) < 0) baseDir = new File(baseDir, aDbName);
        			else baseDir = new File(aDbName);
        	}
        	url = url + "file:" + baseDir;
        }
        return url;
	}

    
    public void ExecQuery(boolean aIsStoredProcCall, IDatabaseQuery aQuery, Class aResultRowClass, List aResults, Locale aLocale, long aTimeZoneOffset) throws Exception {
        try {
            super.ExecQuery(aIsStoredProcCall, aQuery, aResultRowClass, aResults, aLocale, aTimeZoneOffset);
        }
        catch (Exception e) {
            String msg = e.getMessage(); int errCode = 0;
            if (e instanceof SQLException) {
            			errCode = ((SQLException) e).getErrorCode();
            			//String errState = ((SQLException) e).getSQLState(); 
            			//msg = (!Util.StrIsEmpty(errState) ? errState + "(" + errCode + ")": errCode) + ": " + msg; 
            }
            Util.RaiseUException(errCode, msg, aLocale); 
        }
    }

    
    public void BeginTran(IDatabaseConnection conn, String aSQL) throws Exception {
       super._BeginTran(conn, beginTranStatement);
    }


    public String getPingQueryText() {
        return "call Now()";
    };
    public boolean getPingQueryIsSP() {
    		return false;
    }
    public char getQuoteChar() {
        return quoteChar.charAt(0);
    }

    public String BuildStoredProcExecSQL( String aSPName, Properties params, String[] paramNames ) {
        String parStr = "";
        if (params.size() > 0) {
            for (int i=0; i<params.size(); i++) {
                        parStr = parStr + ", " + ":" + paramNames[i] + " "; 
            }
            parStr = parStr.substring(1);
        }
        String sql = "call "+ aSPName + "(" + parStr + ")";
        return sql;
    }
    
    public int getMaxSPParamsCount() {
        return maxSPparamsCount;
    }

    public String getNullWord() {
        return nullWord;
    }

    public String replaceSpecialSymbols(String str) {
        String res = str;
        if (res != null) {
        				// 	 res = res.replace("\\", "|");
        	//res = res.replace("\'", "\""); old design: single quote replaced to double quotes        	
/*!!!*/  res = res.replace("\'", "''"); // single quote replaced to 2 single quotes   


        }
        return res;
    }


    public String getDateTimeStr(long Time) {
        return getDateStr(Time) + " " + getTimeStr(Time);
    }
    public String getDateStr(long Time) {
        Calendar c = Calendar.getInstance();
        c.setTime( new Date(Time) );
        String s = String.valueOf(c.get(Calendar.YEAR)) + "-" + String.valueOf(c.get(Calendar.MONTH)+1) + "-" + String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        return s;
    }
    public String getTimeStr(long Time) {
        Calendar c = Calendar.getInstance();
        c.setTime( new Date(Time) );
        String s = String.valueOf(c.get(Calendar.HOUR_OF_DAY)) + ":" + String.valueOf(c.get(Calendar.MINUTE)) + ":" + String.valueOf(c.get(Calendar.SECOND));
        return s;
    }

    public String ConvertDateStrToSQLDateStr(String str, String formatStr) {
    	String res = null;  byte d = 0; byte m = 0; int y = 0;
    	     if (formatStr == null)  formatStr = "DD.MM.YYYY";
    	     try {
    		     if (str != null) {
    			    	 if (str.length() == 10) {
    			    		   if (formatStr.equalsIgnoreCase("DD.MM.YYYY")) {
    			    		 		d = Util.StrToByte(str.substring(0,2));
    			    		 		m = Util.StrToByte(str.substring(3,5));
    			    		 		y = Util.StrToInt(str.substring(6,10));
    			    		   }	
    			    	 }
    			    	 else
    			         if (str.length() == 8) {
    			        	   if (formatStr.equalsIgnoreCase("DD.MM.YY")) {
    			        		    d = Util.StrToByte(str.substring(0,2));
    			    		 		m = Util.StrToByte(str.substring(3,5));
    			    		 		y = 2000 + Util.StrToInt(str.substring(6,8));
    			        	   }
    			         }
    			    	 
    			    	 if (d > 0 && m >= 0 && y > 0) {
    			    		    String mm = Byte.toString(m); if (mm.length() == 1) mm = "0" + mm;
    			    		    String dd   = Byte.toString(d); if (dd.length()   == 1) dd   = "0" + dd;

    		    		 		res = Integer.toString(y) + mm + dd;  
    			    	 }	 
    		     }
    	     }  
    	     catch (NumberFormatException e) {}
    	     return res;
       }


	@Override
	public String getShutdownText(int mode) {
		return "SHUTDOWN" + (mode == SHRINK_DATABASE ? " COMPACT" : "");
	}

	@Override
	public String getCheckDatabaseText(int mode) {
		return "CHECKPOINT" + (mode == SHRINK_DATABASE ? " DEFRAG" : "");
	}

    @Override
	public void destroy() {
		super.destroy();
	}





}
