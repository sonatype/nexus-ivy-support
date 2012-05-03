package com.bosch.tmp.nexus.ivy.test.tasks;
 
import com.bosch.tmp.nexus.ivy.tasks.IVYDefaultSnapshotRemover;
import org.sonatype.nexus.proxy.walker.Walker;
import com.bosch.tmp.nexus.ivy.repository.IVYHostedRepository;
import com.bosch.tmp.nexus.ivy.repository.IVYRepository;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.log4j.Logger;
import org.codehaus.plexus.component.annotations.Requirement;
import org.junit.Test;
import static org.junit.Assert.*;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalRepositoryResult;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalRequest;
import org.sonatype.nexus.maven.tasks.SnapshotRemovalResult;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.Maven2ContentClass;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.DefaultRepositoryRegistry;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.DefaultRepositoryKind;
import org.sonatype.nexus.proxy.repository.LocalStatus;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryKind;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

 
/**
*
* @author wa21190
*/
public class IVYDefaultSnapshotRemoverTest {
    
    private static Logger logger = Logger.getRootLogger(); 
 
    private List<Repository> repos = new ArrayList<Repository>();
    
    @Requirement(hint="maven2")
    private ContentClass contentClass;
    
    @Requirement
    IVYHostedRepository ivyRepository;
    
    @Requirement 
    RepositoryKind kind;
    
    @Requirement
    IVYDefaultSnapshotRemover instance;
 
    
        RepositoryRegistry registry; 
    
    Collection<Class<?>> facets = new ArrayList<Class<?>>();
    
    public IVYDefaultSnapshotRemoverTest() {
        
        facets.add(IVYHostedRepository.class);
        ivyRepository = new IVYRepository();
        kind = new DefaultRepositoryKind(IVYHostedRepository.class,facets);
        
    }

    /**
     * Test of getRepositoryRegistry method, of class IvyDefaultSnapshotRemover.
     */
    
    @Test
    public void testGetRepositoryRegistry() {
 
        registry = new DefaultRepositoryRegistry();

        logger.info("getRepositoryRegistry:");
        instance = new IVYDefaultSnapshotRemover();
        RepositoryRegistry expResult = registry;
        instance.setRepositoryRegistry(registry);
        RepositoryRegistry result = instance.getRepositoryRegistry();
        assertEquals(expResult, result);
        logger.info("returned as expected (" 
                        + expResult + ") and with result(" + result + ")");
    }
    
    /**
     * Test of removeSnapshots method, of class IvyDefaultSnapshotRemover
     * if no facets (MavenRepository.class or GroupRepository.class) are available
     */
    
    @Test
    public void testRemoveSnapshots() throws Exception {
        
        logger.info("testRemoveSnapshots: ");
        instance = new IVYDefaultSnapshotRemover();
        
        SnapshotRemovalRequest request = 
                new SnapshotRemovalRequest("tmp-snapshots", 2, 0, true);
        
        SnapshotRemovalRepositoryResult res = 
                new SnapshotRemovalRepositoryResult("tmp-snapshots", 0, 0, true);
        
        SnapshotRemovalResult expResult = new SnapshotRemovalResult();
        expResult.addResult(res);
        
        repos.add(ivyRepository);      
        contentClass = new Maven2ContentClass();     
        ivyRepository = mock(IVYHostedRepository.class);
        registry = mock( RepositoryRegistry.class );
        
        when( registry.getRepositories() ).thenReturn(repos);
        when(registry.getRepository("tmp-snapshots")).thenReturn(ivyRepository);
        when(ivyRepository.getRepositoryContentClass()).thenReturn(contentClass);
        when(ivyRepository.getLocalStatus()).thenReturn(LocalStatus.IN_SERVICE);   
        when(ivyRepository.getRepositoryKind()).thenReturn(kind);
        
        instance.setContentClass(contentClass);
        instance.setRepositoryRegistry(registry);
        SnapshotRemovalResult resultInst = instance.removeSnapshots(request);  
        assertEquals(expResult.isSuccessful(),resultInst.isSuccessful());
        logger.info("returned as expected (" 
                        + expResult.isSuccessful() 
                        + ") and with result(" 
                        + resultInst.isSuccessful() + ")");
                       
    }
    
    /**
     * Test of removeSnapshotsFromMavenRepository method, of class IvyDefaultSnapshotRemover.
     * if the request returns a repository with id and repository is a processed one
     * is in service and has a snapshot policy
     */
    
