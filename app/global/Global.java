package global;

import controllers.components.ForceHttps;
import play.*;
import play.mvc.*;
import play.mvc.Http.RequestHeader;
import play.mvc.Results;


public class Global extends GlobalSettings {
	
	static final String HEXES = "0123456789ABCDEF";
	
	public static final String JSON_CONTENT_TYPE = "application/json";

	// it's updated in controllers.components.ControllerFilter
	private static boolean isSecure = false;
	
	public static String getHex( byte [] raw ) 
	{
	    if ( raw == null ) {
	      return null;
	    }
	    final StringBuilder hex = new StringBuilder( 2 * raw.length );
	    for ( final byte b : raw ) {
	      hex.append(HEXES.charAt((b & 0xF0) >> 4))
	         .append(HEXES.charAt((b & 0x0F)));
	    }
	    return hex.toString();
	 }

	public static boolean isSecure() {
		return isSecure;
	}

	public static void setSecure(boolean isSecure) {
		Global.isSecure = isSecure;
	}	
	
	public Result onError(RequestHeader request, Throwable t) {
		
        return Results.badRequest( views.html.Errors.error.render(t) );
    }
	
	public Result onHandlerNotFound(RequestHeader request) {
        return Results.notFound( views.html.Errors.handlerNotFound.render() );
    }
	
}
