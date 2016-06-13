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
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.ZipOutputStream;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.core.ArchiveTypes;
import org.jboss.tattletale.core.Location;
import org.jboss.tattletale.core.NestableArchive;
import org.jboss.tattletale.reporting.AbstractReport;
import org.jboss.tattletale.reporting.Dump;
import org.jboss.tattletale.reporting.Filter;
import org.jboss.tattletale.reporting.KeyFilter;
import org.jboss.tattletale.reporting.Report;
import org.jboss.tattletale.reporting.ReportSeverity;
import org.jboss.tattletale.reporting.SummaryDetailReport;

/**
 * @author bmaxwell
 */
public class MarshalledReport implements ReportWithParameters
{
   /** NAME */
   private static final String NAME = "Marshalled Report";

   private static final String ID = "marshalledReport";
   
   /** DIRECTORY */
   private static final String DIRECTORY = "marshalled-report";
   
   private static final int STATUS = 0;
   
   private static final int SEVERITY = 0;
   
   protected SortedSet<Archive> archives;
   
   private File outputDirectory;
   
   /**
    * Default Constructor
    */
   public MarshalledReport()
   {  
      System.out.println("MarshalledReport()");
   }         

   public MarshalledReport(String id, int severity, String name, String directory)
   {
      System.out.println("MarshalledReport(" + id + "," + severity + ", " + name + ", " + directory + ")");
   }
   
   private void marshal(Object object, File filename)
   {
      System.out.println("marhsal " + object.getClass().getName() + " to " + filename);
      FileOutputStream fos = null;
      DeflaterOutputStream dos = null;
      ObjectOutputStream out = null;
      try
      {         
         fos = new FileOutputStream(filename);         
         dos = new DeflaterOutputStream(fos);
         out = new ObjectOutputStream(dos);
         out.writeObject(object);         
      }
      catch (IOException ex)
      {
         ex.printStackTrace();
      }
      finally
      {
         closeAndEat(out, dos, fos);
      }
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

   public int compareTo(Object o)
   {
      // TODO Auto-generated method stub
      return 0;
   }

   public void generate(String rootOutputDirectory)
   {      
      createOutputDir(rootOutputDirectory);
      marshal(archives, new File(outputDirectory, "archives.ser.deflate"));    
   }

   public String getId()
   {
      return ID;
   }

   public int getSeverity()
   {
      return SEVERITY;
   }

   public int getStatus()
   {
      return STATUS;
   }

   public String getDirectory()
   {
      return DIRECTORY;
   }

   public String getName()
   {
      // TODO Auto-generated method stub
      return NAME;
   }

   public String getFilter()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public File getOutputDirectory()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public String getIndexName()
   {
      // TODO Auto-generated method stub
      return null;
   }

   public void setFilter(String paramString)
   {
      // TODO Auto-generated method stub
      
   }
   
   void createOutputDir(String outputDirectory)
   {
      this.outputDirectory = new File(outputDirectory, getDirectory());
      this.outputDirectory.mkdirs();
   }
   
   public BufferedWriter getBufferedWriter(String filename) throws IOException
   {
      FileWriter fw = new FileWriter(getOutputDirectory().getAbsolutePath() + File.separator + filename);
      return new BufferedWriter(fw, 8192);
   }

   
   public void setCLS(String classloaderStructure)
   {
      // TODO Auto-generated method stub
      
   }

   
   public void setKnown(List known)
   {
      // TODO Auto-generated method stub
      
   }

   
   public void setArchives(SortedSet<Archive> archives)
   {
      this.archives = archives;
   }

   
   public void setConfig(Properties config)
   {
      // TODO Auto-generated method stub
      
   }

   
   public void setGlobalProvides(SortedMap gProvides)
   {
      // TODO Auto-generated method stub
      
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
