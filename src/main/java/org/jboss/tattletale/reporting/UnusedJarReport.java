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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.NestableArchive;

/**
 * A report that shows unused JAR archives
 *
 * @author Jesper Pedersen <jesper.pedersen@jboss.org>
 */
public class UnusedJarReport extends AbstractReport
{
   /** NAME */
   private static final String NAME = "Unused Jar";

   /** DIRECTORY */
   private static final String DIRECTORY = "unusedjar";

   /** Constructor */
   public UnusedJarReport()
   {
      super(DIRECTORY, ReportSeverity.WARNING, NAME, DIRECTORY);
   }

   /**
    * Write out the report's content
    *
    * @param bw the writer to use
    * @throws IOException if an error occurs
    */
   public void writeHtmlBodyContent(BufferedWriter bw) throws IOException
   {
      bw.write("<table>" + Dump.newLine());

      bw.write("  <tr>" + Dump.newLine());
      bw.write("     <th>Archive</th>" + Dump.newLine());
      bw.write("     <th>Used</th>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      boolean odd = true;
      int used = 0;
      int unused = 0;
      
      Collection<Archive> arch = recursivelyListAllArchives(archives);
      arch = arch.stream()
      	.sorted((a1, a2) -> a1.getName().compareTo(a2.getName())).collect(Collectors.toList());
      	
		for (Archive archive : arch) {
			boolean archiveStatus = false;
			String archiveName = archive.getName();
			Iterator<Archive> it = arch.iterator();
			while (!archiveStatus && it.hasNext()) {
				Archive a = it.next();
				if (!archive.getName().equals(a.getName())) {
					Iterator<String> sit = a.getRequires().iterator();
					while (!archiveStatus && sit.hasNext()) {
						String require = sit.next();
						if (archive.getProvides().keySet().contains(require)) {
							archiveStatus = true;
						}
					}
				}
			}
			if (odd) {
				bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
			} else {
				bw.write("  <tr class=\"roweven\">" + Dump.newLine());
			}
			
			bw.write("     <td><a href=\"../"+buildExtensions(archive,"jar/") + archiveName + ".html\">" + archiveName + "</a></td>" + Dump.newLine());
			if (archiveStatus) {
				bw.write("     <td style=\"color: green;\">Yes</td>" + Dump.newLine());
				used++;
			} else {
				unused++;
				if (!isFiltered(archive.getName())) {
					status = ReportStatus.YELLOW;
					bw.write("     <td style=\"color: red;\">No</td>" + Dump.newLine());
				} else {
					bw.write("     <td style=\"color: red; text-decoration: line-through;\">No</td>" + Dump.newLine());
				}
			}	
			bw.write("  </tr>" + Dump.newLine());
			odd = !odd;
		}

      bw.write("</table>" + Dump.newLine());

      bw.write(Dump.newLine());
      bw.write("<p>" + Dump.newLine());

      bw.write("<table>" + Dump.newLine());

      bw.write("  <tr>" + Dump.newLine());
      bw.write("     <th>Status</th>" + Dump.newLine());
      bw.write("     <th>Archives</th>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
      bw.write("     <td>Used</td>" + Dump.newLine());
      bw.write("     <td style=\"color: green;\">" + used + "</td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("  <tr class=\"roweven\">" + Dump.newLine());
      bw.write("     <td>Unused</td>" + Dump.newLine());
      bw.write("     <td style=\"color: red;\">" + unused + "</td>" + Dump.newLine());
      bw.write("  </tr>" + Dump.newLine());

      bw.write("</table>" + Dump.newLine());
   }
   
   	/**
	 * Build the extension path for the jar files.
	 * 
	 * @param archive
	 * @param extensions
	 * @return
	 */
	private String buildExtensions(Archive archive, String extensions) {
		if (archive.getParentArchive() == null) {
			return extensions;
		} else {
			Archive parent = archive.getParentArchive();
			String archiveName = parent.getName();
			int finalDot = archiveName.lastIndexOf(".");
			String extension = archiveName.substring(finalDot + 1);
			return buildExtensions(parent, extension + "/" + extensions);
		}
	}
   

   /**
    * write out the header of the report's content
    *
    * @param bw the writer to use
    * @throws IOException if an errror occurs
    */
   public void writeHtmlBodyHeader(BufferedWriter bw) throws IOException
   {
      bw.write("<body>" + Dump.newLine());
      bw.write(Dump.newLine());

      bw.write("<h1>" + NAME + "</h1>" + Dump.newLine());

      bw.write("<a href=\"../index.html\">Main</a>" + Dump.newLine());
      bw.write("<p>" + Dump.newLine());
   }

   /**
    * Create filter
    *
    * @return The filter
    */
   @Override
   protected Filter createFilter()
   {
      return new KeyFilter();
   }
   
   private Collection<Archive> recursivelyListAllArchives(Collection<Archive> archives)
   {
	  Collection<Archive> allArchives = new HashSet<Archive>();
      for (Archive archive : archives)
      {    	  
    	 if (archive instanceof NestableArchive)
         {
            allArchives.addAll(recursivelyListAllArchives(((NestableArchive) archive).getSubArchives()));
         }
         else
         {
        	allArchives.add(archive);
         }
      }

      return allArchives;
   }



}
