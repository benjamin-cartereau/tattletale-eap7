package com.redhat.gss.middleware.tattletale.reports;


public class EAP600 extends AbstractExtendedProfile
{

   private static final String CLASS_SET = "eap600.clz.gz";

   private static final String PROFILE_NAME = "JBoss Enterprise Application Platform 6.0.0";

   private static final String PROFILE_CODE = "eap600";

   private static final String PROFILE_LOCATION = "jboss-eap-0.0.0.zip";

   private static final String MODULE_IDENTIFIER = "eap600.impl";

   private static final int ARCHIVE_TYPE = 1;

   private static final int CLASSFILE_VERSION = 50; // JDK 6

   public EAP600()
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
