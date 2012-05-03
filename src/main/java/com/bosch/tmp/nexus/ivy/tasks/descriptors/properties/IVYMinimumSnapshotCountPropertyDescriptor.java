
package com.bosch.tmp.nexus.ivy.tasks.descriptors.properties;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.tasks.descriptors.properties.AbstractNumberPropertyDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

/**
 *
 * @author wa21190
 */
@Component(role=ScheduledTaskPropertyDescriptor.class, 
        hint="IvyMinimumSnapshotCount", instantiationStrategy="per-lookup")
public class IVYMinimumSnapshotCountPropertyDescriptor extends AbstractNumberPropertyDescriptor
{
  /**
   * Id to identify field for usage.
  */
  public static final String ID = "IvyMinSnapshotsToKeep";

  public IVYMinimumSnapshotCountPropertyDescriptor()
  {
    setHelpText("Minimum number of snapshots to keep for one IvyGAV.");
    setRequired(false);
  }

    @Override
  public String getId()
  {
    return "IvyMinSnapshotsToKeep";
  }

    @Override
  public String getName()
  {
    return "Minimum snapshot count";
  }
}
