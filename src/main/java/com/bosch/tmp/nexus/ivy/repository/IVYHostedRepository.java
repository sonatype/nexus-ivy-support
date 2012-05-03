
package com.bosch.tmp.nexus.ivy.repository;

import org.sonatype.nexus.plugins.RepositoryType;
import org.sonatype.nexus.proxy.repository.HostedRepository;

/**
 * Class to register the new TMP-IVY content type for hosted repositories only.
 *
 * @author wa20277
 */
@RepositoryType(pathPrefix="repositories")
public interface IVYHostedRepository extends HostedRepository
{
}