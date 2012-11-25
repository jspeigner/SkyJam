package controllers;


import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.collections.map.HashedMap;
import org.jaudiotagger.tag.Tag;



import com.avaje.ebean.Expr;
import com.avaje.ebean.Page;
import com.echonest.api.v4.EchoNestException;


import models.*;
import controllers.UserController.Login;
import actors.TestActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import be.objectify.deadbolt.actions.Restrict;
import play.mvc.Result;
import play.data.DynamicForm;
import play.data.Form;

public class AdminController extends BaseController {

	
	@Restrict(UserRole.ROLE_ADMIN)
	public static Result dashboard(){
		return ok(views.html.Admin.dashboard.render());
	}
	
	public static Result login() {
        return ok( views.html.Admin.login.render(form(UserController.Login.class)) );
    }
    
	@Restrict(UserRole.ROLE_ADMIN)
    public static Result logout() {
    	
    	
        UserController.setAuthUser(null);
        
        flash("success", "You've been logged out");
        
        return redirect( routes.AdminController.login() );
    }	

    public static Result authenticate() {
    	
        Form<Login> loginForm = form(Login.class).bindFromRequest();
        
        if(loginForm.hasErrors()) 
        {
            return ok(views.html.Admin.login.render(loginForm));
        }
        else 
        {
        	User user = loginForm.get().getUser();
        	
        	if( user.roles.contains(UserRole.findByName( UserRole.ROLE_ADMIN ))){

            	UserController.setAuthUser( user );
            	
            	user.setLastLoginDate(new Date());
            	user.update();
                
                return  redirect( routes.AdminController.dashboard() );        		
        		
        	} else {
        		
        		loginForm.reject("Your account has no admin privileges");
        		return ok(views.html.Admin.login.render(loginForm));
        	}
        	

        }
    }
    
    @Restrict(UserRole.ROLE_ADMIN)
    public static Result browseUsers(Integer page, String term ){
    	
    	int pageSize = 15;
    	Page<User> users = User.getPageWithSearch(page, pageSize, term);
    	
    	return ok(views.html.Admin.browseUsers.render(users, term));
    }

    @Restrict(UserRole.ROLE_ADMIN)    
    public static Result editUser(Integer userId){
    
    	User user = User.find.byId(userId);
    	if( user != null ){
    		
    		Form<User> userForm;
    		
    		if(request().method().equals("POST")){
    			userForm = form(User.class).bindFromRequest();
    			DynamicForm formData = form().bindFromRequest();  
    			
    			if( UserController.validateUserPassword(userForm)){
    				
    				user.setPassword(formData.get("password_reset"));
    				user.save();
    				flash("success", "Password was successfully updated");    		
    	    	}    			
    			
    			
    		} else {
    			userForm = form(User.class).fill( user );
    		}
	    	
	    	
	    	Page<Playlist> userPlaylists = Playlist.getUserPlaylistsPage(user, 0, 100);
	    	List<UserSavedPlaylist> savedPlaylists = UserSavedPlaylist.getByUser(user, UserController.maxUserSavedPlaylists);
	    	List<UserInvitationCode> inviatationsSent = UserInvitationCode.find.where().eq("sourceUser", user).findList();
	    	
	    	return ok( views.html.Admin.editUser.render( user, userForm, userPlaylists, savedPlaylists, inviatationsSent ) );
	    	
    	} else {
    		return notFound("User not found"); 
    	}
    	
    }
    
    @Restrict(UserRole.ROLE_ADMIN)    
    public static Result editUserSubmit(Integer userId){
    	
    	return editUser(userId);
    
    }

