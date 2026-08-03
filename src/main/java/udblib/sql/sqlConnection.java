/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) UDB Connection Library contributors.
 * See the LICENSE file in the project root for license information.
 */
package udblib.sql;

import util.Util;

import java.sql.Connection;
import java.sql.DriverManager;
//import java.sql.Statement;
import java.sql.PreparedStatement;
//import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.sql.Types;


import java.util.Properties;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Calendar;
//import java.util.Enumeration;
import java.util.List;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import udblib.IDatabaseAdapter;
import udblib.IDatabaseConnParams;
import udblib.IDatabaseConnection;



/**
 * Managed JDBC connection implementation. It opens and closes the physical connection and maps result-set columns to public fields on result objects.
 *
 * <p><strong>Compatibility note:</strong> Public names follow the original API,
 * including its historical naming conventions. Renaming them would be a breaking change.</p>
 *
 * @since 2.1.0
 */
public class sqlConnection implements IDatabaseConnection {
	
   private   long           	    				ID;
   private   String         	    				OwnerID;
   private   String         	    				key;
   private   long           	    				lastActivityTime;
   private   IDatabaseConnParams 	connParams;
   private   Connection         				connection;
   private   IDatabaseAdapter     		adapter;
   private   Logger								logger;


  public sqlConnection( String aOwnerID, String aKey, long aID, IDatabaseConnParams cp, IDatabaseAdapter ad, Logger aLogger) {
	  	ID 						= aID;	 
	    OwnerID          		= aOwnerID;
	    key              			= aKey;
	    lastActivityTime 	= Util.CurrentTime();
	    connParams       	= new sqlConnParams(aKey, cp);
	    connection       		= null;
	    adapter          		= ad;
	    logger					= aLogger; 	
  }

public Connection Connect() throws Exception {
   Connection c = null; IDatabaseConnParams cp = connParams;
     if (connection != null) { if ( ! isExists() ) connection = null; }
     if (connection == null) {
        Class.forName( cp.getDriverClassName() );
        if (cp.getConnProps() != null) {
           c = DriverManager.getConnection( cp.getDbUrl(), cp.getConnProps() );
	           doSafeLog(cp.getDbUrl());
	           if (cp.getConnProps() !=null) {
	        	   		doSafeLog(cp.getConnProps().toString());
	           }
        }
        else {
           c = DriverManager.getConnection( cp.getDbUrl(), cp.getLogin(), cp.getPassw() );
           doSafeLog(cp.getDbUrl());
        }
        if (c.isReadOnly() 	   !=  adapter.getIsReadOnly()) 	 c.setReadOnly(adapter.getIsReadOnly());
        if (c.getAutoCommit() !=  adapter.getIsAutoCommit())	 c.setAutoCommit(adapter.getIsAutoCommit());
        c.setTransactionIsolation( adapter.getDefaultTransactionIsolation() );
        connection = c;
     }
     else {
        c = connection;
     }
     return c;
  }

public void Disconnect() throws Exception {
    if (connection != null)
        if (isExists()) connection.close();
  }

public boolean isExists() {
    boolean r = false;
    if (connection != null) {
        try  { 
        		r = ! connection.isClosed();
    			if (r) r = connection.isValid(10);
    			// doSafeLog("sqlConnection: IsExists = " + (r ? "true" : "false"));
        }
        catch (Exception e) { doSafeLog(e.getMessage()); }
    }
    return r;
  }


public long getID() { return ID; }

public String getKey() { return key; }

public Connection getConnection() { return connection;  }

public IDatabaseConnParams getConnParams() { return connParams;  }

public IDatabaseAdapter getAdapter() { return adapter; }

public String getOwnerID() { return OwnerID; }
public void setOwnerID(String aOwnerID) { OwnerID = aOwnerID; }

public boolean HasOwner() { return OwnerID != null; }
public boolean IsOwner(String aOwnerID) { return aOwnerID != null && OwnerID != null ? aOwnerID.equalsIgnoreCase(OwnerID) : false; }

public long getInactivePeriod() { // minutes
    return (Util.CurrentTime() - lastActivityTime) / 60000;
}


private void doSafeLog(String aMes) {
     if (logger == null) return;
     try {
            logger.logp(Level.FINEST, " ", " ", aMes);
      }
     catch (Exception e) { }
}


