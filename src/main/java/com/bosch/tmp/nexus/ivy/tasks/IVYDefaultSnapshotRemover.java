package com.bosch.tmp.nexus.ivy.tasks;
 
import com.bosch.tmp.nexus.ivy.repository.IVYHostedRepository;
import com.bosch.tmp.nexus.ivy.repository.IVYRepository;
import com.bosch.tmp.nexus.ivy.repository.util.IVYArtifactRecognizer;
import com.bosch.tmp.nexus.ivy.repository.util.IVYGav;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.logging.AbstractLoggingComponent;
import org.sonatype.nexus.logging.Slf4jPlexusLogger;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalRepositoryResult;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalRequest;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalResult;
import org.sonatype.nexus.proxy.IllegalOperationException;
import org.sonatype.nexus.proxy.ItemNotFoundException;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.item.StorageCollectionItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.maven.RecreateMavenMetadataWalkerProcessor;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.storage.UnsupportedStorageOperationException;
import org.sonatype.nexus.proxy.walker.AbstractWalkerProcessor;
import org.sonatype.nexus.proxy.walker.DefaultWalkerContext;
import org.sonatype.nexus.proxy.walker.DottedStoreWalkerFilter;
import org.sonatype.nexus.proxy.walker.ParentOMatic;
import org.sonatype.nexus.proxy.walker.Walker;
import org.sonatype.nexus.proxy.walker.WalkerContext;
import org.sonatype.nexus.proxy.walker.WalkerException;
import org.sonatype.nexus.proxy.wastebasket.DeleteOperation;
import org.sonatype.nexus.util.ItemPathUtils;
import org.sonatype.scheduling.TaskUtil;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;



