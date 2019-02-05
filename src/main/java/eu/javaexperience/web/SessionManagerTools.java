package eu.javaexperience.web;

import java.util.concurrent.ConcurrentMap;

import eu.javaexperience.text.StringTools;


public class SessionManagerTools
{
	/*
	 * Generálunk egy azonosítót és ha az nincs használatban bejegyezzük. Közben a Map-hez nem férnek hozzá.
	 * Ha egy kulcs már használaban van feloldjuk a zárat, ezzel esélyt adva a hozáféréshez, létrehozunk egy új kulcsot és kezdődik előröl
	 * (régi szöveg)
	 * */
	public static final String assignSessionId(Session session,ConcurrentMap<String, Session> map)
	{
		kint:while(true)
		{
			String sess = StringTools.randomString(25);
			{
				Session ret = map.putIfAbsent(sess, session);
				if(ret != null)
					continue kint;
			}
			session.setId(sess);
			return sess;
		}
	}
}