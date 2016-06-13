package com.redhat.gss.middleware.tattletale.reports;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.tattletale.reporting.Report;

/**
 * This helper class generates reports from report definitions and gathers
 * report definitions into a SortedSet which can be used to build the index.
 *
 * @author Mike Moore
 */
public class ReportSetBuilder
{

   private final boolean allReports;

   private final String outputDir;

   private final Properties filters;

   private Set<String> reportSet;

   private SortedSet<Report> returnReportSet = new TreeSet<Report>();

   private final Map<String, Object> reportParameters = new HashMap<String, Object>();

   /**
    * @param destination Where the reports go
    * @param allReports  Should all reports be generated ?
    * @param reportSet   The set of reports that should be generated
    * @param filters     The filters
    *
    * @throws Exception
    */
   ReportSetBuilder(String destination, boolean allReports, Set<String> reportSet, Properties filters) throws Exception
   {
      this.outputDir = setupOutputDir(destination);
      this.allReports = allReports;
      this.reportSet = reportSet;
      this.filters = filters;
   }

   /**
    * Add a parameter which will be used to initialize the reports built
    *
    * @param setMethodName The name of the method that will set the parameter on the
    *                      report
    * @param parameter     The parameter to set
    */
   public void addReportParameter(String setMethodName, Object parameter)
   {
      reportParameters.put(setMethodName, parameter);
   }

   /**
    * Starts a new report set. This allows a single ReportSetBuilder to be
    * used to generate multiple report sets
    */
   void clear()
   {
      // start a new set, the old sets are still in use for indexing
      returnReportSet = new TreeSet<Report>();
   }

   /**
    * Generates the report from the definition, output goes to the output
    * directory.
    *
    * @param report the definition of the report to generate
    */
   void addReport(Report report)
   {
      if (allReports || reportSet.contains(report.getId()))
      {
         if (filters != null && filters.getProperty(report.getId()) != null)
         {
            report.setFilter(filters.getProperty(report.getId()));
         }
         report.generate(outputDir);
         returnReportSet.add(report);
      }
   }

   /**
    * Generates the report from the definition, output goes to the output
    * directory.
    *
    * @param reportDef the class definition of the report to generate
    *
    * @throws Exception
    */
   void addReport(Class reportDef) throws Exception
   {
      // build report from empty constructor
      Report report = (Report) reportDef.getConstructor(new Class[0]).newInstance(new Object[0]);

      // populate required report parameters
      Method[] allMethods = reportDef.getMethods();
      for (Method m : allMethods)
      {
         if (reportParameters.containsKey(m.getName()))
         {
            m.invoke(report, reportParameters.get(m.getName()));
         }
      }
      addReport(report);
   }

   /** @return A Set of reports generated, useful for building an index */
   SortedSet<Report> getReportSet()
   {
      return returnReportSet;
   }

   /** @return the String representation of the output directory */
   String getOutputDir()
   {
      return outputDir;
   }

   /**
    * Validate and create the outputDir if needed.
    *
    * @param outputDir Where reports go
    *
    * @return The verified output path for the reports
    *
    * @throws IOException If the output directory cant be created
    */
   private String setupOutputDir(String outputDir) throws IOException
   {
      // Verify ending slash
      outputDir = !outputDir.substring(outputDir.length() - 1).equals(File.separator)
            ? outputDir + File.separator
            : outputDir;
      // Verify output directory exists & create if it does not
      File outputDirFile = new File(outputDir);

      if (outputDirFile.exists() && !outputDirFile.equals(new File(".")))
      {
         recursiveDelete(outputDirFile);
      }

      if (!outputDirFile.equals(new File(".")) && !outputDirFile.mkdirs())
      {
         throw new IOException("Cannot create directory: " + outputDir);
      }

      return outputDir;
   }

   /**
    * Recursive delete
    *
    * @param f The file handler
    *
    * @throws IOException Thrown if a file could not be deleted
    */
   private void recursiveDelete(File f) throws IOException
   {
      if (f != null && f.exists())
      {
         File[] files = f.listFiles();
         if (files != null)
         {
            for (int i = 0; i < files.length; i++)
            {
               if (files[i].isDirectory())
               {
                  recursiveDelete(files[i]);
               }
               else
               {
                  if (!files[i].delete())
                  {
                     throw new IOException("Could not delete " + files[i]);
                  }
               }
            }
         }
         if (!f.delete())
         {
            throw new IOException("Could not delete " + f);
         }
      }
   }
}
