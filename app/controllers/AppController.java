package controllers;

import play.mvc.*;

public class AppController extends Controller {

	public static final String PJAX_RESPONSE_HEADER  = "X-PJAX-URL";
	public static final String PJAX_RESPONSE_REDIRECT_HEADER = "X-PJAX-REDIRECT";
	public static final String PJAX_REQUEST_HEADER = "X-PJAX";
	
	public static boolean setPjaxUrl(String url)
	{
		response().setHeader( PJAX_RESPONSE_HEADER, url );
		return true;
	}
	
	public static boolean setPjaxUrl(Call url)
	{
		response().setHeader( PJAX_RESPONSE_HEADER, url.toString() );
		return true;
	}
	
	public static Result pjaxRedirect( Call url )
	{
		
		response().setHeader( PJAX_RESPONSE_REDIRECT_HEADER, url.toString() );
		setPjaxUrl(url);
		return ok("");

	}
	
}
