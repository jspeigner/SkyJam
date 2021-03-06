package controllers;

import global.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import models.AppModel;
import models.MusicCategory;
import models.Playlist;
import models.PlaylistSong;
import models.StorageObject;
import models.behavior.ImageMetadata;

import play.Logger;
import play.Play;
import play.api.Routes;
import play.mvc.*;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

import views.html.*;

public class ApplicationController extends BaseController {
  
  public static Result index() 
  {
	  return category(null);
  }
  
  public static Result category(Integer parentCategoryId)
  {
	  if( ( parentCategoryId != null ) && ( Playlist.findCountByMusicCategoryId(parentCategoryId) > 0 ) )
	  {
		  // redirect on step 3 - playlists
		  // return redirect( controllers.routes.ApplicationController.playlistByCategory(parentCategoryId) );
		  
		  return playlistByCategory(parentCategoryId);
	  }
	  
	  MusicCategory parentCategory = parentCategoryId == null ? null : MusicCategory.find.where().eq("id", parentCategoryId).findUnique();
	  
	  List<MusicCategory> categories = MusicCategory.find.where().eq("parent", parentCategory).eq("type", MusicCategory.Type.activity).findList();
	  
	  if( categories.size() == 0 )
	  {
		  // there are no sub-categories
		  // return redirect( controllers.routes.ApplicationController.playlistByCategory(parentCategoryId) );
		  
		  return playlistByCategory(parentCategoryId);
	  }
	  
	  return ok( views.html.Application.index.render(parentCategory, categories));	  
  }
  
  public static Result playlistByCategory(Integer categoryId)
  {
	  MusicCategory category = MusicCategory.find.where().eq("id", categoryId).findUnique();
	  
	  int maxPlaylistsCount = 4;
	  List<Playlist> playlists = Playlist.find.where().eq("musicCategories.id", categoryId).eq("status", Playlist.Status.Public).setMaxRows(maxPlaylistsCount).findList();
	  
	  
	  return ok( views.html.Application.playlistByCategory.render(category, playlists) );
  }
  
  public static Result twitterCallback()
  {
	  return ok("");  
  }

  
}