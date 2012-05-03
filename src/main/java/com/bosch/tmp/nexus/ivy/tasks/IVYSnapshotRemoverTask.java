package com.bosch.tmp.nexus.ivy.tasks;


import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalRequest;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalResult;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.scheduling.SchedulerTask;
 
/** 
 * Class to set and get attributes of IVYSnapshotRemoverTask.
 * @author wa21190
 */
@Component(role=SchedulerTask.class, hint="IVYSnapshotRemoverTask", instantiationStrategy="per-lookup")
public class IVYSnapshotRemoverTask extends AbstractNexusRepositoriesTask<SnapshotRemovalResult>
{
    /**
     * Variable to call action for exuting task.
     */
    public static final String SYSTEM_REMOVE_SNAPSHOTS_ACTION = "REMOVESNAPSHOTS";
    
    /**
     * Set default minimum snapshots to keep if nothing is inserted. 
     */
    public static final int DEFAULT_MIN_SNAPSHOTS_TO_KEEP = 0;
    
    /**
     * Set default older than days if nothing is inserted.
     */
    public static final int DEFAULT_OLDER_THAN_DAYS = -1;

  @Requirement
  private IVYSnapshotRemover ivySnapshotRemover;

  @Override
  protected String getRepositoryFieldId()
  {
    return "IvyRepositoryId";
  }
  
  /**
   * count of snapshots - minimum to keep.
   * @return returns the number of snapshots to keep from parameter
   */
  public int getMinSnapshotsToKeep()
  {
    String param = (String)getParameters().get("IvyMinSnapshotsToKeep");

    if (StringUtils.isEmpty(param))
    {
      return 0;
    }

    return Integer.parseInt(param);
  }
  
  /**
   * Sets the number of Snapshots to keep in a repository.
   * @param minSnapshotsToKeep sets the number of snapshots to keep delivered from field
   */
  public void setMinSnapshotsToKeep(int minSnapshotsToKeep)
  {
    getParameters().put("IvyMinSnapshotsToKeep", Integer.toString(minSnapshotsToKeep));
  }
  
  /** 
   * @return returns the number of snapshots older than days to keep from parameter
   */
  public int getRemoveOlderThanDays()
  {
    String param = (String)getParameters().get("IvyRemoveOlderThanDays");

    if (StringUtils.isEmpty(param))
    {
      return -1;
    }

    return Integer.parseInt(param);
  }
  
  /**
   * Sets a maximum age for snapshots are allowed to delete.
   * @param removeOlderThanDays sets the number of snapshots older than days 
   * to keep delivered from field
   */
  public void setRemoveOlderThanDays(int removeOlderThanDays)
  {
    getParameters().put("IvyRemoveOlderThanDays", Integer.toString(removeOlderThanDays));
  }
  
  /**
   * Proof for release existence.
   * @return returns true if RemoverTask should proof for release exists 
   */ 
  public boolean isRemoveIfReleaseExists()
  {
    return Boolean.parseBoolean((String)getParameters().get("IvyRemoveIfReleaseExists"));
  }
  
  /** 
   * Sets the option Remove if Release exists if whole GAV should be deleted. 
   * @param removeIfReleaseExists sets the checkbox true if releases should be removed  
   */
  public void setRemoveIfReleaseExists(boolean removeIfReleaseExists)
  {
    getParameters().put("IvyRemoveIfReleaseExists", Boolean.toString(removeIfReleaseExists));
  }

  /**
   * Gives back the option delete permanently.
   * @return returns true if deleteImmediatly is set to true
   */
    public boolean isDeleteImmediately()
  {
    return Boolean.parseBoolean((String)getParameters().get("IvyDeleteImmediately"));
  }

  /** 
   * Move to trash or delete the files permanently.
   *@param deleteImmediately sets the param true if shouldn't be moved into trash folder  */ 
  public void setDeleteImmediately(boolean deleteImmediately)
  {
    getParameters().put("IvyDeleteImmediately", Boolean.toString(deleteImmediately));
  }

  /**
   * Method to run a Task with given options.
   * @return a result for task about deleted files and snapshots
   * @throws NoSuchRepositoryException if repository cant be found
   */
  @Override
  public SnapshotRemovalResult doRun()
    throws NoSuchRepositoryException
  {
    SnapshotRemovalRequest req = new SnapshotRemovalRequest(getRepositoryId(), 
                                                            getMinSnapshotsToKeep(), 
                                                            getRemoveOlderThanDays(), 
                                                            isRemoveIfReleaseExists(),
                                                            isDeleteImmediately());

    return this.ivySnapshotRemover.removeSnapshots(req);
  }

  /**
   * Get the kind of task to run.
   * @return a string for kind of task
   */
  @Override
  protected String getAction()
  {
    return "REMOVESNAPSHOTS";
  }

  /**
   * Get informations that task is running.
   * @return informations if all repositories are chosen or just one
   */
  @Override
  protected String getMessage()
  {
    if (getRepositoryId() != null)
    {
      return "Removing Ivy snapshots from repository " + getRepositoryName();
    }

    return "Removing  Ivy snapshots from all registered repositories";
  }
}
