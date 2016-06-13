package com.redhat.gss.middleware.tattletale.reports;


public class EAP4310 extends AbstractExtendedProfile
{

   private static final String CLASS_SET = "eap4310.clz.gz";

   private static final String PROFILE_NAME = "JBoss Enterprise Application Platform 4.3.10";

   private static final String PROFILE_CODE = "eap4310";

   private static final String PROFILE_LOCATION = "jboss-eap-4.3.0.GA_CP10.zip";

   private static final String MODULE_IDENTIFIER = "eap4310.impl";

   private static final int ARCHIVE_TYPE = 1;

   private static final int CLASSFILE_VERSION = 49; // JDK 5

   public EAP4310()
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
