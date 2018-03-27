package org.jboss.tattletale.utils;

import java.util.Collection;

/**
 * Simple helper class for handling strings
 *
 * @author Benjamin Cartereau
 */
public class StringUtils {
   /**
    * Private constructor
    */
   private StringUtils() {    
   }
   
   /**
    * Join a collection of strings together separated by a joiner
    * @param input Collection&lt;String&gt;
    * @param joiner String as strings separator
    * @return String the joined string
    */
   public static String join(Collection<String> input, String joiner)
   {
      if (null == input || 0 == input.size())
      {
         return "";
      }
      if (null == joiner)
      {
         joiner = "";
      }
      final StringBuilder list = new StringBuilder();
      for (String m : input)
      {
         list.append(m).append(joiner);
      }
      list.setLength(list.length() - joiner.length());
      return list.toString();
   }
   
   /**
    * Get the greatest common prefix between 2 strings
    * @param a the first string
    * @param b the second string
    * @return length of the common prefix or index of the first different letter 
    */
   public static int getGreatestCommonPrefix(String a, String b) {
      if (a == null || b == null)
      {
          return -1;
      }
      int minLength = Math.min(a.length(), b.length());
      for (int i = 0; i < minLength; i++) {
          if (a.charAt(i) != b.charAt(i)) {
              return i;
          }
      }
      return minLength;
   }
}
