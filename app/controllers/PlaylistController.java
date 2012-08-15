package controllers;

import global.Global;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import models.Album;
import models.Playlist;
import models.PlaylistSong;
import models.PlaylistSongRating;
import models.User;
import models.UserPlaylistActivity;
import play.api.libs.json.JerksonJson;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import play.data.DynamicForm;

import views.html.*;

public class PlaylistController extends AppController 
{
	public static String CURRENT_PLAYLIST_ID_KEY = "Playlist.id";
	
	
	  public static Result playlist(Integer playlistId)
	  {
		  Playlist playlist = Playlist.find.byId(playlistId);
		  
		  List<User> recentListeners = UserPlaylistActivity.getUsersOnPlaylist( playlist, 25);
		  
		  return ok( views.html.Playlist.playlist.render(playlist, recentListeners) );
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
			  
			  return ok(views.html.Playlist.searchJson.render(playlists));

		  }
	  }
	  
	  public static Result trackPlaylistSongActivity()
	  {
		  
		  try
		  {
			  
			  DynamicForm data = form().bindFromRequest();
			  
			  String playlistSongId = data.get("playlist_song_id");
			  String type = data.get("type");
			  
			  UserPlaylistActivity.Type t = UserPlaylistActivity.Type.forName(type);
			  
			  User user = session("User.id") != null ? User.find.byId( Integer.parseInt( session("User.id") ) ) : null;
		  
			  UserPlaylistActivity p = new UserPlaylistActivity();
			  p.setCreatedDate(new Date());
			  p.setPlaylistSong( PlaylistSong.find.byId( Integer.parseInt( playlistSongId ) ) );
			  p.setType(t);
			  p.setUser(user);
			  
			  p.save();
		  
		  
			  return ok("");
		  }
		  catch(Exception e)
		  {
			  return badRequest("Wrong data passed");
		  }
	  }
	  
	  public static Result savePlaylistSongRating()
	  {
		  try
		  {
			  
			  DynamicForm data = form().bindFromRequest();
			  
			  String playlistSongId = data.get("playlist_song_id");
			  String type = data.get("type");
			  
			  PlaylistSongRating.Type t = PlaylistSongRating.Type.forName(type);
			  
			  User user = session("User.id") != null ? User.find.byId( Integer.parseInt( session("User.id") ) ) : null;
			  
			  // XXX check duplicate  user ratings
			  
			  if( (user != null) && ( true ) )
			  {
		  
				  PlaylistSongRating p = new PlaylistSongRating();
				  p.setCreatedDate(new Date());
				  p.setPlaylistSong( PlaylistSong.find.byId( Integer.parseInt( playlistSongId ) ) );
				  p.setType(t);
				  p.setUser(user);
				  
				  p.save();
		  
		  
				  return ok("");
			  }
		  }
		  catch(Exception e)
		  {
			  
		  }		
		  
		  return badRequest("Wrong data passed");
	  }
	
	  
	  public static Result setCurrentPlaylistJson(Integer id)
	  {
		  session(CURRENT_PLAYLIST_ID_KEY, id.toString());
		  return ok("");
	  }
	  
	  public static Result getCurrentPlaylistJson(Integer playlistIdPassed)
	  {
		  String playlistIdString = session( CURRENT_PLAYLIST_ID_KEY );
		  
		  try
		  {
			  Integer playlistId = playlistIdPassed > 0 ? playlistIdPassed : Integer.parseInt(playlistIdString);
			  
			  setCurrentPlaylistJson( playlistId );
			  
			  Playlist playlist = Playlist.find.byId(playlistId);
			  if( playlist != null )
			  {
				  
				  return ok( views.html.Playlist.getCurrentPlaylistJson.render(playlist) ).as( Global.JSON_CONTENT_TYPE );
				  
			  }
		  }
		  catch(Exception e)
		  {
			  
		  }
		  
		  return badRequest("{ error: \"Playlist not found\" }");
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
