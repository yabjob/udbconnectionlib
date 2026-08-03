/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) UDB Connection Library contributors.
 * See the LICENSE file in the project root for license information.
 */
package udblib.sql;

import udblib.IDatabaseAdapter;
import udblib.IDatabaseConnection;
import udblib.IDatabaseQuery;
import util.UException;
import util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Locale;

/**
 * Legacy query implementation supporting named parameter expansion, result collection, stored procedures, and transaction helpers.
 *
 * <p><strong>Compatibility note:</strong> Public names follow the original API,
 * including its historical naming conventions. Renaming them would be a breaking change.</p>
 *
 * @since 2.1.0
 */
public class sqlQuery implements IDatabaseQuery {
	
	private 		IDatabaseAdapter 			  adapter;
    private 		IDatabaseConnection  	  connection;
    private 		String					connKey;
    private 		String					ownerID;
    private 		String					sql;
    private 		char						quoteChar;
    private 		Properties				params;
    private        String[]					paramNamesOrdered; // param names by order (need because params is unsorted in Properties object)    
    private 		Locale					locale;
    private 		long						timeZoneOffset; 

    
    
    public sqlQuery(IDatabaseAdapter aAdapter, IDatabaseConnection aConn, String aConnKey, String aOwnerID, Locale aLocale, long aTimeZoneOffset) {
       adapter           	= aAdapter;
       connection      	= aConn;
       connKey         	= aConnKey;
       ownerID         	= aOwnerID;
       locale              	= aLocale;
       timeZoneOffset = aTimeZoneOffset;
       quoteChar       	= aAdapter.getQuoteChar();
       params           	= new Properties();
       paramNamesOrdered =  new String[aAdapter.getMaxSPParamsCount()];
    }
    
    public IDatabaseAdapter getAdapter() 	   		{ return adapter; }

    public IDatabaseConnection getConn() 	   		{ return connection; }

    public boolean testConn(boolean needPing, boolean needReconnect)	throws Exception { 
    	boolean res = false;
   		if (connection != null) {
    			if (connection.isExists()) res = needPing ? adapter.PingConnection(connection) : true;
   		}
   		if (!res) {
				try { connection.Disconnect(); } catch(Exception e) {}   						
				res = connection.Connect() != null;
				if (connection.isExists()) res = needPing ? adapter.PingConnection(connection) : true;
   		}
    	return res;
    }
    
    public String getConnKey() 		       					{ return connKey; }

    public String getOwnerID() 		       					{ return ownerID; }

    public Properties getParams() 	   					{ return params; }

    public String[] getParamNames()   					{ return paramNamesOrdered; }

    public String getSQL() 		          					{ return sql; }

    
    public void ClearParams() {
       params.clear();
    }
    
    public String getParamValue(String name) {
      return params.getProperty(name);
    }

    private void setPar(String name, String value, String type, boolean aReplaceSpecialSymbols) {
      String v = value; long v2;
      if (adapter.getSQLToLowerCase()) name = name.toLowerCase();
      if (type != null) {
        if (type.equalsIgnoreCase(pSTRING)) {
               if (value != null) v =  quoteChar + (aReplaceSpecialSymbols ? adapter.replaceSpecialSymbols(value) : value) + quoteChar;
               else v = pNULL;
        } else
        if (type.equalsIgnoreCase(pINT)) {
           v =  value;
        } else
        if (type.equalsIgnoreCase(pFLOAT)) {
           v =  value;
        } else
        if (type.equalsIgnoreCase(pDATETIME)) {
        	v2 = Long.parseLong(value); if (v2 > 0) v2 = v2 + timeZoneOffset;
            v =  quoteChar + adapter.getDateTimeStr(v2) + quoteChar;
        } else
        if (type.equalsIgnoreCase(pDATE)) {
        	v2 = Long.parseLong(value); if (v2 > 0) v2 = v2 + timeZoneOffset;
            v =  quoteChar + adapter.getDateStr(v2) + quoteChar;
        } else
        if (type.equalsIgnoreCase(pTIME)) {
        	v2 = Long.parseLong(value); if (v2 > 0) v2 = v2 + timeZoneOffset;
            v =  quoteChar + adapter.getTimeStr(v2) + quoteChar;
        } else
        if (type.equalsIgnoreCase(pNULL)) {
           v = adapter.getNullWord();
        }
      }
      params.setProperty(name, v);
      paramNamesOrdered[params.size()-1] = name;
    }