    @Restrict(UserRole.ROLE_ADMIN)    
    public static Result browseSongs(Integer page, String term, Integer albumId){

    	int pageSize = 15;
    	Album album = albumId > 0 ? Album.find.byId(albumId) : null;
    	Page<Song> songs = Song.getPageWithSearch(page, pageSize, term, album == null ? null : Expr.eq("album", album)  );
    	
    	return ok(views.html.Admin.browseSongs.render(songs, term, album));
    }

    
    @Restrict(UserRole.ROLE_ADMIN)    
    public static Result browseArtists(Integer page, String term){

    	int pageSize = 15;
    	
    	Page<Artist> artists = Artist.getPageWithSearch(page, pageSize, term );
    	
    	return ok(views.html.Admin.browseArtists.render(artists, term));
    } 
      
    
    @Restrict(UserRole.ROLE_ADMIN)    
    public static Result browseAlbums(Integer page, String term, Integer artistId){

    	int pageSize = 15;
    	Artist artist = artistId > 0 ? Artist.find.byId(artistId) : null;
    	Page<Album> albums = Album.getPageWithSearch(page, pageSize, term, artist == null ? null : Expr.eq("artist", artist)  );
    	
    	return ok(views.html.Admin.browseAlbums.render(albums, term, artist));
    }    
    
    @Restrict(UserRole.ROLE_ADMIN)    
    public static Result deleteUser(Integer userId){
    	
    	User user = User.find.byId(userId);
    	
    	if(request().method().equals("POST")){
    		flash("success", "User was removed successfully");    		
    		
    		user.delete();
    		
    		return redirect(routes.AdminController.browseUsers(0,""));
    	}
    	
    	return ok(views.html.Admin.deleteUser.render(user));
    	
    }
    
    @Restrict(UserRole.ROLE_ADMIN)
    public static Result deleteUserSubmit(Integer userId){
    	return deleteUser(userId);
    }
    
    @Restrict(UserRole.ROLE_ADMIN)
    public static Result editArtist(Integer artistId){
    	Artist artist = Artist.find.byId(artistId);
    	if( artist == null){
    		return notFound("Artist was not found");
    	}
    	
    	Form<Artist> form = form(Artist.class).fill(artist);
    	
    	if(request().method().equals("POST")){
    		
    		form = form(Artist.class).bindFromRequest();
    		
    		if(!form.hasErrors()){
    			
        		flash("success", "Artist was successfully updated");
        		
        		artist = form.get();
        		
        		artist.update(artistId);
        		
        		return redirect(routes.AdminController.editArtist(artistId));
    			
    		}
    		
    	}
    	
    	return ok(views.html.Admin.editArtist.render(artist, form));
    }
    
    @Restrict(UserRole.ROLE_ADMIN)
    public static Result editArtistSubmit(Integer artistId){
    	return editArtist(artistId);
    }
    
    @Restrict(UserRole.ROLE_ADMIN)
    public static Result editAlbum(Integer albumId){
    	Album album = Album.find.byId(albumId);
    	if( album == null){
    		return notFound("Album was not found");
    	}
    	
    	Form<Album> form = form(Album.class).fill(album);
    	
    	if(request().method().equals("POST")){
    		

    		
    		form = form(Album.class).bindFromRequest();
    		
    		if(!form.hasErrors()){
    			
        		album = form.get();
        		album.setId(albumId);
        		        		
    			processImageUpload(album, "setAlbumArtStorageObject", Album.imageMetadata );
    			
    	    	album.update(albumId);
    			
        		flash("success", "Album was successfully updated");
        		
        		return redirect(routes.AdminController.editAlbum(albumId));
    			
    		}
    		
    	}
    	
    	return ok(views.html.Admin.editAlbum.render(album, form, Genre.find.findList()));
    }
    
    @Restrict(UserRole.ROLE_ADMIN)
    public static Result editAlbumSubmit(Integer artistId){
    	return editAlbum(artistId);
    }    
    
    @Restrict(UserRole.ROLE_ADMIN)
    public static Result editSong(Integer songId){
    	Song song = Song.find.byId(songId);
    	if( song == null){
    		return notFound("Song was not found");
    	}
    	
    	Form<Song> form = form(Song.class).fill(song);
    	
    	if(request().method().equals("POST")){
    		
    		form = form(Song.class).bindFromRequest();
    		
    		if(!form.hasErrors()){
    			
        		flash("success", "Song was successfully updated");
        		
        		song = form.get();
        		
        		song.update(songId);
        		
        		
        		return redirect(routes.AdminController.editSong(songId));
    			
    		}
    		
    	}
    	
    	return ok(views.html.Admin.editSong.render(song, form));
    }

