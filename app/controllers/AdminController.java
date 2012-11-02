package controllers;

import java.util.Date;
import java.util.List;

import com.avaje.ebean.Expr;
import com.avaje.ebean.Page;

import models.Album;
import models.Artist;
import models.Playlist;
import models.Song;
import models.User;
import models.UserRole;
import models.UserSavedPlaylist;
import controllers.UserController.Login;
import be.objectify.deadbolt.actions.Restrict;
import play.mvc.Result;
import play.data.DynamicForm;
import play.data.Form;
import play.db.ebean.Model.Finder;

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
	    	
	    	return ok( views.html.Admin.editUser.render( user, userForm, userPlaylists, savedPlaylists ) );
	    	
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
    		
    		return redirect(routes.AdminController.browseUsers(0,""));
    	}
    	
    	return ok(views.html.Admin.deleteUser.render(user));
    	
    }
    
    public static Result deleteUserSubmit(Integer userId){
    	return deleteUser(userId);
    }
    
}
