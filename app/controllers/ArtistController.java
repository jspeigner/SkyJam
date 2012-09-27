package controllers;

import java.util.List;

import models.Artist;
import models.MusicCategory;
import models.Playlist;

import com.avaje.ebean.Page;

import play.mvc.*;

public class ArtistController extends Controller {
	
	
	public static Result browse(){
		
		return browse(0);
		
	}
	
	public static Result browse(Integer page){
		
		int artistsPerPage = 25;
		
		Page<Artist> artists = Artist.find.where().orderBy("name asc").findPagingList(artistsPerPage).getPage(page);
		  
		return ok(views.html.Artist.browse.render(artists, artistsPerPage));
		
		
	}

}
