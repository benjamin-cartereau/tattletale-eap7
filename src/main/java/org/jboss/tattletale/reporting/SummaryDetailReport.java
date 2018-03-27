// TODO : Luyten de-compiled class from "tattletale-eap6_1.jar"

package org.jboss.tattletale.reporting;

import java.io.BufferedWriter;
import java.io.IOException;


public abstract class SummaryDetailReport extends AbstractReport
{
    private String DIRECTORY;
    protected String rootPath;
    
    public SummaryDetailReport() {
        this("empty", 1);
    }
    
    public SummaryDetailReport(final String id, final int severity) {
        super(id, severity);
        this.DIRECTORY = "empty";
    }
    
    public SummaryDetailReport(final String id, final int severity, final String name, final String directory) {
        super(id, severity, name, directory);
        this.DIRECTORY = "empty";
        this.DIRECTORY = directory;
    }
    
    protected String getRootPath() {
        if (this.rootPath == null) {
            System.out.println("Find rootPath with outputDir:" + this.getOutputDirectory().getAbsolutePath() + " directory: " + this.DIRECTORY + " index: " + this.getOutputDirectory().getAbsolutePath().lastIndexOf(this.DIRECTORY));
            this.rootPath = this.getOutputDirectory().getAbsolutePath().substring(0, this.getOutputDirectory().getAbsolutePath().lastIndexOf(this.DIRECTORY));
            System.out.println("GetDirectory: " + super.getDirectory());
            System.out.println("IndexName: " + super.getIndexName());
            System.out.println("ID: " + super.getId());
        }
        return this.rootPath;
    }
    
    @Override
    public void writeHtmlBodyContent(BufferedWriter bw) throws IOException
    {
       // analyze the archive, so that writeSummary / writeDetailed can be in any order.
       analyze();
       writeHtmlSummary(bw);
       writeHtmlDetailed(bw);
    }
    
    protected abstract void analyze();
    
    public abstract void writeHtmlSummary(final BufferedWriter p0) throws IOException;
    
    public abstract void writeHtmlDetailed(final BufferedWriter p0) throws IOException;
}
