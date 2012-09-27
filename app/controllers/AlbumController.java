package controllers;

import java.util.List;

import models.Album;
import models.Artist;
import models.Song;
import play.mvc.Controller;
import play.mvc.Result;

public class AlbumController extends Controller {

	public static Result browseByArtist(Integer artistId){
		
		Artist artist = Artist.find.byId(artistId);
		if( artist != null ){
			List<Album> albums = Album.find.where().eq("artist", artist).orderBy("name asc").findList();
		
			return ok(views.html.Album.browse.render(artist, albums));
		} else {
			return badRequest("Artist not found");
		}
	}
	
	public static Result view(Integer albumId){
		
		Album album = Album.find.byId(albumId);
		
		if( album != null ){
			List<Song> songs = Song.find.where().eq("album", album).findList();
			
			return ok( views.html.Album.view.render(album, songs) );
		} else {
			return badRequest("Album not found");
		}
		
	}
	
}
