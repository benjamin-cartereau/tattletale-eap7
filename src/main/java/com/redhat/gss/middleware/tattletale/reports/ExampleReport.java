package com.redhat.gss.middleware.tattletale.reports;

import java.io.BufferedWriter;
import java.io.IOException;

import org.jboss.tattletale.reporting.AbstractReport;

public class ExampleReport extends AbstractReport
{
   public ExampleReport(String id, ReportSeverity severity)
   {
      super(id, severity);
   }

   @Override
   public void writeHtmlBodyHeader(BufferedWriter paramBufferedWriter) throws IOException
   {
      // TODO Auto-generated method stub

   }

   @Override
   public void writeHtmlBodyContent(BufferedWriter paramBufferedWriter) throws IOException
   {
      // TODO Auto-generated method stub

   }

}
