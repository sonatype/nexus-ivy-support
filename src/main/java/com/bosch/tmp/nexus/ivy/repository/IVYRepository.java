package com.bosch.tmp.nexus.ivy.repository;

import com.bosch.tmp.nexus.ivy.repository.util.IVYArtifactRecognizer;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;
import org.sonatype.nexus.proxy.maven.gav.Gav;
import org.sonatype.nexus.proxy.maven.gav.GavCalculator;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.Configurator;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.IllegalRequestException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.LocalStorageException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.access.Action;
import org.sonatype.nexus.proxy.item.AbstractStorageItem;
import org.sonatype.nexus.proxy.item.ByteArrayContentLocator;
import org.sonatype.nexus.proxy.item.ContentLocator;
import org.sonatype.nexus.proxy.item.DefaultStorageFileItem;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.maven.metadata.operations.ModelVersionUtility;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.util.AlphanumComparator;
import org.sonatype.nexus.proxy.maven.metadata.operations.MetadataBuilder;
import org.sonatype.nexus.util.DigesterUtils;

/**
 * Class which define the layout of the TMP-IVY repository. The layout is based on the Maven2 default format
 * with a small change for the snapshot format because IVY doesn't support unique snapshot based on a timestamp.
 *
 * @author wa20277
 */
@Component(
    role = Repository.class, hint = "ivy", instantiationStrategy = "per-lookup", description = "TMP-IVY Repository")
public class IVYRepository extends AbstractMavenRepository implements IVYHostedRepository
{
    /**
     * Id to identify kind of repository.
     */
    public static final String ID = "ivy";

    @Requirement(hint = "ivy")
    private GavCalculator gavCalculator;

    @Requirement(hint = "maven2")
    private ContentClass contentClass;

    @Requirement
    private IVYRepositoryConfigurator ivyRepositoryConfigurator;
     
    /**
     * @param request delivers the path if a item is local and remote
     * @return returns a policy (snapshot, release, mixed or null)
     */
    @Override
    public boolean shouldServeByPolicies(ResourceStoreRequest request)
    {
        if (IVYArtifactRecognizer.isMetadata(request.getRequestPath()))
        {
            if (IVYArtifactRecognizer.isSnapshot(request.getRequestPath()))
            {
                return RepositoryPolicy.SNAPSHOT.equals(getRepositoryPolicy());
            }

            return true;
        }

        Gav gav = getGavCalculator().pathToGav(request.getRequestPath());

        if (gav == null)
        {
            return true;
        }

        if (gav.isSnapshot())
        {
            return RepositoryPolicy.SNAPSHOT.equals(getRepositoryPolicy());
        }

        return RepositoryPolicy.RELEASE.equals(getRepositoryPolicy());
    }
    /**
     * @return returns the configurator of a repository
     */
    @Override
    protected Configurator getConfigurator()
    {
        return this.ivyRepositoryConfigurator;
    }
    /**
     * 
     * @param forWrite true if the configuration is writeable
     * @return returns external configuration for write
     */
    @Override
    protected M2RepositoryConfiguration getExternalConfiguration(boolean forWrite)
    {
        return (M2RepositoryConfiguration) super.getExternalConfiguration(forWrite);
    }
    
    /**
     * 
     * @return for repository external configurations 
     * Configuration object is responsible to specifying 
     * which LoginModules should be used for a particular application, 
     * and in what order the LoginModules should be invoked.
     * 
     */
    @Override
    protected CRepositoryExternalConfigurationHolderFactory<?> getExternalConfigurationHolderFactory()
    {
        return new CRepositoryExternalConfigurationHolderFactory()
        {

            @Override
            public M2RepositoryConfiguration createExternalConfigurationHolder(CRepository config)
            {
                return new M2RepositoryConfiguration((Xpp3Dom) config.getExternalConfiguration());
            }
        };
    }
    /**
     * 
     * @return returns the Content Class for given repository
     */
    @Override
    public ContentClass getRepositoryContentClass()
    {
        return this.contentClass;
    }
    /**
     * @return returns the gavCalculator for repository
     */
    @Override
    public GavCalculator getGavCalculator()
    {
        return this.gavCalculator;
    }
    /**
     * 
     * @param request delivers the path if a item is local and remote
     * @param action represents the valid "actions" against Nexus path
     * @throws IllegalRequestException 
     */
    
