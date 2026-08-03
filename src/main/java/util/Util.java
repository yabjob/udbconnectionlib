/*
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) UDB Connection Library contributors.
 * See the LICENSE file in the project root for license information.
 */
package util;

    import util.io.FastByteArrayOutputStream;
    
    import java.util.Locale;
    import java.util.Calendar;
    import java.util.Date;
    import java.text.DateFormat;
    import java.io.StringWriter;
    import java.io.PrintWriter;
    import java.io.ObjectOutputStream;
    import java.io.ObjectInputStream;




/**
 * Legacy conversion, formatting, reflection, timing, and exception helper methods used throughout the library.
 *
 * <p><strong>Compatibility note:</strong> Public names follow the original API,
 * including its historical naming conventions. Renaming them would be a breaking change.</p>
 *
 * @since 2.1.0
 */
public class Util {

    // locale types
    public static final Locale RUSSIAN_LOCALE  = new java.util.Locale("ru","RU","");
    public static final Locale ENGLISH_LOCALE  = java.util.Locale.US;

	
    public static double ObjToDouble(Object o) {
        double res = 0;
        if (o instanceof Double) res = ((Double) o).doubleValue(); else
        if (o instanceof Float) res = ((Float) o).floatValue(); else
        try { if (o instanceof String) res = Double.parseDouble((String) o);} catch (NumberFormatException e) {}
        return res;
   }

    
    public static int ObjToInt(Object o) {
         int res = 0;
         if (o instanceof Integer) res = ((Integer) o).intValue(); else
         if (o instanceof Long) res = ((Long) o).intValue(); else
         if (o instanceof Byte) res = ((Byte) o).byteValue(); else
         if (o instanceof Short) res = ((Short) o).shortValue(); else
         if (o instanceof Double) res = ((Double) o).intValue(); else
         if (o instanceof Float) res = ((Float) o).intValue(); else
         try { if (o instanceof String) res = Integer.parseInt((String) o);} catch (NumberFormatException e) {}
         return res;
   }

    public static long ObjToLong(Object o) {
        long res = 0;
         if (o instanceof Long) res = ((Long) o).longValue(); else
         if (o instanceof Integer) res = ((Integer) o).intValue(); else
         if (o instanceof Byte) res = ((Byte) o).byteValue(); else
         if (o instanceof Short) res = ((Short) o).shortValue(); else
         if (o instanceof Double) res = ((Double) o).longValue(); else
         if (o instanceof Float) res = ((Float) o).longValue(); else
         try { if (o instanceof String) res = Long.parseLong((String) o);} catch (NumberFormatException e) {}
         return res;
   }

    public static byte ObjToByte(Object o) {
    	byte res = 0;
    	if (o instanceof Byte) res = ((Byte) o).byteValue(); else
        try { res = Byte.parseByte(Integer.toString(Util.ObjToInt(o)));  } catch (NumberFormatException e) {}
    	return res;
    }

    public static boolean ObjToBool(Object o) {
        boolean res = false;
         if (o instanceof Boolean) res = ((Boolean) o).booleanValue(); else
         try { if (o instanceof String) res = Boolean.parseBoolean((String) o);} catch (NumberFormatException e) {}
         return res;
   }

    public static String ObjToStr(Object o) {
         String res = null;
         try { if (o instanceof String) res = o.toString();} catch (NumberFormatException e) {}
         return res;
   }

    
    public static long StrToLong(String str) {
	     long res = 0;
	     try { res = Long.parseLong(str); }  catch (NumberFormatException e) {}
	     return res;
    }
    
    public static int StrToInt(Object str) {
	     int res = 0;
	     String s = null;
	     if (str instanceof String) s = (String) str; else
	     if (str instanceof Character) s = ((Character) str).toString();
	     try { res = Integer.parseInt(s); }  catch (NumberFormatException e) {}
	     return res;
    }
    
    public static short StrToShort(String str) {
	     short res = 0;
	     try { res = Short.parseShort(str); } catch (NumberFormatException e) {}
	     return res;
    }
    
    public static byte StrToByte(String str) {
	     byte res = 0;
	     try { res = Byte.parseByte(str); } catch (NumberFormatException e) {}
	     return res;
    }
    
    public static float StrToFloat(String str) {
	     float res = 0;
	     try { res = Float.parseFloat(str); } catch (NumberFormatException e) {}
	     return res;
    }
    
    public static double StrToDouble(String str) {
	     double res = 0;
	     try { res = Double.parseDouble(str); } catch (NumberFormatException e) {}
	     return res;
    }
    
    public static byte BoolToByte(boolean b) {
	     byte res = 0;
	     if (b) res = 1;
	     return res;
    }

