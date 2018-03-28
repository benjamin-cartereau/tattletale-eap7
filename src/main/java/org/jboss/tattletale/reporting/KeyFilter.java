/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.tattletale.reporting;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * Represents a key filter
 *
 * @author Jesper Pedersen <jesper.pedersen@jboss.org>
 */
public class KeyFilter implements Filter
{
   /** Key Filters */
   private final SortedSet<String> keyFilters;

   /** Constructor */
   public KeyFilter()
   {
      keyFilters = new TreeSet<>(new SizeComparator());
   }

   /**
    * Is filtered
    *
    * @return True if filtered; otherwise false
    */
   @Override
   public boolean isFiltered()
   {
      throw new UnsupportedOperationException("isFiltered() not supported");
   }

   /**
    * Is filtered
    *
    * @param archive The archive
    * @return True if filtered; otherwise false
    */
   @Override
   public boolean isFiltered(String archive)
   {
      if (archive.endsWith(".class"))
      {
         archive = archive.substring(0, archive.indexOf(".class"));
      }

      if (archive.endsWith(".jar"))
      {
         archive = archive.substring(0, archive.indexOf(".jar"));
      }

      if (archive.endsWith(".*"))
      {
         archive = archive.substring(0, archive.indexOf(".*"));
      }

      archive = archive.replace('.', '/');

      for (String v : keyFilters)
      {
         if (archive.startsWith(v))
         {
            return true;
         }
      }

      return false;
   }

   /**
    * Is filtered
    *
    * @param archive The archive
    * @param query   The query
    * @return True if filtered; otherwise false
    */
   @Override
   public boolean isFiltered(String archive, String query)
   {
      throw new UnsupportedOperationException("isFiltered(String, String) not supported");
   }

   /**
    * Init the filter
    *
    * @param filter The filter value
    */
   @Override
   public void init(String filter)
   {
      if (filter != null)
      {
         for (String value : filter.split(","))
         {
            boolean includeAll = false;

            if (value.endsWith(".class"))
            {
               value = value.substring(0, value.indexOf(".class"));
            }

            if (value.endsWith(".jar"))
            {
               value = value.substring(0, value.indexOf(".jar"));
            }

            if (value.endsWith(".*"))
            {
               value = value.substring(0, value.indexOf(".*"));
               includeAll = true;
            }

            value = value.replace('.', '/');

            if (includeAll)
            {
               value += '/';
            }

            keyFilters.add(value);
         }
      }
   }
}
