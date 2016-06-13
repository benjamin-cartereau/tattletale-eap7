package com.redhat.gss.middleware.tattletale.reports;


public class EAP700 extends AbstractExtendedProfile
{

   private static final String CLASS_SET = "eap700.clz.gz";

   private static final String PROFILE_NAME = "JBoss Enterprise Application Platform 7.0.0";

   private static final String PROFILE_CODE = "eap700";

   private static final String PROFILE_LOCATION = "jboss-eap-0.0.0.zip";

   private static final String MODULE_IDENTIFIER = "eap700.impl";

   private static final int ARCHIVE_TYPE = 1;

   private static final int CLASSFILE_VERSION = 52; // JDK 8

   public EAP700()
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
