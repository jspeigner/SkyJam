package controllers;



import global.Global;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import models.Album;
import models.Playlist;
import models.User;
import models.UserPlaylistActivity;
import play.api.libs.json.JerksonJson;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import views.html.*;

public class PlaylistController extends Controller 
{
	public static String CURRENT_PLAYLIST_ID_KEY = "Playlist.id";
	
	
	  public static Result playlist(Integer playlistId)
	  {
		  Playlist playlist = Playlist.find.byId(playlistId);
		  
		  List<User> recentListeners = UserPlaylistActivity.getUsersOnPlaylist( playlist, 25);
		  
		  return ok( Playlist_playlist.render(playlist, recentListeners) );
	  }	
	  
	  public static Result searchJson()
	  {
		  String query =  ( request().queryString().containsKey("term") ) ? request().queryString().get("term")[0] : "";
		  
		  if( query == "")
		  {
			  return badRequest("Empty search");
		  }
		  else
		  {
			  List<Playlist> playlists = Playlist.searchWideByName(query, 10);
			  
			  return ok(Playlist_searchJson.render(playlists));

		  }
	  }
	  
	  public static Result setCurrentPlaylistJson(Integer id)
	  {
		  session(CURRENT_PLAYLIST_ID_KEY, id.toString());
		  return ok("");
	  }
	  
	  public static Result getCurrentPlaylistJson(Integer playlistId2)
	  {
		  String playlistIdString = session( CURRENT_PLAYLIST_ID_KEY );
		  try
		  {
			  Integer playlistId = Integer.parseInt(playlistIdString);
			  
			  Playlist playlist = Playlist.find.byId(playlistId);
			  if( playlist != null )
			  {
				  
				  return ok("{ \"name\": \""+playlist.getName()+"\", \"id\" : "+ playlist.getId() +"}").as( Global.JSON_CONTENT_TYPE );
				  
			  }
		  }
		  catch(Exception e)
		  {
			  
		  }
		  
		  return badRequest("Playlist not found");
	  }
	  
	  public static Result playJson(Integer playlistId)
	  {
		  return null;
	  }
	  
	  public static Result pauseJson()
	  {
		  return null;
	  }
	  
	  public static Result skipJson()
	  {
		  return null;
	  }
	
}
