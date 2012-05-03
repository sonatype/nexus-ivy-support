
package com.bosch.tmp.nexus.ivy.tasks.descriptors.properties;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.tasks.descriptors.properties.AbstractNumberPropertyDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

/**
 * 
 * @author wa21190
 */
@Component(role=ScheduledTaskPropertyDescriptor.class, 
           hint="IvySnapshotRetentionDays", instantiationStrategy="per-lookup")
public class IVYSnapshotRetentionDaysPropertyDescriptor extends AbstractNumberPropertyDescriptor
{
    /**
     * Id to identify field for usage.
     */
    public static final String ID = "IvyRemoveOlderThanDays";

  public IVYSnapshotRetentionDaysPropertyDescriptor()
  {
    setHelpText("The job will purge all snapshots older than the entered number of days, "
                    + "but will obey to Min. count of snapshots to keep.");
    setRequired(false);
  }

    @Override
  public String getId()
  {
    return "IvyRemoveOlderThanDays";
  }

    @Override
  public String getName()
  {
    return "Snapshot retention (days)";
  }
}
