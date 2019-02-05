package eu.javaexperience.web;

public enum Headers
{
	AccessControlAllowOrigin("Access-Control-Allow-Origin"),
	AcceptRanges("Accept-Ranges"),
	Age("Age"),
	Allow("Allow"),
	CacheControl("Cache-Control"),
	Connection("Connection"),
	ContentEncoding("Content-Encoding"),
	ContentLanguage("Content-Language"),
	ContentLength("Content-Length"),
	ContentLocation("Content-Location"),
	ContentMD5("Content-MD5"),
	ContentDisposition("Content-Disposition"),
	ContentRange("Content-Range"),
	ContentType("Content-Type"),
	Date("Date"),
	ETag("ETag"),
	Expires("Expires"),
	LastModified("Last-Modified"),
	Link("Link"),
	Location("Location"),
	P3P("P3P"),
	Pragma("Pragma"),
	ProxyAuthenticate("Proxy-Authenticate"),
	Refresh("Refresh"),
	RetryAfter("Retry-After"),
	Server("Server"),
	SetCookie("Set-Cookie"),
	Status("Status"),
	StrictTransportSecurity("Strict-Transport-Security"),
	Trailer("Trailer"),
	TransferEncoding("Transfer-Encoding"),
	Vary("Vary"),
	Via("Via"),
	Warning("Warning"),

	/**
	 * WWW-Authenticate: Basic realm="insert realm"
	 * */
	WWWAuthenticate("WWW-Authenticate");

	private final String val;
	private Headers(String v)
	{
		val = v;
	}

	public String getHeaderName()
	{
		return val;
	}
	
	@Override
	public String toString()
	{
		return val;
	}
}