/**
* The Class SnapshotRemoverJob. 
* After a succesful run, the job guarantees that there will remain at least
* minCountOfSnapshotsToKeep snapshots per one snapshot collection by removing all older from
* removeSnapshotsOlderThanDays. If should remove snaps if their release counterpart exists, the whole GAV will be
* removed.
* 
* @author wa21190
*/
@Component(role=IVYSnapshotRemover.class)
public class IVYDefaultSnapshotRemover extends AbstractLoggingComponent
  implements IVYSnapshotRemover
{
  @Requirement
  private RepositoryRegistry repositoryRegistry;
 
  @Requirement
  private Walker walker;
 
  @Requirement(hint="maven2")
  private ContentClass contentClass;
  private int countMinToKeep;
   
 
  /**
   * Sets Walker aWalker for class tests.
   * @param aWalker sets the walker 
   */
  public void setWalker(Walker aWalker)
  {
   walker = aWalker;
  }

  /**
   * Sets the kind of repository registry.
   * @param repository sets a repository for class tests
   */
  public void setRepositoryRegistry(RepositoryRegistry repository)
  {
      repositoryRegistry = repository;
  }
  
  /** 
   * Gives back the information about repository registry.
   * @return holds the Repositories and repo groups 
   */
  public RepositoryRegistry getRepositoryRegistry() {
    
      return this.repositoryRegistry;
  }
  
  /**
   * @param contClass  sets a contentclass for class tests
   */
  public void setContentClass(ContentClass contClass) {
        contentClass = contClass;
    }
  
  /**
   * @param request i.e.: (RepositoryId: "tmp-snapshots",minCountToKeep: 2,OlderThanDays: 0,ifReleaseExists true)
   * @return returns a result i.e:(RepositoryId: "tmp-snapshots",deletedSnapshots: 0,deletedFiles 0,isSucessful: true)
   * @throws NoSuchRepositoryException if a repository doesn't exist 
   */
  @Override
  public SnapshotRemovalResult removeSnapshots(SnapshotRemovalRequest request)
    throws NoSuchRepositoryException
  {
    SnapshotRemovalResult result = new SnapshotRemovalResult();
    
    logDetails( request );
 
    if (request.getRepositoryId() != null)
    {
      Repository repository = getRepositoryRegistry()
            .getRepository(request.getRepositoryId());
 
      if (!process(request, result, repository))
      {
        throw new IllegalArgumentException("The repository with ID=" 
            + repository.getId() + " is not valid for Snapshot Removal Task!");
      }
    }
    else
    {
      for (Repository repository : getRepositoryRegistry().getRepositories())
      {
        process(request, result, repository);
      }
    }
    return result;
  }
  
  /**
   * Process through the stored files in a repository group.
   * @param request information about chosen parameters for "RemoverTask"
   * @param result gives back the result of the process
   * @param group chosen repository
   */
  private void process(SnapshotRemovalRequest request,
                       SnapshotRemovalResult result,
                       GroupRepository group)
  {
    for (Repository repository : group.getMemberRepositories())
    {
      process(request, result, repository);
    }
  }
  
  /**
   * Process through the stored files in a repository.
   * @param request information about chosen parameters for "RemoverTask"
   * @param result gives back the result of the process
   * @param repository chosen repository
   * @return 
   */
  private boolean process(SnapshotRemovalRequest request, 
                          SnapshotRemovalResult result, 
                          Repository repository)
  {
     // only from IVY repositories, stay silent for others and simply skip
    if (!repository.getRepositoryContentClass().isCompatible(this.contentClass))
    {
      getLogger().debug("Skipping " + repository.getId() + " is not a IVY repository");
      return false;
    }
 
    if (!repository.getLocalStatus().shouldServiceRequest())
    {
      getLogger().debug("Skipping "+ repository.getId() +" the repository is out of service");
      return false;
    }
    
    if (repository.getRepositoryKind().isFacetAvailable(GroupRepository.class))
    {
      process(request, result, (GroupRepository)repository.adaptToFacet(GroupRepository.class));
    }
 
    else if (repository.getRepositoryKind().isFacetAvailable(MavenRepository.class))
    {
      result.addResult(removeSnapshotsFromMavenRepository
      ((MavenRepository)repository.adaptToFacet(MavenRepository.class), request));
    }
    
    return true;
  }
  
  /**
   * Removes the snapshots from maven repository.
   * @param request delivers the request from removeSnapshots
   * @param repository delivers the repository from removeSnapshots
   * @return returns a result like removeSnapshots
   */
  public SnapshotRemovalRepositoryResult removeSnapshotsFromMavenRepository
                   (MavenRepository repository, SnapshotRemovalRequest request)
  {
    TaskUtil.checkInterruption();
    SnapshotRemovalRepositoryResult result =
            new SnapshotRemovalRepositoryResult(repository.getId(), 0, 0, true);
 
    if (!repository.getLocalStatus().shouldServiceRequest())
    {
      return result;
    }
    
    // we are already processed here, so skip repo
    if (request.isProcessedRepo(repository.getId()))
    {
      return new SnapshotRemovalRepositoryResult(repository.getId(), true);
    }
 
    request.addProcessedRepo(repository.getId());
    // if this is not snap repo, do nothing
    if (!RepositoryPolicy.SNAPSHOT.equals(repository.getRepositoryPolicy()))
    {
      return result;
    }
 
    if (getLogger().isDebugEnabled())
    {
      getLogger().debug("Collecting deletable snapshots on repository {}" 
                 + "from storage directory {}", repository.getId(), repository.getLocalUrl());
    }
    
    ParentOMatic parentOMatic = new ParentOMatic();

    // create a walker to collect deletables and let it loose on collections only
   
          SnapshotRemoverWalkerProcessor snapshotRemoveProcessor = 
            new SnapshotRemoverWalkerProcessor(repository, request, parentOMatic); 
    
    DefaultWalkerContext ctxMain =
            new DefaultWalkerContext(repository, new ResourceStoreRequest("/"),
                                         new DottedStoreWalkerFilter());
 
    ctxMain.getContext().put(DeleteOperation.DELETE_OPERATION_CTX_KEY, getDeleteOperation(request));
    ctxMain.getProcessors().add(snapshotRemoveProcessor);
    
    this.walker.walk(ctxMain);
 
    if (ctxMain.getStopCause() != null)
    {
      result.setSuccessful(false);
    }
    
    // and collect results
    result.setDeletedSnapshots(snapshotRemoveProcessor.getDeletedSnapshots());   
    result.setDeletedFiles(snapshotRemoveProcessor.getDeletedFiles());
 
    if (getLogger().isDebugEnabled())
    {
      getLogger().debug("Collected and deleted "    
              + snapshotRemoveProcessor.getDeletedSnapshots() 
              + " snapshots with alltogether "    
              + snapshotRemoveProcessor.getDeletedFiles() 
              + " files on repository " 
              + repository.getId());
    }
    
    RecreateMavenMetadataWalkerProcessor metadataRebuildProcessor;
    if (repository.getRepositoryKind().isFacetAvailable(HostedRepository.class))
    {
      repository.expireNotFoundCaches(new ResourceStoreRequest("/"));

      metadataRebuildProcessor = 
              new RecreateMavenMetadataWalkerProcessor(
                      Slf4jPlexusLogger.getPlexusLogger(getLogger()), 
                      getDeleteOperation(request));

      for (String path : parentOMatic.getMarkedPaths())
      {
        TaskUtil.checkInterruption();

        DefaultWalkerContext ctxMd = 
                new DefaultWalkerContext(repository, 
                        new ResourceStoreRequest(path), 
                        new DottedStoreWalkerFilter());

        ctxMd.getProcessors().add(metadataRebuildProcessor);
        try
        {
          this.walker.walk(ctxMd);
        }
        catch (WalkerException e)
        {
          if (!(e.getCause() instanceof ItemNotFoundException))
          {
            throw e;
          }
        }
      }
    }
    return result;
  }
  
  /**
   * Gets the kind of Operation "move to trash" or "delete permanently".
   * @param request
   * @return setted opertaion from field
   */
  private DeleteOperation getDeleteOperation(SnapshotRemovalRequest request)
  {
    return request.isDeleteImmediately() ? DeleteOperation.DELETE_PERMANENTLY : DeleteOperation.MOVE_TO_TRASH;
  }
  
  /**
   * Shows details about chosen parameters of IVY Snapshot Remover Task
   * @param request delivers the request parameters 
   * to show detailed request in the log while starting the task
   */
  public void logDetails( SnapshotRemovalRequest request )
    {
        if ( request.getRepositoryId() != null )
        {
            getLogger().info( "Removing old SNAPSHOT deployments from " + request.getRepositoryId() + " repository." );
        }
        else
           {
            getLogger().info( "Removing old SNAPSHOT deployments from all repositories." );
           }
            if ( getLogger().isDebugEnabled() )
            {
            getLogger().debug( "With parameters: " );
            getLogger().debug( "    MinCountOfSnapshotsToKeep: " + request.getMinCountOfSnapshotsToKeep() );
            getLogger().debug( "    RemoveSnapshotsOlderThanDays: " + request.getRemoveSnapshotsOlderThanDays() );
            getLogger().debug( "    RemoveIfReleaseExists: " + request.isRemoveIfReleaseExists() );
            getLogger().debug( "    DeleteImmediately: " + request.isDeleteImmediately() );
            }

    }
  
  /**
   * Collects path of artifacts. 
   * Proofs for given field parameters and deletes the found snapshots and files.
   * 
   * @author wa21190
   */
  private class SnapshotRemoverWalkerProcessor extends AbstractWalkerProcessor
  {
    private final MavenRepository repository;
    private final SnapshotRemovalRequest request;
    private final List<StorageItem> 
                  deletableSnapshotsAndFiles = new LinkedList<StorageItem>();
    private final List<StorageCollectionItem> 
                  deletableSnapshotsCollection = new LinkedList<StorageCollectionItem>();  
    private final List<StorageCollectionItem> 
                  deletableSnapshotsCollectionAll = new LinkedList<StorageCollectionItem>();
    
    private final ParentOMatic collectionNodes;
    private boolean shouldProcessCollection;
    private boolean removeWholeGAV;
    private final long dateThreshold;
    private int deletedSnapshots = 0;
    private int deletedFiles = 0;
    private long dayInMillis = 86400000L;
    
    /**
     * Checks for units that are "attachable" to a single storage walk. 
     * The walker will stop, if any Exception is thrown 
     *
     * @param repository delivers the current repository
     * @param request delivers information about selected options
     * @param collectionNodes helper class to "optimize" trees and 
     * count of directories to start a walk from, sort of "gathering for later processing"
     */
    public SnapshotRemoverWalkerProcessor(MavenRepository repository, 
                                          SnapshotRemovalRequest request, ParentOMatic collectionNodes)
    {
      this.collectionNodes = collectionNodes; 
      this.repository = repository;
      this.request = request;
      int days = request.getRemoveSnapshotsOlderThanDays();
      
            if ( days > 0 )
            {
                this.dateThreshold = System.currentTimeMillis() - ( days * dayInMillis );
            }
            else
            {
                this.dateThreshold = -1L;
            } 
    }
    
    /**
     * Process through the items of a collection.
     * @param context current context of walker
     * @param item current stored item
     * @throws Exception 
     */
    @Override
    public void processItem(WalkerContext context, StorageItem item)
            throws Exception
    {
     countMinToKeep = request.getMinCountOfSnapshotsToKeep();
    }
    
    /**
     * After collected the deleteable folders this method proofs the selected options and deletes the files and snapshots.
     * @param context current context of walker
     * @param coll delivers information about a item in collection
     * @throws Exception if process is failed
     */
    @Override
    public void onCollectionExit(WalkerContext context, StorageCollectionItem coll)
            throws  Exception
    {
      try
      {
        boolean itemAlreadyExistInDeletable = false;
        
        for (StorageCollectionItem item : this.deletableSnapshotsCollection)
        {
          if (item.equals(coll))
          {
            itemAlreadyExistInDeletable = true;
          }
          else
          {
            itemAlreadyExistInDeletable = false;
          }

        }

        if (!itemAlreadyExistInDeletable)
        {
          this.deletableSnapshotsCollection.add(coll);
          this.deletableSnapshotsCollectionAll.add(coll);
        }
        
        if (!IVYArtifactRecognizer.isSnapshotFolder(coll.getPath()))
        {
          int delCollSize = this.deletableSnapshotsCollection.size();

          if (this.request.getMinCountOfSnapshotsToKeep() > 0)
          {
            ListIterator item = this.deletableSnapshotsCollection.listIterator(delCollSize);

            while (item.hasPrevious())
            {
              String s = ((StorageCollectionItem)item.previous()).getPath();
              
              if ((IVYArtifactRecognizer.isSnapshotFolder(s)) 
                  && (IVYDefaultSnapshotRemover.this.countMinToKeep != 0))
              {
                item.remove();
                countMinToKeep--;
              }
            }
          }
        }
      }
      catch (WalkerException e)
      {
        // we always simply log the exception and continue
         IVYDefaultSnapshotRemover.this.getLogger().warn(
                                 "SnapshotRemover is failed to process path: '{}'.",coll.getPath(), e);
      }
      
    }
     
    /**
     * The WalkerContext is usable to control the walk through. 
     * He walks through the current repository and shares some contextual data.
     * If there is a IllegalOperationException,
     * ItemNotFoundException, UnsupportedStorageOperationException, AccessDeniedException,
     * and NoSuchRepositoryException
     * @param context current context of walker
     * @throws Exception for repository or item not found, no access or unsupported operation
     */
    @Override
    public void afterWalk(WalkerContext context) throws Exception
    {
            super.afterWalk(context);
        
            doOnCollectionExit(context);
    }
    
    /** 
     * Verifies that StorageItems have a released version. 
     * Also that tehy are not older than days, 
     * finally deletes matched Snapshots and files from Repository.
     * @param context current context of walker 
     * @throws Exception if any exception is occured, 
     * there is a illegal operation or files are not found in storage
     */
    public void doOnCollectionExit(WalkerContext context)
            throws Exception
    {
        
      for(StorageCollectionItem coll : deletableSnapshotsCollectionAll)
      {    
        if (IVYDefaultSnapshotRemover.this.getLogger().isDebugEnabled())
        {
            IVYDefaultSnapshotRemover.this.getLogger().debug("onCollectionExit() :: "                    
                    + coll.getRepositoryItemUid().toString());
        }
            getLogger().info("End of path :" + coll.getPath());
      
            this.shouldProcessCollection 
                    = IVYArtifactRecognizer.isSnapshotOrGroupFolder(coll.getPath());
             
      
            getLogger().info("shouldProcessCollection return with coll.getPath().endsWith "          
                              + this.shouldProcessCollection);
      
      if (!this.shouldProcessCollection)
      {
        return;
      }
 
        this.deletableSnapshotsAndFiles.clear();
        this.removeWholeGAV = false;
      
        Collection<StorageItem> items = this.repository.list(false, coll);

        gatheringFactsForItems(items, coll);
        doIfRemoveWholeGavTrue(context, items, coll);
       
      
      }
      doIfNotRemoveWholeGavTrue(context);

      for(StorageCollectionItem coll : deletableSnapshotsCollectionAll)
      {
        updateMetadataIfNecessary(coll);
        removeDirectoryIfEmpty(coll);
      }
    }
    
    /**
     * This method proofs for existence of a pom file, and a release version.
     * @param items collection of items were found
     * @param coll delivers information about a item in collection
     * @throws Exception if it is not possible to proof for existence of release versions
     */
    private void gatheringFactsForItems(Collection<StorageItem> items,
                                        StorageCollectionItem coll) throws Exception
    {
        IVYGav gav = null;
        // gathering the facts
        for (StorageItem item : items)
        {
          gav = null;  
          if ((!item.isVirtual()) 
              && (!StorageCollectionItem.class.isAssignableFrom(item.getClass())))
          {
            if (IVYArtifactRecognizer.isPom(item.getPath()))
            {
              gav = (IVYGav)((MavenRepository)coll.getRepositoryItemUid()
                     .getRepository()).getGavCalculator().pathToGav(item.getPath());
            }

            this.deletableSnapshotsAndFiles.add(item);

            if ((gav != null) && (this.request.isRemoveIfReleaseExists())
                      && (releaseExistsForSnapshot(gav, item.getItemContext())))
              {
                IVYDefaultSnapshotRemover.this.getLogger().debug(
                        "Found POM and release exists, removing whole gav.");

                this.removeWholeGAV = true;
                
                break;
              }
           }
        }
    }
    
    /**
     * This method deletes all snapshots of a version.
     * If there is a release version somewhere in storage, its not necessary
     * to keep snapshots for this release.
     * @param context current context of walker
     * @param items collection of items were found
     * @param coll delivers information about a item in collection
     * @throws Exception if not whole GAV could be deleted
     */
    private void doIfRemoveWholeGavTrue(WalkerContext context, 
                                        Collection<StorageItem> items,
                                        StorageCollectionItem coll) throws Exception
    {
             if (this.removeWholeGAV)
        {
          try
          {
            for (StorageItem item : items ) 
            {
              try
              {
                if (!(item instanceof StorageCollectionItem))
                {
                      
                  this.repository.deleteItem(false, createResourceStoreRequest(item, context)); 
                  this.deletedFiles += 1;
                    
                }
              }
              catch (ItemNotFoundException e)
              {
                if (IVYDefaultSnapshotRemover.this.getLogger().isDebugEnabled())
                {
                  IVYDefaultSnapshotRemover.this.getLogger()
                  .debug("Could not delete whole GAV " 
                  + coll.getRepositoryItemUid().toString(), e);
                }
              }

            }
          
          }
          catch (StorageException e)
          {
            IVYDefaultSnapshotRemover.this.getLogger()
            .warn("Could not delete whole GAV " 
            + coll.getRepositoryItemUid().toString(), e);
          }
        }
    }
    
    /**
     * Deletes all snapshots and files with given parameters. 
     * Just if "Remove if released" field is not activated.
     * @param context current context of walker
     * @throws IllegalOperationException
     * @throws ItemNotFoundException
     * @throws StorageException
     * @throws UnsupportedStorageOperationException 
     */
      private void doIfNotRemoveWholeGavTrue(WalkerContext context) throws Exception
      {        
        if (!this.removeWholeGAV)
        {     
            long itemAge = 0;  
            
            for (StorageCollectionItem collection : this.deletableSnapshotsCollection)
            {    
                this.deletableSnapshotsAndFiles.clear();
                Collection<StorageItem> items = this.repository.list(false, collection);
          
              for (StorageItem item : items)
              {
                if ((!item.isVirtual()) 
                    && (!StorageCollectionItem.class.isAssignableFrom(item.getClass())))
                {
                    itemAge = item.getModified();
                    if ((-1L == this.dateThreshold) || (itemAge < this.dateThreshold))
                    {
                        this.deletableSnapshotsAndFiles.add(item);
                    }
                }
              }
              
              for (StorageItem file : this.deletableSnapshotsAndFiles)
              {

                try
                {
                    this.repository.deleteItem(false, createResourceStoreRequest(file, context));
                    this.deletedFiles += 1;
                }
                catch (ItemNotFoundException e)
                {
                    if (IVYDefaultSnapshotRemover.this.getLogger().isDebugEnabled())
                    {
                    IVYDefaultSnapshotRemover.this.getLogger()
                       .debug("Could not delete file:", e);
                    }
                }
                catch (IOException e)
                {
                    IVYDefaultSnapshotRemover.this.getLogger()
                      .info("Could not delete file:", e);
                }
              }
              
            }
        }
    }
    
    /**
       * Updates the Metadata in artifact folders.
       * @param coll delivers items from storage collection
       * @throws Exception 
       */  
    private void updateMetadataIfNecessary(StorageCollectionItem coll)
        throws Exception
    {
      if ((!this.deletableSnapshotsAndFiles.isEmpty()))
      {
        if(!coll.getPath().equals("/"))
        {
        this.collectionNodes.addAndMarkPath(ItemPathUtils.getParentPath(coll.getPath()));
        }
      }
      else
      {
        this.collectionNodes.addAndMarkPath(coll.getPath());
        
      }
    }
    
    /**
     * After deleting all files from a snapshot folder, the folder is empty.
     * This method deletes those folders.
     * @param collItem delivers path of items in a collection
     * @throws StorageException if path is not found in storage
     * @throws IllegalOperationException if operation is permitted
     * @throws UnsupportedStorageOperationException if operation is unsupported
     */
    private void removeDirectoryIfEmpty(StorageCollectionItem collItem)
      throws StorageException, IllegalOperationException, UnsupportedStorageOperationException
    {
      try
      {
        if (this.repository.list(false, collItem).size() > 0)
        {
          return;
        }
        if (IVYDefaultSnapshotRemover.this.getLogger().isDebugEnabled())
        {
          IVYDefaultSnapshotRemover.this.getLogger()
                  .debug("Removing the empty directory leftover: UID=" 
                            + collItem.getRepositoryItemUid().toString());
        }
        
        
        if(IVYArtifactRecognizer.isSnapshotFolder(collItem.getPath()))
        {
          deletedSnapshots += 1;
        }
        this.repository.deleteItem(false, createResourceStoreRequest(collItem, DeleteOperation.DELETE_PERMANENTLY));
      }
      catch (ItemNotFoundException e)
      {
        IVYDefaultSnapshotRemover.this.getLogger()
                  .debug("Error Occurred Item was not found: " +
                            e);
      }
    }
 
    /**
     * Whole information about items Group,ArtifactId and Version is delivered.
     * The method gets the curent item context.
     * It is living only during item processing and is not stored.
     * @param snapshotGav informations about current snapshot GAV
     * @param context informations about current item context
     * @return returns true if there is a released version in any repository 
     * @throws IOException  If any input or output exception occurred
     */
    public boolean releaseExistsForSnapshot(IVYGav snapshotGav, Map<String, Object> context) 
    throws Exception
    {
      for (Repository repos : 
              IVYDefaultSnapshotRemover.this.repositoryRegistry.getRepositories())
      {
        // repository that has release policy            
        if (repos instanceof IVYHostedRepository)
       
        {
          // actually, we don't care is it proxy or hosted, we only need to filter out groups and other
          // "composite" reposes like shadows
          IVYRepository irepository = (IVYRepository)repos;
          
           // look in release reposes only
           if ( RepositoryPolicy.RELEASE.equals(irepository.getRepositoryPolicy()))
             {
                 try
                {   
                  
                    String[] splitToBaseVersion = snapshotGav.getBaseVersion().split("\\-");
                    String releaseVersion = splitToBaseVersion[0];

                    String pathToGroup = snapshotGav.getGroupId().replace(".",RepositoryItemUid.PATH_ROOT);
                    
                    ResourceStoreRequest storeRequest = 
                            new ResourceStoreRequest(
                                    RepositoryItemUid.PATH_ROOT
                                    + pathToGroup
                                    + RepositoryItemUid.PATH_ROOT
                                    + snapshotGav.getArtifactId());
                    int hits = 0;
                    Collection<StorageItem> listItems = irepository.getLocalStorage()
                                                        .listItems(irepository, storeRequest);
                           
                    for(StorageItem item : listItems)
                    {
                         
                     if(item.getPath().contains(releaseVersion))
                     {
                      if (IVYDefaultSnapshotRemover.this.getLogger().isDebugEnabled())
                        { 
                           IVYDefaultSnapshotRemover.this.getLogger()
                                .debug("For group: {} artifact: {}",
                                           snapshotGav.getGroupId(),
                                           snapshotGav.getArtifactId());   
                           IVYDefaultSnapshotRemover.this.getLogger()
                                .debug("and version: {} release exists: {}",
                                           snapshotGav.getVersion(),
                                           item.getPath());
                           IVYDefaultSnapshotRemover.this.getLogger()
                                .debug("in repository: "+irepository.getId());
                           hits += 1;
                        }
                     }
                    }    
                    return hits > 0;
                }
                 catch (Exception e )
                 {
                           IVYDefaultSnapshotRemover.this.getLogger()
                           .error("Error occurred while processing: " + e);
                 }
           }
        }
      }
      return false;
    }
    
    /**
     * This method gets a request for a resource.
     * @param item delivers imformation about stored items
     * @param ctx delivers some contextual data
     * @return a ResourceStoreRequest is given back
     */
    private ResourceStoreRequest createResourceStoreRequest(StorageItem item, WalkerContext ctx)
    {
      ResourceStoreRequest storeRequest = new ResourceStoreRequest(item);

      if (ctx.getContext().containsKey(DeleteOperation.DELETE_OPERATION_CTX_KEY))
      {
        storeRequest.getRequestContext()
                .put(DeleteOperation
                .DELETE_OPERATION_CTX_KEY, 
                ctx.getContext().get(DeleteOperation
                .DELETE_OPERATION_CTX_KEY));
      }

      return storeRequest;
    }
    
    /**
     * This method gives back a request for resources in a collection.
     * and sets the chosen kind of delete operation(DELETE_PERMANENTLY, MOVE_TO_TRASH). 
     * @param item information about items in collection
     * @param operation kind of delete operation
     * @return a request for a resource
     */
    private ResourceStoreRequest createResourceStoreRequest(StorageCollectionItem item, DeleteOperation operation)
    {
      ResourceStoreRequest aRequest = new ResourceStoreRequest(item);
      aRequest.getRequestContext().put(DeleteOperation.DELETE_OPERATION_CTX_KEY, operation);
      return aRequest;
    }
    
    /**
     * Gets the Number of deleted Snapshots.
     * @return returns a count of deleted Snapshots 
     */
    public int getDeletedSnapshots()
    {
      return this.deletedSnapshots;
    }
    
    /** 
     * Gets the Number of deleted Files.
     * @return returns a count of deleted files 
     */
    public int getDeletedFiles()
    {
      return this.deletedFiles;
    }
  }
}