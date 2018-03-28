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
package org.jboss.tattletale.reporting;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.jboss.tattletale.utils.StringUtils;

/**
 * Packages in multiple JAR files report
 *
 * @author <a href="mailto:jesper.pedersen@jboss.org">Jesper Pedersen</a>
 * @author <a href="mailto:torben.jaeger@jit-consulting.de">Torben Jaeger</a>
 */
public class PackageMultipleJarsReportSummary extends SummaryDetailReport 
{

    /**
     * NAME
     */
    private static final String NAME = "Multiple Jar files (Summary)";

    /**
     * DIRECTORY
     */
    private static final String DIRECTORY = "multiplejarspackage2";

    /**
     * Globally provides
     */
    private SortedMap<String, SortedSet<String>> gProvides;

    /**
     * Constructor
     */
    public PackageMultipleJarsReportSummary() 
    {
        super(DIRECTORY, ReportSeverity.WARNING, NAME, DIRECTORY);
    }

    /**
     * Set the globally provides map to be used in generating this report
     *
     * @param gProvides the map of global provides
     */
    public void setGlobalProvides(SortedMap<String, SortedSet<String>> gProvides) 
    {
        this.gProvides = gProvides;
    }

    /**
     * write the report's content
     *
     * @param bw the BufferedWriter to use
     * @throws IOException if an error occurs
     */
    @Override
    public void writeHtmlBodyContent(BufferedWriter bw) throws IOException 
    {
        // Start of HTML table
        bw.write("<table>" + Dump.newLine());

        writeHtmlTableHeaders(bw);

        SortedMap<String, SortedSet<String>> packageProvides = extractPackageProvides();

        writePackagesArchives(packageProvides, bw);

        // End of HTML table
        bw.write("</table>" + Dump.newLine());
    }

    private void writeHtmlTableHeaders(BufferedWriter bw) throws IOException 
    {
        // Write HTML Table columns headers
        bw.write("  <tr>" + Dump.newLine());
        bw.write("    <th>Common SubPackage</th>" + Dump.newLine());
        bw.write("    <th>Packages included</th>" + Dump.newLine());
        bw.write("    <th>Archives</th>" + Dump.newLine());
        bw.write("  </tr>" + Dump.newLine());
    }

    /**
     * Extract for each package, the jars that provide it
     * @return SortedMap that map a package name to archives (jars) that provide it
     */
    private SortedMap<String, SortedSet<String>> extractPackageProvides() 
    {
        // [package name] -> [archives : ie. jars]
        final SortedMap<String, SortedSet<String>> packageProvides = new TreeMap<>();
        for (Map.Entry<String, SortedSet<String>> entry : gProvides.entrySet()) 
        {
            String clz = entry.getKey();
            SortedSet<String> clzArchives = entry.getValue();

            // Extract package name
            String packageName;

            if (clz.indexOf('.') == -1) 
            {
                packageName = "";
            } 
            else 
            {
                packageName = clz.substring(0, clz.lastIndexOf('.'));
            }

            // Get jars having this package inside if any or create a new set
            SortedSet<String> packageJars = packageProvides.get(packageName);
            if (null == packageJars) 
            {
                packageJars = new TreeSet<>();
            }

            // Add new jars found having this package inside
            packageJars.addAll(clzArchives);

            // Update the jars set for this package
            packageProvides.put(packageName, packageJars);
        }
        return packageProvides;
    }