     public static boolean StrIsEmpty(String s) {
	      boolean res = false;
	      if (s != null) {
	        res = s.trim().equals("");
	      }
	      else res = true;
	      return res;
    }

    
    public static long CurrentTime() {
	      return (Calendar.getInstance().getTime()).getTime();
    }

    public static void Delay(int value) { // msec
	        long startTime = CurrentTime();
	        //long period    = 0;
	        while ((CurrentTime() - startTime) <= value) {
	        }
	    }

     public static String FormatDateTime(String aDateFormat, String aTimeFormat, long aTime, Locale locale) {
	      String res = null;
	      if (locale == null) locale = Locale.getDefault();
	      int DateFrmt = 0;
	      if (aDateFormat != null) {
	        if (aDateFormat.equalsIgnoreCase("FULL"))   DateFrmt = DateFormat.FULL;   else
	        if (aDateFormat.equalsIgnoreCase("MEDIUM")) DateFrmt = DateFormat.MEDIUM; else
	        if (aDateFormat.equalsIgnoreCase("SHORT"))  DateFrmt = DateFormat.SHORT;
	      }
	      int TimeFrmt = 0;
	      if (aTimeFormat != null) {
	        if (aTimeFormat.equalsIgnoreCase("FULL"))   TimeFrmt = DateFormat.FULL;   else
	        if (aTimeFormat.equalsIgnoreCase("MEDIUM")) TimeFrmt = DateFormat.MEDIUM; else
	        if (aTimeFormat.equalsIgnoreCase("SHORT"))  TimeFrmt = DateFormat.SHORT;
	      }
	      Date   dt  = new Date(aTime);
	      if (DateFrmt  > 0  && TimeFrmt  > 0) res = DateFormat.getDateTimeInstance(DateFrmt,TimeFrmt,locale).format(dt); else
	      if (DateFrmt  > 0  && TimeFrmt == 0) res = DateFormat.getDateInstance(DateFrmt,locale).format(dt); else
	      if (DateFrmt == 0 && TimeFrmt   > 0) res = DateFormat.getTimeInstance(TimeFrmt,locale).format(dt);
	      else res = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, locale).format(dt);

	      return res;
    }

     
     public static String FormatDecimals(Object aSource, int aDecSize) throws UException {
    	 String res = null;
   		 String s = String.format("%." + aDecSize +"f", aSource);
   		 if (s != null) {
   			 	s = s.trim();	s = s.replace(",", ".");
   			 	res = s;
   		 }
    	 return res;
     }
     

     
    public static void RaiseUException(String aErrorText) throws UException {
        UException e = new UException(aErrorText); throw e;
    }

    public static void RaiseUException(int aErrorCode, Locale l) throws UException {
        String aErrorText = UException.getErrorMessage(aErrorCode, l);
        UException e = new UException(aErrorText); throw e;
    }
    public static void RaiseUException(int aErrorCode, String aErrorText, Locale l) throws UException {
        if (aErrorCode != 0) {
                String aErrorCodeText = UException.getErrorMessage(aErrorCode, l);
                if (aErrorCodeText != null) aErrorText = aErrorCodeText + "\n" + aErrorText;
        }
        UException e = new UException(aErrorText); throw e;
    }
   
    public static void RaiseUException(Exception e) throws UException {
        UException ex = new UException( e.getMessage() ); throw ex;
    }
    
    public static String StackTraceToString(Exception e) {
	StringWriter sw = new StringWriter();
	e.printStackTrace(new PrintWriter(sw));
	return sw.toString();
    }

    public static Class getClassPrototype(Class clazz) {
        while (clazz.getSuperclass() != null) {
            if (clazz.getSuperclass() != Object.class)  clazz = clazz.getSuperclass();
            else break;
        }
        return clazz;
    }

    public static Class ObjToClass(Object o) {
        Class res = null;
        try { res = (Class) o; } catch (Exception e) {}
        return res;
    }
    
    public static char[] getAbc(Locale locale, boolean upperCase) {
        String s = null;
        if (locale == null) locale = Locale.getDefault();
        if (locale == ENGLISH_LOCALE)  s = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"; else
        if (locale == RUSSIAN_LOCALE)  s = "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ";
        if (s != null && !upperCase) s = s.toLowerCase();
        return s.toCharArray();
    }
    public static char[] get123() {
        String s = "0123456789";
        return s.toCharArray();
    }

    public static Object Copy(Object src) {
    	Object obj = null;
        try {
            // Write the object out to a byte array
            FastByteArrayOutputStream fbos = new FastByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(fbos);
            out.writeObject(src);
            out.flush();
            out.close();

            // Retrieve an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in =
                new ObjectInputStream(fbos.getInputStream());
            obj = in.readObject();
        }
        catch(Exception e) {
        	RaiseUException(e);
        }
        return obj;
    }

}
