package eu.javaexperience.web.dispatch.url;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import eu.javaexperience.arrays.ArrayTools;
import eu.javaexperience.collection.iterator.ArrayIterator;
import eu.javaexperience.dispatch.Dispatcher;
import eu.javaexperience.semantic.references.MayNotModified;
import eu.javaexperience.semantic.references.MayNull;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.RequestContext;

/**
 * A 0. parent kiemelt szerepű, ő az elsődleges csatlakozási pont, ha egy objektum le akarja kérni az
 * útvonalát akkor minden URLNode-nál az elő parent elem lesz figyelembe véve. 
 * */
public class URLNode implements URLLink, Dispatcher<Context>
{
	//SiteFacility owner;
	
	String nodeName;
	
	protected boolean isDomainNode = false;
	
	public static final URLNode[] emptyURLNodeArray = new URLNode[0];
	
	public static final URLLink[] emptyURLLinkArray = new URLLink[0];
	
	protected URLNode[] childs = emptyURLNodeArray;
	
	protected URLNode[] parents = emptyURLNodeArray;

	protected URLNodePattern pattern;
	
	public URLNodePattern getPattern()
	{
		return pattern;
	}
	
	public void setURLNodePattern(URLNodePattern patt)
	{
		this.pattern = patt;
	}
	
	protected URLNode(){};
	
	public URLNode getFistParent()
	{
		if(parents.length == 0)
			return null;
		
		return parents[0];
	}
	
	
	public final int depth()
	{
		int i=0;
		for(URLNode n = this;n != null;n = n.getFistParent(),i++);
		return i;
	}
	
	public URLNode(String nodeName)
	{
		this.nodeName = nodeName;
		pattern = MultiUrlNodePattern.simpleCaseSensitiveString(nodeName);
	}

	public URLNode(String nodeName,boolean caseSensitive)
	{
		this.nodeName = nodeName;
		pattern = (caseSensitive?
					MultiUrlNodePattern.simpleCaseSensitiveString(nodeName)
						:
					MultiUrlNodePattern.simpleCaseInsensitiveString(nodeName));
	}
	
	public URLNode(String nodeName,Pattern regex)
	{
		this.nodeName = nodeName;
		pattern = MultiUrlNodePattern.fromRegex(regex);
	}
	
	/*public final void setOwner(SiteFacility owner)
	{
		this.owner = owner;
		for(URLNode c:childs)
			c.setOwner(owner);
	}
	
	protected SiteFacility getOwner()
	{
		return this.owner;
	}*/
	
	public final boolean canHandleRequest(Context ctx)
	{
		return pattern.match(ctx);
	}
	
	public boolean shallFinishRequest(RequestContext ctx)
	{
		return ctx.getRequestUrl().getRemainElementNum() < 1;
	}
	
	public String getNodeName()
	{
		return nodeName;
	}

	@Override
	public @MayNotModified URLNode[] childs()
	{
		return childs;//Arrays.copyOf(childs, childs.length);
	}
	
	@Override
	public @MayNotModified URLNode[] parents()
	{
		return parents;//Arrays.copyOf(parents, parents.length);
	}
	
	public void removeChild(URLNode node)
	{
		for(URLNode n:childs)
			if(node.equals(n))
			{
				//node.setOwner(null);
				childs = ArrayTools.withoutElementIdentically(childs, node);
				node.parents = ArrayTools.withoutElementIdentically(node.parents, this);
				break;
			}
	}
	
	public void addChild(URLNode node)
	{
		if(node == null)
			return;
		
		for(URLNode n:childs)
			if(node.equals(n))
				return;

		//node.setOwner(owner);
		childs = ArrayTools.arrayAppend(childs, node);
		node.parents = ArrayTools.arrayAppend(node.parents, this);
	}
	
	public boolean dispatch(Context ctx)
	{
		return tryDispatchSubNodes(ctx);
	}
	
