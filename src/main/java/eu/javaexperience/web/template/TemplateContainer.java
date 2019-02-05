package eu.javaexperience.web.template;

/**
 * Designed for create Enumeration, which enumerates
 * all available container in template (for prevent typos)
 * use with {@link SiteObjectTools#finishWithLastViewSet(hu.ddsi.srv.web.cms.genericinterfaces.Context, TemplateContainer, Object)}
 * */
public interface TemplateContainer
{
	public String name();
}