    @Override
    protected void enforceWritePolicy(ResourceStoreRequest request, Action action) throws IllegalRequestException
    {
        if ((!IVYArtifactRecognizer.isMetadata(request.getRequestPath()))
                && (!IVYArtifactRecognizer.isSnapshot(request.getRequestPath())))
        {
            super.enforceWritePolicy(request, action);
        }
    }
    /**
     * 
     * @param item is a path of the artifact
     * @return returns the maxAge of artifacts deployed in Nexus
     */
    @Override
    protected boolean isOld(StorageItem item)
    {
        if (IVYArtifactRecognizer.isMetadata(item.getPath()))
        {
            return isOld(getMetadataMaxAge(), item);
        }
        if (IVYArtifactRecognizer.isSnapshot(item.getPath()))
        {
            return isOld(getArtifactMaxAge(), item);
        }

        if (null == getGavCalculator().pathToGav(item.getPath()))
        {
            return super.isOld(item);
        }

        return isOld(getArtifactMaxAge(), item);
    }
    /** 
     * @param versions list of all versions available
     * @return returns the latest version of artifacts */
    public String getLatestVersion(List<String> versions)
    {
        Collections.sort(versions, new AlphanumComparator());
        return (String) versions.get(versions.size() - 1);
    }

    /**
     * 
     * @param snapshot delivers true if it is a snapshot
     * @param metadata delivers Metadata of artifacts
     * @return Meta Data for repository
     */
    protected Metadata cleanseMetadataForRepository(boolean snapshot, Metadata metadata)
    {
        List versions = metadata.getVersioning().getVersions();
        for (Iterator iversion = versions.iterator(); iversion.hasNext(); )
        {
          if (((!snapshot) || (IVYArtifactRecognizer.isSnapshot((String)iversion.next())))
                  && ((snapshot) || (!IVYArtifactRecognizer.isSnapshot((String)iversion.next()))))
          {
            continue;
          }
          iversion.remove();
        }

        metadata.getVersioning().setLatest(getLatestVersion(metadata.getVersioning().getVersions()));
        if (snapshot)
        {
          metadata.getVersioning().setRelease(null);
        }
        else
        {
          metadata.getVersioning().setRelease(metadata.getVersioning().getLatest());
        }
        return metadata;
    }

    /**
     * 
     * @param path delivers the path of artifacts
     * @return returns true if the path is Metadata
     */
    @Override
    public boolean isMavenMetadataPath(String path)
    {
        return IVYArtifactRecognizer.isMetadata(path);
    }
    
    /**
     * 
     * @return returns true if a repository is set to indexable
     */
    @Override
    public boolean isIndexable()
    {
        return true;
    }
    
    /**
     * 
     * @param path delivers the infos of artifacts
     * @return returns a storage item
     */
    @Override
    public AbstractStorageItem doCacheItem(AbstractStorageItem item)
    throws LocalStorageException
  {
  
    if ((isCleanseRepositoryMetadata()) 
            && ((item instanceof StorageFileItem)) 
            && (IVYArtifactRecognizer.isMetadata(item.getPath())))
    {
      InputStream orig = null;
      StorageFileItem mdFile = (StorageFileItem)item;
      ByteArrayInputStream backup = null;
      ByteArrayOutputStream backup1 = new ByteArrayOutputStream();
      try
      {
        try
        {
          orig = mdFile.getInputStream();
          IOUtil.copy(orig, backup1);
        }
        finally
        {
          IOUtil.close(orig);
        }
        backup = new ByteArrayInputStream(backup1.toByteArray());

        MetadataXpp3Reader metadataReader = new MetadataXpp3Reader();
        InputStreamReader isr = new InputStreamReader(backup);
        Metadata imd = metadataReader.read(isr);

        imd = cleanseMetadataForRepository(RepositoryPolicy.SNAPSHOT.equals(getRepositoryPolicy()), imd);

        MetadataXpp3Writer metadataWriter = new MetadataXpp3Writer();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(bos);
        metadataWriter.write(osw, imd);
        mdFile.setContentLocator(new ByteArrayContentLocator(bos.toByteArray(), mdFile.getMimeType()));
      }
      catch (Exception e)
      {
        getLogger().error("Exception during repository metadata cleansing.", e);

        if (backup != null)
        {
          backup.reset();
          mdFile.setContentLocator(new ByteArrayContentLocator(backup1.toByteArray(), mdFile.getMimeType()));
        }
      }
    }
    return super.doCacheItem(item);
    }
    
