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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.ArchiveType;
import org.jboss.tattletale.core.Location;
import org.jboss.tattletale.core.NestableArchive;
import org.jboss.tattletale.profiles.AbstractProfile;
import org.jboss.tattletale.reporting.Dump;
import org.jboss.tattletale.reporting.Filter;
import org.jboss.tattletale.reporting.KeyFilter;
import org.jboss.tattletale.reporting.ReportSeverity;
import org.jboss.tattletale.reporting.ReportStatus;
import org.jboss.tattletale.reporting.SummaryDetailReport;

/**
 * @author bmaxwell
 */
public class PackagedJBossClasses extends SummaryDetailReport
{
   /** NAME */
   private static final String NAME = "Packaged JBoss Classes";

   /** DIRECTORY */
   private static final String DIRECTORY = "packaged-jboss-classes";

   /* This is a set which will create a summary of the problematic archives as the report analyzes the archives */
//   private Set<Archive> summarySet = new TreeSet<Archive>();

   private final Set<ProblematicArchive> problemSet = new TreeSet<>();

   private final AbstractExtendedProfile[] profiles; // = new AbstractExtendedProfile[] {new EAP512()};
   
   private Properties config = null;
   
   private int classListId = 1;
   
   /**
    * Default Constructor
    */
   public PackagedJBossClasses()
   {
      super(DIRECTORY, ReportSeverity.WARNING, NAME, DIRECTORY);
      profiles = new AbstractExtendedProfile[] { getEAPProfile() };    
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
   
   public void setConfig(Properties config)
   {
      this.config = config;
   }
   
   private AbstractExtendedProfile getEAPProfile()
   {
      if(config == null)
         config = loadDefaultConfiguration();
      
      // try system property
      String eapVersion = System.getProperty("EAP");
      
//      System.out.println("Read System Property: " + eapVersion);
      
      // try jboss-tattletale.properties
      if(eapVersion == null)
      {
         eapVersion = (String) config.get("EAP");
//         System.out.println("Read EAP from config: " + eapVersion);
      }
      
      return getEAPProfile(eapVersion);
   }
   
   private AbstractExtendedProfile getEAPProfile(String eapVersion)
   {      
      if(eapVersion != null)
      {
         // TODO change this to look for class with the version, else pick one based on the major version number
       
         if(eapVersion.contains("4.2"))
         {
            System.out.println("Using EAP 4.2 Profile");
            return new EAP429();
         }
         if(eapVersion.contains("4.3"))
         {
            System.out.println("Using EAP 4.3 Profile");
            return new EAP4310();
         }
         if(eapVersion.contains("5"))
         {
            System.out.println("Using EAP 5.1.x Profile");
            return new EAP512();
         }
         if(eapVersion.contains("6"))
         {
            System.out.println("Using EAP 6.0.0 Profile");
            return new EAP600();
         }
         if(eapVersion.contains("7"))
         {
            System.out.println("Using EAP 7.0.0 Profile");
            return new EAP700();
         }         
      }
      // Default to EAP 5.1.x for now
      System.out.println("Using EAP 5.1.x Profile");
      return new EAP512();     
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
   
   private void writeDetailToFile(BufferedWriter bw) throws IOException
   {
      
      String[] profileProblemLevel = new String[]
      {"PROBLEM", "PROBLEM"};
      
      boolean archiveNameWritten;
      
      // if true, it will create a hashmap mapping customer packaged classes to a jar in JBoss and then output them at the end
      // if false, it will output the classes as it goes and will put the jboss jar that contains each class in brackets
      boolean groupOutputByJBossJar = true; 
      
      Map<String, ArrayList<String>> jbossClassLocationsMap = new HashMap<>();

      // TODO here we want to write the problem classes in the archive to a different file, this file could then be loaded via a single floating frame
      // write html or xml & use xslt

      // write a dialog div into the page
      bw.write("<div id=\"dialog\" style='display:none'><iframe id=\"iframe\" width='800' height='400'></iframe></div>");
      
      bw.write("<script> $(document).ready(function() { $('#dialog').dialog({position:['right','top'], minWidth:850, minHeight:450, autoOpen:true}); });");
      bw.write("function switchDialog(el, newSrc, title) { $('#'+el.id).toggleClass('green-selected'); $('#iframe').attr('src',newSrc); if(! $('#dialog').dialog('isOpen') != true) { $('#dialog').dialog('open');} }");
      bw.write("</script>");

      
      for (ProblematicArchive problemmaticArchive : problemSet)
      {
         archiveNameWritten = false;
         Archive archive = problemmaticArchive.archive;

         Set<String> classes = archive.getProvides().keySet();
         int i = -1;
         for (AbstractExtendedProfile profile : profiles)
         {
            i++;
            boolean profileNameWritten = false;

            for (String clz : classes)
            {
               if (profile.doesProvide(clz))
               {
                  // log the archive name once
                  if (!groupOutputByJBossJar && !archiveNameWritten)
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

                  // class location in JBoss
                  List<String> locationsInJBoss = profile.getLocationProvided(clz);
                  
                  String locationInJBoss = "";
                  if(locationsInJBoss != null)
                  {
                     locationInJBoss = locationsInJBoss.toString();
                  }                  
                  
                  // log the class that is included by the jdk or
                  // container
                  if( ! groupOutputByJBossJar )
                     bw.write("<li>" + clz + " " + locationInJBoss + "</li>" + Dump.newLine());
                  else
                  {
                     // TODO loop through classes, instead of printing them out, group them by location jar, then output location jar with classes                  
                     for(String loc : locationsInJBoss)
                     {
                        ArrayList<String> list = jbossClassLocationsMap.get(loc);
                        if(list == null)
                        {
                           list = new ArrayList<>();
                        }
                        
                        list.add(clz);
                        jbossClassLocationsMap.put(loc, list);                        
                     }
                  }
               }
            }                      

            // close the profile block
            if (profileNameWritten)
               bw.write("</ul>" + Dump.newLine());

            if(groupOutputByJBossJar)
            {
               bw.write("<h4>" + addColor(asString(archive.getLocations()), "red") + " duplicates classes in :</h4>");                  

               
               
//               classListId++;
               bw.write("<ul>" + Dump.newLine());
               for(String jbossJar : jbossClassLocationsMap.keySet())
               {
                  classListId++;
                  // TODO Here we need to load the iframe
                  ArrayList<String> classList = jbossClassLocationsMap.get(jbossJar);
                  String jbossJarFileName = jbossJar.replace("/", "-")+".html";
                  
//                  String displayClassesJavaScript = "\"$('#iframe').attr('src','"+ jbossJarFileName +"'); $('#dialog').show({title:'"+jbossJar+"'});\"";
                  String displayClassesJavaScript = "\"switchDialog(this,'"+ jbossJarFileName +"', '"+ jbossJar +"')\"";
//                  String displayClassesJavaScript = "\"switchDialog('blah', 'blah2');\"";

                  String unHighlight = "\"$(this).toggleClass('green-selected');\"";
                  
                  bw.write("<li id=\"cl" +classListId+ "\" onmouseover="+ displayClassesJavaScript + " onmouseout=" + unHighlight +">" + addColor(jbossJar, "green"));
//                  bw.write("<h4 onmouseover="+displayClassesJavaScript+">" + addColor(asString(archive.getLocations()), "red") + " duplicates classes in " + addColor(jbossJar, "green"));                  
//                  bw.write(" <span onclick="+ displayClassesJavaScript +">Show Classes</span> ");
//                  bw.write(" <span onclick=\"$('#iframe').attr('src','"+ jbossJarFileName +"'); $('#dialog').dialog({minWidth:850, minHeight:450, title:'"+jbossJar+"'});\">Show Classes</span> ");
//                  bw.write("</h4>" + Dump.newLine());
                  bw.write(Dump.newLine());
                  bw.write("</li>" + Dump.newLine());
                  //bw.write("<span onclick=\"$('#cl"+classListId+"').toggle( 'blind', {}, 500, null);\">Show/Hide Details</span>");                  

                  // here we need to create the custom file                  
                  BufferedWriter out = new BufferedWriter(new FileWriter(new File(getOutputDirectory(), jbossJarFileName )));
                  out.write(jbossJar + "already contains these classes:<br/>" + Dump.newLine());
//                  out.write("<ul class=\"toggleOff\" id=\"cl" +classListId+ "\">" + Dump.newLine());
                  out.write("<ul class=\"toggleOff>\"" + Dump.newLine());
                  for(String clz : classList)
                  {                     
                     out.write("<li>" + clz + "</li>" + Dump.newLine());
                     // TODO write these to a file named with profile.getName()
                  }
                                    
                  out.write("</ul>" + Dump.newLine());
                  out.close();
                  
               }
               bw.write("</ul>" + Dump.newLine());

               jbossClassLocationsMap.clear();
            }
         }         
      }            

   }
   
   private void writeDetailed(BufferedWriter bw) throws IOException
   {
      bw.write("<h1>Detailed analysis of problematic archives:</h1>");
      
      String[] profileProblemLevel = new String[]
      {"PROBLEM", "PROBLEM"};
      
      boolean archiveNameWritten;
      
      // if true, it will create a hashmap mapping customer packaged classes to a jar in JBoss and then output them at the end
      // if false, it will output the classes as it goes and will put the jboss jar that contains each class in brackets
      boolean groupOutputByJBossJar = true; 
      
      Map<String, ArrayList<String>> jbossClassLocationsMap = new HashMap<>();

      // TODO here we want to write the problem classes in the archive to a different file, this file could then be loaded via a single floating frame
      // write html or xml & use xslt
      
      for (ProblematicArchive problemmaticArchive : problemSet)
      {
         archiveNameWritten = false;
         Archive archive = problemmaticArchive.archive;

         Set<String> classes = archive.getProvides().keySet();
         int i = -1;
         for (AbstractExtendedProfile profile : profiles)
         {
            i++;
            boolean profileNameWritten = false;

            for (String clz : classes)
            {
               if (profile.doesProvide(clz))
               {
                  // log the archive name once
                  if (!groupOutputByJBossJar && !archiveNameWritten)
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

                  // class location in JBoss
                  List<String> locationsInJBoss = profile.getLocationProvided(clz);
                  
                  String locationInJBoss = "";
                  if(locationsInJBoss != null)
                  {
                     locationInJBoss = locationsInJBoss.toString();
                  }                  
                  
                  // log the class that is included by the jdk or
                  // container
                  if( ! groupOutputByJBossJar )
                     bw.write("<li>" + clz + " " + locationInJBoss + "</li>" + Dump.newLine());
                  else
                  {
                     // TODO loop through classes, instead of printing them out, group them by location jar, then output location jar with classes                  
                     for(String loc : locationsInJBoss)
                     {
                        ArrayList<String> list = jbossClassLocationsMap.get(loc);
                        if(list == null)
                        {
                           list = new ArrayList<>();
                        }
                        
                        list.add(clz);
                        jbossClassLocationsMap.put(loc, list);                        
                     }
                  }
               }
            }                      

            // close the profile block
            if (profileNameWritten)
               bw.write("</ul>" + Dump.newLine());

            if(groupOutputByJBossJar)
            {
//               classListId++;
               for(String jbossJar : jbossClassLocationsMap.keySet())
               {
                  classListId++;
                  ArrayList<String> classList = jbossClassLocationsMap.get(jbossJar);
                  bw.write("<h4> The class below from " + addColor(asString(archive.getLocations()), "red") + " are already contained in " + profile.getName() + " jar " + addColor(jbossJar, "green") + "</h4>" + Dump.newLine());                  
                  bw.write("<span onclick=\"$('#cl"+classListId+"').toggle( 'blind', {}, 500, null);\">Show/Hide Details</span>");
                  bw.write("<ul class=\"toggleOff\" id=\"cl" +classListId+ "\">" + Dump.newLine());
                  for(String clz : classList)
                  {
                     bw.write("<li>" + clz + "</li>" + Dump.newLine());
                  }
                  bw.write("</ul>" + Dump.newLine());
               }
               jbossClassLocationsMap.clear();
            }
         }         
      }            
   }

   /**
    * analyze the archives and create a summarySet and problemsSet so we can print out the report
    */
   @Override
   protected void analyze()
   {      
      // archives comes from AbstractReport, if given an ear, it only contains an EarArchive, we now have to call to get all subArchives
      int nonFilteredProblems = 0;
       
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
            }

            if (a.getType() == ArchiveType.JAR)
            {
               Set<String> classes = a.getProvides().keySet();
               // loop through profiles, create a section for each profile
            
               List<AbstractProfile> profilesMatched = new ArrayList<>();
               for (AbstractExtendedProfile profile : profiles)
               {               
                  //System.out.println("Checking profile: " + profile);
                  for (String clz : classes)
                  {
//                     if (profile.doesProvide(clz))
                     List<String> locations = profile.getLocationProvided(clz);
                     if (locations != null) // then the propfile contains it
                     {
                        //System.out.println("packaging class: " + clz);
//                        problemSet.add(new ProblematicArchive(a, profile));
                        profilesMatched.add(profile);

                        // track archives that contain classes they shouldn't in summary
//                        summarySet.add(a);

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
      
      if (nonFilteredProblems >= 10) {
          this.status = ReportStatus.RED;
      }
      else if (nonFilteredProblems >= 5) {
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
      for (ProblematicArchive a : problemSet)
      {
         for (Location l : a.archive.getLocations())
         {
            bw.write("<li>" + l.getFilename() + " [ " + profilesListToString(a.profiles) +"]</li>");
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
      if(sb.length() > 2)
         sb.delete(sb.length()-2, sb.length());
      return sb.toString();      
   }
   
   private void writeJQueryHeader(BufferedWriter bw) throws IOException
   {
      bw.write("<style>" + Dump.newLine());
      bw.write(".green { color: green; background-color:transparent; }" + Dump.newLine());
      bw.write(".red { color: red; background-color:transparent; }" + Dump.newLine());
      bw.write(".green-selected { color: white; background-color:black; }" + Dump.newLine());
      bw.write(".jbossJar { color: green; background-color:transparent; }" + Dump.newLine());
      bw.write("#left {  left:0px; width:49%;height:100%;float:left; border:1px solid black; overflow: scroll }" + Dump.newLine());
      bw.write("#right { left:51%;width:49%;height:100%;float:right; border:1px solid black; }" + Dump.newLine());
      bw.write("#left-right-parent { width:98%;height:100%; }" + Dump.newLine());
      
      bw.write(".toggleOff{ }" + Dump.newLine());
      bw.write("</style>" + Dump.newLine());
      
      bw.write("<link type=\"text/css\" href=\"jquery/css/ui-lightness/jquery-ui-1.8.18.custom.css\" rel=\"stylesheet\"/>");
      bw.write("<script type=\"text/javascript\" src=\"jquery/js/jquery-1.7.1.min.js\"></script>");
      bw.write("<script type=\"text/javascript\" src=\"jquery/js/jquery-ui-1.8.18.custom.min.js\"></script>");
      bw.write("<script>$(document).ready(function() { $('.toggleOff').toggle(false); });</script>");

      // copy jquery js & css into the directory
      File outputDir = getOutputDirectory();
      for(String filePath : new String[] { "jquery/css/ui-lightness/jquery-ui-1.8.18.custom.css", "jquery/js/jquery-1.7.1.min.js", "jquery/js/jquery-ui-1.8.18.custom.min.js" })
      {
    	  
    	 URL url = PackagedJBossClasses.class.getClassLoader().getResource(filePath);
         //System.out.println("filePath: " + filePath + " url: " + url);
         //System.out.println("Brad: rootPath: " + getRootPath());

         // create output dirs
         File dir = new File(outputDir, filePath.substring(0, filePath.lastIndexOf("/")+1));
         dir.mkdirs();
         
         FileOutputStream fos;
          try ( // copy file over
                  InputStream is = url.openStream()) {
              fos = new FileOutputStream(new File(outputDir, filePath));
              int oneChar, count=0;
              while ((oneChar=is.read()) != -1)
              {
                  fos.write(oneChar);
                  count++;
              }
          }
         fos.close();
      }
      
   }
   
   private void writeHtmlBodyLeft(BufferedWriter bw) throws IOException
   {
      bw.write("<span id='left'>" + Dump.newLine());
      String[] profileProblemLevel = new String[]
      {"PROBLEM", "PROBLEM"};
      
      boolean archiveNameWritten;
      
      // if true, it will create a hashmap mapping customer packaged classes to a jar in JBoss and then output them at the end
      // if false, it will output the classes as it goes and will put the jboss jar that contains each class in brackets
      boolean groupOutputByJBossJar = true; 
      
      // write a dialog div into the page
      //bw.write("<div id=\"dialog\" style='display:none'><iframe id=\"iframe\" width='800' height='400'></iframe></div>");
      
      bw.write("<script> " + Dump.newLine());

      //bw.write("$(document).ready(function() { $('#dialog').dialog({position:['right','top'], minWidth:850, minHeight:450, autoOpen:true}); });");
      bw.write("var previousSelected = null; function switchDialog(el, newSrc, title) { if(previousSelected) $('#'+previousSelected.id).toggleClass('green-selected'); $('#'+el.id).toggleClass('green-selected'); $('#iframe').attr('src',newSrc); previousSelected=el; }");
      bw.write("</script>");
      
      Map<String, ArrayList<String>> jbossClassLocationsMap = new HashMap<>();
      
      for (ProblematicArchive problemmaticArchive : problemSet)
      {
         archiveNameWritten = false;
         Archive archive = problemmaticArchive.archive;

         Set<String> classes = archive.getProvides().keySet();
         int i = -1;
         for (AbstractExtendedProfile profile : profiles)
         {
            i++;
            boolean profileNameWritten = false;

            for (String clz : classes)
            {
               if (profile.doesProvide(clz))
               {
                  // log the archive name once
                  if (!groupOutputByJBossJar && !archiveNameWritten)
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

                  // class location in JBoss
                  List<String> locationsInJBoss = profile.getLocationProvided(clz);
                  
                  String locationInJBoss = "";
                  if(locationsInJBoss != null)
                  {
                     locationInJBoss = locationsInJBoss.toString();
                  }                  
                  
                  // log the class that is included by the jdk or
                  // container
                  if( ! groupOutputByJBossJar )
                     bw.write("<li>" + clz + " " + locationInJBoss + "</li>" + Dump.newLine());
                  else
                  {
                     // TODO loop through classes, instead of printing them out, group them by location jar, then output location jar with classes                  
                     for(String loc : locationsInJBoss)
                     {
                        ArrayList<String> list = jbossClassLocationsMap.get(loc);
                        if(list == null)
                        {
                           list = new ArrayList<>();
                        }
                        
                        list.add(clz);
                        jbossClassLocationsMap.put(loc, list);                        
                     }
                  }
               }
            }                      

            // close the profile block
            if (profileNameWritten)
               bw.write("</ul>" + Dump.newLine());

            if(groupOutputByJBossJar)
            {
               bw.write("<h4>" + addColor(asString(archive.getLocations()), "red") + " duplicates classes in :</h4>");                  
                           
//               classListId++;
               bw.write("<ul>" + Dump.newLine());
               for(String jbossJar : jbossClassLocationsMap.keySet())
               {
                  classListId++;
                  // TODO Here we need to load the iframe
                  ArrayList<String> classList = jbossClassLocationsMap.get(jbossJar);
                  String jbossJarFileName = jbossJar.replace("/", "-")+".html";
                  
//                  String displayClassesJavaScript = "\"$('#iframe').attr('src','"+ jbossJarFileName +"'); $('#dialog').show({title:'"+jbossJar+"'});\"";
                  String displayClassesJavaScript = "\"switchDialog(this,'"+ jbossJarFileName +"', '"+ jbossJar +"')\"";
//                  String displayClassesJavaScript = "\"switchDialog('blah', 'blah2');\"";

                  String unHighlight = "\"$(this).toggleClass('green-selected');\"";
                  
//                  bw.write("<li id=\"cl" +classListId+ "\" onmouseover="+ displayClassesJavaScript + " onmouseout=" + unHighlight +">" + addColor(jbossJar, "green"));
                  bw.write("<li id=\"cl" +classListId+ "\" onclick="+ displayClassesJavaScript + " style=\"cursor:help\">" + addClass(jbossJar, "jbossJar"));
//                  bw.write("<h4 onmouseover="+displayClassesJavaScript+">" + addColor(asString(archive.getLocations()), "red") + " duplicates classes in " + addColor(jbossJar, "green"));                  
//                  bw.write(" <span onclick="+ displayClassesJavaScript +">Show Classes</span> ");
//                  bw.write(" <span onclick=\"$('#iframe').attr('src','"+ jbossJarFileName +"'); $('#dialog').dialog({minWidth:850, minHeight:450, title:'"+jbossJar+"'});\">Show Classes</span> ");
//                  bw.write("</h4>" + Dump.newLine());
                  bw.write(Dump.newLine());
                  bw.write("</li>" + Dump.newLine());
                  //bw.write("<span onclick=\"$('#cl"+classListId+"').toggle( 'blind', {}, 500, null);\">Show/Hide Details</span>");                  

                  // here we need to create the custom file                  
                  BufferedWriter out = new BufferedWriter(new FileWriter(new File(getOutputDirectory(), jbossJarFileName )));
                  out.write(jbossJar + "already contains these classes:<br/>" + Dump.newLine());
//                  out.write("<ul class=\"toggleOff\" id=\"cl" +classListId+ "\">" + Dump.newLine());
                  out.write("<ul class=\"toggleOff>\">" + Dump.newLine());
                  for(String clz : classList)
                  {
                     out.write("<li>" + clz + "</li>" + Dump.newLine());
                     // TODO write these to a file named with profile.getName()
                  }
                                    
                  out.write("</ul>" + Dump.newLine());
                  out.close();
                  
               }
               bw.write("</ul>" + Dump.newLine());

               jbossClassLocationsMap.clear();
            }
         }         
      }            

      bw.write("</span>" + Dump.newLine());
   }

   private void writeHtmlBodyRight(BufferedWriter bw) throws IOException
   {
      bw.write("<span id='right'>" + Dump.newLine());
      bw.write("<iframe id='iframe' style='width:100%;height:100%;'></iframe>" + Dump.newLine());
      bw.write("</span>" + Dump.newLine());

   }

   
   @Override
   public void writeHtmlBodyHeader(BufferedWriter bw) throws IOException
   {
      
      writeJQueryHeader(bw);      
      
      bw.write("<body style='width:100%;height:100%;'>" + Dump.newLine());
      bw.write(Dump.newLine());
      bw.write("<h1>" + NAME + "</h1>" + Dump.newLine());
      bw.write("<h3>" + addColor("POSSIBLE PROBLEM", "orange") + " - indicates these classes will most likely cause ClassCastExceptions and should be removed</h3>"
            + Dump.newLine());           
      
      // J2EE vs Servlet container rant
      bw.write("<p>");
      bw.write("JBoss Enterprise Application Platform is a Java Enterprise Edition (J2EE) certified implementation application container.");
      bw.write("J2EE containers are required to implement various specifications versions, such as Java Persistence API (JPA), Java Server Faces (JSF), etc.");
      bw.write("Other issues occur when an application packages a JDK API which conflicts with the JDK, some other servlet containers such as Apache Tomcat ignore these classes, so they were never actually used, however JBoss will try to use them and thus they should be removed or JBoss configured to ignore them");
      bw.write("In contrast with Servlet containers such as Apache Tomcat, which do not include all of the specifications that a J2EE container does, this is why web applications (wars) being migrated to a J2EE application containers often encounter classloading issues.");
      bw.write("The issues are a result of the application server's implementation of a particular specification trying to do its job and finding a diffferent version of the APIs or implementation classes in the application being migrated.");
      bw.write("</p>");
      bw.write("<a href=\"../index.html\">Main</a>" + Dump.newLine() + "<br/>");      
//      bw.write("<p>" + Dump.newLine());            
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
      //writeDetailed(bw);
//      writeDetailToFile(bw);
//      writeHtmlBodyLeft(bw);
//      writeHtmlBodyRight(bw);
       bw.write("<div id='left-right-parent'>" + Dump.newLine());
       writeHtmlBodyLeft(bw);
       writeHtmlBodyRight(bw);
       bw.write("</div'>" + Dump.newLine());
   }
   
   private String asString(SortedSet<Location> locations)
   {
      String st = "[";      
      for(Location l : locations)
      {
         st = st + l.getFilename() + ", ";
      }
      return st + "]";
   }
   private String addColor(String st, String color)
   {
      return "<span style='color:" + color + "'>" + st + "</span>";
   }
   private String addClass(String st, String clazz)
   {
      return "<span class='" + clazz + "'>" + st + "</span>";
   }

}
