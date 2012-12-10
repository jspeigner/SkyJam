package actors;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import com.echonest.api.v4.EchoNestException;

import models.Album;
import models.Song;
import models.SongMetadata;

public class SongMetadataActor extends BaseActor {

	@Override
	public void onReceive(Object data) throws Exception {
		
		super.onReceive(data);
		if( batchJobActor != null){
			// Batch Job Actor contains Amazon Account Id
			
			try {
			
				int songId = batchJobActor.getObjectId();
			
				readSongMetadata(songId);
				
			} catch (Exception e){
				
				e.printStackTrace();
				
				markFailed(500);
			}

		}
		
	}
	
	
	protected boolean readSongMetadata(Integer songId){
		
		Song song = Song.find.byId(songId);
		
		
		
		  
		if( song != null ){
			
				if( song.getEchonestSong() == null ){

					com.echonest.api.v4.Song echonestSong = readEchonestSong(song);
					if( echonestSong != null){
						
						 song.saveEchonestSong(echonestSong);
						 
						 processEchonestData(song, echonestSong);
						 
						 markCompleted();
						 
					} else {
						
						readSongId3(song);
						
					}
				} else {
					// alredy in the DB
					markCompleted();
				}
				
			


		} else {
			markFailed(0);
		}
		
		return false;
	}
	
	protected com.echonest.api.v4.Song readEchonestSong(Song song){
		try {
			
			List<com.echonest.api.v4.Song> songs = song.getEchonestSongsCall(1);
			
			if( songs.size() > 0 ){
				return songs.get(0);
			}
			
		} catch (EchoNestException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}		
		
		return null;
	}
	
	protected void readSongId3(Song song){
		
		// try to read song ID3 tags
		
		String artistName = null;
		String songName = null;
		
		SongMetadata songTags = song.getSongMetadata(); 
		
		if( songTags != null ){
			artistName = songTags.getArtist();
			songName = songTags.getTitle();
			
		} else {
		
			Tag tags = song.readMetadataTags();
			if( tags != null ){
				
				song.saveSongMetadata(tags);
				
				
				artistName = tags.getFirst(FieldKey.ARTIST);
				songName = tags.getFirst(FieldKey.TITLE);
				
				// true to read the echonest metadata
				
		
			} 		
		}
		
		
		if( ( artistName != null ) && ( songName != null) ){
			try {
				
				com.echonest.api.v4.Song echonestSong = Song.getEchonestSong(artistName, songName);
				if( echonestSong != null){
					
					song.saveEchonestSong(echonestSong);
					processEchonestData(song, echonestSong);
					
					markCompleted();
					
				} else {
					markFailed(3);
				}
				
			} catch (EchoNestException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				markFailed(2);
			}			
		} else {
			markFailed(1);
		}
		
		
	}
	
	protected void processEchonestData( Song song, com.echonest.api.v4.Song echonestSong ){
		// save the album art
		
		// check whether Album Art exists
		Album album = song.getAlbum();
		if( album != null ){
			if( album.getAlbumArtStorageObject() != null ){
				
				String albumArtUrl = echonestSong.getString("tracks[0].release_image");
				
				if( ( albumArtUrl != null ) && ( !albumArtUrl.isEmpty() )){ 
				
					try {
						URL url = new URL(albumArtUrl);
						
						InputStream sourceImage;

						sourceImage = url.openStream();
						
						album.updateImage(sourceImage);
						
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
					} catch (IOException e) {
						
					}
					
					
				}
			}
		}
		
	}
}