    public void setParam(String name, String value, boolean aReplaceSpecialSymbols) {
        	setPar(name,value, pSTRING, aReplaceSpecialSymbols);
      }

    
    public void setParam(String name, String value) {
      setPar(name,value,pSTRING, true);
    }

    public void setCharParam(String name, char value) {
      setPar(name, String.valueOf(value), pSTRING, true);
    }

    public void setParam(String name, int value) {
      setPar(name, String.valueOf(value), pINT, false);
    }
    
    public void setParam(String name, long value) {
      setPar(name, String.valueOf(value), pINT, false);
    }
    
    public void setParam(String name, long value, boolean IsDateTime, boolean IsDate, boolean IsTime) {
       if (IsDateTime || IsDate || IsTime) {
           if (value > 0 || IsTime) {
               if (IsDateTime) setPar(name, String.valueOf(value), pDATETIME, false); else
               if (IsDate) setPar(name, String.valueOf(value), pDATE, false); else
               if (IsTime) setPar(name, String.valueOf(value), pTIME, false);
           }
           else setParamToNull(name);
       }
       else setPar(name,String.valueOf(value),pINT, false);
    }
    
    public void setParam(String name, double value) {
       setPar(name, String.valueOf(value), pFLOAT, false );
    }
    
    public void setParam(String name, Long value) {
      if (value != null) setParam(name, value.longValue());
      else setParamToNull(name);
    }
    
    public void setParam(String name, Integer value) {
      if (value != null) setParam(name, value.intValue());
      else setParamToNull(name);
    }
    
    public void setParam(String name, Double value) {
      if (value != null) setParam(name, value.doubleValue());
      else setParamToNull(name);
    }

    public void setParamToNull(String name) {
      setPar(name, null, pNULL, false );
    }

    public String ParseTime(long value, boolean IsDateTime, boolean IsDate, boolean IsTime) {
       String res = null;
       if (IsDateTime || IsDate || IsTime) {
            if (value > 0 || IsTime) {
                if (IsDateTime) res = quoteChar + adapter.getDateTimeStr(value) + quoteChar; else
                if (IsDate) res = quoteChar + adapter.getDateStr(value) + quoteChar; else
                if (IsTime) res = quoteChar + adapter.getTimeStr(value) + quoteChar;
            }
       }
       return res;
    }
    
    public String ConvertDateStrToSQLDateStr(String str, String formatStr) {
    	return adapter.ConvertDateStrToSQLDateStr(str, formatStr);
    }

    public IDatabaseConnection ReservConnect() throws UException {
      try { connection = adapter.ReservConnect(this); }
      catch (Exception e) { Util.RaiseUException(e.getMessage()); }
     return connection;
    }

    public void FreeConnect() throws UException {
      try { adapter.FreeConnect(connection); }
      catch (Exception e) { Util.RaiseUException(e.getMessage()); }
    }

    public void DestroyConnect() throws UException {
        try { adapter.DestroyConnect(connection); }
        catch (Exception e) { Util.RaiseUException(e.getMessage()); }
   }

