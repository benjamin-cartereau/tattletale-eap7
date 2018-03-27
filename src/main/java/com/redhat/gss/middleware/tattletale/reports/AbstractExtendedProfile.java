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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.jboss.tattletale.core.ArchiveType;

import org.jboss.tattletale.profiles.AbstractProfile;
import org.jboss.tattletale.profiles.Profile;

/**Ø
 * Base profile class.
 *
 * @author Michele
 * @author Navin Surtani
 */
public abstract class AbstractExtendedProfile extends AbstractProfile implements Profile
{
   // searchJarsForTattletale is a bash script that will create a zipped class file that contains jarPath= and then the classes contained in it
   // TODO searchJarsForTattletale jboss-as eap429.clz
   // note this works for EAP 4.x and EAP 5.x, it has not been configured to work with EAP 6 yet
   
   // class => List<String>location (there could be multiple locations)
   protected Map<String, ArrayList<String>> classMap = new HashMap<>();
   
   /**
    * Constructor
    *
    * @param classSet The .gz file with the classes
    * @param type     Archive type
    * @param name     Profile name
    * @param version  Profile's class version
    * @param location Profile's location
    */
   public AbstractExtendedProfile(String classSet, ArchiveType type, String name, int version, String location)
   {
      this (type, name, version, location);
      loadProfile(classSet);
   }

   /**
    * Constructor
    *
    * @param type     Archive type
    * @param name     Profile name
    * @param version  Profile's class version
    * @param location Profile's location
    */
   public AbstractExtendedProfile(ArchiveType type, String name, int version, String location)
   {
      super(type, name, version, location);
   }
   
   public List<String> getLocationProvided(String clz)
   {
      // TODO subprofiles not implemented here
      
      return classMap.get(clz);
   }

   /**
    * Loads this profile's class list from the resources.
    *
    * @param resourceFile File name
    */
   @Override
   protected void loadProfile(String resourceFile)
   {      
      InputStream is = null;
      try
      {
         is = this.getClass().getClassLoader().getResourceAsStream(resourceFile);

         GZIPInputStream gis = new GZIPInputStream(is);
         InputStreamReader isr = new InputStreamReader(gis);
         BufferedReader br = new BufferedReader(isr);

         String s = br.readLine();
         String currentLocation = "<not initialized>";
         while (s != null)
         {
            if(s.endsWith("="))
            {
               currentLocation = s.substring(0, s.length()-1);
            }
            else
            {
               ArrayList<String> tmpList = classMap.get(s);
               if(tmpList == null)
               {
                  tmpList = new ArrayList<>(1);
               }
               tmpList.add(currentLocation);
               
               classMap.put(s, tmpList);
               
               //classSet.add(s); // classSet can just be map.keySet()
            }
            
            s = br.readLine();
         }
         // set super classSet to be the map keySet
         super.classSet = classMap.keySet();
      }
      catch (Exception e)
      {
         // Ignore
      }
      finally
      {
         try
         {
            if (is != null)
            {
               is.close();
            }
         }
         catch (IOException ioe)
         {
            // Ignore
         }
      }
   }
}
