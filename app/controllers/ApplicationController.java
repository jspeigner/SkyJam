package controllers;

import java.util.Set;

import models.MusicCategory;
import models.Playlist;
import models.PlaylistSong;

import play.Logger;
import play.api.Routes;
import play.mvc.*;

import views.html.*;

public class ApplicationController extends Controller {
  
  public static Result index() 
  {
	  return category(0);
  }
  
  public static Result category(Integer parentCategoryId)
  {
	  if( Playlist.findCountByMusicCategoryId(parentCategoryId) > 0 )
	  {
		  // redirect on step 3 - playlists
		  return redirect( controllers.routes.ApplicationController.playlistByCategory(parentCategoryId) );
	  }
	  
	  MusicCategory parentCategory = parentCategoryId <= 0 ? null : MusicCategory.find.where().eq("id", parentCategoryId).findUnique();
	  
	  int parentCategoriesCount = 4;
	  Set<MusicCategory> categories = MusicCategory.find.where().eq("parent_id", parentCategoryId).setMaxRows(parentCategoriesCount).findSet();
	  
	  if( categories.size() == 0 )
	  {
		  // there are no sub-categories
		  return redirect( controllers.routes.ApplicationController.playlistByCategory(parentCategoryId) );
	  }
	  
	  return ok( Application_index.render(parentCategory, categories));	  
  }
  
  public static Result playlistByCategory(Integer categoryId)
  {
	  MusicCategory category = MusicCategory.find.where().eq("id", categoryId).findUnique();
	  
	  int maxPlaylistsCount = 4;
	  
	  Set<Playlist> playlists = Playlist.find.where().eq("music_category_id", categoryId).setMaxRows(maxPlaylistsCount).findSet();
	  
	  return ok( Application_playlistByCategory.render(category, playlists) );
  }

  
}