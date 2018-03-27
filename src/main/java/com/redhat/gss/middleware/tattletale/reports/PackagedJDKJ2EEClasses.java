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
package com.redhat.gss.middleware.tattletale.reports;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.ArchiveType;
import org.jboss.tattletale.core.Location;
import org.jboss.tattletale.core.NestableArchive;
import org.jboss.tattletale.profiles.AbstractProfile;
import org.jboss.tattletale.profiles.Java7;
import org.jboss.tattletale.profiles.Java8;
import org.jboss.tattletale.profiles.JavaEE5;
import org.jboss.tattletale.profiles.JavaEE6;
import org.jboss.tattletale.profiles.JavaEE7;
import org.jboss.tattletale.profiles.SunJava5;
import org.jboss.tattletale.profiles.SunJava6;
import org.jboss.tattletale.reporting.Dump;
import org.jboss.tattletale.reporting.Filter;
import org.jboss.tattletale.reporting.KeyFilter;
import org.jboss.tattletale.reporting.ReportSeverity;
import org.jboss.tattletale.reporting.ReportStatus;
import org.jboss.tattletale.reporting.SummaryDetailReport;

/**
 * @author bmaxwell
 */
public class PackagedJDKJ2EEClasses extends SummaryDetailReport
{
   /** NAME */
   private static final String NAME = "Packaged JDK / J2EE Classes";

   /** DIRECTORY */
   private static final String DIRECTORY = "packaged-jdk-j2ee-classes";      

   // TODO removing since it is duplicated by problemSet
   /* This is a set which will create a summary of the problematic archives as the report analyzes the archives */
//   private Set<Archive> summarySet = new TreeSet<Archive>();

   private final Set<ProblematicArchive> problemSet = new TreeSet<>();
   
   private Properties config = null;
   
   /**
    * Default Constructor
    */
   public PackagedJDKJ2EEClasses()
   {
      super(DIRECTORY, ReportSeverity.WARNING, NAME, DIRECTORY);      
   }         
   
   /**
    * This writes out the locations that the archive containing jdk/j2ee classes is located at
    * @param bw - BufferedWriter
    * @param locations - a set of the locations to write to the report
    * @throws IOException
    */
   private void writeLocations(BufferedWriter bw, SortedSet<Location> locations) throws IOException
   {
      bw.write("<h5>Location: ");
      for (Location l : locations)
      {
         bw.write(l.getFilename() + " ");
      }
      bw.write("</h5>" + Dump.newLine());
   }

   /**
    * This writes out the archive name in the report
    * @param bw - BufferedWriter
    * @param archive - archive
    * @throws IOException
    */
   private void writeArchiveName(BufferedWriter bw, Archive archive) throws IOException
   {
      bw.write("<h2>Archive: " + archive.getName() + " contains these classes which should not be packaged:</h2>"
            + Dump.newLine());
      writeLocations(bw, archive.getLocations());
   }

   private void writeDetailed(BufferedWriter bw) throws IOException
   {
      bw.write("<h1>Detailed analysis of problematic archives:</h1>");
      
      String[] profileProblemLevel = new String[]
      {"PROBLEM", "PROBLEM"};
      
      AbstractProfile jdkProfile = getProfile(getConfigProperty("JDK"));
      AbstractProfile eeProfile = getProfile(getConfigProperty("EE"));      
      AbstractProfile[] profiles = new AbstractProfile[] {jdkProfile, eeProfile};
      
      boolean archiveNameWritten;

      for (ProblematicArchive problemmaticArchive : problemSet)
      {
         archiveNameWritten = false;
         Archive archive = problemmaticArchive.archive;
         Set<String> classes = archive.getProvides().keySet();
         int i = -1;
         for (AbstractProfile profile : profiles)
         {
            i++;
            boolean profileNameWritten = false;

            for (String clz : classes)
            {
               if (profile.doesProvide(clz))
               {
                  // log the archive name once
                  if (!archiveNameWritten)
                  {
                     writeArchiveName(bw, archive);
                     archiveNameWritten = true;
                  }
                  if (!profileNameWritten)
                  {
                     bw.write("<h3>" + profileProblemLevel[i] + " - '" + profile.getName()
                           + "' already contains these classes:</h3>" + Dump.newLine());
                     bw.write("<ul>" + Dump.newLine());
                     profileNameWritten = true;
                  }

                  // log the class that is included by the jdk or
                  // container
                  bw.write("<li>" + clz + "</li>" + Dump.newLine());
               }
            }
            // close the profile block
            if (profileNameWritten)
               bw.write("</ul>" + Dump.newLine());
         }
      }
   }

