/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) UDB Connection Library contributors.
 * See the LICENSE file in the project root for license information.
 */
package udblib.sql;

import util.Util;

import java.util.Date;
import java.sql.Timestamp;
import java.util.Locale;
import java.math.BigInteger;


/**
 * Generic result-row container used when callers do not provide a custom result class.
 *
 * <p><strong>Compatibility note:</strong> Public names follow the original API,
 * including its historical naming conventions. Renaming them would be a breaking change.</p>
 *
 * @since 2.1.0
 */
public class sqlResultRow {
    private String[] names;
    private Object[] values;
    private Locale   locale;


  public sqlResultRow(int aFieldCount, Locale aLocale) {
     locale = aLocale;
     names  = new String[aFieldCount];
     values = new Object[aFieldCount];
  }


  private int getIndexByName(String aFieldName) {
    int res = -1;
    if (aFieldName != null) {
        for (int i = 0; i < names.length; i++) {
          if (aFieldName.equalsIgnoreCase(names[i])) {res = i; break;}
        }
    }
    return res;
  }


  public void set(int idx, String aFieldName, Object aFieldValue) {
     names[idx]  = aFieldName;
     values[idx] = aFieldValue;
  }

  public void setValue(String aFieldName, Object aFieldValue) {
     int idx = getIndexByName(aFieldName);
     if (idx >= 0) values[idx] = aFieldValue;
  }

  public Object get(String aFieldName) {
     Object res = null;
     int idx = getIndexByName(aFieldName);
     if (idx >= 0) res = values[idx];
     return res;
  }

  public Object get(int idx) {
     Object res = null;
     if (idx >= 0) res = values[idx];
     return res;
  }

  public long getAsLong(String aFieldName) {
     long res = 0;
     int idx = getIndexByName(aFieldName);
     if (idx >= 0) {
         Object o = values[idx];
         if (o != null) {
             if (o.getClass().equals(Timestamp.class) ||
                 o.getClass().equals(Date.class))      res = ((Date) o).getTime(); else
             if (o.getClass().equals(Long.class))      res = ((Long) o).longValue();    else
             if (o.getClass().equals(BigInteger.class))   res = ((BigInteger) o).longValue();  else
             if (o.getClass().equals(Integer.class))   res = ((Integer) o).intValue();  else
             if (o.getClass().equals(Byte.class))      res = ((Byte) o).byteValue();    else
             if (o.getClass().equals(Double.class))    res = ((Double) o).longValue();  else
             if (o.getClass().equals(Float.class))     res = ((Float) o).longValue();   else
             if (o.getClass().equals(String.class))    res = Util.StrToLong((String) o);
         }
     }
     return res;
  }

  public String getAsDateTime(String aFieldName, String aDateFormat, String aTimeFormat) {
     String res = null;
     int idx = getIndexByName(aFieldName);
     if (idx >= 0) {
         Object o = values[idx];
         long tm;
         if (o != null) {
             if (o.getClass().equals(Timestamp.class) || o.getClass().equals(Date.class)) {
                 tm = ((Date) o).getTime();
                 res = Util.FormatDateTime(aDateFormat,aTimeFormat,tm,locale);
             } else
             if (o.getClass().equals(Long.class)) {
                 tm = ((Long) o).longValue();
                 res = Util.FormatDateTime(aDateFormat,aTimeFormat,tm,locale);
             }
         }
     }
     return res;
  }

  public String getAsDateTime(String aFieldName) {
      return getAsDateTime(aFieldName, "SHORT","SHORT");
  }
  public String getValueAsDate(String aFieldName) {
      return getAsDateTime(aFieldName, "SHORT", null);
  }
  public String getAsTime(String aFieldName) {
      return getAsDateTime(aFieldName, null, "MEDIUM");
  }


}
