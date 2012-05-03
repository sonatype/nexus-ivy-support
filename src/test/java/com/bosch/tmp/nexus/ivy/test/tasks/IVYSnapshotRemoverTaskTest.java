/*
* To change this template, choose Tools | Templates
* and open the template in the editor.
*/
package com.bosch.tmp.nexus.ivy.test.tasks;
 
import com.bosch.tmp.nexus.ivy.tasks.IVYSnapshotRemover;
import com.bosch.tmp.nexus.ivy.tasks.IVYSnapshotRemoverTask;
import org.apache.log4j.xml.DOMConfigurator;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.junit.Before;
import org.junit.Test;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalRequest;
import static org.junit.Assert.*;
import org.apache.log4j.Logger;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalResult;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.scheduling.SchedulerTask;
/**
*
* @author Daniels
*/
@Component(role=SchedulerTask.class, hint="IVYSnapshotRemoverTask", instantiationStrategy="per-lookup")
public class IVYSnapshotRemoverTaskTest extends AbstractNexusRepositoriesTask<SnapshotRemovalResult> {
    
     private static Logger log = Logger.getRootLogger(); 
    
    //private String repoId = "repositoryId";
    @Requirement
    IVYSnapshotRemoverTask instance; 
    SnapshotRemovalRequest req = new SnapshotRemovalRequest("tmp-snapshots", 2, 2, true);
    SnapshotRemovalRequest requestRepIdNull = new SnapshotRemovalRequest(null,2,3,true);
    SnapshotRemovalResult remResult = new SnapshotRemovalResult();
   
    
    
    
    IVYSnapshotRemover remover = new IVYSnapshotRemover() {
 
        @Override
        public SnapshotRemovalResult removeSnapshots(SnapshotRemovalRequest paramSnapshotRemovalRequest)
                throws NoSuchRepositoryException, IllegalArgumentException{return remResult;};
    };
     
    
         
    public IVYSnapshotRemoverTaskTest() throws NoSuchRepositoryException {
        
        
    }
 
    @Before
    public void setUp(){
        
        instance = new IVYSnapshotRemoverTask();
  
    }
 
    /**
     * Test of getMinSnapshotsToKeep method, of class IvySnapshotRemoverTask.
     */
    @Test
    public void testGetMinSnapshotsToKeep() {
        
        log.info("testGetMinSnapshotsToKeep:");
        int expResult = 0;
        int result = instance.getMinSnapshotsToKeep();
        assertEquals(expResult, result);
        log.info("returned as expected (" 
                        + expResult + ") and with result(" + result + ")");
    }
 
    /**
     * Test of setMinSnapshotsToKeep method, of class IvySnapshotRemoverTask.
     */
    @Test
    public void testSetMinSnapshotsToKeep() {
        
        log.info("testSetMinSnapshotsToKeep:");
        int minSnapshotsToKeep = 2;
        instance.setMinSnapshotsToKeep(minSnapshotsToKeep);
        log.info("was successfully");
    }
 
    /**
     * Test of getRemoveOlderThanDays method, of class IvySnapshotRemoverTask.
     */
    @Test
    public void testGetRemoveOlderThanDays() {
      
        log.info("testGetRemoveOlderThanDays:");
        int expResult = -1;
        int result = instance.getRemoveOlderThanDays();
        assertEquals(expResult, result);
        log.info("returned as expected (" 
                        + expResult + ") and with result(" + result + ")");
    }
 
    /**
     * Test of setRemoveOlderThanDays method, of class IvySnapshotRemoverTask.
     */
    @Test
    public void testSetRemoveOlderThanDays() {
        
        log.info("testSetRemoveOlderThanDays:");
        int removeOlderThanDays = 2;
        instance.setRemoveOlderThanDays(removeOlderThanDays);
        log.info("was successfully");
    }
 
    /**
     * Test of isRemoveIfReleaseExists method, of class IvySnapshotRemoverTask.
     */
    @Test
    public void testIsRemoveIfReleaseExists() {
 
        log.info("testIsRemoveIfReleaseExists:");
        boolean expResult = false;
        boolean result = instance.isRemoveIfReleaseExists();
        assertEquals(expResult, result);
        log.info("returned as expected (" 
                        + expResult + ") and with result(" + result + ")");
    }
 
    /**
     * Test of setRemoveIfReleaseExists method, of class IvySnapshotRemoverTask.
     */
    @Test
    public void testSetRemoveIfReleaseExists() {

        log.info("testSetRemoveIfReleaseExists:");
        boolean removeIfReleaseExists = true;
        instance.setRemoveIfReleaseExists(removeIfReleaseExists);
        log.info("was successfully");
 
    }
 
    /**
     * Test of doRun method, of class IvySnapshotRemoverTask.
     */
    @Test
    public void testDoRun() throws Exception {
        
        log.info("testDoRun:");
        instance.setRepositoryId("tmp-snapshots");
     
        remResult = remover.removeSnapshots(req);
        SnapshotRemovalResult result = doRun();
        assertEquals(remResult, result);
        log.info("returned as expected: " + remResult + " and with result: " + result);
 
    }

    @Override
    protected SnapshotRemovalResult doRun() throws Exception {

        return this.remover.removeSnapshots(req);
    }

    @Override
    protected String getAction() {
        return "REMOVESNAPSHOTS";
    }

    @Override
    protected String getMessage() {
        if (getRepositoryId() != null)
    {
      return "Removing IVY snapshots from repository " + getRepositoryName();
    }

    return "Removing snapshots from all registered repositories";
    }
    

}
