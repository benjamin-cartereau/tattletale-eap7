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

import java.io.File;

/**
 * Top-level interface that would define what all report types would have to satisfy.
 *
 * @author Navin Surtani
 */
public interface Report extends Comparable<Report>
{
   /**
    * Represents a report severity
    */
   enum ReportSeverity
   {
      /**
       * INFO
       */
      INFO,
      /**
       * WARN
       */
      WARNING,
      /**
       * FAIL
       */
      ERROR
   }

   /**
    * Represents a report status
    */
   enum ReportStatus
   {
       /**
        * GREEN
        */
      GREEN,
      /**
       * YELLOW
       */
      YELLOW,
      /**
       * READ
       */
      RED;
      
      public String getHtmlColor() {
          if (this == YELLOW) {
              return "#9b9b12";
          }
          return toString().toLowerCase();
      }
   }
   
   /**
    * Method that any implementation must provide in order to generate any type of report.
    * @param outputDirectory - the top-level output directory that the generated report would end up.
    */
   void generate(String outputDirectory);

   /**
    * Method to obtain the id of each Report.
    * @return - the report id.
    */
   String getId();

   /**
    * Method to obtain the severity of each Report.
    * @return - the severity of the Report.
    */
   ReportSeverity getSeverity();

   /**
    * Method to obtain the status of each Report.
    * @return - the status of the Report.
    */
   ReportStatus getStatus();

   /**
    * Method to obtain the directory within the filesystem that the Report is intended to go into.
    * @return - the directory on the filesystem where the Report would be.
    */
   String getDirectory();

   /**
    * Returns the name of the Report.
    * @return - the name of the report.
    */
   String getName();

   /**
    * Method to get hold of the filter that are being applied.
    * @return - the filter that is being used.
    */
   String getFilter();

   /**
    * Method that is required in order to generate the Report in {@link org.jboss.tattletale.reporting.Dump}
    * @return - the output directory as a File.
    */
   File getOutputDirectory();

   /**
    * Method that would be used to return the name of the index file where the report would sit. For example,
    * index.html, report.html, mycustomreport.html etc etc.
    *
    * @return String (including file suffix) of what the index file would be which would contain the Report and
    * its data.
    */
   String getIndexName();
   
   /**
    * Assigns the filter to be used by a String.
    * @param filter - the String to be used to set the filter.
    */
   void setFilter(String filter);

}
