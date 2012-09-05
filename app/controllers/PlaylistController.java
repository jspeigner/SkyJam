package controllers;

import global.Global;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import be.objectify.deadbolt.actions.Restrict;

import com.amazonaws.services.autoscaling.model.Activity;
import com.avaje.ebean.Page;


import models.MusicCategory;
import models.Playlist;
import models.PlaylistSong;
import models.PlaylistSongRating;
import models.User;
import models.UserPlaylistActivity;
import models.UserSavedPlaylist;
import play.mvc.Result;
import play.data.DynamicForm;

import views.html.*;
import play.data.Form;

public class PlaylistController extends BaseController 
{
	public static String CURRENT_PLAYLIST_ID_KEY = "Playlist.id";
	
	
	  public static Result playlist(Integer playlistId, Integer musicCategoryId)
	  {
		  Playlist playlist = Playlist.find.byId(playlistId);
		  
		  List<User> recentListeners = UserPlaylistActivity.getUsersOnPlaylist( playlist, 25);
		  
		  MusicCategory m = musicCategoryId != null ? MusicCategory.find.byId(musicCategoryId) : null;
		  
		  return ok( views.html.Playlist.playlist.render(playlist, recentListeners, m) );
	  }
	  
	  public static Result playlist(Integer playlistId)
	  {
		  return playlist(playlistId, null);
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
	  
	  public static Result browseCategories()
	  {
		  return browseCategory(-1, 0);
		  
		  // return ok(views.html.Playlist.browseCategories.render(topLevelCategories));
	  }
	  
	  public static Result browseCategory(Integer musicCategoryId, Integer page)
	  {
		  int playlistsPerPage = 5;
		  
		  List<MusicCategory> topLevelCategories = MusicCategory.find.where().eq("type", MusicCategory.Type.popular ).eq("parent_id", 0).findList();
		  
		  MusicCategory cat = null;
		  
		  
		  if(musicCategoryId <= 0 ){
			  // set the default category
			  
			  List<MusicCategory> subCategories = topLevelCategories.get(0).getChildren(); 
			  
			  cat = ( subCategories != null && subCategories.size() > 0) ? subCategories.get(0) : null;
		  }
		  else
		  {
		  
			  cat = MusicCategory.find.where().eq("id", musicCategoryId).eq("type", MusicCategory.Type.popular).findUnique();
			  
			  // top level category
			  if( cat.getParentId() == 0 )
			  {
				  List<MusicCategory> subCategories = cat.getChildren(); 
				  
				  cat = ( subCategories != null && subCategories.size() > 0) ? subCategories.get(0) : null;				  
			  }
		  }
		  
		  if( cat != null )
		  {
			  List<MusicCategory> siblingCategories = MusicCategory.find.where().eq("parent", cat.getParent()).findList();
			  
			  Page<Playlist> playlists = Playlist.pageByCategory(page, playlistsPerPage, cat, "loadedTimes", "desc");
			  
			  return ok(views.html.Playlist.browseCategory.render(topLevelCategories, siblingCategories, cat, playlists));
		  }
		  else
		  {
			  return badRequest("Category not found");
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
			  
			  User user = UserController.getAuthUser();
			  
			  if( (user != null) )
			  {
				  Integer playlistSongIdInt = Integer.parseInt( playlistSongId );
				  
				  if( PlaylistSongRating.find.where().eq("user", user).eq("playlistSong.id", playlistSongIdInt).findRowCount() == 0 )
				  {
					  
					  PlaylistSongRating p = new PlaylistSongRating();
					  p.setCreatedDate(new Date());
					  p.setPlaylistSong( PlaylistSong.find.byId( playlistSongIdInt ) );
					  p.setType(t);
					  
					  p.setUser(user);
					  
					  p.save();
					  
			  
					  // return getUserPlaylistData(playlistSongIdInt);
					  return ok("");
					  
				  }
				  else
				  {
					  return badRequest("Rating was already saved");
				  }
				  
			  }
			  else 
			  {
				  return badRequest("User not found");
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
	  
	  public static Result saveFavoritePlaylist(Integer playlistId)
	  {
		  User user = UserController.getAuthUser();
		  Playlist playlist = Playlist.find.byId( playlistId );	  
		  
		  
		  if( user == null )
		  {
			  return badRequest("User not found");
		  } 
		  else if( playlist == null )
		  {
			  return badRequest("Playlist not found");
		  }
		  
		  try
		  {
			  
			  
			  UserSavedPlaylist u = new UserSavedPlaylist();
			  u.setUser(user);
			  u.setPlaylist( playlist );
			  u.setCreatedDate(new Date());
			  
			  u.save();
		  }
		  catch(Exception ex)
		  {
			  
		  }
		  return ok("");
	  }
	  
	  public static Result deleteFavoritePlaylist(Integer playlistId)
	  {
		  
		  User user = UserController.getAuthUser();
		  Playlist playlist = Playlist.find.byId( playlistId );	  
		  
		  
		  if( user == null )
		  {
			  return badRequest("User not found");
		  } 
		  else if( playlist == null )
		  {
			  return badRequest("Playlist not found");
		  }		  
		  
		  try
		  {
			  UserSavedPlaylist u = UserSavedPlaylist.find.where().eq("user", user).eq("playlist", playlist).findUnique();
			  
			  u.delete();
		  }
		  catch (Exception e) {

		} 
		  	
		  
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
				  
				  playlist.setLoadedTimes( playlist.getLoadedTimes()+1 );
				  playlist.save();
				  
				  // System.out.println(playlist.getLoadedTimes());
				  
				  return ok( views.html.Playlist.getCurrentPlaylistJson.render(playlist) ).as( Global.JSON_CONTENT_TYPE );
				  
			  }
		  }
		  catch(Exception e)
		  {
			  
		  }
		  
		  return badRequest("{ \"error\": \"Playlist not found\" }");
	  }
	  
	  public static Result getUserPlaylistData(Integer playlistId)
	  {
		  User user = UserController.getAuthUser();
		  
		  if( user != null )
		  {
			  
			  List<PlaylistSongRating> ratings = PlaylistSongRating.find.where().eq("playlistSong.playlist.id", playlistId).eq("user", user).findList(); 
			  
			  return ok(views.html.Playlist.getUserPlaylistData.render(user,ratings)).as( Global.JSON_CONTENT_TYPE);
			  
		  }
		  else
		  {
			  return badRequest("User not found");
		  }
		
		  
	  }
	  
	  @Restrict("user")
	  public static Result create()
	  {
		  User user = UserController.getAuthUser();
		  Form<Playlist> form = form(Playlist.class);
		  
		  return ok(views.html.Playlist.create.render(user, form, MusicCategory.getActivitiesList(), null));
	  }
	  
	  @Restrict("user")
	  public static Result createSubmit()
	  {
		  
		  User user = UserController.getAuthUser();
		  Form<Playlist> form = form(Playlist.class).bindFromRequest("name", "description");
		  
		  List<PlaylistSong> playlistSongs = Playlist.getSongsFromForm( form().bindFromRequest().data() );
		  
		  
		  if( form.hasErrors()){
			  
			  return ok(views.html.Playlist.create.render(user, form, MusicCategory.getActivitiesList(), playlistSongs));
			  
		  } else {
			
			  Playlist p = form.get();
			  p.setUser(user);
			  p.setStatus(Playlist.Status.Draft);
			  p.setCreatedDate(new Date());
			  p.setLoadedTimes(0);
			 
			  try {
				  
				  p.save();
				  p.setActivity( form().bindFromRequest().field("activity").value() );
				  p.updatePlaylistSongs(playlistSongs);
				  
				  flash("success", "Playlist was created successfully");
				  
				  return pjaxRedirect( routes.PlaylistController.edit( p.getId() ) );
				  
			  } catch( Exception e){
				  
				  System.out.println(e);
				  
				  return badRequest("Playlist save failed");
			  }
		  }
	  }
	  
	  @Restrict("user")
	  public static Result edit(Integer playlistId)
	  {
		  User user = UserController.getAuthUser();
		  Playlist p = Playlist.find.where().eq("id", playlistId).eq("user", user ).findUnique();
		  if( p == null ){
			  return notFound("Playlist not found");
		  }
		  List<PlaylistSong> playlistSongs = p.getPlaylistSongs();
		  
		  Form<Playlist> form = form(Playlist.class);
		  
		  if( ( request().method() == "POST" ) || ( request().method() == "PUT" ) ){
			  
			  form = form.bindFromRequest("name", "description");
			  
			  playlistSongs = Playlist.getSongsFromForm( form().bindFromRequest().data() );
			  
			  if( form.hasErrors()){
				  
			  } else {
				  
				  Playlist formPlaylist = form.get();
				  formPlaylist.update(playlistId);
				  
				  p.setActivity( form().bindFromRequest().field("activity").value() );
				  p.updatePlaylistSongs( playlistSongs );
				  
				  flash("success", "Playlist was saved successfully");
				  
				  // redirect
				  return pjaxRedirect( routes.PlaylistController.edit( p.getId() ) );
			  }
		  } else {
			 
			 form = form.fill(p);

			 MusicCategory c = p.getActivity();
			 if( c != null ){
				form.data().put("activity", c.getId().toString()); 
			 }

			 
			 
		  }
		  
		  return ok(views.html.Playlist.create.render(user, form, MusicCategory.getActivitiesList() ,  playlistSongs));		  
		  
	  }
	  
	  @Restrict("user")
	  public static Result editSubmit(Integer playlistId)
	  {
		  return edit(playlistId);
	  }
	  
	  @Restrict("user")
	  public static Result publish(Integer playlistId){
		  
		  
		  
		  return null;
	  }
	  
	
}
