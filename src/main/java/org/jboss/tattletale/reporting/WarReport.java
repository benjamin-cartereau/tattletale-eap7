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
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.jboss.tattletale.core.NestableArchive;
import static org.jboss.tattletale.reporting.AbstractReport.INDEX_HTML;
import org.jboss.tattletale.utils.StringUtils;


/**
 * This type of report is to .war files as to {@link JarReport} is to .jar files.
 *
 * @author Navin Surtani
 */
public class WarReport extends NestableReport
{
   /** File name */
   private String fileName;

   /** The level of depth from the top output directory */
   private int depth;

   /**
    * Constructor
    *
    * @param nestableArchive - the war nestableArchive.
    */
   public WarReport(NestableArchive nestableArchive)
   {
      this(nestableArchive, 1);
   }

   /**
    * Constructor
    *
    * @param nestableArchive The nestableArchive
    * @param depth   The level of depth at which this report would lie
    */
   public WarReport(NestableArchive nestableArchive, int depth)
   {
      super (nestableArchive.getType().toString(), ReportSeverity.INFO, nestableArchive);
      StringBuilder sb = new StringBuilder(nestableArchive.getName());
      setFilename(sb.append(".html").toString());
      this.depth = depth;
   }

   @Override
   public String getIndexName()
   {
       String indexName = INDEX_HTML;
       // If nested (depth>1), add parent directory navigation
       if (depth>1) 
       {
          String[] subdirs = new String[depth-1];
          Arrays.fill(subdirs, "..");
          indexName = StringUtils.join(Arrays.asList(subdirs), File.separator) + File.separator + indexName;
       }
       
      return indexName;
   }
   
   /**
    * write the header of a html file.
    *
    * @param bw the buffered writer
    * @throws IOException if an error occurs
    */

   @Override
   public void writeHtmlHead(BufferedWriter bw) throws IOException
   {
      super.writeHtmlHead(bw, depth);
   }

   /**
    * returns a war report specific writer.
    * war reports do not use an index.html but create one html file per archive.
    *
    * @return the BufferedWriter
    * @throws IOException if an error occurs
    */
   @Override
   BufferedWriter getBufferedWriter() throws IOException
   {
      return getBufferedWriter(getFilename());
   }
   private String getFilename()
   {
      return fileName;
   }

   private void setFilename(String fileName)
   {
      this.fileName = fileName;
   }
}