    @Restrict(UserRole.ROLE_ADMIN)
    public static Result readSongMetadata(Integer songId){
    	Song song = Song.find.byId(songId);
    	if( song == null ){
    		return notFound("Song was not found");
    	}
    	
    	Tag t = song.readMetadataTags();
    	if( t != null){
    		song.saveSongMetadata(t);
    	}
    	
    	return ok(views.html.Admin.readSongMetadata.render(song, t));
    }

    
    @Restrict(UserRole.ROLE_ADMIN)
    public static Result editSongSubmit(Integer artistId){
    	return editSong(artistId);
    }    
    
    @Restrict(UserRole.ROLE_ADMIN)
    public static Result sendInvitation(Integer userId){
    	
    	User user = User.find.byId(userId);
    	if(user == null){
    		return notFound("User not found");
    	}
    	
    	UserInvitationCode uic = UserInvitationCode.createNewCode(user);
    	
    	email(user.getEmail(), "A private invitation to check out the SkyJam.fm", views.html.Email.text.userInvitation.render(user, uic).toString());
    	
    	flash("success", "Invitation has been sent");
    	
    	return redirect(routes.AdminController.editUser(userId));
    }
    
    @Restrict(UserRole.ROLE_ADMIN)
    public static Result getEchonestInfo(Integer songId){
    	Song song = Song.find.byId(songId);
    	if(song==null){
    		return notFound("Song was not found");
    	}    	
        
        Exception exception = null;
        
        List<com.echonest.api.v4.Song> songs = null;
		try {
			songs = song.getEchonestApiSongs();
			if( ( songs != null ) && ( songs.size() > 0 )){
				song.saveEchonestSong(songs.get(0));
			}
			
		} catch (EchoNestException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			exception = e;
		}
    	
    	return ok(views.html.Admin.getEchonestInfo.render(song, songs, exception));
    }

    @Restrict(UserRole.ROLE_ADMIN)
	public static Result browseGenres(Integer page, String term){
		
    	int pageSize = 15;
    	
    	Page<Genre> genres = Genre.getPageWithSearch(page, pageSize, term );
    	
    	
    	return ok(views.html.Admin.browseGenres.render(genres, term));
	}    
    
    @Restrict(UserRole.ROLE_ADMIN)
    public static Result editGenre(Integer genreId){
    	
    	Genre genre = Genre.find.byId(genreId);
    	if( genre == null){
    		return notFound("Genre was not found");
    	}
    	
    	Form<Genre> form = form(Genre.class).fill(genre);
    	
    	if(request().method().equals("POST")){
    		
    		form = form(Genre.class).bindFromRequest();
    		
    		if(!form.hasErrors()){
    			
        		flash("success", "Genre was successfully updated");
        		
        		genre = form.get();
        		genre.update(genreId);
        		return redirect(routes.AdminController.editGenre(genreId));
    			
    		}
    		
    	}    	
    	
    	return ok(views.html.Admin.editGenre.render(genre, form));
    }
    
    @Restrict(UserRole.ROLE_ADMIN)
    public static Result editGenreSubmit(Integer genreId){
    	return editGenre(genreId);
    }
    
    @Restrict(UserRole.ROLE_ADMIN)
    public static Result addGenre(){
    	Form<Genre> form = form(Genre.class);
    	
    	System.out.println((request().method()));
    	
    	if(request().method().equals("POST")){
    		
    		form = form(Genre.class).bindFromRequest();
    		
    		if(!form.hasErrors()){
    			
        		flash("success", "Genre was successfully created");
        		
        		Genre genre = form.get();
        		genre.save();
        		
        		return redirect(routes.AdminController.editGenre(genre.getId()));
    			
    		}
    		
    	}    	
    	
    	
    	return ok(views.html.Admin.addGenre.render(form));
    }

    @Restrict(UserRole.ROLE_ADMIN)
    public static Result addGenreSubmit(){
    	return addGenre();
    }    
    
    @Restrict(UserRole.ROLE_ADMIN)
    public static Result deleteGenre(Integer genreId){
    	
    	Genre genre = Genre.find.byId(genreId);
    	if( genre != null){
    		return notFound("Genre was not found");
    	}
    	
    	flash("success", "Genre was successfully deleted");
    	
    	genre.delete();
    	
    	return redirect(routes.AdminController.browseGenres(0, null));
    }
    
