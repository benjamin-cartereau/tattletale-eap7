package com.redhat.gss.middleware.tattletale.reports;


public class EAP429 extends AbstractExtendedProfile
{

   private static final String CLASS_SET = "eap429.clz.gz";

   private static final String PROFILE_NAME = "JBoss Enterprise Application Platform 4.2 CP09";

   private static final String PROFILE_CODE = "eap429";

   private static final String PROFILE_LOCATION = "jboss-eap-4.2.9.GA_CP09.zip";

   private static final String MODULE_IDENTIFIER = "eap429.impl";

   private static final int ARCHIVE_TYPE = 1;

   private static final int CLASSFILE_VERSION = 49; // JDK 5

   public EAP429()
   {
      super(CLASS_SET, ARCHIVE_TYPE, PROFILE_NAME, CLASSFILE_VERSION, PROFILE_LOCATION);
   }

   public String getProfileCode()
   {
      return PROFILE_CODE;
   }

   protected String getProfileName()
   {
      return PROFILE_NAME;
   }

   public String getModuleIdentifier()
   {
      return MODULE_IDENTIFIER;
   }
}
