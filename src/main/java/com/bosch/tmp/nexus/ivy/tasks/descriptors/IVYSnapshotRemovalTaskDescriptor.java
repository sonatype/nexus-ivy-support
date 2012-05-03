package com.bosch.tmp.nexus.ivy.tasks.descriptors;

import java.util.ArrayList;
import java.util.List;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.formfields.CheckboxFormField;
import org.sonatype.nexus.formfields.FormField;
import org.sonatype.nexus.formfields.NumberTextFormField;
import org.sonatype.nexus.formfields.RepoOrGroupComboFormField;
import org.sonatype.nexus.tasks.descriptors.AbstractScheduledTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.ScheduledTaskDescriptor;
 
/**
 * This class sets the graphical user interface for the IVY Remover Task.
 * Next to the fields are informations for users about usage
 * The public static Strings are needed to deliver task options to other classes or requests
 * for example to set Task parameters in the Integration Test or in a SnapshotRemoverTask
 * 
 * @author wa21190
 */
@Component(role=ScheduledTaskDescriptor.class, hint="IvySnapshotRemoval", 
           description="Remove Ivy Snapshots From Repository")
public class IVYSnapshotRemovalTaskDescriptor extends AbstractScheduledTaskDescriptor
{
  /**
   * ID to identify the task.
   */
  public static final String ID = "IVYSnapshotRemoverTask";
  
  /**
   * Id to set or get repository parameters. 
   */
  public static final String REPO_OR_GROUP_FIELD_ID = "IvyRepositoryId";
  
  /**
   * Id to set or get minimum to keep field parameters.
   */
  public static final String MIN_TO_KEEP_FIELD_ID = "IvyMinSnapshotsToKeep";
  
  /**
   * Id to set or get older than days field parameters.
   */
  public static final String KEEP_DAYS_FIELD_ID = "IvyRemoveOlderThanDays";
  
  /**
   * Id to set or get remove if released field parameters.
   */
  public static final String REMOVE_WHEN_RELEASED_FIELD_ID = "IvyRemoveIfReleaseExists";
  
  /**
   * Id to set or get delete immediatly field parameters.
   */
  public static final String DELETE_IMMEDIATELY = "IvyDeleteImmediately";

  
  private RepoOrGroupComboFormField repoField; 

  private NumberTextFormField minToKeepField; 

  private NumberTextFormField keepDaysField;

  private CheckboxFormField removeWhenReleasedField; 
  
  private CheckboxFormField deleteImmediatelyField;

  
  @Override
  public String getId()
  {
    return "IVYSnapshotRemoverTask";
  }

    @Override
  public String getName()
  {
    return "Remove Ivy Snapshots From Repository";
  }

    @Override
  public List<FormField> formFields()
  {
    List fields = new ArrayList();
    
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

    return fields;
  }
}