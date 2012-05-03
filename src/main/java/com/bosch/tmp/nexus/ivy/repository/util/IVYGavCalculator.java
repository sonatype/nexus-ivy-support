package com.bosch.tmp.nexus.ivy.repository.util;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.maven.gav.GavCalculator;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;

/**
 * Class to provide the project coordinates (GAV - GROUP, ARTIFACT-ID, VERSION).
 *
 * @author wa20277
 */
@Component(role = GavCalculator.class, hint = "ivy")
public class IVYGavCalculator implements GavCalculator 
{

    @Requirement
    private Logger logger;
    /**
     * 
     * @param str is the path of the artifact
     * @return returns null if the path is "/" or Metadata
     * delivers the string to getSnapshotGav, or getReleaseGav and
     * cuts the endings if it is a checksum or signature
     * and returns a Gav to given path
     */
    @Override
    public Gav pathToGav(String str)
    {
        try
        {
            String s = str.startsWith("/") ? str.substring(1) : str;

            int vEndPos = s.lastIndexOf('/');

            if (vEndPos == -1)
            {
                return null;
            }

            int aEndPos = s.lastIndexOf('/', vEndPos - 1);

            if (aEndPos == -1)
            {
                return null;
            }

            int gEndPos = s.lastIndexOf('/', aEndPos - 1);

            if (gEndPos == -1)
            {
                return null;
            }

            String groupId = s.substring(0, gEndPos).replace('/', '.');
            String artifactId = s.substring(gEndPos + 1, aEndPos);
            String version = s.substring(aEndPos + 1, vEndPos);
            String fileName = s.substring(vEndPos + 1);

            boolean checksum = false;
            boolean signature = false;
            Gav.HashType checksumType = null;
            Gav.SignatureType signatureType = null;
            if (IVYArtifactRecognizer.isChecksum(s) && s.endsWith(".md5"))
            {
                checksum = true;
                checksumType = Gav.HashType.md5;
                s = s.substring(0, s.length() - 4);
            }
            else if (IVYArtifactRecognizer.isChecksum(s) && s.endsWith(".sha1"))
            {
                checksum = true;
                checksumType = Gav.HashType.sha1;
                s = s.substring(0, s.length() - 5);
            }

            if (IVYArtifactRecognizer.isSignature(s))
            {
                signature = true;
                signatureType = Gav.SignatureType.gpg;
                s = s.substring(0, s.length() - 4);
            }

            if (IVYArtifactRecognizer.isMetadata(s))
            {
                return null;
            }

            if (IVYArtifactRecognizer.isSnapshot(version))
            {
                return getSnapshotGav(s, vEndPos, groupId, artifactId, version, fileName, checksum, signature,
                        checksumType, signatureType);
            }

            return getReleaseGav(s, vEndPos, groupId, artifactId, version, fileName, checksum,
                    signature, checksumType, signatureType);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
        catch (StringIndexOutOfBoundsException e)
        {
            this.logger.debug("Can not determine GAV", e);
        }
        return null;
    }
    /**
     * 
     * @param s path of item
     * @param vEndPos Integer endposition of the string
     * @param groupId like "folder.folder2"
     * @param artifactId like "a-artifact"
     * @param version like "1.0-1234"
     * @param fileName like "a-artifact-1.0-1234"
     * @param checksum true or false
     * @param signature true or false
     * @param checksumType ".md5 or .sha1"
     * @param signatureType ".asc"
     * @return returns a new IVYGav with given parameters
     */
    private Gav getReleaseGav(String s, int vEndPos, String groupId, String artifactId, String version, 
            String fileName, boolean checksum, boolean signature, Gav.HashType checksumType,
            Gav.SignatureType signatureType)
    {
        if ((!fileName.startsWith(artifactId + "-" + version + ".")) && (!fileName.startsWith(
                artifactId + "-" + version + "-")))
        {
            return null;
        }

        int nTailPos = vEndPos + artifactId.length() + version.length() + 2;

        String tail = s.substring(nTailPos);

        int nExtPos = tail.indexOf('.');

        if (nExtPos == -1)
        {
            return null;
        }

        String ext = tail.substring(nExtPos + 1);
        String classifier = tail.charAt(0) == '-' ? tail.substring(1, nExtPos) : null;

        return new IVYGav(groupId, artifactId, version, classifier, ext, null, null, fileName, checksum, checksumType,
                signature, signatureType);
    }
    /**
     * 
     * @param path of item
     * @param vEndPos Integer endposition of the string
     * @param groupId like "folder.folder2"
     * @param artifactId like "a-artifact"
     * @param version like "1.0-1234"
     * @param fileName like "a-artifact-1.0-1234"
     * @param checksum true or false
     * @param signature true or false
     * @param checksumType ".md5 or .sha1"
     * @param signatureType ".asc"
     * @return returns a new IVYGav with given parameters
     */
    private Gav getSnapshotGav(String path, int vEndPos, String groupId, String artifactId, String version, 
            String fileName, boolean checksum, boolean signature, Gav.HashType checksumType,
            Gav.SignatureType signatureType)
    {
        Integer snapshotBuildNo = null;
        Long snapshotTimestamp = null;
        String classifier = null;
        String ext = null;

        if (IVYArtifactRecognizer.isSnapshot(version))
        {
            snapshotBuildNo = Integer.valueOf(version.substring(version.lastIndexOf("-") + 1));
            int nTailPos = vEndPos + artifactId.length() + version.length() + 2;
            if (nTailPos >= path.length())
            {
                this.logger.error(
                        "Error can not determine nTailPos based on " + artifactId + ", " + version + ", " + vEndPos);
            }
            String tail = path.substring(nTailPos);
            int nExtPos = tail.indexOf('.');

            if (nExtPos == -1)
            {
                return null;
            }

            ext = tail.substring(nExtPos + 1);
            classifier = tail.charAt(0) == '-' ? tail.substring(1, nExtPos) : null;
        }
        
        return new IVYGav(groupId, artifactId, version, classifier, ext, snapshotBuildNo, snapshotTimestamp, fileName,
                checksum, checksumType, signature, signatureType);
    }
    /**
     * 
     * @param gav delivers a given Gav
     * @return returns a path to given Gav
     */
    @Override
    public String gavToPath(Gav gav)
    {
        StringBuffer path = new StringBuffer("/");
        path.append(gav.getGroupId().replaceAll("(?m)(.)\\.", "$1/"));
        path.append("/");
        path.append(gav.getArtifactId());
        path.append("/");
        path.append(gav.getBaseVersion());
        path.append("/");
        path.append(calculateArtifactName(gav));

        return path.toString();
    }
    
    /** 
     * @param gav delivers a given Gav
     * @return returns the path of artifact in the right syntax*/
    public String calculateArtifactName(Gav gav)
    {
        if ((gav.getName() != null) && (gav.getName().trim().length() > 0))
        {
            return gav.getName();
        }

        StringBuffer path = new StringBuffer(gav.getArtifactId());
        path.append("-");
        path.append(gav.getVersion());

        if ((gav.getClassifier() != null) && (gav.getClassifier().trim().length() > 0))
        {
            path.append("-");
            path.append(gav.getClassifier());
        }

        if (gav.getExtension() != null)
        {
            path.append(".");
            path.append(gav.getExtension());
        }

        if (gav.isSignature())
        {
            path.append(".");
            path.append(gav.getSignatureType().toString());
        }

        if (gav.isHash())
        {
            path.append(".");
            path.append(gav.getHashType().toString());
        }

        return path.toString();
    }
}