    @Test
    public void testRemoveSnapshotsFromMavenRepositoryWithIdProcessedRepo() {
        logger.info("testRemoveSnapshotsFromMavenRepositoryWithIdProcessedRepo:");
             
        SnapshotRemovalRequest request = 
                new SnapshotRemovalRequest("tmp-snapshots", 2, 0, true);
        
        SnapshotRemovalRepositoryResult res = 
                new SnapshotRemovalRepositoryResult("tmp-snapshots", 0, 0, true);
        
        SnapshotRemovalResult expResult = new SnapshotRemovalResult();
        expResult.addResult(res);
        
        request.addProcessedRepo("tmp-snapshots");
        
        IVYRepository rep = mock(IVYRepository.class);
        when(rep.getLocalStatus()).thenReturn(LocalStatus.IN_SERVICE);
        when(rep.getRepositoryPolicy()).thenReturn(RepositoryPolicy.SNAPSHOT);
        when(rep.getId()).thenReturn("tmp-snapshots");
      
        
        instance = new IVYDefaultSnapshotRemover();
        
        SnapshotRemovalRepositoryResult result = 
                instance.removeSnapshotsFromMavenRepository(rep, request);
        
        assertEquals(expResult.isSuccessful(), result.isSuccessful());
        
        logger.info("returned as expected (" 
                        + expResult.isSuccessful() 
                        + ") and with result(" 
                        + result.isSuccessful() + ")");
                        
    }
    
    /**
     * Test of removeSnapshotsFromMavenRepository method, of class IvyDefaultSnapshotRemover.
     * repository has no ID, is in service, and is processed
     * if the repository is no Snapshot but has a local status
     */   
    @Test
    public void testRemoveSnapshotsFromMavenRepositoryRepNoSnaphot() {
        logger.info("testRemoveSnapshotsFromMavenRepositoryRepNoSnaphot:");
        
        SnapshotRemovalRequest request = 
                new SnapshotRemovalRequest("tmp-snapshots", 2, 0, true);
        
        SnapshotRemovalRepositoryResult res = 
                new SnapshotRemovalRepositoryResult("tmp-snapshots", 0, 0, true);
        
        SnapshotRemovalResult expResult = new SnapshotRemovalResult();
        expResult.addResult(res);
        
        request.addProcessedRepo("tmp-snapshots");
        
        IVYRepository rep = mock(IVYRepository.class);
        when(rep.getLocalStatus()).thenReturn(LocalStatus.IN_SERVICE);
        
        instance = new IVYDefaultSnapshotRemover();
       
        SnapshotRemovalRepositoryResult result = 
                instance.removeSnapshotsFromMavenRepository(rep, request);
        
        assertEquals(expResult.isSuccessful(), result.isSuccessful());
        
        logger.info("returned as expected (" 
                        + expResult.isSuccessful() 
                        + ") and with result(" 
                        + result.isSuccessful() + ")");
    }
    
    /**
     * Test of removeSnapshotsFromMavenRepository method, of class IvyDefaultSnapshotRemover.
     * repository has ID and is in service
     * if the repository is a Snapshot and is not a processed repository
     */
    
    @Test
    public void testRemoveSnapshotsFromMavenRepositoryRepIsSnaphotWithWalker() {
        logger.info("testRemoveSnapshotsFromMavenRepositoryRepIsSnaphot:");
        
        SnapshotRemovalRequest request = 
                new SnapshotRemovalRequest("tmp-snapshots", 2, 0, true);
        
        SnapshotRemovalRepositoryResult res = 
                new SnapshotRemovalRepositoryResult("tmp-snapshots", 0, 0, true);
        
        SnapshotRemovalResult expResult = new SnapshotRemovalResult();
        expResult.addResult(res);
       
        Walker walker = mock(Walker.class);
        
        IVYRepository rep = mock(IVYRepository.class);
        when(rep.getLocalStatus()).thenReturn(LocalStatus.IN_SERVICE);
        when(rep.getRepositoryPolicy()).thenReturn(RepositoryPolicy.SNAPSHOT);
        when(rep.getRepositoryKind()).thenReturn(kind);
        when(rep.getId()).thenReturn("tmp-snapshots");
        
        instance = new IVYDefaultSnapshotRemover();
        instance.setWalker(walker);
        
        SnapshotRemovalRepositoryResult result = 
                instance.removeSnapshotsFromMavenRepository(rep, request);
        
        assertEquals(expResult.isSuccessful(), result.isSuccessful());
        
        logger.info("returned as expected (" 
                        + expResult.isSuccessful() 
                        + ") and with result(" 
                        + result.isSuccessful() + ")");
    }
    
}
