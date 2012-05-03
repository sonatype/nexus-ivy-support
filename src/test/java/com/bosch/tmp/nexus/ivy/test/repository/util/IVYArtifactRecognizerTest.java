
package com.bosch.tmp.nexus.ivy.test.repository.util;

import org.apache.log4j.Logger;
import com.bosch.tmp.nexus.ivy.repository.util.IVYArtifactRecognizer;
import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for verifying artifact format.
 *
 * @author wa20277
 */
public class IVYArtifactRecognizerTest
{
    
    private static Logger logger = Logger.getRootLogger(); 
    
    @Test
    public void testIsSnapshot()
    {
       
        logger.info("testIsSnapshot:");
        // Valid SNAPSHOT FORMATS
        boolean result = IVYArtifactRecognizer.isSnapshot(
                new File("/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-13482.jar").toString());
        assertTrue(result);

        result = IVYArtifactRecognizer.isSnapshot(new File("auth-module-0.8-13482.jar").toString());
        assertTrue(result);

        result = IVYArtifactRecognizer.isSnapshot("0.8-13482.jar");
        assertTrue(result);

        result = IVYArtifactRecognizer.isSnapshot("0.8-13482");
        assertTrue(result);

        result = IVYArtifactRecognizer.isSnapshot("0.8-13482.pom");
        assertTrue(result);

        result = IVYArtifactRecognizer.isSnapshot("0.8-13482.zip");
        assertTrue(result);

        result = IVYArtifactRecognizer.isSnapshot("auth_module-0.8-13482");
        assertTrue(result);

        // Invalid SNAPSHOT FORMATS
        result = IVYArtifactRecognizer.isSnapshot(
                new File("/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-13482.1jar").toString());
        assertFalse(result);

        result = IVYArtifactRecognizer.isSnapshot(
                new File("/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8.13482.jar").toString());
        assertFalse(result);

        result = IVYArtifactRecognizer.isSnapshot(
                new File("/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8").toString());
        assertFalse(result);

        result = IVYArtifactRecognizer.isSnapshot("auth-module-0.8");
        assertFalse(result);

        result = IVYArtifactRecognizer.isSnapshot("auth-module-0.8.1-123123");
        assertFalse(result);

        result = IVYArtifactRecognizer.isSnapshot("auth-module-0.8.1.23");
        assertFalse(result);

        result = IVYArtifactRecognizer.isSnapshot("auth.module.0.8.1.23");
        assertFalse(result);

        result = IVYArtifactRecognizer.isSnapshot(
                new File("/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-SNAPSHOT.jar").toString());
        assertFalse(result);
        
        logger.info("was successfully");
 
    }
    @Test
    public void testIsChecksum()
    {
        logger.info("testIsChecksum:");
        // Valid CHECKSUM FORMATS
        boolean result = IVYArtifactRecognizer.isChecksum(
                new File("/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-13482.jar.sha1").toString());
        assertTrue(result);
        
              result = IVYArtifactRecognizer.isChecksum("auth-module-0.8-13482.jar.sha1");
        assertTrue(result);

        result = IVYArtifactRecognizer.isChecksum("0.8-13482.jar.sha1");
        assertTrue(result);

        result = IVYArtifactRecognizer.isChecksum("0.8-13482.sha1");
        assertTrue(result);

        result = IVYArtifactRecognizer.isChecksum("0.8-13482.pom.sha1");
        assertTrue(result);

        result = IVYArtifactRecognizer.isChecksum("0.8-13482.zip.sha1");
        assertTrue(result);

        result = IVYArtifactRecognizer.isChecksum("auth_module-0.8-13482.sha1");
        assertTrue(result);
        
        result = IVYArtifactRecognizer.isChecksum(
                "/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-13482.pom.md5");
        assertTrue(result);
        
              result = IVYArtifactRecognizer.isChecksum("auth-module-0.8-13482.xml.md5");
        assertTrue(result);

        result = IVYArtifactRecognizer.isChecksum("0.8-13482.jar.md5");
        assertTrue(result);

        result = IVYArtifactRecognizer.isChecksum("0.8-13482.md5");
        assertTrue(result);

        result = IVYArtifactRecognizer.isChecksum("0.8-13482.pom.md5");
        assertTrue(result);

        result = IVYArtifactRecognizer.isChecksum("0.8-13482.zip.md5");
        assertTrue(result);

        result = IVYArtifactRecognizer.isChecksum("auth_module-0.8-13482.md5");
        assertTrue(result);


        // Invalid CHECKSUM FORMATS
        result = IVYArtifactRecognizer.isChecksum(
                new File("/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-13482.1jar.1sha1").toString());
        assertFalse(result);

        result = IVYArtifactRecognizer.isChecksum(
                "/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8.13482.pom.SHA");
        assertFalse(result);

        result = IVYArtifactRecognizer.isChecksum(
                "/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8.md4");
        assertFalse(result);

        result = IVYArtifactRecognizer.isChecksum("auth-module-0.8");
        assertFalse(result);

        result = IVYArtifactRecognizer.isChecksum("auth-module-0.8.1-123123.sha2");
        assertFalse(result);

        result = IVYArtifactRecognizer.isChecksum("auth-module-0.8.1.23.Sha1");
        assertFalse(result);

        result = IVYArtifactRecognizer.isChecksum("auth.module.0.8.1.23.sHa1");
        assertFalse(result);

        result = IVYArtifactRecognizer.isChecksum(
                new File("/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-SNAPSHOT.xml.shA1").toString());
        assertFalse(result);
        
        result = IVYArtifactRecognizer.isChecksum(
                new File("/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-13482.1jar.1md5").toString());
        assertFalse(result);

        result = IVYArtifactRecognizer.isChecksum(
                "/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8.13482.jar.nd5");
        assertFalse(result);

        result = IVYArtifactRecognizer.isChecksum(
                "/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8.md5.txt");
        assertFalse(result);

        result = IVYArtifactRecognizer.isChecksum("auth-module-0.8.mdl");
        assertFalse(result);

        result = IVYArtifactRecognizer.isChecksum("auth-module-0.8.1-123123.sha2");
        assertFalse(result);

        result = IVYArtifactRecognizer.isChecksum("auth-module-0.8.1.23.md");
        assertFalse(result);

        result = IVYArtifactRecognizer.isChecksum(
                new File("/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-SNAPSHOT.jar").toString());
        assertFalse(result);
    
        logger.info("was successfully");
    }
    @Test
    public void testIsPom()
    {
        logger.info("testIsPom:");
        // Valid POM FORMATS
        boolean result = IVYArtifactRecognizer.isPom(
                new File("/com/bosch/tmp/auth-api/0.8-13482/auth-api-0.8-13482.pom").toString());
        assertTrue(result);
        
              result = IVYArtifactRecognizer.isPom("auth-module-0.8-13482.pom");
        assertTrue(result);

        result = IVYArtifactRecognizer.isPom("0.8-13482.pom");
        assertTrue(result);

        result = IVYArtifactRecognizer.isPom(new File("0.8-13482.pom").toString());
        assertTrue(result);

        result = IVYArtifactRecognizer.isPom("0.8-12.pom");
        assertTrue(result);

        result = IVYArtifactRecognizer.isPom("0.8-123.pom");
        assertTrue(result);

        result = IVYArtifactRecognizer.isPom("auth_module-0.8-13482.pom");
        assertTrue(result);
        
        result = IVYArtifactRecognizer.isPom(
                "/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-13482.pom");
        assertTrue(result);
        
              result = IVYArtifactRecognizer.isPom("auth-api-0.8-13482.pom");
        assertTrue(result);

        result = IVYArtifactRecognizer.isPom("0.8-134.pom");
        assertTrue(result);

        result = IVYArtifactRecognizer.isPom("0.8-13.pom");
        assertTrue(result);

        result = IVYArtifactRecognizer.isPom("0.8-1.pom");
        assertTrue(result);

        result = IVYArtifactRecognizer.isPom("0.8-1348.pom");
        assertTrue(result);

        result = IVYArtifactRecognizer.isPom("auth_module-0.8-13482.pom");
        assertTrue(result);


        // Invalid POM FORMATS
        result = IVYArtifactRecognizer.isPom(
                new File("/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-13482.1pom").toString());
        assertFalse(result);

        result = IVYArtifactRecognizer.isPom(
                "/com/bosch/tmp/auth-api/0.8-13482/auth-api-0.8.13482.pim");
        assertFalse(result);

        result = IVYArtifactRecognizer.isPom(
                "/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8.par");
        assertFalse(result);

        result = IVYArtifactRecognizer.isPom("auth-module-0.8");
        assertFalse(result);

        result = IVYArtifactRecognizer.isPom("auth-module-0.8.1-123123.part");
        assertFalse(result);

        result = IVYArtifactRecognizer.isPom("auth-module-0.8.1.23.pom.xml");
        assertFalse(result);

        result = IVYArtifactRecognizer.isPom("auth.module.0.8.1.23");
        assertFalse(result);

        result = IVYArtifactRecognizer.isPom(
                new File("/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-SNAPSHOT.jar").toString());
        assertFalse(result);
        
        result = IVYArtifactRecognizer.isPom(
                new File("/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-13482.pm").toString());
        assertFalse(result);

        result = IVYArtifactRecognizer.isPom(
                "/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8.13482.po");
        assertFalse(result);

        logger.info("was successfully");
    }
    
