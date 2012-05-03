package com.bosch.tmp.nexus.ivy.repository;

import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepositoryConfiguration;

/**
 * General repository configuration for IVY.
 *
 * @author wa20277
 */
public class IVYRepositoryConfiguration extends AbstractMavenRepositoryConfiguration
{
    /** Set Maven configuration for repository. 
     * Reference to ancestor class AbstractMavenRepositoryConfiguration. */
    public IVYRepositoryConfiguration(Xpp3Dom configuration)
    {
        super(configuration);
    }
}
