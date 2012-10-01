package controllers.components;

import global.Global;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import play.Play;
import play.mvc.*;
import play.mvc.Http.Context;
import play.mvc.Http.Request;

public class ControllerFilter extends Action.Simple {


	@Override
	public Result call(Context ctx) throws Throwable {
		
		Global.setSecure( ForceHttps.isHttpsRequest(ctx.request()) );

		String serviceUrl = ctx.request().host() + "/"; 
		
		// allow http and https origins - http://tools.ietf.org/html/rfc6454#section-7.1		
		ctx.response().setHeader("Access-Control-Allow-Origin", ( Global.isSecure() ? "http://" : "https://" ) + serviceUrl );

		return delegate.call(ctx);
	}


}
