/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) UDB Connection Library contributors.
 * See the LICENSE file in the project root for license information.
 */
package udblib.sql.mariadb;

	import java.util.Locale;
import java.util.Properties;

import udblib.IDatabaseAdapter;
import udblib.IDatabaseQuery;
import udblib.sql.mysql.mysqlAdapter;


	


/**
 * MariaDB-specific adapter built on the MySQL adapter behavior while using the MariaDB JDBC driver and URL scheme.
 *
 * <p><strong>Compatibility note:</strong> Public names follow the original API,
 * including its historical naming conventions. Renaming them would be a breaking change.</p>
 *
 * @since 2.1.0
 */
public class mariadbAdapter extends mysqlAdapter {
	
	private static final String 	mariadbAdapterVersion  	= "1.01";
	
	private static final String 	mariadbDriverClassName  	= "org.mariadb.jdbc.Driver";
            




    public mariadbAdapter(String aAppPath, String aDbHost, String aDbPort,
                        			  boolean aIsEmbeded, int aConnMode,
                        			  int aPoolClearPeriod, int aPoolMinConnCount, int aPoolMaxConnCount, int aPoolConnTimeout, 
                        			  boolean aNeedLogger, String aLogFileName ) {

      super (IDatabaseAdapter.DB_Mariadb, mariadbDriverClassName, 
    		  	 aAppPath, aDbHost, aDbPort,
    		  	 aIsEmbeded, aConnMode, 
    		  	 aPoolClearPeriod, aPoolMinConnCount, aPoolMaxConnCount, aPoolConnTimeout, 
    		  	 aNeedLogger, aLogFileName);
      
      setVersion(mariadbAdapterVersion);
    }
    
    
    @Override
	public String getDbUrl(String aDbName) {
        String url = "jdbc:mariadb://" + dbHost;
        if (dbPort != null) { url = url + ":" + dbPort; }
        if (aDbName != null) {
        		if (aDbName.indexOf("jdbc:mariadb://") < 0)  url = url + "/" + aDbName;	else url = aDbName;
        }
        return url;
	}

    
    @Override
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
				// props.setProperty("useUnicode", "true"); 
				// props.setProperty("characterEncoding", "utf8");
				props.setProperty("connectionCollation","utf8mb4_general_ci");
				props.setProperty("noAccessToProcedureBodies","true");
				props.setProperty("autocommit","false"); // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
			}
			
			IDatabaseQuery dbQuery = super.PrepareConnect(aAutoCreatePool, aOwnerID, url, aLogin, aPassword, sqlToLowerCase, props, aLocale, aTimeZoneOffset);
			
			// NO_BACKSLASH_ESCAPES to sql_mode if this option is not exists by default
			// dbQuery.Exec("SET SESSION sql_mode = if(locate('NO_BACKSLASH_ESCAPES',@@GLOBAL.sql_mode)<=0,concat('NO_BACKSLASH_ESCAPES',',',@@GLOBAL.sql_mode),@@GLOBAL.sql_mode)");
			// dbQuery.Exec("SET SESSION OLD_MODE = if(locate('UTF8_IS_UTF8MB3',@@GLOBAL.OLD_MODE)<=0,concat('UTF8_IS_UTF8MB3',',',@@GLOBAL.OLD_MODE),@@GLOBAL.OLD_MODE)");
			
			//List tmpLst = new ArrayList();
			//dbQuery.Exec(false, "select @@SESSION.sql_mode as SqlMode", tmpLst);
			
			return dbQuery;
	}
    

}