    @Restrict(UserRole.ROLE_ADMIN)
    public static Result browseMusicCategories(String type){
    	
    	List<MusicCategory> musicCategories = MusicCategory.find.where().eq("parent", null).eq("type", type).findList();
    	
    	return ok(views.html.Admin.browseMusicCategories.render(musicCategories, type));
    }

    @Restrict(UserRole.ROLE_ADMIN)
    public static Result editMusicCategory(Integer id){
    	
    	MusicCategory category = MusicCategory.find.byId(id);
    	if( category == null){
    		return notFound("Music Category was not found");
    	}
    	
    	Form<MusicCategory> form = form(MusicCategory.class).fill(category);
    	
    	if(request().method().equals("POST")){
    		
    		form = form(MusicCategory.class).bindFromRequest();
    		
    		if(!form.hasErrors()){
    			
        		flash("success", "Music Category was successfully updated");
        		
        		category = form.get();
        		category.setId(id);
        		
        		processImageUpload(category, "setImageStorageObject", MusicCategory.imageMetadata );
        		
        		category.update();
        		
        		return redirect(routes.AdminController.editMusicCategory(id));
    			
    		}
    		
    	}     	
    	
    	return ok(views.html.Admin.editMusicCategory.render(category, form, MusicCategory.getTypeList()));

    }

    @Restrict(UserRole.ROLE_ADMIN)
    public static Result editMusicCategorySubmit(Integer id){
    	
    	return editMusicCategory(id);
    }
    	
    @Restrict(UserRole.ROLE_ADMIN)
    public static Result addMusicCategory(Integer parentId, String type){
    	
    	Form<MusicCategory> form = form(MusicCategory.class);
    	MusicCategory parent = parentId > 0 ? MusicCategory.find.byId(parentId) : null;
    	
    	if(request().method().equals("POST")){
    		
    		form = form.bindFromRequest();
    		
    		if(!form.hasErrors()){
    			
        		flash("success", "Music Category was successfully updated");
        		
        		MusicCategory category = form.get();
        		category.setParent(MusicCategory.find.byId(parentId));
        		
        		processImageUpload(category, "setImageStorageObject", MusicCategory.imageMetadata );
        		
        		category.save();
        		
        		return redirect(routes.AdminController.editMusicCategory(category.getId()));
    			
    		}
    		
    	} else {
    		// HashMap<String,String> data = new HashMap<String,String>();
    		// data.put("type", type);
    		// form = form.bind( data, "type" );
    		form.data().put("type", type);
    	}
    	
    	return ok(views.html.Admin.addMusicCategory.render(form, parent, MusicCategory.getTypeList(), type));

    }

    @Restrict(UserRole.ROLE_ADMIN)
    public static Result addMusicCategorySubmit(Integer parentId, String type){
    	
    	return addMusicCategory(parentId, type);
    }    
    
    @Restrict(UserRole.ROLE_ADMIN)
    public static Result deleteMusicCategory(Integer id){
    	
    	
    	MusicCategory category = MusicCategory.find.byId(id);
    	if( category == null){
    		return notFound("Music Category was not found");
    	}    	
    	String type = category.getType().toString();
    	
    	if(request().method().equals("POST")){
    		
    		category.delete();
    		
    		flash("Music Category was removed successfully");
    		
    		return redirect( routes.AdminController.browseMusicCategories( type ) );
    	}
    	
    	return ok( views.html.Admin.deleteMusicCategory.render(category) );
    }
    
    public static Result deleteMusicCategorySubmit(Integer id){
    	
    	return deleteMusicCategory(id);
    	
    }

    @Restrict(UserRole.ROLE_ADMIN)
	public static Result browseBatchJobs(Integer page, String term){
		
    	int pageSize = 15;
    	
    	Page<BatchJob> batchJobs = BatchJob.getPageWithSearch(page, pageSize, term );
    	
    	
    	return ok(views.html.Admin.browseBatchJobs.render(batchJobs, term));
	}    
    
    
}
