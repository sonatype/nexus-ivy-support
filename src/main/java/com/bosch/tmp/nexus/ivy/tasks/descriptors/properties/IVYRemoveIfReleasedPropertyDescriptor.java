
package com.bosch.tmp.nexus.ivy.tasks.descriptors.properties;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.tasks.descriptors.properties.AbstractBooleanPropertyDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

/**
 *
 * @author wa21190
 */
@Component(role=ScheduledTaskPropertyDescriptor.class, hint="IvyRemoveIfReleased", instantiationStrategy="per-lookup")
public class IVYRemoveIfReleasedPropertyDescriptor extends AbstractBooleanPropertyDescriptor
{
  /**
   * Id to identify field for usage.
   */
  public static final String ID = "IvyRemoveIfReleaseExists";

  public IVYRemoveIfReleasedPropertyDescriptor()
  {
    setHelpText("The job will purge all snapshots that have a "
            + "corresponding released artifact "
            + "(same version not including a serial snapshot number like -1234).");
    setRequired(false);
  }

    @Override
  public String getId()
  {
    return "IvyRemoveIfReleaseExists";
  }

    @Override
  public String getName()
  {
    return "Remove if released";
  }
}
