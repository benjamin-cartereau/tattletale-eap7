/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.ArchiveType;
import org.jboss.tattletale.core.NestableArchive;
import org.jboss.tattletale.utils.StringUtils;

/**
 * Transitive Depends On report
 *
 * @author Jesper Pedersen <jesper.pedersen@jboss.org>
 * @author <a href="mailto:torben.jaeger@jit-consulting.de">Torben Jaeger</a>
 */
public class TransitiveDependsOnReport extends CLSReport
{
   /** NAME */
   private static final String NAME = "Transitive Depends On";

   /** DIRECTORY */
   private static final String DIRECTORY = "transitivedependson";

   /** Constructor */
   public TransitiveDependsOnReport()
   {
      super(DIRECTORY, ReportSeverity.INFO, NAME, DIRECTORY);
   }

   /**
    * write out the report's content
    *
    * @param bw the writer to use
    * @throws IOException if an error occurs
    */
   @Override
   public void writeHtmlBodyContent(BufferedWriter bw) throws IOException
   {
      bw.write("<table>" + Dump.newLine());

      bw.write("  <tr>" + Dump.newLine());
      bw.write("     <th>Archive</th>" + Dump.newLine());
      bw.write("     <th>Depends On</th>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      SortedMap<String, SortedSet<String>> dependsOnMap = new TreeMap<>();

      for (Archive archive : archives)
      {
         SortedSet<String> result = new TreeSet<>();

         for (Archive a : archives)
         {

            if (a.getType() == ArchiveType.JAR)
            {
               for (String require : getRequires(a))
               {

                  if (archive.doesProvide(require) && (getCLS() == null || getCLS().isVisible(a, archive)))
                  {
                     result.add(a.getName());
                  }
               }
            }
         }

         dependsOnMap.put(archive.getName(), result);
      }

      SortedMap<String, SortedSet<String>> transitiveDependsOnMap = new TreeMap<>();

      for (Map.Entry<String, SortedSet<String>> entry : dependsOnMap.entrySet())
      {

         String archive = entry.getKey();
         SortedSet<String> value = entry.getValue();

         SortedSet<String> result = new TreeSet<>();

         if (value != null && value.size() > 0)
         {
            for (String aValue : value)
            {
               resolveDependsOn(aValue, archive, dependsOnMap, result);
            }
         }

         transitiveDependsOnMap.put(archive, result);
      }

      boolean odd = true;

      for (Map.Entry<String, SortedSet<String>> entry : transitiveDependsOnMap.entrySet())
      {

         String archive = entry.getKey();
         SortedSet<String> value = entry.getValue();

         if (odd)
         {
            bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
         }
         else
         {
            bw.write("  <tr class=\"roweven\">" + Dump.newLine());
         }
         bw.write("    <td>" + hrefToReport(archive) + "</td>" + Dump.newLine());
         bw.write("     <td>");

         if (value.size() == 0)
         {
            bw.write("&nbsp;");
         }
         else
         {
            List<String> hrefs = new ArrayList<String>();
            for (String r : value)
            {
               if (r.endsWith(".jar"))
               {
                  hrefs.add(hrefToReport(r));
               }
               else
               {
                  if (!isFiltered(archive, r))
                  {
                     hrefs.add("<i>" + r + "</i>");
                     status = ReportStatus.YELLOW;
                  }
                  else
                  {
                     hrefs.add("<i style=\"text-decoration: line-through;\">" + r + "</i>");
                  }
               }
            }
            bw.write(StringUtils.join(hrefs, ", "));
         }

         bw.write("</td>" + Dump.newLine());
         bw.write("  </tr>" + Dump.newLine());

         odd = !odd;
      }

      bw.write("</table>" + Dump.newLine());
   }

   private Set<String> getRequires(Archive a)
   {
      Set<String> requires = new HashSet<>();
      if (a instanceof NestableArchive)
      {
         NestableArchive na = (NestableArchive) a;
         List<Archive> subArchives = na.getSubArchives();
         requires.addAll(na.getRequires());

         for (Archive sa : subArchives)
         {
            requires.addAll(getRequires(sa));
         }
      }
      else
      {
         requires.addAll(a.getRequires());
      }
      return requires;
   }

   /**
    * Get depends on
    *
    * @param scanArchive The scan archive
    * @param archive     The archive
    * @param map         The depends on map
    * @param result      The result
    */
   private void resolveDependsOn(String scanArchive, String archive,
                                 SortedMap<String, SortedSet<String>> map, SortedSet<String> result)
   {
      if (!archive.equals(scanArchive) && !result.contains(scanArchive))
      {
         result.add(scanArchive);

         SortedSet<String> value = map.get(scanArchive);
         if (value != null)
         {
            for (String aValue : value)
            {
               resolveDependsOn(aValue, archive, map, result);
            }
         }
      }
   }
}
