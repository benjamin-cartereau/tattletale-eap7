/**
 * 
 */
package com.redhat.gss.middleware.tattletale.reports;

import java.util.List;

import org.jboss.tattletale.core.Archive;
import org.jboss.tattletale.profiles.AbstractProfile;

/**
 * This is a class to link archive/profiles for printing later
 * @author bmaxwell
 */
public class ProblematicArchive implements Comparable<ProblematicArchive>
{
   public List<AbstractProfile> profiles;

   public Archive archive;

   public ProblematicArchive(Archive archive, List<AbstractProfile> profiles)
   {
      this.archive = archive;
      this.profiles = profiles;
   }
   
   @Override
   public int compareTo(ProblematicArchive other)
   {
      int val = this.archive.compareTo(other.archive);
      
      if(val != 0)
         return val;
                  
//      return this.profile.getProfileCode().compareTo(other.profile.getProfileCode());
      // TODO this is a hack since I changes profile to a list
      return this.profiles.hashCode() - other.profiles.hashCode();
   }
   
   @Override
   public boolean equals(Object obj)
   {
      if(! (obj instanceof ProblematicArchive))
         return false;
      return compareTo((ProblematicArchive)obj) == 0;
   }
}