package com.bosch.tmp.nexus.ivy.repository.util;

import org.sonatype.nexus.proxy.maven.gav.Gav;

/**
 * IVY-GAV Model.
 *
 * @author wa20277
 */
public class IVYGav extends Gav
{

    private final String groupId;

    private final String artifactId;

    private final String version;

    private final String baseVersion;

    private final String classifier;

    private final String extension;

    private final Integer snapshotBuildNumber;

    private final Long snapshotTimeStamp;

    private final String name;

    private final boolean snapshot;

    private final boolean hash;

    private final HashType hashType;

    private final boolean signature;

    private final SignatureType signatureType;
    
    /** Constructor with parameters. */
    public IVYGav(String groupId, String artifactId, String version)
    {
        this(groupId, artifactId, version, null, null, null, null, null, false, null, false, null);
    }

    /** @deprecated */
    public IVYGav(String groupId, String artifactId, String version, String classifier, String extension,
            Integer snapshotBuildNumber, Long snapshotTimeStamp, String name, boolean snapshot, boolean hash,
            HashType hashType, boolean signature, SignatureType signatureType)
    {
        this(groupId, artifactId, version, classifier, extension, snapshotBuildNumber, snapshotTimeStamp, name, hash,
                hashType, signature, signatureType);
    }
    
    /** 
     * Second constructor with all parameters parent class call 
     * proves also for IVY snapshots. */ 
    public IVYGav(String groupId, String artifactId, String version, String classifier, String extension,
            Integer snapshotBuildNumber, Long snapshotTimeStamp, String name, boolean hash, HashType hashType,
            boolean signature, SignatureType signatureType)
    {

        super(groupId, artifactId, version, classifier, extension, snapshotBuildNumber, snapshotTimeStamp, name, hash,
                hashType, signature, signatureType);

        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;

        if (IVYArtifactRecognizer.isSnapshot(version))
        {
            this.baseVersion = version;
            this.snapshot = true;
        }
        else
        {
            this.snapshot = false;
            this.baseVersion = null;
        }
        
        this.classifier = classifier;
        this.extension = extension;
        this.snapshotBuildNumber = snapshotBuildNumber;
        this.snapshotTimeStamp = snapshotTimeStamp;
        this.name = name;
        this.hash = hash;
        this.hashType = hashType;
        this.signature = signature;
        this.signatureType = signatureType;
    }

    @Override
    public String getGroupId()
    {
        return groupId;
    }

    @Override
    public String getArtifactId()
    {
        return artifactId;
    }

    @Override
    public String getVersion()
    {
        return version;
    }

    @Override
    public String getBaseVersion()
    {
        if (baseVersion == null)
        {
            return getVersion();
        }

        return baseVersion;
    }

    @Override
    public String getClassifier()
    {
        return classifier;
    }

    @Override
    public String getExtension()
    {
        return extension;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean isSnapshot()
    {
        return snapshot;
    }

    @Override
    public Integer getSnapshotBuildNumber()
    {
        return snapshotBuildNumber;
    }

    @Override
    public Long getSnapshotTimeStamp()
    {
        return snapshotTimeStamp;
    }

    @Override
    public boolean isHash()
    {
        return hash;
    }

    @Override
    public HashType getHashType()
    {
        return hashType;
    }

    @Override
    public boolean isSignature()
    {
        return signature;
    }

    @Override
    public SignatureType getSignatureType()
    {
        return signatureType;
    }

    @Override
    public int hashCode()
    {
        int result = 1;
        result = 31 * result + (groupId == null ? 0 : groupId.hashCode());
        result = 31 * result + (artifactId == null ? 0 : artifactId.hashCode());
        result = 31 * result + (version == null ? 0 : version.hashCode());
        result = 31 * result + (baseVersion == null ? 0 : baseVersion.hashCode());
        result = 31 * result + (classifier == null ? 0 : classifier.hashCode());
        result = 31 * result + (extension == null ? 0 : extension.hashCode());
        result = 31 * result + (name == null ? 0 : name.hashCode());
        result = 31 * result + (snapshot ? 1231 : 1237);
        result = 31 * result + (snapshotBuildNumber == null ? 0 : snapshotBuildNumber.hashCode());
        result = 31 * result + (snapshotTimeStamp == null ? 0 : snapshotTimeStamp.hashCode());
        result = 31 * result + (hash ? 1231 : 1237);
        result = 31 * result + (hashType == null ? 0 : hashType.hashCode());
        result = 31 * result + (signature ? 1231 : 1237);
        result = 31 * result + (signatureType == null ? 0 : signatureType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }

        IVYGav other = (IVYGav) obj;

        if (groupId == null)
        {
            if (other.groupId != null)
            {
                return false;
            }
        }
        else if (!groupId.equals(other.groupId))
        {
            return false;
        }

        if (artifactId == null)
        {
            if (other.artifactId != null)
            {
                return false;
            }
        }
        else if (!artifactId.equals(other.artifactId))
        {
            return false;
        }

        if (version == null)
        {
            if (other.version != null)
            {
                return false;
            }
        }
        else if (!version.equals(other.version))
        {
            return false;
        }

        if (baseVersion == null)
        {
            if (other.baseVersion != null)
            {
                return false;
            }
        }
        else if (!baseVersion.equals(other.baseVersion))
        {
            return false;
        }

        if (classifier == null)
        {
            if (other.classifier != null)
            {
                return false;
            }
        }
        else if (!classifier.equals(other.classifier))
        {
            return false;
        }

        if (extension == null)
        {
            if (other.extension != null)
            {
                return false;
            }
        }
        else if (!extension.equals(other.extension))
        {
            return false;
        }

        if (name == null)
        {
            if (other.name != null)
            {
                return false;
            }
        }
        else if (!name.equals(other.name))
        {
            return false;
        }

        if (snapshot != other.snapshot)
        {
            return false;
        }

        if (snapshotBuildNumber == null)
        {
            if (other.snapshotBuildNumber != null)
            {
                return false;
            }
        }
        else if (!snapshotBuildNumber.equals(other.snapshotBuildNumber))
        {
            return false;
        }

        if (snapshotTimeStamp == null)
        {
            if (other.snapshotTimeStamp != null)
            {
                return false;
            }
        }
        else if (!snapshotTimeStamp.equals(other.snapshotTimeStamp))
        {
            return false;
        }

        if (hash != other.hash)
        {
            return false;
        }

        if (hashType == null)
        {
            if (other.hashType != null)
            {
                return false;
            }
        }
        else if (!hashType.equals(other.hashType))
        {
            return false;
        }

        if (signature != other.signature)
        {
            return false;
        }

        if (signatureType == null)
        {
            if (other.signatureType != null)
            {
                return false;
            }
        }
        else if (!signatureType.equals(other.signatureType))
        {
            return false;
        }

        return true;
    }

}
