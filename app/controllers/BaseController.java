package controllers;



import controllers.components.ControllerFilter;

import play.mvc.*;
import play.mvc.Http.Context;

@With(ControllerFilter.class)
public class BaseController extends Controller {

	public static final String PJAX_RESPONSE_HEADER  = "X-PJAX-URL";
	public static final String PJAX_RESPONSE_REDIRECT_HEADER = "X-PJAX-REDIRECT";
	public static final String PJAX_REQUEST_HEADER = "X-PJAX";
	
	public static long DAY_IN_MS = 1000 * 60 * 60 * 24;
	
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
		
		String isPjax =  ( request().queryString().containsKey("_pjax") ) ? request().queryString().get("_pjax")[0] : "";
		
		
		
		if( isPjax != "" )
		{
			response().setHeader( PJAX_RESPONSE_REDIRECT_HEADER, url.toString() );
			setPjaxUrl(url);
			
			return ok("");
		}
		else
		{
			return redirect(url);
		}

	}
	
	

}