  private String ParseSQL(String aSQL, Properties aParams, String aNoValue) throws Exception
  {
    String       res		= "";
    String       c			= null;
    String       c2			= "";
    String       p			= null;
    String       a			= null;
    
    boolean   needCopy	= false;
    boolean   needIgnore	= false;

    if (aNoValue == null) {aNoValue = "";}

    for (int i = 0; i < aSQL.length(); i++) {
      c  = aSQL.substring(i, i+1);
      c2 = c;
      //if (c.equals("'") || c.equals("\"")) { needIgnore = ! needIgnore; }
      if (needIgnore) continue;

      if (c.equals(":")) { needCopy = true; p = ""; c2 = ""; }
         else
      if (c.equals(" ") || c.equals(",") || c.equals(";")) {
           if (needCopy) {
               c2 = aNoValue + c;
               if (aParams != null) {
                  a = aParams.getProperty(p);
                  if (a != null) {
                       c2 = a + c;
                       if (a.equals("")) { c2 = aNoValue + c; }
                  }
               }
            }
            needCopy = false;
      }
         else
      if (needCopy) { p = p + c;  c2 = "";}

      res = res + c2;
    }

    if (needCopy) {
      c2 = aNoValue;
      if (aParams != null) {
         a = aParams.getProperty(p);
         if (a != null) {
              c2 = a;
              if (a.equals("")) { c2 = aNoValue; }
         }
      }
      res = res + c2;
    }
    return res;
  }


  private void setResultRowAbstractFieldValue( Object aRowObject,
                                               int dbColNum, String dbColName, int dbColType,
                                               ResultSet rs, Locale locale, long aTimeZoneOffset)  throws Exception {
     Object dbColValue = rs.getObject(dbColNum);

     if (dbColType == Types.TIMESTAMP || dbColType == Types.DATE || dbColType == Types.TIME) {
    	 	dbColValue = rs.getTimestamp(dbColNum, Calendar.getInstance(locale));
    	 	if (aTimeZoneOffset > 0 && dbColValue != null) {
    	 			long v = ((Timestamp) dbColValue).getTime();
    	 			if (v > 0) dbColValue = new java.sql.Timestamp( v - aTimeZoneOffset);
    	 	}
     }
     ((sqlResultRow) aRowObject).set(dbColNum-1, dbColName, dbColValue);
  }



