package com.redhat.gss.middleware.tattletale.reports;

import java.io.BufferedWriter;
import java.io.IOException;

import org.jboss.tattletale.reporting.AbstractReport;

public class ExampleReport extends AbstractReport
{
   public ExampleReport(String id, int severity)
   {
      super(id, severity);
   }

   public void writeHtmlBodyHeader(BufferedWriter paramBufferedWriter) throws IOException
   {
      // TODO Auto-generated method stub

   }

   public void writeHtmlBodyContent(BufferedWriter paramBufferedWriter) throws IOException
   {
      // TODO Auto-generated method stub

   }

}
