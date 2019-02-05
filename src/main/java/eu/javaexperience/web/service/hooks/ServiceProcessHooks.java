package eu.javaexperience.web.service.hooks;

import java.util.Map;

import eu.javaexperience.interfaces.simple.getBy.GetBy1;
import eu.javaexperience.interfaces.simple.getBy.GetBy2;
import eu.javaexperience.interfaces.simple.publish.SimplePublish1;
import eu.javaexperience.interfaces.simple.publish.SimplePublish2;
import eu.javaexperience.web.Context;
import eu.javaexperience.web.Session;

public interface ServiceProcessHooks
{
	public SimplePublish1<Context> beforeRequestDispatchStart();
	public SimplePublish2<Session, Context> onSessionRefreshLastUse();
	public GetBy1<Boolean, Context> applyView();
	public SimplePublish1<Context> beforeHeaderSent();
	public SimplePublish1<Context> rightBeforeFinishOperation();
	public SimplePublish1<Context> afterRequestEnd();
	
	public GetBy2<Map<String,Object>, Context, Map<String,Object>> wrapContextForRender();
}