   /**
    * analyze the archives and create a summarySet and problemsSet so we can print out the report
    */
   @Override
   protected void analyze()
   {
/*
      SortedSet<String> envProvidedClassSet = new TreeSet<String>();
      envProvidedClassSet.addAll(new SunJava6().getClassSet());
      envProvidedClassSet.addAll(new JavaEE5().getClassSet());
*/      
      AbstractProfile jdkProfile = getProfile(getConfigProperty("JDK"));
      AbstractProfile eeProfile = getProfile(getConfigProperty("EE"));	   
      AbstractProfile[] profiles = new AbstractProfile[] {jdkProfile, eeProfile};
	
      int nonFilteredProblems = 0;
      
      // archives comes from AbstractReport, if given an ear, it only contains an EarArchive, we now have to call to get all subArchives
      for (Archive archive : archives)
      {
         //System.out.println("Checking : " + archive);

         List<Archive> archiveQueue = new ArrayList<>();
         List<Archive> newItems  = new ArrayList<>();

         // if archive is a jar, process it, if nestable, add its subarchives

         archiveQueue.add(archive);
         ListIterator it = archiveQueue.listIterator();

         while(it.hasNext())
         {
            Archive a = (Archive) it.next();
            it.remove();
            //System.out.println("Checking sub archive: " + a);

            if(a instanceof NestableArchive)
            {
               newItems.addAll( ((NestableArchive)a).getSubArchives() );

/*
               for(Archive b : ((NestableArchive)a).getSubArchives() )
               {
                  //System.out.println("Sub Archives: " + b);
                  it.add(b); 
               }
*/
            }

            if (a.getType() == ArchiveType.JAR)
            {
               Set<String> classes = a.getProvides().keySet();
               // loop through profiles, create a section for each profile
            
               List<AbstractProfile> profilesMatched = new ArrayList<>(); 
               for (AbstractProfile profile : profiles)
               { 
//                  System.out.println("Checking profile: " + profile);
                  for (String clz : classes)
                  {
                     if (profile.doesProvide(clz))
                     {
                        //System.out.println("packaging class: " + clz);
                        profilesMatched.add(profile);
//                        problemSet.add(new ProblematicArchive(a, profile));

                        // track archives that contain classes they shouldn't in summary
                        //TODO summarySet.add(a); // this was duplicated, changing to use problemSet

                        // break out and check the next profile, we will get the classes in the writing
                        break;
                     }
                  }
               }
               if(profilesMatched.size() > 0) {
                  problemSet.add(new ProblematicArchive(a, profilesMatched));
                  if (!isFiltered(a.getName())) {
                      nonFilteredProblems++;
                  }
               }
            }
            if(! it.hasNext() && newItems.size() > 0)
            {
               archiveQueue.addAll(newItems);
               newItems.clear();
               it = archiveQueue.listIterator();
            }
         }
      }
      if (nonFilteredProblems > 5) {
          this.status = ReportStatus.RED;
      }
      else if (nonFilteredProblems > 1) {
          this.status = ReportStatus.YELLOW;
      }
   }

