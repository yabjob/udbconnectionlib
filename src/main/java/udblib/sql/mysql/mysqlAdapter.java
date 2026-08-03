/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) UDB Connection Library contributors.
 * See the LICENSE file in the project root for license information.
 */
package udblib.sql.mysql;

	import udblib.IDatabaseAdapter;
	import udblib.IDatabaseConnection;
	import udblib.IDatabaseQuery;
	import udblib.sql.sqlAdapter;
	import util.Util;

	// import com.mysql.management.MysqldResource;


	import java.sql.Connection;
	import java.util.List;
	import java.util.Properties;
	import java.util.Locale;
	import java.util.Date;
	import java.util.Calendar;


/**
 * MySQL-specific adapter providing JDBC URL construction, SQL escaping, date formatting, health checks, and stored-procedure syntax.
 *
 * <p><strong>Compatibility note:</strong> Public names follow the original API,
 * including its historical naming conventions. Renaming them would be a breaking change.</p>
 *
 * @since 2.1.0
 */
public class mysqlAdapter extends sqlAdapter {
	
	private static String					version							=	"2.01"; 
	
	private static final String 		driverClassName       	= "com.mysql.cj.jdbc.Driver";
    private static final boolean 	defIsAutoCommit			=  true;
    private static final boolean 	defIsReadOnly				=  false;
    private static final int      		defTransactionIsolation =  Connection.TRANSACTION_REPEATABLE_READ; //; TRANSACTION_READ_COMMITTED
    private static final String 		quoteChar          	     	=  "'";
    private static final String 		beginTranStatement 		= "BEGIN";
    private static final String 		nullWord           	     	= "null";
    private static final int      		maxSPparamsCount 		= 100;
            


   // private String   	appPath;
    protected String  	dbHost;
    protected String  	dbPort;
   // private boolean  isEmbeded;
    protected Object  EmbededDBEngine;


    public mysqlAdapter(String aAppPath, String aDbHost, String aDbPort,
                        			  boolean aIsEmbeded, int aConnMode,
                        			  int aPoolClearPeriod, int aPoolMinConnCount, int aPoolMaxConnCount, int aPoolConnTimeout, 
                        			  boolean aNeedLogger, String aLogFileName ) {
    	
       this (IDatabaseAdapter.DB_MySQL, driverClassName, aAppPath, aDbHost, aDbPort,
    		   aIsEmbeded, aConnMode, 
    		   aPoolClearPeriod, aPoolMinConnCount, aPoolMaxConnCount, aPoolConnTimeout, 
    		   aNeedLogger, aLogFileName);
    }
    
    protected mysqlAdapter(int aDbType, String aDriverClassName, String aAppPath, String aDbHost, String aDbPort,
    								boolean aIsEmbeded, int aConnMode,
    								int aPoolClearPeriod, int aPoolMinConnCount, int aPoolMaxConnCount, int aPoolConnTimeout, 
    								boolean aNeedLogger, String aLogFileName ) {

        super (aDbType, aDriverClassName, aConnMode, aPoolClearPeriod, aPoolMinConnCount, aPoolMaxConnCount, aPoolConnTimeout, aNeedLogger, aLogFileName);
        // appPath   = aAppPath;
        // isEmbeded = aIsEmbeded;
        dbHost    = aDbHost;
        dbPort    = aDbPort;
     
        if (aIsEmbeded) { StartEmbededDbEngine(); }
    }    

    public Object StartEmbededDbEngine() {
        if (EmbededDBEngine == null) {
        	/*
                File baseDir = new File(appPath, "mysql");
                EmbededDBEngine = new MysqldResource(baseDir);
                Map<String, Object> options = new HashMap<String, Object>();
                options.put("port", dbPort);
                options.put("default-character-set", "utf8");
                options.put("default-collation", "utf8_general_ci");
//                options.put("noAccessToProcedureBodies", true);
                //options.put("useServerPrepStmts",true); 
                String threadName = "CRMT MySQL embeded";
                ((MysqldResource) EmbededDBEngine).start(threadName, options);
           */     
        }
      return EmbededDBEngine;
    }

    public void StopEmbededDbEngine() {
        if (EmbededDBEngine != null) {
        	/*
           ((MysqldResource) EmbededDBEngine).shutdown();
            EmbededDBEngine = null;
            */
        }
    }