  private void setResultRowFieldValue( Object aRowObject, Field[] aRowFields,
                                       int dbColNum,  String dbColName, int dbColType,
                                       ResultSet rs, Locale locale, long aTimeZoneOffset) throws Exception {

    Field rowField = null;   Field fld = null;
    for (int i = 0; i < aRowFields.length; i++) {
      fld = aRowFields[i];
      if (dbColName.equalsIgnoreCase(fld.getName())) { rowField = fld;  break; }
    }
    if (rowField == null) return;

    Object dbColValue = rs.getObject(dbColNum);

    if (dbColType == Types.TIMESTAMP || dbColType == Types.DATE || dbColType == Types.TIME) {
      dbColValue = rs.getTimestamp(dbColNum, Calendar.getInstance(locale));
    }

    String cl = rowField.getType().getName();

     if (cl.equals("java.lang.String")) {
        if (dbColValue != null && (dbColType == Types.TIMESTAMP || dbColType == Types.DATE || dbColType == Types.TIME)) {
           long v = ((Timestamp) dbColValue).getTime();
             if (dbColType==Types.TIMESTAMP) dbColValue = Util.FormatDateTime("SHORT","MEDIUM",v,locale); else
             if (dbColType==Types.DATE)      dbColValue = Util.FormatDateTime("SHORT",null,v,locale); else
             if (dbColType==Types.TIME)      dbColValue = Util.FormatDateTime(null,"MEDIUM",v,locale);
        } else
        if (dbColType==Types.BIGINT || dbColType==Types.INTEGER || dbColType==Types.SMALLINT || dbColType==Types.NUMERIC ||
            dbColType==Types.FLOAT || dbColType==Types.DOUBLE || dbColType==Types.REAL || dbColType==Types.DECIMAL ) {
            dbColValue = rs.getString(dbColNum);
        }
        rowField.set( aRowObject, dbColValue );
     } else
     if (cl.equals("java.lang.Long")) {
          if (dbColValue instanceof BigInteger) {long v = rs.getLong(dbColNum); dbColValue = new Long(v);}
          rowField.set(aRowObject, dbColValue);
     } else
     if (cl.equals("java.lang.Integer")) {
          if (dbColValue instanceof Long || dbColValue instanceof BigInteger) {
              int v = rs.getInt(dbColNum); dbColValue = new Integer(v);
          }
          else 
          if (dbColValue instanceof Boolean) {
        	  /* int v = rs.getBoolean(dbColNum) ? 1 : 0; */
        	  int v = rs.getInt(dbColNum); 
        	  dbColValue = new Integer(v);
          }
          rowField.set(aRowObject, dbColValue);
     } else
     if (cl.equals("java.lang.Byte")) {
          if (dbColValue instanceof Long || dbColValue instanceof Integer || dbColValue instanceof BigInteger) {
              byte v = rs.getByte(dbColNum); dbColValue = new Byte(v);
          }
          rowField.set(aRowObject, dbColValue);
     } else
     if (cl.equals("java.lang.Double")) {
          if (dbColValue instanceof BigDecimal) {double v = rs.getDouble(dbColNum); dbColValue = new Double(v);}
          rowField.set(aRowObject, dbColValue);
     } else
     if (cl.equals("java.lang.Float")) {
          if (dbColValue instanceof Double || dbColValue instanceof java.math.BigDecimal) {float v = rs.getFloat(dbColNum); dbColValue = new Float(v);}
          rowField.set(aRowObject, dbColValue);
     } else

     if (cl.equals("char")) { char v = dbColValue.toString().charAt(0); rowField.setChar(aRowObject, v);} else
     if (cl.equals("long")) {
        long v = 0;
          if (dbColValue != null && (dbColType == Types.TIMESTAMP || dbColType == Types.DATE || dbColType == Types.TIME)) {
               v = ((Timestamp) dbColValue).getTime();
          	   if (v > 0) v = v - aTimeZoneOffset;
          }	   
          else v = rs.getLong(dbColNum);
        rowField.setLong(aRowObject, v);
     } else
     if (cl.equals("int"))    {int    v = rs.getInt(dbColNum);   rowField.setInt(aRowObject, v); } else
     if (cl.equals("short"))  {short  v = rs.getShort(dbColNum); rowField.setShort(aRowObject,v); } else
     if (cl.equals("byte"))   {byte   v = rs.getByte(dbColNum);  rowField.setByte(aRowObject,v); } else
     if (cl.equals("float"))  {float  v = rs.getFloat(dbColNum); rowField.setFloat(aRowObject,v); } else
     if (cl.equals("double")) {double v = rs.getDouble(dbColNum); rowField.setDouble(aRowObject,v); } else
     if (cl.equals("boolean")){boolean v= rs.getBoolean(dbColNum); rowField.setBoolean(aRowObject,v);}
     else rowField.set( aRowObject, dbColValue);

  }


@SuppressWarnings("unchecked")
public void doExec( boolean aIsStoredProcCall, String aSQL, Properties aParams, Class aResultRowClass, List aResults,
								Locale aLocale, long aTimeZoneOffset) throws Exception {
     lastActivityTime = Util.CurrentTime();
     //Util.Delay(10000);
     boolean    needResultSet = aResults != null;
     boolean    isAbstractRow = aResultRowClass == null;

     String  sql = ParseSQL(aSQL, aParams, adapter.getNullWord());

     try (PreparedStatement stmt = (aIsStoredProcCall) ? connection.prepareCall(sql) : connection.prepareStatement(sql)) {

     if (needResultSet) {
         if (aResults.size() > 0) aResults.clear();

         doSafeLog(sql);

         
         try (ResultSet rs = stmt.executeQuery()) {
         ResultSetMetaData rsmd = rs.getMetaData();

         Field[]    rowFields     = null;

         if (   isAbstractRow )  aResultRowClass = sqlResultRow.class;
         if ( ! isAbstractRow )  rowFields = aResultRowClass.getFields();
         
         
         while (rs.next()) {
           Object rowObject  = null;

           if ( isAbstractRow ) rowObject = new sqlResultRow( rsmd.getColumnCount(), aLocale );
           else rowObject = aResultRowClass.getDeclaredConstructor().newInstance();
           
           for (int i = 1; i <= rsmd.getColumnCount(); i++) {
              String  colName   = rsmd.getColumnLabel(i);   //String  colName   = rsmd.getColumnName(i);
              int        colType   = rsmd.getColumnType(i) ;

              if ( ! isAbstractRow ) setResultRowFieldValue( rowObject, rowFields, i, colName, colType, rs, aLocale, aTimeZoneOffset);
              else  setResultRowAbstractFieldValue( rowObject, i, colName, colType, rs, aLocale, aTimeZoneOffset);
                
              
           }
           aResults.add(rowObject);
         }
         }

     }
     else {
         doSafeLog(sql);
    	 stmt.execute();
     }
     }
     
 	if (adapter.isDebugMode()) {
  		System.out.println("QUERY: " + sql + "   -  " + (needResultSet && aResults != null ? aResults.size()  : ""));
  	}

  }


public void BeginTran(String aSQL) throws Exception {
     //Statement stmt = connection.createStatement();
     //stmt.execute(aSQL);	
	 try (PreparedStatement stmt = connection.prepareStatement(aSQL)) {
         stmt.execute();
     }
  }

public void CommitTran() throws Exception {
     connection.commit();
  }

public void RollbackTran() throws Exception {
     connection.rollback();
  }


}