   /**
    * This writes out a summary of the packaged archives which could cause problems
    * @param bw - BufferedWriter
    * @throws IOException
    */
   private void writeSummary(BufferedWriter bw) throws IOException
   {
      bw.write("<h1>Summary of problematic archives:</h1>");
      bw.write("<ul>");
//      for (Archive a : summarySet)      
        for (ProblematicArchive a : problemSet)
        {
         for (Location l : a.archive.getLocations())
         {
            bw.write("<li>" + l.getFilename() + " [" + profilesListToString(a.profiles) + "]</li>");
         }
      }
      bw.write("</ul>");
   }

   private String profilesListToString(List<AbstractProfile> list)
   {
      StringBuilder sb = new StringBuilder();
      for(AbstractProfile profile : list)
      {
         sb.append(profile.getName()).append(", ");
      }
      if(sb.length() > 1)
         sb.delete(sb.length()-2, sb.length());
      return sb.toString();      
   }
   
   @Override
   public void writeHtmlBodyHeader(BufferedWriter bw) throws IOException
   {
      bw.write("<body>" + Dump.newLine());
      bw.write(Dump.newLine());
      bw.write("<h1>" + NAME + "</h1>" + Dump.newLine());
      bw.write("<h3>PROBLEM - indicates these classes will most likely cause ClassCastExceptions and should be removed</h3>"
            + Dump.newLine());           
      bw.write("<a href=\"../index.html\">Main</a>" + Dump.newLine() + "<br/>");      
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

   @Override
   public void writeHtmlSummary(BufferedWriter bw) throws IOException
   {
      writeSummary(bw);       
   }

   @Override
   public void writeHtmlDetailed(BufferedWriter bw) throws IOException
   {
      writeDetailed(bw);      
   }
   
   public void setConfig(Properties config)
   {
      this.config = config;
   }
   
   private Properties loadDefaultConfiguration()
   {
      Properties properties = new Properties();
      String propertiesFile = System.getProperty("jboss-tattletale.properties");
      boolean loaded = false;

      if (propertiesFile != null)
      {
         try (FileInputStream fis = new FileInputStream(propertiesFile)) {
            properties.load(fis);
            loaded = true;
         }
         catch (IOException ioe)
         {
            System.err.println("Unable to open " + propertiesFile);
         }
      }

      if (!(loaded))
      {
         try (FileInputStream fis = new FileInputStream("jboss-tattletale.properties")) {
            properties.load(fis);
            loaded = true;
         }
         catch (IOException ioe)
         {
         }
      }

      if (!(loaded))
      {
          ClassLoader cl = Main.class.getClassLoader();
         
         try (InputStream is = cl.getResourceAsStream("jboss-tattletale.properties")) {
            properties.load(is);
            //loaded = true;
         }
         catch (Exception ioe)
         {
         }
      }

      return properties;
   }
   
   private String getConfigProperty(String configPropertyName)
   {
      if(config == null)
         config = loadDefaultConfiguration();
      
      // try system property
      String value = System.getProperty(configPropertyName);
      
      // try jboss-tattletale.properties
      if(value == null)
      {
    	  value = (String) config.get(configPropertyName);
      }
      
      return value.trim();
   }
   
   private AbstractProfile getProfile(String profileCode)
   {
      if(profileCode != null)
      {
         // TODO change this to look for class with the version, else pick one based on the major version number
    	  //AbstractProfile profile = null;
    	  AbstractProfile[] profiles = new AbstractProfile[] { new EAP429(), new EAP512(), new EAP600(), new EAP700(), new SunJava5(), new SunJava6(), new Java7(), new Java8(), new JavaEE5(), new JavaEE6(), new JavaEE7() };
    	  for(AbstractProfile p : profiles) {
    		  if(profileCode.equalsIgnoreCase(p.getProfileCode())) {
    			  return p;    			  
    		  }
    	  }    	  
    	  throw new RuntimeException("Unable to find profile for: " + profileCode);
      }
      return null;      
   }
}