    /**
     *
     * @param request asks for items in storage
     * @return returns a storage item
     */
    @Override
    protected StorageItem doRetrieveItem(ResourceStoreRequest request)
    throws IllegalOperationException, ItemNotFoundException, StorageException
    {
      
    boolean remoteCall = request.getRequestContext().containsKey("request.address");
    String userAgent = (String)request.getRequestContext().get("request.agent");

    
    if ((remoteCall) && (null != userAgent))
    {
      ModelVersionUtility.Version userSupportedVersion = getClientSupportedVersion(userAgent);

      if ((IVYArtifactRecognizer.isMetadata(request.getRequestPath())) 
              && (userSupportedVersion != null) 
              && (!ModelVersionUtility.LATEST_MODEL_VERSION.equals(userSupportedVersion)))
      {
        StorageFileItem mdItem;
        
        if (IVYArtifactRecognizer.isChecksum(request.getRequestPath()))
        {
          String path = request.getRequestPath();
          if (request.getRequestPath().endsWith(".md5"))
          {
            path = path.substring(0, path.length() - 4);
          }
          else if (request.getRequestPath().endsWith(".sha1"))
          {
            path = path.substring(0, path.length() - 5);
          }

          ResourceStoreRequest mdRequest = new ResourceStoreRequest(path, 
                  request.isRequestLocalOnly(), request.isRequestRemoteOnly());

          mdRequest.getRequestContext().setParentContext(request.getRequestContext());

          mdItem = (StorageFileItem)super.retrieveItem(false, mdRequest);
        }
        else
        {
          mdItem = (StorageFileItem)super.doRetrieveItem(request);
        }

        try {
          InputStream inputStream = null;
          Metadata metadata;
          try {
            inputStream = mdItem.getInputStream();
            metadata = MetadataBuilder.read(inputStream);
          }
          finally
          {
            IOUtil.close(inputStream);
          }

          ModelVersionUtility.Version requiredVersion = getClientSupportedVersion(userAgent);
          ModelVersionUtility.Version metadataVersion = ModelVersionUtility.getModelVersion(metadata);

          if ((requiredVersion == null) || (requiredVersion.equals(metadataVersion)))
          {
            return super.doRetrieveItem(request);
          }

          ModelVersionUtility.setModelVersion(metadata, requiredVersion);

          ByteArrayOutputStream mdOutput = new ByteArrayOutputStream();

          MetadataBuilder.write(metadata, mdOutput);
          byte[] content;
          if (IVYArtifactRecognizer.isChecksum(request.getRequestPath()))
          {
            
            String digest;
            if (request.getRequestPath().endsWith(".md5"))
            {
              digest = DigesterUtils.getMd5Digest(mdOutput.toByteArray());
            }
            else
            {
              digest = DigesterUtils.getSha1Digest(mdOutput.toByteArray());
            }
            content = (digest + '\n').getBytes("UTF-8");
          }
          else
          {
            content = mdOutput.toByteArray();
          }

          String mimeType = getMimeSupport().guessMimeTypeFromPath(getMimeRulesSource()
                                                        , request.getRequestPath());

          ContentLocator contentLocator = new ByteArrayContentLocator(content, mimeType);

          DefaultStorageFileItem result = new DefaultStorageFileItem(this, 
                                                                     request, 
                                                                     true, 
                                                                     false, 
                                                                     contentLocator);

          result.setLength(content.length);
          result.setCreated(mdItem.getCreated());
          result.setModified(System.currentTimeMillis());
          return result;
        }
        catch (IOException e)
        {
          if (getLogger().isDebugEnabled())
          {
            getLogger().error("Error parsing metadata, serving as retrieved", e);
          }
          else
          {
            getLogger().error("Error parsing metadata, serving as retrieved: " + e.getMessage());
          }

          return super.doRetrieveItem(request);
        }
      }
    }

    return super.doRetrieveItem(request);
  }

  /**
     * 
     * @param delivers the user agent like Ivy, Maven, or Java
     * @return Handling model version of Maven repository metadata
     */
  protected ModelVersionUtility.Version getClientSupportedVersion(String userAgent)
  {
    if (userAgent == null)
    {
      return null;
    }

    if (userAgent.startsWith("Apache Ivy"))
    {
      return ModelVersionUtility.Version.V100;
    }

    if (userAgent.startsWith("Java"))
    {
      return ModelVersionUtility.Version.V100;
    }

    if (userAgent.startsWith("Apache-Maven/2"))
    {
      return ModelVersionUtility.Version.V100;
    }

    return ModelVersionUtility.Version.V110;
  }
}

