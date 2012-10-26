package controllers;

import java.util.Date;

import models.User;
import models.UserRole;
import controllers.UserController.Login;
import be.objectify.deadbolt.actions.Restrict;
import play.mvc.Result;
import play.data.Form;

public class AdminController extends BaseController {

	
	@Restrict(UserRole.ROLE_ADMIN)
	public static Result dashboard(){
		return ok(views.html.Admin.dashboard.render());
	}
	
	public static Result login() {
        return ok( views.html.Admin.login.render(form(UserController.Login.class)) );
    }
    
	@Restrict(UserRole.ROLE_ADMIN)
    public static Result logout() {
    	
    	
        UserController.setAuthUser(null);
        
        flash("success", "You've been logged out");
        
        return redirect( routes.AdminController.login() );
    }	

    public static Result authenticate() {
    	
        Form<Login> loginForm = form(Login.class).bindFromRequest();
        
        if(loginForm.hasErrors()) 
        {
            return ok(views.html.Admin.login.render(loginForm));
        }
        else 
        {
        	User user = loginForm.get().getUser();
        	
        	if( user.roles.contains(UserRole.findByName( UserRole.ROLE_ADMIN ))){

            	UserController.setAuthUser( user );
            	
            	user.setLastLoginDate(new Date());
            	user.update();
                
                return  redirect( routes.AdminController.dashboard() );        		
        		
        	} else {
        		
        		loginForm.reject("Your account has no admin privileges");
        		return ok(views.html.Admin.login.render(loginForm));
        	}
        	

        }
    }

	
}