    /**
     * Write into HTML table common package prefix, included packages and associated archives
     * @param packageProvides Map of packages names and archives (jars) that provide it
     * @param bw
     * @throws IOException 
     */
    private void writePackagesArchives(SortedMap<String, SortedSet<String>> packageProvides, BufferedWriter bw) throws IOException 
    {
        // Extract packages that are provided by more than 1 lib
        SortedSet<String> packagesInMultipleLibs = new TreeSet<>();
        int nonFilteredProblems = 0;
        for (Map.Entry<String, SortedSet<String>> entry : packageProvides.entrySet()) 
        {
            String pkg = entry.getKey();
            SortedSet<String> pkgArchives = entry.getValue();
            if (pkgArchives.size() > 1) 
            {
                packagesInMultipleLibs.add(pkg);
            }
        }
        
        SortedMap<String, SortedSet<String>> commons = getCommonsSubpackages(packagesInMultipleLibs);
        
        // Display package with associated archives/jars
        boolean odd = true;

        for ( Map.Entry<String, SortedSet<String>> entry : commons.entrySet()) 
        {
            String pkg = entry.getKey();
            SortedSet<String> commonPkgs = entry.getValue();

            // Only display packages that are present in more than 1 lib
            if (odd) 
            {
                bw.write("  <tr class=\"rowodd\">" + Dump.newLine());
            } 
            else 
            {
                bw.write("  <tr class=\"roweven\">" + Dump.newLine());
            }
            bw.write("    <td>" + pkg + ".*</td>" + Dump.newLine());

            bw.write("    <td>" + StringUtils.join(commonPkgs, ", ") + "</td>");

            if (!isFiltered(pkg)) 
            {
                nonFilteredProblems++;
                status = ReportStatus.YELLOW;
                bw.write("        <td>");
            } 
            else 
            {
                bw.write("        <td style=\"text-decoration: line-through;\">");
            }
            List<String> hrefs = new ArrayList<>();

            SortedSet<String> archives = new TreeSet<>();
            for (String commonPkg : commonPkgs) 
            {
                archives = packageProvides.get(commonPkg);
            }

            for (String archive : archives) 
            {
                hrefs.add(hrefToReport(archive));
            }
            bw.write(StringUtils.join(hrefs, ", "));
            bw.write("</td>" + Dump.newLine());

            bw.write("  </tr>" + Dump.newLine());

            odd = !odd;
        }

        if (nonFilteredProblems >= 5) 
        {
            status = ReportStatus.RED;
        }
    }

    /**
     * Create filter
     * @return The filter
     */
    @Override
    protected Filter createFilter() 
    {
        return new KeyFilter();
    }

    /**
     * Get common subpackage for packages
     * @param packages a Set of packages
     * @return SortedMap that maps a common sub package name to included packages
     */
    public static SortedMap<String, SortedSet<String>> getCommonsSubpackages(Set<String> packages) 
    {
        // Common sub-package name -> set of common/included packages
        SortedMap<String, SortedSet<String>> commons = new TreeMap<>();
        for (String pkg : packages) 
        {
            boolean found = false;
            for (String common : commons.keySet()) 
            {
                String commonPkg = getLongestCommonSubpackage(pkg, common);
                if (commonPkg != null) 
                {
                    found = true;
                    commons.get(common).add(pkg);
                    commons.put(commonPkg, commons.remove(common));
                    break;
                }
            }
            if (!found) 
            {
                SortedSet<String> commonPackages = new TreeSet<>();
                commonPackages.add(pkg);
                commons.put(pkg, commonPackages);
            }
        }
        return commons;
    }
    
    /**
     * Get the longest common subpackage
     * @param a first package name
     * @param b second package name
     * @return common subpackage name
     */
    protected static String getLongestCommonSubpackage(String a, String b) 
    {
        int max = StringUtils.getGreatestCommonPrefix(a, b);
        if (max <= 0) 
        {
            return null;
        }
        String longestString = a.length() > b.length() ? a : b;
        String commonString = longestString.substring(0, max);
        
        String[] parts = commonString.split("\\.");
        int dotOccurances = parts.length - 1;
        if (dotOccurances<=0) 
        {
            return null;
        }
        else if (dotOccurances==1) 
        {
            if (commonString.endsWith(".")) 
            {
                return commonString.substring(0, commonString.length()-1);
            }
            else 
            {
                int indexOfNextDot = longestString.indexOf(".", max);
                if (indexOfNextDot<0) {
                    // Only parent package and a part of the sub package
                    //  are the same -> not common
                    return null;
                } 
                else if (commonString.equals(longestString.substring(0, indexOfNextDot))) 
                {
                    return commonString;
                }
            }
        }
        else 
        {
            if (commonString.endsWith(".")) 
            {
                return commonString.substring(0, commonString.length()-1);
            }
            else 
            {
                int indexOfNextDot = longestString.indexOf(".", max);
                if (indexOfNextDot < 0) 
                {
                    return commonString.substring(0, commonString.lastIndexOf("."));
                }
                else if (commonString.equals(longestString.substring(0, indexOfNextDot))) 
                {
                    return commonString;
                }
            }
        }
        return null;
    }

    @Override
    protected void analyze() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeHtmlSummary(BufferedWriter p0) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void writeHtmlDetailed(BufferedWriter p0) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
