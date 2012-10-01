package controllers.components;

import org.apache.commons.lang.StringUtils;

import play.Play;
import play.mvc.*;
import play.mvc.Http.Context;
import play.mvc.Http.Request;

public class ForceHttps extends Action.Simple {

	/**
	 *  Elastic Load Balancing HTTPS header
	 * https://forums.aws.amazon.com/ann.jspa?annID=805
	 */
	private static String SSL_PROTO_REQUEST_HEADER = "X-Forwarded-Proto";

	@Override
	public Result call(Context ctx) throws Throwable {

		
		
		if (!isHttpsRequest(ctx.request())) {
			
			return redirect("https://" + ctx.request().host() + ctx.request().uri());
		}

		return delegate.call(ctx);
	}

	public static boolean isHttpsRequest(Request request) {

		if (Play.isDev()) {
			return false;
		}

		if ( ( request.getHeader(SSL_PROTO_REQUEST_HEADER) != null ) && StringUtils.contains( request.getHeader(SSL_PROTO_REQUEST_HEADER), "https")) {
			return true;
		}

		return false;
	}
}
