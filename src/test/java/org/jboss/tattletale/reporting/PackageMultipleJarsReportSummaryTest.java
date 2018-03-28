/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jboss.tattletale.reporting;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Z016876
 */
public class PackageMultipleJarsReportSummaryTest {
    
    public PackageMultipleJarsReportSummaryTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }

    /**
     * Test of getCommonsSubpackages method, of class PackageMultipleJarsReportSummary.
     */
    @Test
    public void testGetCommonsSubpackages() {
        Set<String> packages = new LinkedHashSet<>();
        packages.add("javax.jms");
        packages.add("javassist.convert-max");
        
        packages.add("cnv.arch.usi.so.framework.base.exception");
        packages.add("cnv.arch.usi.so.framework.base.jmx");
        packages.add("cnv.arch.usi.so.framework.base.pom");
        packages.add("cnv.arch.usi.so.framework.base.properties");
        packages.add("cnv.arch.usi.so.framework.base.pot");
        
        packages.add("cnx.ax.bw");
        
        packages.add("org.aspectj.lang.annotation");
        packages.add("org.aspectj.internal.lang.annotation");
        packages.add("org.aspectj.lang.internal.lang");
        packages.add("org.aspectj.lang.reflect");
        packages.add("org.aspectj.lang.annotation.control");
        packages.add("org.aspectj.runtime.reflect");
        packages.add("org.aspectj.runtime");
        packages.add("org.aspectj.lang");
        packages.add("org.aspectj.runtime.internal");
        
        packages.add("javassist.compiler-ast");
        packages.add("org.aspectj.internal.lang.reflect");
        packages.add("javax.cdi");
        packages.add("org.aspectj.runtime.internal.cflowstack");
        
        packages.add("javax.json.spi");
        packages.add("javax.json");
        
        packages.add("javax.annotation");
        packages.add("org.aspectj.internal.lang.annotation");
        
        SortedMap<String, SortedSet<String>> commons = PackageMultipleJarsReportSummary.getCommonsSubpackages(packages);
        
        assertEquals(5, commons.get("cnv.arch.usi.so.framework.base").size());
	assertEquals(1, commons.get("cnx.ax.bw").size());
	assertEquals(1, commons.get("javassist.compiler-ast").size());
	assertEquals(1, commons.get("javassist.convert-max").size());
	assertEquals(1, commons.get("javax.annotation").size());
	assertEquals(1, commons.get("javax.cdi").size());
	assertEquals(1, commons.get("javax.jms").size());
	assertEquals(2, commons.get("javax.json").size());
	assertEquals(11, commons.get("org.aspectj").size());
    }
}
