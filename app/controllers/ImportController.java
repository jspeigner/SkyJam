package controllers;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import models.Album;
import models.AmazonAccount;
import models.Artist;
import models.BatchJob;
import models.Genre;
import models.MusicCategory;
import models.Playlist;
import models.PlaylistSong;
import models.Song;
import models.StorageObject;
import models.User;

import actors.TestActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import play.mvc.Result;
import play.mvc.Controller;
import views.html.*;

public class ImportController extends BaseController {
	
	public static Result importMusicFromS3Account()
	{
		
		
		List<String> files = new ArrayList<String>();
		
		AmazonAccount defaultS3Account = AmazonAccount.getDefaultAccount();
		
		// System.out.println( defaultS3Account);
		
		// models.Bucket bucket = models.Bucket.find.where().eq("amazon_account_id", defaultS3Account.getId() ).findUnique();
		List<models.Bucket> buckets = models.Bucket.find.where().eq("amazon_account_id", defaultS3Account.getId()).findList(); 
		
		for(models.Bucket bucket : buckets){
			bucket.importMusicFromBucket();
		}

		
		// return ok("Ok");
		return ok( views.html.Import.importMusicFromS3Account.render("Bucket" ,files) );
	}
	
	public static Result generateRandomPlaylists(Integer countPerCategory, Integer songsPerPlaylist)
	{
		Set<MusicCategory> categories = MusicCategory.find.findSet();
		User user = User.find.where().setMaxRows(1).findUnique();
		Genre genre = Genre.find.where().setMaxRows(1).findUnique();
		List<Song> songs = Song.find.where().findList();
		
		int songsCount = songs.size();
		Random rnd = new Random();
		
		int playlistIndex = 0;
		for( MusicCategory category : categories )
		{
			if( category.getParent().getId() > 0 )
			{
				for(int i =0; i<countPerCategory; i++)
				{
					Playlist p = new Playlist();
					p.setStatus(Playlist.Status.Public);
					p.setName("Auto Playlist "+( playlistIndex ));
					p.setCreatedDate(Calendar.getInstance().getTime());
					p.setDescription("");
					p.setUser(user);
					p.setGenre(genre);
					p.getMusicCategories().add(category);
					
					p.save();
					
					for( int songIndex = 0; songIndex < songsPerPlaylist ; songIndex++ )
					{
						PlaylistSong playlistSong = new PlaylistSong();
						playlistSong.setCreatedDate(Calendar.getInstance().getTime());
						playlistSong.setDislikesCount(playlistSong.setLikesCount(0));
						playlistSong.setPosition(songIndex);
						playlistSong.setSong(songs.get( rnd.nextInt(songsCount) ));
						playlistSong.setPlaylist(p);
						
						playlistSong.save();
					}
					
					playlistIndex++;
				}
			}
			
		}
		
		return ok("Done");
	}
	
    public static Result akkaTest(){
    	
    	ActorSystem system = ActorSystem.create("MySystem");
    	
    	
    	ActorRef myActor = system.actorOf(new Props(TestActor.class), "TestActor");
    	for(int i=0; i<100000; i++){
    		myActor.tell("Hello "+i);
    	}
    	
    	return ok("OK");
    }
    
    public static Result actorBatchTest(){
    	
    	BatchJob b = new BatchJob();
    	b.setCreatedDate(new java.util.Date());
    	b.setName("test " + Math.abs( ( new Random().nextInt() ) ) );
    	b.setActorClass("Actor");
    	b.save();
    	
    	b.createActors(1, 100000);
    	
    	return ok("Started");
    }

}
