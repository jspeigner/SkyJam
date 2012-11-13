package controllers;


import java.util.Date;
import java.util.List;

import org.jaudiotagger.tag.Tag;



import com.avaje.ebean.Expr;
import com.avaje.ebean.Page;
import com.echonest.api.v4.EchoNestAPI;
import com.echonest.api.v4.EchoNestException;
import com.echonest.api.v4.Params;
import com.echonest.api.v4.SongCatalog;
import com.echonest.api.v4.SongParams;

import models.*;
import controllers.UserController.Login;
import be.objectify.deadbolt.actions.Restrict;
import play.Play;
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
    	if(song==null){
    		return notFound("Song was not found");
    	}
    	
    	Tag t = song.readMetadataTags();
    	
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
    	
    	String fullName = song.getName();
    	
    	
    	
    	int results = 1;
    	EchoNestAPI en = new EchoNestAPI( Play.application().configuration().getString("echonest.api_key") );
    	
    	SongParams p = new SongParams();
    	
    	if( ( song.getAlbum() != null ) && ( song.getAlbum().getArtist() != null ) ){

    		p.setArtist(song.getAlbum().getArtist().getName());
    		
    		fullName = song.getAlbum().getArtist().getName() + " " + fullName;
    	}
    	
    	System.out.printf("[%s]\n", fullName);
    	
    	p.setTitle(song.getName());
        
        p.setResults(results);
        
        Exception exception = null;
        
        List<com.echonest.api.v4.Song> songs = null;
		try {
			songs = en.searchSongs(p);
			
			/*
			if( songs.size() > 0 ){
	        	for (com.echonest.api.v4.Song songItem : songs) {
	        		dumpSong(songItem);
	        	}
	            
	        }  else {
	        	System.out.println("There are no songs found");
	        }
	        */
		
			
		} catch (EchoNestException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			exception = e;
		}
  	
    	
    	
    	return ok(views.html.Admin.getEchonestInfo.render(song, songs, exception));
    }
    
    public static void dumpSong(com.echonest.api.v4.Song song) throws EchoNestException {
    	
    	// song.
    	
    	
    	
    	System.out.println("Echonest Result "+ song.getID());
        System.out.printf("%s\n", song.getTitle());
        System.out.printf("   artist: %s\n", song.getArtistName());
        System.out.printf("   dur   : %.3f\n", song.getDuration());
        System.out.printf("   BPM   : %.3f\n", song.getTempo());
        System.out.printf("   Mode  : %d\n", song.getMode());
        System.out.printf("   S hot : %.3f\n", song.getSongHotttnesss());
        System.out.printf("   A hot : %.3f\n", song.getArtistHotttnesss());
        System.out.printf("   A fam : %.3f\n", song.getArtistFamiliarity());
        System.out.printf("   A loc : %s\n", song.getArtistLocation());
        System.out.printf("   Covert Art : %s\n", song.getCoverArt());
        System.out.printf("   Release Name : %s\n", song.getReleaseName());
        System.out.printf("   Release Image : %s\n", song.getReleaseImage());
        
        
        
        /*

 		http://developer.echonest.com/forums/thread/443
 
        SongParams p = new SongParams();
        p.setArtist("Weezer");
        p.setTitle("El Scorcho");
        p.includeTracks();               // the album art is in the track data
        p.setLimit(true);                // only return songs that have track data
        p.addIDSpace("7digital-US");     // include 7digital specific track data
        List<Song> songs = en.searchSongs(p);   // make the query
        for (Song song : songs) {
            String url = song.getString("tracks[0].release_image");   // get the release data from the first track returned for each song
            System.out.println("release image" + url);
        }        
        
        http://developer.echonest.com/forums/thread/744
        
        */
        
        
    }    
    
}
