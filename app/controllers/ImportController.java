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
	
	        /*
	        String bucketName = "my-first-s3-bucket-" + UUID.randomUUID();
	        String key = "MyObjectKey";
	
	        System.out.println("===========================================");
	        System.out.println("Getting Started with Amazon S3");
	        System.out.println("===========================================\n");
	        */
	
	        try {
	            /*
	             * Create a new S3 bucket - Amazon S3 bucket names are globally unique,
	             * so once a bucket name has been taken by any user, you can't create
	             * another bucket with that same name.
	             *
	             * You can optionally specify a location for your bucket if you want to
	             * keep your data closer to your applications or users.
	             */
	            // System.out.println("Creating bucket " + bucketName + "\n");
	            // s3.createBucket(bucketName);
	
	            /*
	             * List the buckets in your account
	             */
	        	/*
	            System.out.println("Listing buckets");
	            for (Bucket bucket : s3.listBuckets()) {
	                System.out.println(" - " + bucket.getName());
	            }
	            System.out.println();
				*/
	            /*
	             * Upload an object to your bucket - You can easily upload a file to
	             * S3, or upload directly an InputStream if you know the length of
	             * the data in the stream. You can also specify your own metadata
	             * when uploading to S3, which allows you set a variety of options
	             * like content-type and content-encoding, plus additional metadata
	             * specific to your applications.
	             */
	            // System.out.println("Uploading a new object to S3 from a file\n");
	            // s3.putObject(new PutObjectRequest(bucketName, key, createSampleFile()));
	
	            /*
	             * Download an object - When you download an object, you get all of
	             * the object's metadata and a stream from which to read the contents.
	             * It's important to read the contents of the stream as quickly as
	             * possibly since the data is streamed directly from Amazon S3 and your
	             * network connection will remain open until you read all the data or
	             * close the input stream.
	             *
	             * GetObjectRequest also supports several other options, including
	             * conditional downloading of objects based on modification times,
	             * ETags, and selectively downloading a range of an object.
	             */
	            // System.out.println("Downloading an object");
	            // S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));
	            // System.out.println("Content-Type: "  + object.getObjectMetadata().getContentType());
	            // displayTextInputStream(object.getObjectContent());
	
	            /*
	             * List objects in your bucket by prefix - There are many options for
	             * listing the objects in your bucket.  Keep in mind that buckets with
	             * many objects might truncate their results when listing their objects,
	             * so be sure to check if the returned object listing is truncated, and
	             * use the AmazonS3.listNextBatchOfObjects(...) operation to retrieve
	             * additional results.
	             */
	        	
	        	
	        	
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
		                
		                files.add(objectSummary.getKey());
		                
		                importS3Object(bucket, objectSummary );
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
		if( object != null )
		{
			try
			{
			
				String name = object.getKey();
				
				
				String[] nameParts = name.split("/");
				
				
				if( ( nameParts.length == 4 ) && ( nameParts[0].equals("music") ) )
				{
					String artistName = nameParts[1];
					String albumName = nameParts[2];
					String songName = nameParts[3];
					
					// remove the extension
					String cleanSongName; 
					
					
			        int p = songName.lastIndexOf(".");
			        if(p <= 0){
			        	cleanSongName = songName;
			        } else {
			        	cleanSongName = songName.substring(0, p);
			        }
					
					
					
					
					System.out.println( artistName + " - " + albumName + " - " + cleanSongName );
					
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
							

							
							song = new Song();
							song.setName(cleanSongName);
							song.setAlbum(album);
							song.setKeywords("");
							song.setDuration(0);
							song.setStatus(Song.Status.visible);
							song.setStorageObject(storageObject); 
							
							song.save();
						}
						
						
					}
					
				}
			}
			catch(Exception e)
			{
				System.out.println(e);
			}
			
		}
		
		return false;
		
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
			if( category.getParentId() > 0 )
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
					p.musicCategories.add(category);
					
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