	public boolean tryDispatchSubNodes(Context ctx)
	{
		PreparedURL url = ctx.getRequestUrl();
		//url.jumpNextURLElement();
		String node = url.getCurrentURLElement();//url.tryGetNextURLElement();
		if(null == node)
		{
			return false;//TODO processEndpoint
		}
		
		if(ctx.getRequestUrl().isPointerValid())
		{
			for(URLNode ch:childs)
			{
				if(node.equals(ch.getNodeName()))
				{
					url.jumpNextURLElement();
					//dispatch anyway
					ch.dispatch(ctx);
					url.jumpPrevURLElement();
					return true;
				}
			}
		}
		
		return false;
	}
	
	@Override
	public boolean isDomainNode()
	{
		return this.isDomainNode;
	}
	
	public void setDomainNode(boolean dom)
	{
		this.isDomainNode = dom;
	}
	
	public String getPathWithDomain(@MayNull URLLink[] opt)
	{
		URLLink[] lst = opt==null?getRawPath():opt;
		int ni = 0;
		
		for(int i=0;i<lst.length;i++)
			if(lst[i] == null)
			{
				ni = i;
				break;
			}
		
		StringBuilder path = new StringBuilder();
		
		for(int i=ni-1;i>-1;i--)
		{
			path.append("/");
			path.append(lst[i].getNodeName());
		}
		
		StringBuilder domain = new StringBuilder();
		
		for(int i=ni+1;i<lst.length;i++)
		{
			if(domain.length() > 0)
				domain.append(".");
			
			domain.append(lst[i].getNodeName());
		}
		
		return domain.toString()+path.toString();
	}
	
	public static String getDomain(URLLink[] lst)
	{
		int ni = 0;
		for(int i=0;i<lst.length;i++)
			if(lst[i] == null)
			{
				ni = i;
				break;
			}
		
		StringBuilder domain = new StringBuilder();
		
		for(int i=ni+1;i<lst.length;i++)
		{
			if(domain.length() > 0)
				domain.append(".");
			
			domain.append(lst[i].getNodeName());
		}
		
		return domain.toString();
	}
	
	public static String getPath(URLLink[] lst)
	{
		int ni = lst.length;
		for(int i=0;i<lst.length;i++)
			if(lst[i] == null)
			{
				ni = i;
				break;
			}
		
		StringBuilder path = new StringBuilder();
		
		for(int i=ni-1;i>-1;i--)
		{
			path.append("/");
			path.append(lst[i].getNodeName());
		}
		
		return path.toString();
	}
	
	public String getPath()
	{
		URLLink[] lst = getRawPath();
		StringBuilder sb = new StringBuilder();
		int sn = lst.length-1;
		boolean ps = false;
		for(int i=sn;i>-1;i--)
		{
			URLLink n = lst[i];
			if(n == null)
			{
				ps = true;
				continue;
			}

			if(!ps)
				continue;
			
			sb.append("/");
			
			sb.append(n.getNodeName());
		}
		
		return sb.toString();
	}
	
	/**
	 * 
	 * */
	public URLLink[] getRawPath()
	{
		ArrayList<URLLink> path = new ArrayList<>();
		this.appendRawURLPath(path,false,false);
		return path.toArray(emptyURLNodeArray);
	}
	
	protected String appendRawURLPath(List<URLLink> list,boolean nullAdded, boolean generateUrl)
	{
		if(isDomainNode && ! nullAdded)
		{
			list.add(null);
			nullAdded = true;
		}
		
		list.add(this);
		
		if(parents.length != 0)
			return parents[0].appendRawURLPath(list,nullAdded,generateUrl);
		
		return processCanonicalUrl(list);
	}
	
	protected String processCanonicalUrl(List<URLLink> nodes)
	{		
		URLLink[] lnk = nodes.toArray(URLNode.emptyURLLinkArray);
		String path = getPath(lnk);
		return path.toString();
	}
	
	@Override
	public Iterator<URLLink> iterateOwnedElements()
	{
		return new ArrayIterator<URLLink>(childs);
	}
	
	@Override
	public String getCanonicalURL()
	{
		return appendRawURLPath(new ArrayList<URLLink>(), false, true);
	}
	
	@Override
	public String toString()
	{
		return "URLNode: "+getNodeName();
	}
}