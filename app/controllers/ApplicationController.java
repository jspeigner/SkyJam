package controllers;

import play.mvc.*;

import views.html.*;

public class ApplicationController extends Controller {
  
  public static Result index() 
  {
    
	  return ok(index.render());
    
  }
  
}