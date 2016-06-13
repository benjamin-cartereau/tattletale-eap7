/**
 * 
 */
package com.redhat.gss.middleware.tattletale.reports;

import java.util.List;
import java.util.Properties;
import java.util.SortedMap;
import java.util.SortedSet;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.reporting.Report;

/**
 * @author bmaxwell
 *
 */
public interface ReportWithParameters extends Report
{
   // pulled from properties.get("classloader"), it looks like it defaults to org.jboss.tattletale.reporting.classloader.NoopClassLoaderStructure
   public void setCLS(String classloaderStructure);
   
   // somethign to do with Profiles, possibly the profiles that should be checked?
   public void setKnown(List known);

   // this is a set of the archives passed to tattletale on the commandline which have been analyszed
   // note some are NestableArchives which have children
   public void setArchives(SortedSet<Archive> archives);
   
   // This is the properties configuration jboss-tattletale.properties, it will use what is in the external properties file first, 
   //  if it is not there then it will use the default inside the tattletale.jar
   // tattletale main can be created programtically and a configuration set on it, it will try to load the propeties file given, if not, it will use load default configuration.
   // default configuration checks for a sytem property jboss-tattletale.properties, if set it will use the value as the path to the properties file to load.
   // If the property wasn't set or it failed to load from it, then it looks for a file in the dir named jboss-tattletale.properties and loads it
   // if not loaded, then it looks for jboss-tattletale.properties on the classloader
   public void setConfig(Properties config);
   
   // looks like this is populated as jars, wars, and ears are scanned
   public void setGlobalProvides(SortedMap gProvides);
}
