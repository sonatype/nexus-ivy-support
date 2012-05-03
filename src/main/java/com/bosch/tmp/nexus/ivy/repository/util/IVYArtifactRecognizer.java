package com.bosch.tmp.nexus.ivy.repository.util;

import java.io.File;
import java.util.regex.Pattern;

/**
 * Helper class to identify different types of data.
 *
 * @author wa20277
 */
public class IVYArtifactRecognizer
{

    /**
     * Pattern to identify a SNAPSHOT.
     */
    private static final Pattern SNAPSHOT_PATTERN
            
            = Pattern.compile("[a-zA-Z-_&&[^\\.]]*(\\d+\\.\\d+-\\d+(\\.[a-z]*)?)$", Pattern.CASE_INSENSITIVE);
    
    /**
     * Pattern to identify a SNAPSHOT Folder.
     * ".*[0-9.]*+-+[0-9]*$"
     * ""
     */
    private static final Pattern SNAPSHOT_OR_GROUP_FOLDER_PATTERN 
            = Pattern.compile("^[a-z-./][a-zA-Z0-9-./]*$", Pattern.CASE_INSENSITIVE);
    
    /**
     * Pattern to identify a SNAPSHOT Folder.
     * 
     * 
     */
    private static final Pattern SNAPSHOT_FOLDER_PATTERN 
            = Pattern.compile(".*[0-9.]*+-+[0-9]*$", Pattern.CASE_INSENSITIVE);
    
    /** 
     * @param path of the artifact
     * @return returns true if path is declared as a checksum */
    public static boolean isChecksum(String path)
    {
        return (path.endsWith(".sha1")) || (path.endsWith(".md5"));
    }
    
    /** 
     * @param path of the artifact
     * @return returns true if path is declared as a IVY.pom file or checksum */
    public static boolean isPom(String path)
    {
        return (path.endsWith(".pom")) 
                || (path.endsWith(".pom.sha1"))
                || (path.endsWith(".pom.md5"));
    }

    /** 
     * @param path of the artifact
     * @return returns true if path is declared as a IVY snapshot 
     */
    public static boolean isSnapshot(String path)
    {
        if (path != null && path.indexOf(File.separator) >= 0)
        {
            return SNAPSHOT_PATTERN.matcher(
                    path.substring(path.lastIndexOf(File.separator)+1, path.length()-1)).matches();
        }
        else
        {
            return SNAPSHOT_PATTERN.matcher(path).matches();
        }
    }
    
    /**
     * @param path of the artifact
     * @return returns true if path is declared as a IVY snapshot or group folder 
     */
    public static boolean isSnapshotOrGroupFolder(String path)
    {
        return SNAPSHOT_OR_GROUP_FOLDER_PATTERN.matcher(path).matches();       
    }
    
    /** 
     * @param path of snapshot folders
     * @return returns true if path is declared as a IVY snapshot */
    public static boolean isSnapshotFolder(String path)
    {
        return SNAPSHOT_FOLDER_PATTERN.matcher(path).matches();       
    }
    
    /** 
     * @param path of the artifact
     * @return returns true if path is declared as maven-metadata.xml or checksum */
    public static boolean isMetadata(String path)
    {
        return (path.endsWith("maven-metadata.xml"))
                    || (path.endsWith("maven-metadata.xml.sha1"))
                    || (path.endsWith("maven-metadata.xml.md5"));
    }
    
    /** 
     * @param path of the artifact
     * @return returns true if path is declared as a signature */
    public static boolean isSignature(String path)
    {
        return path.endsWith(".asc");
    }

}
