package com.redhat.gss.middleware.tattletale.reports;

import org.jboss.tattletale.core.ArchiveType;


public class EAP512 extends AbstractExtendedProfile
{

   private static final String CLASS_SET = "eap512.clz.gz";

   private static final String PROFILE_NAME = "JBoss Enterprise Application Platform 5.1.2";

   private static final String PROFILE_CODE = "eap512";

   private static final String PROFILE_LOCATION = "jboss-eap-5.1.2.zip";

   private static final String MODULE_IDENTIFIER = "eap512.impl";

   private static final ArchiveType ARCHIVE_TYPE = ArchiveType.JAR;

   private static final int CLASSFILE_VERSION = 50; // JDK 6

   public EAP512()
   {
      super(CLASS_SET, ARCHIVE_TYPE, PROFILE_NAME, CLASSFILE_VERSION, PROFILE_LOCATION);
   }

   @Override
   public String getProfileCode()
   {
      return PROFILE_CODE;
   }

   @Override
   protected String getProfileName()
   {
      return PROFILE_NAME;
   }

   @Override
   public String getModuleIdentifier()
   {
      return MODULE_IDENTIFIER;
   }
}
