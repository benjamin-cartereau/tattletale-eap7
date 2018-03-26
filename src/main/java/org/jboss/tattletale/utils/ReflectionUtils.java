package org.jboss.tattletale.utils;

import java.lang.reflect.Field;
import java.util.logging.Level;

/**
 * Simple helper class for handling reflection
 *
 * @author Benjamin Cartereau
 */
public class ReflectionUtils {
   /**
    * Constructor
    */
   private ReflectionUtils() {    
   }
    
   /**
    * Retrieve a constant value from a Class
    * @param clazz The class to get the constant value from
    * @param fieldName the name of the constant field
    * @return Value of the constant value as <code>String</code>
    */
   public static String getConstantValue(Class<?> clazz, String fieldName)
   {
      String result  = null;
        
      try
      {
          Field f = clazz.getDeclaredField(fieldName);
          f.setAccessible(true);
          if(f.isAccessible()) {
             result = (String) f.get(null);
          }
      } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
          //logger.log(Level.SEVERE, null, ex);
      }
             
      return result;
    }  
}
