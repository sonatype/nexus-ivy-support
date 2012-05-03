
package com.bosch.tmp.nexus.ivy.repository;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepositoryConfigurator;

/**
 * General repository configurator for IVY.
 *
 * @author wa20277
 */
@Component(role=IVYRepositoryConfigurator.class)
public class IVYRepositoryConfigurator extends AbstractMavenRepositoryConfigurator
{
}