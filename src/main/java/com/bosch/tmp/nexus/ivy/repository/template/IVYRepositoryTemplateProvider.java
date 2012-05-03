package com.bosch.tmp.nexus.ivy.repository.template;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.templates.TemplateProvider;
import org.sonatype.nexus.templates.TemplateSet;
import org.sonatype.nexus.templates.repository.DefaultRepositoryTemplateProvider;

/**
 * Template provider to make the new TMP-IVY specific template available in Nexus.
 *
 * @author wa20277
 */
@Component(role = TemplateProvider.class, hint = "default-repository")
public class IVYRepositoryTemplateProvider extends DefaultRepositoryTemplateProvider
{

    @Override
    public TemplateSet getTemplates()
    {
        TemplateSet templates = new TemplateSet(null);
        try
        {
            templates.add(new IVYHostedRepositoryTemplate(
                    this, "default_hosted_snapshot", "TMP-IVY (hosted, snapshot)", RepositoryPolicy.SNAPSHOT));

            templates.add(new IVYHostedRepositoryTemplate(
                    this, "default_hosted_snapshot", "TMP-IVY (hosted, release)", RepositoryPolicy.RELEASE));
        }
        catch (RuntimeException e)
        {
            throw new RuntimeException("Error on register TMP-IVY repository templates.",  e);
        }

        return templates;
    }
}
