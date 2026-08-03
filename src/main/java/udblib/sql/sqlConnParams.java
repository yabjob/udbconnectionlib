/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) UDB Connection Library contributors.
 * See the LICENSE file in the project root for license information.
 */
package udblib.sql;

import java.util.Properties;

import udblib.IDatabaseConnParams;


/**
 * Immutable-style value object containing the JDBC driver, URL, credentials, and connection properties for a pooled connection.
 *
 * <p><strong>Compatibility note:</strong> Public names follow the original API,
 * including its historical naming conventions. Renaming them would be a breaking change.</p>
 *
 * @since 2.1.0
 */
public class sqlConnParams implements IDatabaseConnParams {
	//private static final long serialVersionUID = 1L;
	
	
	private   String      		key                		= null;
    private   int         			dbType             	= 0;
    private   String      		driverClassName	= null;
    private   String      		dbUrl              		= null;
    private   String      		login              		= null;
    private   String      		passw              		= null;
    private   boolean     	sqlToLowerCase    = false;
    private   Properties  		connProps          	= null;



    public sqlConnParams( String aKey, int aDbType, String aDriverClassName,
                         		 String aDbUrl, String aLogin, String aPassw,
                         		 boolean aSqlToLowerCase,  Properties aConnProps ) {
	    
        key               		= aKey;
        dbType            		= aDbType;
        driverClassName	= aDriverClassName;
        dbUrl            		    = aDbUrl;
        login             		= aLogin;
        passw             		= aPassw;
        sqlToLowerCase    = aSqlToLowerCase;
        connProps         	= aConnProps;
    }

    
    public sqlConnParams(String aKey, IDatabaseConnParams cp) {
        key               		= aKey;
        dbType            		= cp.getType();
        driverClassName   = cp.getDriverClassName();
        dbUrl             		= cp.getDbUrl();
        login             		= cp.getLogin();
        passw             		= cp.getPassw();
        sqlToLowerCase    = cp.getSqlToLowerCase();
        connProps         	= cp.getConnProps();
    }



    public String getKey()  {  return key; }

    public int getType() { return dbType; }

    public String getDriverClassName() { return driverClassName; }

    public String getDbUrl() { return dbUrl; }

    public String getLogin() { return login; }

    public String getPassw() { return passw; }

    public boolean getSqlToLowerCase() { return sqlToLowerCase; }

    public Properties getConnProps() { return connProps; }

}
