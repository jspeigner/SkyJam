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
import models.Genre;
import models.MusicCategory;
import models.Playlist;
import models.PlaylistSong;
import models.Song;
import models.StorageObject;
import models.User;

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
		
		System.out.println( defaultS3Account);
		
		models.Bucket bucket = models.Bucket.find.where().eq("amazon_account_id", defaultS3Account.getId() ).findUnique();
		
		if( defaultS3Account != null )
		{
		
			
	        AmazonS3 s3 = new AmazonS3Client( defaultS3Account );
	
	        try {
	        	
	            System.out.println("Listing objects");
	            
	            ListObjectsRequest request = new ListObjectsRequest().withBucketName( bucket.getName() );
	            
	            
	            
	            ObjectListing objectListing = s3.listObjects(request);
	            
	            int batchCount = 0;
	            int batchCountMax = Integer.MAX_VALUE;
	            
	            do
	            {
	            	if( batchCount++ > 0 )
	            	{
	            		objectListing = s3.listNextBatchOfObjects(objectListing);
	            	}
	            	
	            	
	            	
	            	
		            for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) 
		            {
		                // System.out.println(" - " + objectSummary.getKey() + "  " + "(size = " + objectSummary.getSize() + ")");
		                
		                
		                
		                if( importS3Object(bucket, objectSummary )){
		                	files.add(objectSummary.getKey());
		                }
		            }
		            
		            
	            }
	            while(  ( objectListing.isTruncated() ) && ( batchCount < batchCountMax) );
	            
	            System.out.println();
	
	            /*
	             * Delete an object - Unless versioning has been turned on for your bucket,
	             * there is no way to undelete an object, so use caution when deleting objects.
	             */
	            // System.out.println("Deleting an object\n");
	            // s3.deleteObject(bucketName, key);
	
	            /*
	             * Delete a bucket - A bucket must be completely empty before it can be
	             * deleted, so remember to delete any objects from your buckets before
	             * you try to delete them.
	             */
	            // System.out.println("Deleting bucket " + bucketName + "\n");
	            // s3.deleteBucket(bucketName);
	            
	        } catch (AmazonServiceException ase) 
	        {
	            System.out.println("Caught an AmazonServiceException, which means your request made it "
	                    + "to Amazon S3, but was rejected with an error response for some reason.");
	            System.out.println("Error Message:    " + ase.getMessage());
	            System.out.println("HTTP Status Code: " + ase.getStatusCode());
	            System.out.println("AWS Error Code:   " + ase.getErrorCode());
	            System.out.println("Error Type:       " + ase.getErrorType());
	            System.out.println("Request ID:       " + ase.getRequestId());
	        } catch (AmazonClientException ace) {
	            System.out.println("Caught an AmazonClientException, which means the client encountered "
	                    + "a serious internal problem while trying to communicate with S3, "
	                    + "such as not being able to access the network.");
	            System.out.println("Error Message: " + ace.getMessage());
	        }
		}
		else
		{
			System.out.println("There are no S3 Accounts found");
		}
		
		// return ok("Ok");
		return ok( views.html.Import.importMusicFromS3Account.render(bucket.getName() ,files) );
	}
	
	protected static boolean importS3Object(models.Bucket bucket, S3ObjectSummary object)
	{
		boolean isImported = false;
		
		if( ( object != null ) && ( object.getSize() > 0 ) )
		{
			try
			{
			
				String name = object.getKey();
				
				
				String[] nameParts = name.split("/");
				
				
				if( ( nameParts.length >= 4 ) && ( nameParts[0].toLowerCase().equals("music") ) )
				{
					String artistName = nameParts[1];
					String albumName = nameParts[2];
					String songName = nameParts[ nameParts.length - 1 ];
					
					// trying to cleanup the song name
					String[] songNameParts = songName.split(" - ");
					if( songNameParts.length > 1 ){
						songName = songNameParts[ songNameParts.length - 1 ];
					}
					
					// remove the extension
					String cleanSongName; 
					
					
			        int p = songName.lastIndexOf(".");
			        if(p <= 0){
			        	cleanSongName = songName;
			        } else {
			        	cleanSongName = songName.substring(0, p);
			        }
					
					
					
					StorageObject s = StorageObject.find.where().eq("name", name).eq("bucket", bucket).findUnique();
					String logAction = "";
					if( s!= null){
						logAction = "EXISTS";
					} else {
						
						if( isSong( artistName,  albumName, songName,  cleanSongName, bucket, object, name) ){
							
							addSong(artistName, albumName, songName, cleanSongName, bucket, object, name);
							
							logAction = "CREATED";
							
							isImported = true;
							
						} else {
							logAction = "NOT_A_SONG";
							
						}
						
						System.out.println( logAction + " - " + artistName + " -> " + albumName + " -> " + cleanSongName );
						
					}
					
					// System.out.println( logAction + " - " + artistName + " -> " + albumName + " -> " + cleanSongName );
					
				}
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
			
		}
		
		return isImported;
		
	}

	protected static boolean isSong(String artistName, String albumName,
			String songName, String cleanSongName, models.Bucket bucket,
			S3ObjectSummary object, String storageObjectName) {
		
		if( 
				artistName.trim().equals("") || 
				albumName.trim().equals("") ||
				cleanSongName.trim().equals("")
		){
			return false;
		}
		
		
		String ext = "";
        int p = storageObjectName.lastIndexOf(".");
        if(p > 0){
        	ext = storageObjectName.substring(p + 1).toLowerCase();
        	
        	
        	if( ext.equals("mp3") || ext.equals("m4a")  ){
        		return true;
        	}
        } 	
        
        
		
		return false;
	}
	
	protected static void addSong(String artistName, String albumName,
			String songName, String cleanSongName, models.Bucket bucket,
			S3ObjectSummary object, String storageObjectName) {
		
		importSongByArtist(artistName, albumName, songName, cleanSongName, bucket, object, storageObjectName);
	}

	protected static void importSongByArtist(String artistName,
			String albumName, String songName, String cleanSongName,
			models.Bucket bucket, S3ObjectSummary object, String name) {
		Artist artist = Artist.getByName(artistName, true);
		
		if( artist != null )
		{
			Album album = Album.getByNameAndArtistId(albumName, artist.getId());
			if( album == null )
			{
				album = new Album();
				album.setName(albumName);
				album.setCreatedDate(Calendar.getInstance().getTime());
				album.setCopyrightYear(1900);
				album.setDescription("");
				album.setKeywords("");
				album.setArtist(artist);
				
				album.save();
				
			}
			
			importSongByAlbum(songName, cleanSongName, bucket, object, name, album);
			
			
		}
	}

	protected static void importSongByAlbum(String songName,
			String cleanSongName, models.Bucket bucket, S3ObjectSummary object,
			String name, Album album) {
		Song song = Song.getByNameAndAlbumId(songName, album.getId());
		if( song == null )
		{
			
			StorageObject storageObject = StorageObject.getByName(name);
			if( storageObject == null )
			{
				storageObject = new StorageObject();
				storageObject.setBucket(bucket);
				storageObject.setCreatedDate(Calendar.getInstance().getTime());
				storageObject.setName(name);
				storageObject.setFilesize(object.getSize());
				storageObject.save();
			}
			

			
			importSong(cleanSongName, album, storageObject);
		}
	}

	protected static void importSong(String cleanSongName, Album album,
			StorageObject storageObject) {
		Song song;
		song = new Song();
		song.setName(cleanSongName);
		song.setAlbum(album);
		song.setKeywords("");
		song.setTracknumber(0);
		song.setDuration(0);
		song.setStatus(Song.Status.visible);
		song.setStorageObject(storageObject); 
		
		song.save();
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

}
