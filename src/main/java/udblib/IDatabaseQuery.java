/*
SPDX-License-Identifier: Apache-2.0
Copyright (c) 2026 Yuri Boltovski
Author: Yuri Boltovski yabjob@gmail.com
This file is part of the UDB Connection Library.
See the project LICENSE file for license terms.
NOTE: This header was added/verified by Yuri Boltovski. */
package udblib;

import java.util.List;
import java.util.Properties;

import util.UException;

/**
 * Mutable query facade used to set named parameters, execute SQL or stored procedures, and control transactions.
 *
 * <p><strong>Compatibility note:</strong> Public names follow the original API,
 * including its historical naming conventions. Renaming them would be a breaking change.</p>
 *
 * @since 2.1.0
 */
public interface IDatabaseQuery {

    public static final String pSTRING = "STRING";

    public static final String pINT = "INT";

    public static final String pFLOAT = "FLOAT";

    public static final String pDATETIME = "DATETIME";

    public static final String pDATE = "DATE";

    public static final String pTIME = "TIME";

    public static final String pNULL = "NULL";

    public abstract IDatabaseAdapter getAdapter();

    public abstract IDatabaseConnection getConn();
    
    public abstract boolean testConn(boolean needPing, boolean needReconnect) throws Exception; 

    public abstract String getConnKey();

    public abstract String getOwnerID();

    public abstract Properties getParams();

    public abstract String[] getParamNames();

    public abstract String getSQL();

    public abstract void ClearParams();

    public abstract String getParamValue(String name);

    public abstract void setParam(String name, String value);
    
    public abstract void setParam(String name, String value, boolean aReplaceSpecialSymbols);

    public abstract void setCharParam(String name, char value);

    public abstract void setParam(String name, int value);

    public abstract void setParam(String name, long value);

    public abstract void setParam(String name, long value, boolean IsDateTime, boolean IsDate, boolean IsTime);

    public abstract void setParam(String name, double value);

    public abstract void setParam(String name, Long value);

    public abstract void setParam(String name, Integer value);

    public abstract void setParam(String name, Double value);

    public abstract void setParamToNull(String name);

    public abstract String ParseTime(long value, boolean IsDateTime, boolean IsDate, boolean IsTime);

    public abstract String ConvertDateStrToSQLDateStr(String str, String formatStr);
    
    public abstract IDatabaseConnection ReservConnect() throws UException;

    public abstract void FreeConnect() throws UException;

    public abstract void DestroyConnect() throws UException;
    
    public abstract void Exec(boolean aIsStoredProcCall, String aSQL, Class aResultRowClass,  List aResults) throws UException;

    public abstract List Exec(boolean aIsStoredProcCall, String aSQL, Class aResultRowClass)    throws UException;
    
    public abstract List Exec(boolean aIsStoredProcCall, String aSQL, String[] queryParNames, String[] queryParValues, Class aResultRowClass, boolean  needResultSet)  throws UException;
    
    public abstract void Exec(boolean aIsStoredProcCall, String aSQL, List aResults)     throws UException;

    public abstract void Exec(boolean aIsStoredProcCall, String aSQL) throws UException;

    public abstract void Exec(String aSQL) throws UException;
    
    public abstract Object Exec_Get(String aSQL, Class aResultRowClass) throws UException;
    
    public abstract void ExecSP(String aStoredProcName, Class aResultRowClass, List aResults) throws UException;

    public abstract void ExecSP(String aStoredProcName, List aResults)     throws UException;

    public abstract void ExecSP(String aStoredProcName) throws UException;

    public abstract Object ExecSP_Get(String aStoredProcName, Class aResultRowClass) throws UException;

    public abstract Object ExecSP_Get(String aStoredProcName, Class aResultRowClass, List aResults) throws UException;
    
    public abstract long ExecSP_Ins(String aStoredProcName, String resultSetIdColumnName) throws UException;

    public abstract long ExecSP_Ins(String aStoredProcName, String resultSetIdColumnName, List aResults) throws UException;

    public abstract void BeginTran() throws UException;

    public abstract void CommitTran() throws UException;

    public abstract void RollbackTran(boolean needRaiseEx) throws UException;

    public abstract void RollbackTran() throws UException;

    public abstract void Shutdown(int mode) throws UException;
    
    public abstract void CheckDatabase(int mode) throws UException;

}