    public Object getEmbededDBEngine() {
        return EmbededDBEngine;
    }

	public String getVersion() { return version; };
	protected String setVersion(String v) { return version = v; };
    
    public void setDbHost(String value) {	dbHost = value; }
    public String getDbHost() { return dbHost;   }
    
    public void setDbPort(String value) { 	dbPort = value;  }
    public String getDbPort() {  return dbPort;   }
    

	public String getDbUrl(String aDbName) {
        String url = "jdbc:mysql://" + dbHost;
        if (dbPort != null) { url = url + ":" + dbPort; }
        if (aDbName != null) {
        		if (aDbName.indexOf("jdbc:mysql://") < 0)  url = url + "/" + aDbName;	else url = aDbName;
        }
        return url;
	}

    
    public synchronized IDatabaseQuery PrepareConnect( boolean aAutoCreatePool,
                                                 String aOwnerID, String aDbName,  String aLogin, String aPassword,
                                                 boolean useConnProps, int aTimeout,  Locale aLocale, long aTimeZoneOffset) throws Exception {
        
    	Properties props =  useConnProps ?  new Properties() : null;

        boolean sqlToLowerCase = true;

        String url = getDbUrl(aDbName);
        
        if (aTimeout > 0 && props != null) {
        	props.setProperty("connectTimeout",String.valueOf(aTimeout));
        	// props.setProperty("socketTimeout",String.valueOf(aTimeout));
        	// props.setProperty("interactiveClient","true");
        }
        if (props != null) {
	          props.setProperty("user",aLogin);
	          props.setProperty("password",aPassword);
	          props.setProperty("useUnicode", "true"); 
	          props.setProperty("characterEncoding", "UTF-8");
	          props.setProperty("connectionCollation","utf8_general_ci");
	          props.setProperty("noAccessToProcedureBodies","true");
	          props.setProperty("relaxAutoCommit","true"); // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        }
        
        IDatabaseQuery dbQuery = super.PrepareConnect(aAutoCreatePool, aOwnerID, url, aLogin, aPassword, sqlToLowerCase, props, aLocale, aTimeZoneOffset);
        
        // NO_BACKSLASH_ESCAPES to sql_mode if this option is not exists by default
        dbQuery.Exec("SET SESSION sql_mode = if(locate('NO_BACKSLASH_ESCAPES',@@GLOBAL.sql_mode)<=0,concat('NO_BACKSLASH_ESCAPES',',',@@GLOBAL.sql_mode),@@GLOBAL.sql_mode)");
        
        		//List tmpLst = new ArrayList();
        		//dbQuery.Exec(false, "select @@SESSION.sql_mode as SqlMode", tmpLst);
        
        return dbQuery;
    }

    public boolean getIsAutoCommit() { return defIsAutoCommit; }

    public boolean getIsReadOnly()  { return defIsReadOnly; }

    public int getDefaultTransactionIsolation() {return defTransactionIsolation; } 

    public void ExecQuery(boolean aIsStoredProcCall, IDatabaseQuery aQuery, Class aResultRowClass, List aResults, Locale aLocale, long aTimeZoneOffset) throws Exception {
        try {
            super.ExecQuery(aIsStoredProcCall, aQuery, aResultRowClass, aResults, aLocale, aTimeZoneOffset);
        }
        catch (Exception e) {
            String msg = e.getMessage();    String AbortErrorTxt = "@@AbortError";     int errCode = 0; 
            if (msg.indexOf(AbortErrorTxt) >= 0) {
                        msg = msg.substring(msg.indexOf(AbortErrorTxt) + AbortErrorTxt.length() + 1);
                        msg = msg.substring(0, msg.indexOf(AbortErrorTxt)-1);
                        errCode = Util.ObjToInt( msg.substring(msg.indexOf("Code=") + 5, msg.indexOf(";")) );
                        msg = msg.substring(msg.indexOf("Message=")+8);
            }
            Util.RaiseUException(errCode, msg, aLocale); 
        }
    }

    
    public void BeginTran(IDatabaseConnection conn, String aSQL) throws Exception {
       super._BeginTran(conn, beginTranStatement);
    }


    public String getPingQueryText() {
        return "select Now() as dt";
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
		return null;
	}

	@Override
	public String getCheckDatabaseText(int mode) {
		return null;
	}

    @Override
	public void destroy() {
		super.destroy();
	}




}
