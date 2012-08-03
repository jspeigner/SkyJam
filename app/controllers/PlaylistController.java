package controllers;



import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import models.Playlist;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import views.html.*;



public class PlaylistController extends Controller {

	  public static Result playlist(Integer playlistId)
	  {
		  Playlist playlist = Playlist.find.byId(playlistId);
		  
		  return ok( Playlist_playlist.render(playlist) );
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
			
			  
			  // TODO - move to view - include Jerkson for Scala
			  ObjectMapper mapper = new ObjectMapper();
			  
			  ArrayNode a = mapper.createArrayNode();
			  for(Playlist playlist : playlists )
			  {
				  ObjectNode o = mapper.createObjectNode();
				  o.put("label", playlist.name);
				  o.put("value", playlist.getId());
				  o.put("description", playlist.description);
				  o.put("id", playlist.getId());
				  
				  a.add(o);
			  }
			  
			  return ok(a).as("application/json");
			  
			  // return ok(Playlist_searchJson.render(playlists)).as("application/json");
		  }
	  }
	
}
