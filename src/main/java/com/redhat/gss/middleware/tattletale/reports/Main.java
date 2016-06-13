/**
 * 
 */
package com.redhat.gss.middleware.tattletale.reports;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.InflaterInputStream;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.reporting.Dump;
import org.jboss.tattletale.reporting.Report;

/**
 * @author bmaxwell
 *
 */
public class Main
{
   private String serailizedFilename;
   private String outputDir;
   
   /** A List of the Constructors used to create custom reports */
   private final List<Class> customReports = new ArrayList<Class>();
   
   private boolean failOnInfo = false;
   private boolean failOnWarn = false;
   private boolean failOnError = false;
   
   /**
    * @param args
    */
   public static void main(String[] args)
   {
   
      if(args.length != 2)
      {
         System.out.println("Usage: java -jar tattletale-reports.jar archives.ser.deflate output-dir");
         return;
      }
      
      String serailizedFilename = args[0];
      String outputDir = args[1];
      
      new Main(serailizedFilename, outputDir).generate();
      
   }
   
   public Main(String serailizedFilename, String outputDir)
   {
      this.serailizedFilename = serailizedFilename;
      this.outputDir = outputDir;
   }
   
   public void generate()
   {
      SortedSet<Archive> archives = (SortedSet<Archive>) unmarshal(serailizedFilename);
      
      System.out.println("Unmarshaled " + serailizedFilename + " archives size: " + archives.size());
      
      Properties config = loadDefaultConfiguration();
      loadCustomReports(config);
      
      for(Class clz : customReports)
      {
         System.out.println("CustomReport: " + clz.getName());
      }
      
      Properties filters = new Properties();
      boolean allReports = true;
      Set reportSet = new HashSet();
      String destination = outputDir;
      
//      BufferedWriter bw = null;
      try
      {         
         ReportSetBuilder reportSetBuilder = new ReportSetBuilder(destination, allReports, reportSet, filters);
         reportSetBuilder.addReportParameter("setArchives", archives);
         
         outputReport(reportSetBuilder, archives);
         
//         PackagedJBossClasses report = new PackagedJBossClasses();
//         report.setArchives(archives);         
//         bw = getBufferedWriter("post-report.html");         
//         report.writeHtmlBodyHeader(bw);
//         report.writeHtmlBodyContent(bw);                           
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
      finally
      {
//         try
//         {
//            if(bw != null)
//               bw.close();
//         }
//         catch(Exception e)
//         {
//            // eat it
//         }
      }

   }
   
   /**
    * Add a report to the list of those to be generated
    *
    * @param clazz The class definition of the custom report
    */
   public final void addCustomReport(Class clazz)
   {
      customReports.add(clazz);
   }

   
   private void loadCustomReports(Properties config)
   {
      FileInputStream inputStream = null;
      try
      {
         int index = 1;
         String keyString = "customreport." + index;

         while (config.getProperty(keyString) != null)
         {
            ClassLoader cl = Main.class.getClassLoader();
            String reportName = config.getProperty(keyString);
            Class customReportClass = Class.forName(reportName, true, cl);
            addCustomReport(customReportClass);
            ++index;
            keyString = "customreport." + index;
         }
      }
      catch (Exception e)
      {
         System.err.println("Exception of type: " + e.getClass().toString()
               + " thrown in loadCustomReports() in org.jboss.tattletale.Main");
      }
      finally
      {
         if (inputStream != null)
         {
            try
            {
               inputStream.close();
            }
            catch (IOException e)
            {
            }
         }
      }
   }
   
   private Properties loadDefaultConfiguration()
   {
      Properties properties = new Properties();
      String propertiesFile = System.getProperty("jboss-tattletale.properties");
      boolean loaded = false;

      if (propertiesFile != null)
      {
         FileInputStream fis = null;
         try
         {
            fis = new FileInputStream(propertiesFile);
            properties.load(fis);
            loaded = true;
         }
         catch (IOException ioe)
         {
            System.err.println("Unable to open " + propertiesFile);
         }
         finally
         {
            if (fis != null)
            {
               try
               {
                  fis.close();
               }
               catch (IOException ioe)
               {
               }
            }
         }
      }

      if (!(loaded))
      {
         FileInputStream fis = null;
         try
         {
            fis = new FileInputStream("jboss-tattletale.properties");
            properties.load(fis);
            loaded = true;
         }
         catch (IOException ioe)
         {
         }
         finally
         {
            if (fis != null)
            {
               try
               {
                  fis.close();
               }
               catch (IOException ioe)
               {
               }
            }
         }
      }

      if (!(loaded))
      {
         InputStream is = null;
         try
         {
            ClassLoader cl = Main.class.getClassLoader();
            is = cl.getResourceAsStream("jboss-tattletale.properties");
            properties.load(is);
            loaded = true;
         }
         catch (Exception ioe)
         {
         }
         finally
         {
            if (is != null)
            {
               try
               {
                  is.close();
               }
               catch (IOException ioe)
               {
               }
            }
         }

      }

      return properties;
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
                  ? outputDir + File.separator : outputDir;
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

   private void outputReport(ReportSetBuilder reportSetBuilder, SortedSet<Archive> archives) throws Exception
   {      
      reportSetBuilder.clear();
      for (Class reportDef : this.customReports)
      {
         reportSetBuilder.addReport(reportDef);
      }
      SortedSet customReportSet = reportSetBuilder.getReportSet();
      reportSetBuilder.clear();
     
      String outputDir = reportSetBuilder.getOutputDir();
      Dump.generateIndex(new TreeSet(), new TreeSet(), new TreeSet(), customReportSet, outputDir);
      Dump.generateCSS(outputDir);

//      if ((!(this.failOnInfo)) && (!(this.failOnWarn)) && (!(this.failOnError)))
//         return;
      FailureCheck failureCheck = new FailureCheck();
      failureCheck.processReports(customReportSet);

      if (failureCheck.errorReport() == null)
         return;
      throw new Exception(failureCheck.errorReport());
   }
   
   private class FailureCheck
   {
      private boolean foundError;

      private boolean first;

      private StringBuilder stringbuffer;

      private FailureCheck()
      {
         this.foundError = false;
         this.first = true;
         this.stringbuffer = new StringBuilder();
      }

      String errorReport()
      {
         if (this.foundError)
         {
            return this.stringbuffer.toString();
         }
         return null;
      }

      void processReports(Set<Report> reports)
      {
         for (Report report : reports)
         {
            processReport(report);
         }
      }

      void processReport(Report report)
      {
         if (((1 != report.getStatus()) && (2 != report.getStatus()))
               || ((((0 != report.getSeverity()) || (!(Main.this.failOnInfo))))
                     && (((1 != report.getSeverity()) || (!(Main.this.failOnWarn)))) && (((2 != report.getSeverity()) || (!(Main.this.failOnError))))))
         {
            return;
         }

         appendReportInfo(report);
      }

      void appendReportInfo(Report report)
      {
         if (!(this.first))
         {
            this.stringbuffer = this.stringbuffer.append(System.getProperty("line.separator"));
         }

         this.stringbuffer = this.stringbuffer.append(report.getId());
         this.stringbuffer = this.stringbuffer.append("=");

         if (1 == report.getStatus())
         {
            this.stringbuffer = this.stringbuffer.append("YELLOW");
         }
         else if (2 == report.getStatus())
         {
            this.stringbuffer = this.stringbuffer.append("RED");
         }

         this.foundError = true;
         this.first = false;
      }
   }
   
   private class ReportSetBuilder
   {
      private final boolean allReports;

      private final String outputDir;

      private final Properties filters;

      private Set<String> reportSet;

      private SortedSet<Report> returnReportSet = new TreeSet();

      private final Map<String, Object> reportParameters = new HashMap();

      ReportSetBuilder(String destination, boolean allReports, Set<String> reportSet, Properties filters) throws Exception
      {
         this.outputDir = setupOutputDir(destination);
         this.allReports = allReports;
         this.reportSet = reportSet;
         this.filters = filters;
      }

      public void addReportParameter(String setMethodName, Object parameter)
      {
         this.reportParameters.put(setMethodName, parameter);
      }

      void clear()
      {
         this.returnReportSet = new TreeSet();
      }

      void addReport(Report report)
      {
         if ((!(this.allReports)) && (!(this.reportSet.contains(report.getId()))))
            return;
         if ((this.filters != null) && (this.filters.getProperty(report.getId()) != null))
         {
            report.setFilter(this.filters.getProperty(report.getId()));
         }
         report.generate(this.outputDir);
         this.returnReportSet.add(report);
      }

      void addReport(Class reportDef) throws Exception
      {
         Report report = (Report) reportDef.getConstructor(new Class[0]).newInstance(new Object[0]);

         Method[] allMethods = reportDef.getMethods();
         for (Method m : allMethods)
         {
            if (!(this.reportParameters.containsKey(m.getName())))
               continue;
            m.invoke(report, new Object[]
            {this.reportParameters.get(m.getName())});
         }

         addReport(report);
      }

      SortedSet<Report> getReportSet()
      {
         return this.returnReportSet;
      }

      String getOutputDir()
      {
         return this.outputDir;
      }

      private String setupOutputDir(String outputDir) throws IOException
      {
         outputDir = (!(outputDir.substring(outputDir.length() - 1).equals(File.separator))) ? outputDir
               + File.separator : outputDir;

         File outputDirFile = new File(outputDir);

         if ((outputDirFile.exists()) && (!(outputDirFile.equals(new File(".")))))
         {
            recursiveDelete(outputDirFile);
         }

         if ((!(outputDirFile.equals(new File(".")))) && (!(outputDirFile.mkdirs())))
         {
            throw new IOException("Cannot create directory: " + outputDir);
         }

         return outputDir;
      }

      private void recursiveDelete(File f) throws IOException
      {
         if ((f == null) || (!(f.exists())))
            return;
         File[] files = f.listFiles();
         if (files != null)
         {
            for (int i = 0; i < files.length; ++i)
            {
               if (files[i].isDirectory())
               {
                  recursiveDelete(files[i]);
               }
               else
               {
                  if (files[i].delete())
                     continue;
                  throw new IOException("Could not delete " + files[i]);
               }
            }
         }

         if (f.delete())
            return;
         throw new IOException("Could not delete " + f);
      }
   } 
   
   private File getOutputDirectory()
   {
      return new File("post-analysis");
   }
   
   public BufferedWriter getBufferedWriter(String filename) throws IOException
   {
      getOutputDirectory().mkdirs();
      System.out.println("Writing to " + getOutputDirectory().getAbsolutePath() + File.separator + filename);
      FileWriter fw = new FileWriter(getOutputDirectory().getAbsolutePath() + File.separator + filename);
      return new BufferedWriter(fw, 8192);
   } 
   
   private static Object unmarshal(String filename)
   {
      Object object = null;
      FileInputStream fis = null;
      InflaterInputStream iis = null;
      ObjectInputStream in = null;
      try
      {
         fis = new FileInputStream(filename);
         iis = new InflaterInputStream(fis);
         in = new ObjectInputStream(iis);
         object = (Object)in.readObject();         
      }
      catch(IOException ex)
      {
         ex.printStackTrace();
      }
      catch(ClassNotFoundException ex)
      {
         ex.printStackTrace();
      }
      finally
      {
         closeAndEat(in, iis, fis);
      }
      return object;
   }
   
   private static void closeAndEat(Closeable... closeables)
   {
      for(Closeable closeable : closeables)
      {
         if(closeable != null)
         {
            try
            {
               closeable.close();
            }
            catch(Exception e)
            {
               // eat it
            }
         }
      }
   }
}