   private void doExec(boolean aIsStoredProcCall, String aSQL, Class aResultRowClass, List aResults) throws UException {
      try {
            if (adapter.getSQLToLowerCase()) aSQL = aSQL.toLowerCase();
            sql = aSQL;
            adapter.ExecQuery( aIsStoredProcCall, this, aResultRowClass, aResults, locale, timeZoneOffset); 
      }
      catch (Exception e) { Util.RaiseUException(e.getMessage()); }
   }


public void Exec(boolean aIsStoredProcCall, String aSQL, Class aResultRowClass, List aResultArray) throws UException {
      doExec(aIsStoredProcCall, aSQL, aResultRowClass, aResultArray);
}

public List Exec(boolean aIsStoredProcCall, String aSQL, Class aResultRowClass) throws UException {
      List resArray = new ArrayList();
      doExec(aIsStoredProcCall, aSQL, aResultRowClass, resArray);
      return resArray;
     }

public List Exec(boolean aIsStoredProcCall, String aSQL, String[] queryParNames, String[] queryParValues, Class aResultRowClass, boolean  needResultSet)  throws UException {
	List  res = needResultSet ? new ArrayList() : null;

	String[]  qParNames = queryParNames != null ? queryParNames : new String [0];
    String[]  qParValues = queryParValues != null ? queryParValues : new String [0];
    
    ClearParams();
	for (int i=0; i<qParNames.length; i++) {
			setParam(qParNames[i], qParValues[i]); 
	}
	
	doExec(aIsStoredProcCall, aSQL, aResultRowClass, res);	
    
	return res;
}

public void Exec(boolean aIsStoredProcCall, String aSQL, List aResults) throws UException {
      doExec(aIsStoredProcCall, aSQL, null, aResults);
    }

public void Exec(boolean aIsStoredProcCall, String aSQL) throws UException {
      doExec(aIsStoredProcCall, aSQL, null, null);
   }

public void Exec(String aSQL) throws UException {
    doExec(false, aSQL, null, null);
 }

public Object Exec_Get(String aSQL, Class aResultRowClass) throws UException {
    List resLst = new ArrayList();
    doExec(false, aSQL, aResultRowClass, resLst);
    return resLst.size() > 0 ? resLst.get(0) : null;
}

public void ExecSP(String aStoredProcName, Class aResultRowClass, List aResults) throws UException {
       String aSQL = adapter.BuildStoredProcExecSQL( aStoredProcName, params, paramNamesOrdered );
       Exec(true, aSQL, aResultRowClass, aResults);
   }

public void ExecSP(String aStoredProcName, List aResults) throws UException {
       String aSQL = adapter.BuildStoredProcExecSQL( aStoredProcName, params, paramNamesOrdered );
       Exec(true, aSQL, aResults);
   }
public void ExecSP(String aStoredProcName) throws UException {
       String aSQL = adapter.BuildStoredProcExecSQL( aStoredProcName, params, paramNamesOrdered );
       Exec(true, aSQL);
   }
   
public Object ExecSP_Get(String aStoredProcName, Class aResultRowClass, List aResults) throws UException {
       ExecSP(aStoredProcName, aResultRowClass, aResults);
       return aResults.size() > 0 ? aResults.get(0) : null;
   }
public Object ExecSP_Get(String aStoredProcName, Class aResultRowClass) throws UException {
	   List resLst = new ArrayList(); 	
       ExecSP(aStoredProcName, aResultRowClass, resLst);
       return resLst.size() > 0 ? resLst.get(0) : null;
   }

public long ExecSP_Ins(String aStoredProcName, String resultSetIdColumnName, List aResults) throws UException {
       ExecSP(aStoredProcName, aResults);
       sqlResultRow r = ( sqlResultRow ) aResults.get(0);
       return r.getAsLong(resultSetIdColumnName);
   }
public long ExecSP_Ins(String aStoredProcName, String resultSetIdColumnName) throws UException {
	   List resLst = new ArrayList(); 	
       ExecSP(aStoredProcName, resLst);
       sqlResultRow r = ( sqlResultRow ) resLst.get(0);
       return r.getAsLong(resultSetIdColumnName);
   }

public void BeginTran() throws UException {
    try { adapter.BeginTran(connection, null); }
    catch (Exception e) { Util.RaiseUException(e.getMessage()); }
   }
public void CommitTran() throws UException  {
     try { adapter.CommitTran(connection); }
     catch (Exception e) { Util.RaiseUException(e.getMessage()); }
   }
public void RollbackTran(boolean needRaiseEx) throws UException  {
       try { adapter.RollbackTran(connection); }
       catch (Exception e) { if (needRaiseEx) Util.RaiseUException(e.getMessage()); }
   }
public void RollbackTran() throws UException  {
       this.RollbackTran(false);
   }

public void Shutdown(int mode) throws UException {
	   try { Exec(adapter.getShutdownText(mode)); }
	   catch (Exception e) { Util.RaiseUException(e.getMessage()); }
   }

public void CheckDatabase(int mode) throws UException {
	   try { Exec(adapter.getCheckDatabaseText(mode)); }
	   catch (Exception e) { Util.RaiseUException(e.getMessage()); }
}


}
