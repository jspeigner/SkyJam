package controllers;

import play.mvc.*;
import views.html.*;

public class PageController extends BaseController {

	
	public static Result termsOfUse()
	{
		return ok(views.html.Page.termsOfUse.render());
	}
	
	public static Result privacyPolicy()
	{
		return ok(views.html.Page.privacyPolicy.render());
	}
	
	public static Result about()
	{
		return ok(views.html.Page.about.render());
	}
	
	public static Result contactUs()
	{
		return ok(views.html.Page.contactUs.render());
	}
	
	public static Result copyrightInfringment(){
		return ok(views.html.Page.copyrightInfringment.render());
	}
}
