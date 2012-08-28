package controllers;

import global.Global;

import java.util.ArrayList;
import java.util.List;
import views.html.*;
import models.Playlist;
import play.mvc.Result;
import models.Song;

public class SongController extends BaseController {

	public static Result searchJson()
	{
		  String query =  ( request().queryString().containsKey("term") ) ? request().queryString().get("term")[0] : "";
		  
		  if( query == "")
		  {
			  return ok(views.html.Song.searchJson.render(new ArrayList<Song>()));
		  }
		  else
		  {
			  List<Song> songs = Song.searchWideByName(query, 30);
			  
			  return ok(views.html.Song.searchJson.render(songs)).as( Global.JSON_CONTENT_TYPE );

		  }
		
	}
	
}
