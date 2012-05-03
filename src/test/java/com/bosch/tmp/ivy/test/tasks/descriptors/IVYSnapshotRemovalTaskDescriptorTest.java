
package com.bosch.tmp.ivy.test.tasks.descriptors;
 
import com.bosch.tmp.nexus.ivy.tasks.descriptors.IVYSnapshotRemovalTaskDescriptor;
import org.apache.log4j.xml.DOMConfigurator;
import org.apache.log4j.Logger;
import java.util.ArrayList;
import org.sonatype.nexus.formfields.CheckboxFormField;
import org.sonatype.nexus.formfields.NumberTextFormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
 
/**
*
* @author wa21190
*/
public class IVYSnapshotRemovalTaskDescriptorTest {
    
    private static Logger logger = Logger.getRootLogger(); 
    String getId = "IVYSnapshotRemoverTask";
    String getName = "Remove Ivy Snapshots From Repository";
    IVYSnapshotRemovalTaskDescriptor instance = new IVYSnapshotRemovalTaskDescriptor();
    private RepoOrGroupComboFormField repoField; 
    private NumberTextFormField minToKeepField; 
    private NumberTextFormField keepDaysField;
    private CheckboxFormField removeWhenReleasedField; 
    private CheckboxFormField deleteImmediatelyField;
    private List fields = new ArrayList();
    
    
    public IVYSnapshotRemovalTaskDescriptorTest() {
       
    
        repoField = new RepoOrGroupComboFormField("IvyRepositoryId", true);
    
        fields.add(this.repoField);
    
        minToKeepField = new NumberTextFormField(
                                                     "IvyMinSnapshotsToKeep", 
                                                     "Minimum snapshot count", 
                                                     "Minimum number of snapshots to keep for one GAV.", 
                                                                        false);
    
        fields.add(this.minToKeepField);
    
        keepDaysField  = new NumberTextFormField(
                                                    "IvyRemoveOlderThanDays", 
                                                    "Snapshot retention (days)", 
                                                    "The job will purge all snapshots"
                                                  + " older than the entered number of days, "
                                                  + "but will obey to Min. count of snapshots to keep.",
                                                                       false);
    
        fields.add(this.keepDaysField);
    
        removeWhenReleasedField = new CheckboxFormField(
                                                            "IvyRemoveIfReleaseExists", 
                                                            "Remove if released", 
                                                            "The job will purge all snapshots "
                                                            + "that have a corresponding released artifact "
                                                            + "(same version not including a serial snapshot "
                                                            + "number like -1234).",
                                                                             false);
    
        fields.add(this.removeWhenReleasedField);
    
        deleteImmediatelyField = new CheckboxFormField("IvyDeleteImmediately", 
                                                           "Delete immediately", 
                                                           "The job will not move deleted items into "
                                                           + "the repository trash but delete immediately.", 
                                                                            false);
    
        fields.add(this.deleteImmediatelyField);

    }
 
    @Before
    public void setUp() {
        
        DOMConfigurator.configureAndWatch("log4j.xml"); 
        
    }
    
    /**
     * Test of getId method, of class IvySnapshotRemovalTaskDescriptor.
     */
    @Test
    public void testGetId() {
        
        logger.info("testGetId:");
        String expResult = getId;
        String result = instance.getId();
        assertEquals(expResult, result);
        logger.info("returned as expected: " 
                        + expResult + " and with result:" + result);
    }
 
    /**
     * Test of getName method, of class IvySnapshotRemovalTaskDescriptor.
     */
    @Test
    public void testGetName() {
        logger.info("testGetName:");
        String expResult = getName;
        String result = instance.getName();
        assertEquals(expResult, result);
        logger.info("returned as expected: " 
                        + expResult + " and with result:" + result);
    }
 
    /**
     * Test of formFields method, of class IvySnapshotRemovalTaskDescriptor.
     */
    
    @Test
    public void testFormFields() {
        logger.info("testFormFields:");
      
        List result = fields;
        
        assertEquals(result.size(), instance.formFields().size());
        logger.info("returned as expected: " 
                        + result.size() + " and with result:" + instance.formFields().size());
     
    }
}
