package controllers;

import java.util.Set;

import models.MusicCategory;

import play.mvc.*;

import views.html.*;

public class ApplicationController extends Controller {
  
  public static Result index() 
  {
	  return category(0);
  }
  
  public static Result category(Integer parentCategoryId)
  {
	  MusicCategory parentCategory = parentCategoryId <= 0 ? null : MusicCategory.find.where().eq("id", parentCategoryId).findUnique();
	  
	  int parentCategoriesCount = 4;
	  Set<MusicCategory> categories = MusicCategory.find.where().eq("parent_id", parentCategoryId).setMaxRows(parentCategoriesCount).findSet();
	  
	  return ok( Application_index.render(parentCategory, categories));	  
  }
  
  public static Result playlistByCategory(Integer categoryId)
  {
	  MusicCategory category = MusicCategory.find.where().eq("id", categoryId).findUnique();
	  
	  return ok( Application_playlistByCategory.render(category) );
  }
  
}