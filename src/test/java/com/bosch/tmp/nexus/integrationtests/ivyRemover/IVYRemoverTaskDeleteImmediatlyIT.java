/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bosch.tmp.nexus.integrationtests.ivyRemover;

import com.bosch.tmp.nexus.ivy.tasks.descriptors.IVYSnapshotRemovalTaskDescriptor;
import java.io.File;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.restlet.data.MediaType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.proxy.maven.ChecksumPolicy;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.XStreamFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
* @author wa21190
*/
public class IVYRemoverTaskDeleteImmediatlyIT 
 extends AbstractNexusIntegrationTest
{
 

    private RepositoryMessageUtil repoUtil;
    private static final String snapRepoId = "tmp-snapshots";
    private File repositoryPath = new File(nexusWorkDir, "storage/" + snapRepoId );
    private File trashPath = new File(repositoryPath, ".nexus/trash");
    private String artifactRelativePathOne = "ivyRemover/auth-api/0.8-100";
    private String artifactRelativePathTwo = "ivyRemover/auth-mod/0.8-100";
    private File artifactFolderOne = new File( repositoryPath, artifactRelativePathOne );
    private File artifactFolderTwo = new File( repositoryPath, artifactRelativePathTwo );
    private File trashFolderOne = new File( trashPath, artifactRelativePathOne );
    private File trashFolderTwo = new File( trashPath, artifactRelativePathTwo );
    
    
    @BeforeClass
    public void setSecureTest() throws Exception{
        
        //get a test container for the test
        TestContainer.getInstance().getTestContext().setSecureTest( true );
        
        //set log level of Nexus to debug
        System.setProperty("it.nexus.log.level", "DEBUG");
        
     
    }

    @BeforeClass
    public void init()
        throws ComponentLookupException
    {
       //get infos from xml documents of nexus
       this.repoUtil = new RepositoryMessageUtil( this, 
               XStreamFactory.getXmlXStream(), MediaType.APPLICATION_XML ); 
    }

       
    @Test
    public void testSnapRepoDeployFilesRunTaskDeleteImmediately()
        throws IOException, Exception
    {
        // create a snapshot repository
        createSnapshotRepo(snapRepoId);
        
        // add some artifacts
        copyToSnapRepo(snapRepoId); 
        
        // downloadAndCopyFilesToIVYSnap(repoId);

        // update indexes for search
        RepositoryMessageUtil.updateIndexes(snapRepoId);
        
        TaskScheduleUtil.waitForAllTasksToStop();
        
        // set the parameters of SnapshotRemoverTask, delete at least 1 of 4 Snapshots 
        ScheduledServicePropertyResource keepSnapshotsProp = 
                                            new ScheduledServicePropertyResource();
        keepSnapshotsProp.setKey(IVYSnapshotRemovalTaskDescriptor.MIN_TO_KEEP_FIELD_ID);
        keepSnapshotsProp.setValue( String.valueOf( 3 ) );

        ScheduledServicePropertyResource ageProp = 
                                            new ScheduledServicePropertyResource();
        ageProp.setKey( IVYSnapshotRemovalTaskDescriptor.KEEP_DAYS_FIELD_ID );
        ageProp.setValue( String.valueOf( 0 ) );
        
        ScheduledServicePropertyResource relProp = 
                                            new ScheduledServicePropertyResource();
        relProp.setKey( IVYSnapshotRemovalTaskDescriptor.REMOVE_WHEN_RELEASED_FIELD_ID);
        relProp.setValue( Boolean.toString(false) );
        
        // set the Delete Immediatly Field to true, without moving the two deleted artifacts to trash
        ScheduledServicePropertyResource delProp = 
                                            new ScheduledServicePropertyResource();
        delProp.setKey( IVYSnapshotRemovalTaskDescriptor.DELETE_IMMEDIATELY);
        delProp.setValue( Boolean.toString(true) );
        
        ScheduledServicePropertyResource repos = 
                                            new ScheduledServicePropertyResource();
        repos.setKey(IVYSnapshotRemovalTaskDescriptor.REPO_OR_GROUP_FIELD_ID);
        repos.setValue( snapRepoId );
        TaskScheduleUtil.runTask( "IvySnapshotRemoval", 
                                   IVYSnapshotRemovalTaskDescriptor.ID, 
                                   repos,
                                   keepSnapshotsProp, 
                                   ageProp, 
                                   relProp, 
                                   delProp );
        
        TaskScheduleUtil.waitForAllTasksToStop();

        getEventInspectorsUtil().waitForCalmPeriod();
        
        //firstly assert that there is nothing left in the repository folder .../ivyRemover/auth-api/0.8-100
        assertThat( artifactFolderOne.list(), nullValue() );
        
        //secondly assert that there is nothing moved into trash folder of .../ivyRemover/auth-api/0.8-100
        assertThat( "removed snapshots did go into trash", 
                        trashFolderOne.list(), 
                        nullValue() );
        
        //thirdly assert that there is nothing left in the repository folder .../ivyRemover/auth-mod/0.8-100
        assertThat( artifactFolderTwo.list(), nullValue() );
        
        //fourthly assert that there is nothing moved into trash folder of .../ivyRemover/auth-mod/0.8-100
        assertThat( "removed snapshots did go into trash", 
                        trashFolderTwo.list(), 
                        nullValue() );
        
        
       }
       
       public void createSnapshotRepo(String repoId) throws IOException
       {             
        RepositoryResource repo = new RepositoryResource();
        repo.setProvider( "ivy" );
        repo.setFormat( "maven2" );
        repo.setRepoPolicy( "snapshot" );
        repo.setChecksumPolicy( "ignore" );
        repo.setBrowseable( false );

        repo.setId( repoId );
        repo.setName( repoId );
        repo.setRepoType( "hosted" );
        repo.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );
        repo.setDownloadRemoteIndexes( true );
        repo.setBrowseable( true );
        repo.setRepoPolicy( RepositoryPolicy.SNAPSHOT.name() );
        repo.setChecksumPolicy( ChecksumPolicy.IGNORE.name() );
          
        repo.setIndexable( true ); // being sure!!!
       
        repoUtil.createRepository( repo );

        repo = (RepositoryResource) repoUtil.getRepository( repoId );
       
        //assert that the snapshot repository has been created
        Assert.assertTrue( repo.isIndexable() );
           
       }

       public void copyToSnapRepo(String repoId) throws IOException
       {
        
       List<List<String>> artifactInfos = new ArrayList<List<String>>();
           List<String> singleArtifactInfo = new ArrayList<String>();
           List<String> secondArtifactInfo = new ArrayList<String>();
           
           //set First Artifact Group
           singleArtifactInfo.add("ivyRemover");
           singleArtifactInfo.add("auth-api");
           singleArtifactInfo.add("0.8");
           artifactInfos.add(singleArtifactInfo);
           
           //set 2nd Artifact Group 
           secondArtifactInfo.add("ivyRemover");
           secondArtifactInfo.add("auth-mod");
           secondArtifactInfo.add("1.0");
           artifactInfos.add(secondArtifactInfo);
           
           for (List<String> artiInfo : artifactInfos)
           {
               
            String groupId = null;
            String artifactId = null;
            String version = null;
               
            List<String> ArtifactInfos = artiInfo;
           
            groupId = ArtifactInfos.get(0);
            artifactId = ArtifactInfos.get(1);
            version = ArtifactInfos.get(2);
               
           //download and copy to snapshot repository
             for(int buildNum = 100; buildNum <=103; buildNum++)
             {   
               File dest = new File( nexusWorkDir, "storage/" + repoId + "/" 
                                                              + groupId + "/"
                                                              + artifactId +"/" 
                                                              + version +"-"+ buildNum + "/"
                                                              + artifactId + "-" 
                                                              + version + "-"
                                                              + buildNum +"-sources.jar" );
               dest.getParentFile().mkdirs();
               FileUtils.copyFile( getTestFile( "simple-artifact.jar" ), dest );
        
               dest = new File( nexusWorkDir, "storage/" + repoId + "/" 
                                                         + groupId + "/"
                                                         + artifactId +"/" 
                                                         + version +"-"+ buildNum + "/"
                                                         + artifactId + "-" 
                                                         + version + "-"
                                                         + buildNum +"-sources.jar.md5" );
               dest.getParentFile().mkdirs();
               FileUtils.copyFile( getTestFile( "simple-artifact-hash.md5" ), dest );
               
               dest = new File( nexusWorkDir, "storage/" + repoId + "/"
                                                         + groupId + "/"
                                                         + artifactId + "/"
                                                         + version +"-"+ buildNum + "/"
                                                         + artifactId + "-" 
                                                         + version + "-"
                                                         + buildNum +"-sources.jar.sha1" );
               dest.getParentFile().mkdirs();
               FileUtils.copyFile( getTestFile( "simple-artifact-hash.sha1" ), dest );
               
               dest = new File( nexusWorkDir, "storage/" + repoId + "/" 
                                                         + groupId + "/"
                                                         + artifactId + "/" 
                                                         + version +"-"+ buildNum + "/"
                                                         + artifactId + "-" 
                                                         + version + "-"
                                                         + buildNum +".jar" );
               dest.getParentFile().mkdirs();
               FileUtils.copyFile( getTestFile( "simple-artifact.jar" ), dest );
               
               dest = new File( nexusWorkDir, "storage/" + repoId + "/" 
                                                         + groupId + "/"
                                                         + artifactId + "/" 
                                                         + version +"-"+ buildNum + "/"
                                                         + artifactId + "-" 
                                                         + version + "-"
                                                         + buildNum +".jar.md5" );
               dest.getParentFile().mkdirs();
               FileUtils.copyFile( getTestFile( "simple-artifact-hash.md5" ), dest );
               
               dest = new File( nexusWorkDir, "storage/" + repoId + "/" 
                                                         + groupId + "/"
                                                         + artifactId + "/" 
                                                         + version +"-"+ buildNum + "/"
                                                         + artifactId + "-" 
                                                         + version + "-"
                                                         + buildNum +".jar.sha1" );
               dest.getParentFile().mkdirs();
               FileUtils.copyFile( getTestFile( "simple-artifact-hash.sha1" ), dest );
               
               dest = new File( nexusWorkDir, "storage/" + repoId + "/" 
                                                         + groupId + "/"
                                                         + artifactId + "/" 
                                                         + version +"-"+ buildNum + "/"
                                                         + artifactId + "-" 
                                                         + version + "-"
                                                         + buildNum +".pom" );
               dest.getParentFile().mkdirs();
               FileUtils.copyFile( getTestFile( "simple-artifact.pom" ), dest );
               
               dest = new File( nexusWorkDir, "storage/" + repoId + "/" 
                                                         + groupId + "/"
                                                         + artifactId + "/" 
                                                         + version +"-"+ buildNum + "/"
                                                         + artifactId + "-" 
                                                         + version + "-"
                                                         + buildNum +".pom.md5" );
               dest.getParentFile().mkdirs();
               FileUtils.copyFile( getTestFile( "simple-artifact-hash.md5" ), dest );
               
               dest = new File( nexusWorkDir, "storage/" + repoId + "/" 
                                                         + groupId + "/"
                                                         + artifactId + "/" 
                                                         + version +"-"+ buildNum + "/"
                                                         + artifactId + "-" 
                                                         + version + "-"
                                                         + buildNum +".pom.sha1" );
               dest.getParentFile().mkdirs();
               FileUtils.copyFile( getTestFile( "simple-artifact-hash.sha1" ), dest );
               
               dest = new File( nexusWorkDir, "storage/" + repoId + "/" 
                                                         + groupId + "/"
                                                         + artifactId + "/" 
                                                         + version +"-"+ buildNum + "/"
                                                         + "ivy-" 
                                                         + version + "-"
                                                         + buildNum +".xml" );
               dest.getParentFile().mkdirs();
               FileUtils.copyFile( getTestFile( "simple-artifact.xml" ), dest );
               
               dest = new File( nexusWorkDir, "storage/" + repoId + "/" 
                                                         + groupId + "/"
                                                         + artifactId + "/" 
                                                         + version +"-"+ buildNum + "/"
                                                         + "ivy-" 
                                                         + version + "-"
                                                         + buildNum +".xml.md5" );
               dest.getParentFile().mkdirs();
               FileUtils.copyFile( getTestFile( "simple-artifact-hash.md5" ), dest );
               
               dest = new File( nexusWorkDir, "storage/" + repoId + "/" 
                                                         + groupId + "/" 
                                                         + artifactId + "/" 
                                                         + version +"-"+ buildNum + "/"
                                                         + "ivy-" 
                                                         + version + "-"
                                                         + buildNum +".xml.sha1" );
               dest.getParentFile().mkdirs();
               FileUtils.copyFile( getTestFile( "simple-artifact-hash.sha1" ), dest );
               
               dest.getParentFile().mkdirs();
               FileUtils.copyFile( getTestFile( "simple-artifact-hash.md5" ), dest );
               
               dest = new File( nexusWorkDir, "storage/" + repoId + "/" 
                                                         + groupId + "/" 
                                                         + artifactId + "/" 
                                                         + "maven-metadata.xml"); 
                                                          
               dest.getParentFile().mkdirs();
               FileUtils.copyFile( getTestFile( "maven-metadata.xml" ), dest );
               
               dest = new File( nexusWorkDir, "storage/" + repoId + "/" 
                                                         + groupId + "/" 
                                                         + artifactId + "/" 
                                                         + "maven-metadata.xml.md5"); 
                                                          
               dest.getParentFile().mkdirs();
               FileUtils.copyFile( getTestFile( "simple-artifact-hash.md5" ), dest );
               
               dest = new File( nexusWorkDir, "storage/" + repoId + "/" 
                                                         + groupId + "/" 
                                                         + artifactId + "/" 
                                                         + "maven-metadata.xml.sha1"); 
                                                          
               dest.getParentFile().mkdirs();
               FileUtils.copyFile( getTestFile( "simple-artifact-hash.sha1" ), dest );
             }
           }
       }
}
