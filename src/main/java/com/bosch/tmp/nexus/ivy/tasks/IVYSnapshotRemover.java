package com.bosch.tmp.nexus.ivy.tasks;

import org.sonatype.nexus.maven.tasks.SnapshotRemovalRequest;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalResult;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
 
/** 
 * @author wa21190
 * interface for IVYDefaultSnapshotRemover
 */
public abstract interface IVYSnapshotRemover
{
   
   /** 
   * Method to remove snapshots.
   * a SnapshotRemovalRequest with these parameters:
   * repositoryId, 
   * minCountOfSnapshotsToKeep,
   * removeSnapshotsOlderThanDays, 
   * removeIfReleaseExists, 
   * metadataRebuildPaths, 
   * processedRepos, is needed
   * @return returns a result i.e:(RepositoryId: "tmp-snapshots",deletedSnapshots: 0,deletedFiles 0,isSucessful: true)
   * @param paramSnapshotRemovalRequest get request from IVYSnapshotRemoverTask.doRun()
   * @throws NoSuchRepositoryException if repository doesn't exist anymore
   */
  SnapshotRemovalResult removeSnapshots(SnapshotRemovalRequest paramSnapshotRemovalRequest)
    throws NoSuchRepositoryException;
}