    @Test
    public void testIsMetadata()
    {
        logger.info("testIsMetadata:");
        //Valid METADATA FORMATS
        boolean result = IVYArtifactRecognizer.isMetadata(
                new File("/com/bosch/tmp/auth-module/0.8-13482/maven-metadata.xml").toString());
        assertTrue(result);
        
        result = IVYArtifactRecognizer.isMetadata(
                "/com/bosch/tmp/auth-module/0.8-13482/maven-metadata.xml");
        assertTrue(result);
        
        result = IVYArtifactRecognizer.isMetadata(
                new File("/com/bosch/tmp/auth-module/0.8-13482/maven-metadata.xml.sha1").toString());
        assertTrue(result);
        
        result = IVYArtifactRecognizer.isMetadata(
                "/com/bosch/tmp/auth-module/0.8-13482/maven-metadata.xml.sha1");
        assertTrue(result);
        
        result = IVYArtifactRecognizer.isMetadata(
                new File("/com/bosch/tmp/auth-module/0.8-13482/maven-metadata.xml.md5").toString());
        assertTrue(result);
        
        result = IVYArtifactRecognizer.isMetadata(
                "/com/bosch/tmp/auth-module/0.8-13482/maven-metadata.xml.md5");
        assertTrue(result);
       
        //Invalid METADATA FORMATS
        
        result = IVYArtifactRecognizer.isMetadata(
                new File("/com/bosch/tmp/auth-module/0.8-13482/archetype-catalog.xml").toString());
        assertFalse(result);
        
        result = IVYArtifactRecognizer.isMetadata(
                "/com/bosch/tmp/auth-module/0.8-13482/archetype-catalog.xml");
        assertFalse(result);
        
        result = IVYArtifactRecognizer.isMetadata(
                new File("/com/bosch/tmp/auth-module/0.8-13482/archetype-catalog.xml.sha1").toString());
        assertFalse(result);
        
        result = IVYArtifactRecognizer.isMetadata(
                "/com/bosch/tmp/auth-module/0.8-13482/archetype-catalog.xml.sha1");
        assertFalse(result);
        
        result = IVYArtifactRecognizer.isMetadata(
                new File("/com/bosch/tmp/auth-module/0.8-13482/maven-metAdata.xml.md5").toString());
        assertFalse(result);
        
        result = IVYArtifactRecognizer.isMetadata(
                "/com/bosch/tmp/auth-module/0.8-13482/maven-metadata.xml.md4");
        assertFalse(result);
                  
        logger.info("was successfully");
    }
    
