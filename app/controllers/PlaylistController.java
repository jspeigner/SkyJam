package controllers;

import global.Global;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import be.objectify.deadbolt.actions.Restrict;

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


import play.data.Form;
import scala.Tuple2;


public class PlaylistController extends BaseController 
{
	
	
	
	public static String CURRENT_PLAYLIST_ID_KEY = "Playlist.id";
	
	
	  public static Result playlist(Integer playlistId, Integer musicCategoryId)
	  {
		  Playlist playlist = Playlist.find.byId(playlistId);
		  
		  if( playlist.getStatus() != Playlist.Status.Deleted ){
		  
			  List<User> recentListeners = UserPlaylistActivity.getUsersOnPlaylist( playlist, 25);
			  
			  MusicCategory m = musicCategoryId != null ? MusicCategory.find.byId(musicCategoryId) : null;
		  
			  return ok( views.html.Playlist.playlist.render(playlist, recentListeners, m) );
			  
		  } else {
			  
			  return badRequest("Playlist not found");
		  }
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
			  List<Playlist> playlists = Playlist.searchDeepByName(query, 10);
			  
			  return ok(views.html.Playlist.searchJson.render(playlists));

		  }
	  }
	  
	  public static Result browseCategories()
	  {
		  return browseCategory(null, 0);
		  
		  // return ok(views.html.Playlist.browseCategories.render(topLevelCategories));
	  }
	  
	  public static Result browseCategory(Integer musicCategoryId, Integer page)
	  {
		  int playlistsPerPage = 5;
		  
		  List<MusicCategory> topLevelCategories = MusicCategory.find.where().eq("type", MusicCategory.Type.popular ).eq("parent_id", null).findList();
		  
		  MusicCategory cat = null;
		  
		  
		  if(musicCategoryId == null ){
			  // set the default category
			  
			  List<MusicCategory> subCategories = topLevelCategories.get(0).getChildren(); 
			  
			  cat = ( subCategories != null && subCategories.size() > 0) ? subCategories.get(0) : null;
		  }
		  else
		  {
		  
			  cat = MusicCategory.find.where().eq("id", musicCategoryId).eq("type", MusicCategory.Type.popular).findUnique();
			  
			  // top level category
			  if( ( cat != null ) && ( cat.getParent() == null ) ) {
				  List<MusicCategory> subCategories = cat.getChildren(); 
				  
				  cat = ( ( subCategories != null ) && ( subCategories.size() > 0 ) ) ? subCategories.get(0) : null;				  
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
			  
			  User user = UserController.getAuthUser();
		  
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
		  User user = UserController.getAuthUser();
		  
		  String playlistIdString = session( CURRENT_PLAYLIST_ID_KEY );
		  
		  try
		  {
			  Integer playlistId = playlistIdPassed > 0 ? playlistIdPassed : Integer.parseInt(playlistIdString);
			  
			  Playlist playlist = Playlist.find.where().eq("id", playlistId).ne("status", Playlist.Status.Deleted).findUnique();
			  if( playlist != null )
			  {
				  
				  setCurrentPlaylistJson( playlistId );
				  playlist.setLoadedTimes( playlist.getLoadedTimes()+1 );
				  playlist.save();
				  
				  return ok( views.html.Playlist.getCurrentPlaylistJson.render( user, playlist) ).as( Global.JSON_CONTENT_TYPE );
				  
			  }
		  }
		  catch(Exception e)
		  {
			  
		  }
		  
		  return ok( views.html.Playlist.getCurrentPlaylistJson.render( user, null) ).as( Global.JSON_CONTENT_TYPE );
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
			  return notFound("User not found");
		  }
		
		  
	  }
	  
	  @Restrict("user")
	  public static Result create()
	  {
		  User user = UserController.getAuthUser();
		  Form<Playlist> form = form(Playlist.class);
		  
		  return ok(views.html.Playlist.create.render(user, form, MusicCategory.getActivitiesList(), null, false ));
	  }
	  
	  @Restrict("user")
	  public static Result createSubmit(){
		  
		  User user = UserController.getAuthUser();
		  Form<Playlist> form = form(Playlist.class).bindFromRequest("name", "description");
		  
		  List<PlaylistSong> playlistSongs = Playlist.getSongsFromForm( form().bindFromRequest().data() );
		  
		  
		  if( form.hasErrors()){
			  
			  return ok(views.html.Playlist.create.render(user, form, MusicCategory.getActivitiesList(), playlistSongs, false ));
			  
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
		  
		  Playlist p = Playlist.getUserPlaylist(playlistId, user);
		  
		  if( p == null ) return notFound("Playlist not found");
		  
		  List<PlaylistSong> playlistSongs = p.getPlaylistSongs();
		  
		  Form<Playlist> form = form(Playlist.class);
		  
		  if( ( request().method().equals("POST") ) || ( request().method().equals("PUT") ) ){
			  
			  form = form.bindFromRequest("name", "description");
			  
			  playlistSongs = Playlist.getSongsFromForm( form().bindFromRequest().data() );
			  
			  if( form.hasErrors()){
				  // render errors
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
		  
		  return ok(views.html.Playlist.create.render(user, form, MusicCategory.getActivitiesList(),  playlistSongs, p.isAllowedToPublish()));		  
		  
	  }
	  
	  @Restrict("user")
	  public static Result editSubmit(Integer playlistId)
	  {
		  return edit(playlistId);
	  }
	  
	  @Restrict("user")
	  public static Result publish(Integer playlistId){
		  
		  User user = UserController.getAuthUser();
		  
		  Playlist p = Playlist.getUserPlaylist(playlistId, user);
		  
		  if( p == null ){
			  return notFound("Playlist not found");
		  }
		  
		  if( p.isAllowedToPublish() ){
			  
			  p.setStatus(Playlist.Status.Public);
			  p.save();
			  
			  flash("success", "Playlist was published successfully");
			  
			  return pjaxRedirect( routes.PlaylistController.edit(playlistId) );
			  
		  } else {
			  
			  flash("error", "It's not possible to publish the playlist");
			  
			  return pjaxRedirect( routes.PlaylistController.edit(playlistId) );
		  }
	  }
	  
	  @Restrict("user")
	  public static Result makePrivate(Integer playlistId){
		  
		  User user = UserController.getAuthUser();
		  
		  Playlist p = Playlist.getUserPlaylist(playlistId, user);
		  
		  if( p == null ){
			  return notFound("Playlist not found");
		  }
			  
		  p.setStatus(Playlist.Status.Private);
		  p.save();
		  
		  flash("success", "Playlist was set to private");
		  
		  return pjaxRedirect( routes.PlaylistController.edit(playlistId) );
		  
		  
	  }	  
	  
	  @Restrict("user")
	  public static Result userPlaylists(Integer page){
		  
		  User user = UserController.getAuthUser();
		  int pageSize = 5;
		  
		  Page<Playlist> playlists = Playlist.getUserPlaylistsPage( user, page, pageSize); 
		  
		  return ok(views.html.Playlist.userPlaylists.render(playlists));
	  }
	  
	  public static Result popular(String type){
		  
		  int limitTopPlaylists = 10;
		  
		  List<Playlist> playlists;
		  
		  if ( type.equals("week")){
			  playlists = Playlist.getPopularPlaylists( new Date() , new Date(System.currentTimeMillis() - ( 7 * DAY_IN_MS) ), "loaded_times DESC", limitTopPlaylists);
		  } else if ( type.equals("month")){
			  playlists = Playlist.getPopularPlaylists( new Date() , new Date(System.currentTimeMillis() - ( 30 * DAY_IN_MS) ), "loaded_times DESC", limitTopPlaylists);
		  } else if ( type.equals("all_time")){
			  playlists = Playlist.getPopularPlaylists( new Date() , new Date(System.currentTimeMillis() - ( 10 * 365 * DAY_IN_MS) ), "loaded_times DESC", limitTopPlaylists);
		  } else {
			  playlists = Playlist.getPopularPlaylists( new Date() , new Date(System.currentTimeMillis() - ( 1 * DAY_IN_MS) ), "loaded_times DESC", limitTopPlaylists);
		  }
		  
		  
		  
		  List<Tuple2<String,String>> categories= new ArrayList<Tuple2<String,String>>();
		  
		  categories.add( new Tuple2<String, String>("trending", "Trending"));
		  categories.add( new Tuple2<String, String>("week", "This Week"));
		  categories.add( new Tuple2<String, String>("month", "This Month"));
		  categories.add( new Tuple2<String, String>("all_time", "All time"));
		  
		  
		  return ok(views.html.Playlist.popular.render(type,playlists,categories));
		  
	  }
	  
	  @Restrict("user")
	  public static Result delete(Integer playlistId){
		  
		  User user = UserController.getAuthUser();
		  Playlist playlist = Playlist.getUserPlaylist(playlistId, user);
		  
		  if( playlist != null){
			  Form<Playlist> playlistForm = form(Playlist.class).fill(playlist);
			  
			  return ok(views.html.Playlist.delete.render(playlist, playlistForm));
		  } else {
			  return notFound("Playlist not found");
		  }
	  }
	  
	  
	  
	  @Restrict("user")
	  public static Result deleteSubmit(Integer playlistId){
		  User user = UserController.getAuthUser();
		  Playlist playlist = Playlist.find.where().eq("user", user).eq("id", playlistId).ne("status", Playlist.Status.Deleted).findUnique();
		  
		  if( playlist != null){
			  
			  playlist.setStatus(Playlist.Status.Deleted);
			  playlist.save();
			  
			  flash("success", "Playlist was deleted successfully");
			  
			  return pjaxRedirect( routes.PlaylistController.userPlaylists(0) );
			  
		  } else {
			  return notFound("Playlist not found");
		  }		  
	  }
}
