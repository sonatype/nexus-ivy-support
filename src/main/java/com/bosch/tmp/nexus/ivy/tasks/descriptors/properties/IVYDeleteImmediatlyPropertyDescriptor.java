
package com.bosch.tmp.nexus.ivy.tasks.descriptors.properties;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.tasks.descriptors.properties.AbstractNumberPropertyDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.ScheduledTaskPropertyDescriptor;

/**
 *
 * @author wa21190
 */
@Component(role=ScheduledTaskPropertyDescriptor.class, hint="IvyDeleteImmediately", instantiationStrategy="per-lookup")
public class IVYDeleteImmediatlyPropertyDescriptor extends AbstractNumberPropertyDescriptor
{
  /**
   * Id to identify field for usage.
  */
  public static final String ID = "IvyDeleteImmediately";

  public IVYDeleteImmediatlyPropertyDescriptor()
  {
    setHelpText("The job will not move deleted items into "
                + "the repository trash but delete immediately.");
    setRequired(false);
  }

    @Override
  public String getId()
  {
    return "IvyDeleteImmediately";
  }

    @Override
  public String getName()
  {
    return "Delete immediately";
  }
}