    @Test
    public void testIsSignature()
    {
        logger.info("testIsSignature:");
        //Valid SIGNATURES
        boolean result = IVYArtifactRecognizer.isSignature(
                new File("/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-123.asc").toString());
        assertTrue(result);
        
        result = IVYArtifactRecognizer.isSignature(
                "/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-123.asc");
        assertTrue(result);
        
        result = IVYArtifactRecognizer.isSignature(
                "auth-module-0.8-123.asc");
        assertTrue(result);
        
        result = IVYArtifactRecognizer.isSignature(
                "/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-123.asc");
        assertTrue(result);
        
        result = IVYArtifactRecognizer.isSignature(
                "0.8-123.asc");
        assertTrue(result);
        
        result = IVYArtifactRecognizer.isSignature(
                "0.8-12323423.asc");
        assertTrue(result);
        
        
        //Invalid SIGNATURES
        
        result = IVYArtifactRecognizer.isSignature(
                new File("/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-123.asC").toString());
        assertFalse(result);
        
        result = IVYArtifactRecognizer.isSignature(
                "/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-123.ascx");
        assertFalse(result);
        
        result = IVYArtifactRecognizer.isSignature(
                "auth-module-0.8-123.ase");
        assertFalse(result);
        
        result = IVYArtifactRecognizer.isSignature(
                "/com/bosch/tmp/auth-module/0.8-13482/auth-module-0.8-123.asl");
        assertFalse(result);
        
        result = IVYArtifactRecognizer.isSignature(
                "0.8-123asc");
        assertFalse(result);
        
        result = IVYArtifactRecognizer.isSignature(
                "0.8-12323423-asc");
        assertFalse(result);
        
        logger.info("was successfully");
    }
}
