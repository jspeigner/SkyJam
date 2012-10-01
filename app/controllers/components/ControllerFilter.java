package controllers.components;

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
		
		// allow http and https origins
		String serviceUrl = ctx.request().host() + "/"; 
		
		ctx.response().setHeader("Access-Control-Allow-Origin", "http://"+serviceUrl+" https://"+serviceUrl );

		return delegate.call(ctx);
	}


}
