package security;

import controllers.BaseController;
import controllers.UserController;
import controllers.routes;
import be.objectify.deadbolt.AbstractDeadboltHandler;
import be.objectify.deadbolt.DynamicResourceHandler;
import be.objectify.deadbolt.models.RoleHolder;


import play.mvc.Http;
import play.mvc.Result;


public class MyDeadboltHandler extends AbstractDeadboltHandler
{
	

	
    public Result beforeRoleCheck(Http.Context context)
    {
    	 Http.Context.current.set(context);
        // returning null means that everything is OK.  Return a real result if you want a redirect to a login page or
        // somewhere else
        return null;
    }

    public RoleHolder getRoleHolder(Http.Context context)
    {
    	Http.Context.current.set(context);
    	// System.out.println("Get User" + UserController.getAuthUser() );
    	
        return UserController.getAuthUser();
    }

    public DynamicResourceHandler getDynamicResourceHandler(Http.Context context)
    {
    	 Http.Context.current.set(context);
        
    	return null;
    }

    public Result onAccessFailure(Http.Context context, String content)
    {
    	 Http.Context.current.set(context);
        // you can return any result from here - forbidden, etc

    	 //TODO - create a better way to determine the required role holder    	 
    	 String path = context.request().path();
    	 
    	 if( path.startsWith("/admin")){
        	 return BaseController.pjaxRedirect(routes.AdminController.login());    		 
    	 } else {
        	 return BaseController.pjaxRedirect(routes.UserController.login());
    	 }
    	 

    }
}