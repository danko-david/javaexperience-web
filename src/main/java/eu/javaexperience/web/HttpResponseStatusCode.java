package eu.javaexperience.web;

import java.util.regex.Pattern;

public enum HttpResponseStatusCode
{
	_100_coninue(100,"HTTP/1.1 100 Continue"),
	_101_switching_protocols(101,"HTTP/1.1 101 Switching Protocols"),
	_200_OK(200,"HTTP/1.1 200 OK"),
	_201_created(201,"HTTP/1.1 201 Created"),
	_202_accepted(202,"HTTP/1.1 202 Accepted"),
	_203_non_authoritative_information(203,"HTTP/1.1 203 Non-Authoritative Information"),
	_204_no_content(204,"HTTP/1.1 204 No Content"),
	_205_reset_content(205,"HTTP/1.1 205 Reset Content"),
	_206_partial_content(206,"HTTP/1.1 206 Partial Content"),
	_300_multiple_choices(300,"HTTP/1.1 300 Multiple Choices"),
	_301_moved_permanently(301,"HTTP/1.1 301 Moved Permanently"),
	_302_found(302,"HTTP/1.1 302 Found"),
	_303_see_other(303,"HTTP/1.1 303 See Other"),
	_304_not_modified(304,"HTTP/1.1 304 Not Modified"),
	_305_use_proxy(305,"HTTP/1.1 305 Use Proxy"),
	_306_unused(306,"HTTP/1.1 306 NotUsed"),
	_307_temporary_redirect(307,"HTTP/1.1 307 Temporary Redirect"),
	_400_bad_request(400,"HTTP/1.1 400 Bad Request"),
	_401_unauthorized(401,"HTTP/1.1 401 Unauthorized"),
	_402_payment_required(402,"HTTP/1.1 402 Payment Required"),
	_403_forbidden(403,"HTTP/1.1 403 Forbidden"),
	_404_not_found(404,"HTTP/1.1 404 Not Found"),
	_405_method_not_allowed(405,"HTTP/1.1 405 Method Not Allowed"),
	_406_not_acceptable(406,"HTTP/1.1 406 Not Acceptable"),
	_407_proxy_authentication_required(407,"HTTP/1.1 407 Proxy Authentication Required"),
	_408_request_timeout(408,"HTTP/1.1 408 Request Timeout"),
	_409_conflict(409,"HTTP/1.1 409 Conflict"),
	_410_gone(410,"HTTP/1.1 410 Gone"),
	_411_length_required(411,"HTTP/1.1 411 Length Required"),
	_412_precondition_failed(412,"HTTP/1.1 412 Precondition Failed"),
	_413_request_entity_too_large(413,"HTTP/1.1 413 Request Entity Too Large"),
	_414_request_URI_too_long(414,"HTTP/1.1 414 Request-URI Too Long"),
	_415_unsupported_media_type(415,"HTTP/1.1 415 Unsupported Media Type"),
	_416_requested_range_not_satisfiable(416,"HTTP/1.1 416 Requested Range Not Satisfiable"),
	_417_expectation_failed(417,"HTTP/1.1 417 Expectation Failed"),
	_500_internal_server_error(500,"HTTP/1.1 500 Internal Server Error"),
	_501_not_implemented(501,"HTTP/1.1 501 Not Implemented"),
	_502_bad_gateway(502,"HTTP/1.1 502 Bad Gateway"),
	_503_service_unavailable(503,"HTTP/1.1 503 Service Unavailable"),
	_504_gateway_timeout(504,"HTTP/1.1 504 Gateway Timeout"),
	_505_HTTP_version_not_supported(505,"HTTP/1.1  505 HTTP Version Not Supported");

	private final String val;
	private final int num;
	
	private HttpResponseStatusCode(int num, String val)
	{
		this.num = num;
		this.val = val;
		this.descr = Pattern.compile("\\d{3,3}").split(val)[1];
	}

	public int getStatus()
	{
		return num;
	}
	
	public String toHeaderLineWhitoutLF()
	{
		return val;
	}
	
	@Override
	public String toString()
	{
		return val;
	}

	protected String descr;
	
	public String getDescription()
	{
		return descr;
	}

}