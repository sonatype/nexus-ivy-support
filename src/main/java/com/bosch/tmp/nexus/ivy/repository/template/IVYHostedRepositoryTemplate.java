package com.bosch.tmp.nexus.ivy.repository.template;

import com.bosch.tmp.nexus.ivy.repository.IVYRepository;
import com.bosch.tmp.nexus.ivy.repository.IVYRepositoryConfiguration;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepositoryExternalConfigurationHolderFactory;
import org.sonatype.nexus.configuration.model.DefaultCRepository;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.maven.maven2.M2RepositoryConfiguration;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.templates.repository.DefaultRepositoryTemplateProvider;
import org.sonatype.nexus.templates.repository.maven.Maven2HostedRepositoryTemplate;

/**
 * TMP-IVY specific template. The template is based on the Maven2 default format with 
 * a smaller change for snapshot.
 *
 * @author wa20277
 */
public class IVYHostedRepositoryTemplate extends Maven2HostedRepositoryTemplate
{
    /**
     * 
     * @param provider a template provider provides a set of templates for one implementation
     * @param id like "a-repository"
     * @param description like "TMP-IVY Repository"
     * @param repositoryPolicy "snaphot, mixed or release"
     */
    public IVYHostedRepositoryTemplate(DefaultRepositoryTemplateProvider provider, String id,
            String description, RepositoryPolicy repositoryPolicy)
    {
        super(provider, id, description, repositoryPolicy);
    }
    /**
     * 
     * @param forWrite true if the configuration is writeable
     * @return returns external configuration writeable or not
     */
    @Override
    public M2RepositoryConfiguration getExternalConfiguration(boolean forWrite)
    {
        return (M2RepositoryConfiguration) getCoreConfiguration().getExternalConfiguration().getConfiguration(forWrite);
    }
    /**
     * 
     * @return a new IVYRepositoryConfiguration
     */
    @Override
    protected CRepositoryCoreConfiguration initCoreConfiguration()
    {
        CRepository repo = new DefaultCRepository();

        repo.setId("");
        repo.setName("");

        repo.setProviderRole(Repository.class.getName());
        repo.setProviderHint(IVYRepository.ID);

        Xpp3Dom ex = new Xpp3Dom("externalConfiguration");
        repo.setExternalConfiguration(ex);

        IVYRepositoryConfiguration exConf = new IVYRepositoryConfiguration(ex);

        if (getRepositoryPolicy() != null)
        {
            exConf.setRepositoryPolicy(getRepositoryPolicy());
        }

        repo.externalConfigurationImple = exConf;

        repo.setWritePolicy(RepositoryWritePolicy.ALLOW_WRITE_ONCE.name());
        repo.setNotFoundCacheTTL(1440);
        repo.setIndexable(true);
        repo.setSearchable(true);
        

        return new CRepositoryCoreConfiguration(getTemplateProvider().
                getApplicationConfiguration(), repo, new CRepositoryExternalConfigurationHolderFactory()
        {
            @Override
            public IVYRepositoryConfiguration createExternalConfigurationHolder(CRepository config)
            {
                return new IVYRepositoryConfiguration((Xpp3Dom) config.getExternalConfiguration());
            }
        });

    }
}
