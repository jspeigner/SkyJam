package actors;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import models.Album;
import models.Song;

public class EchonestUpdateAlbumArt extends BaseActor {
	
	@Override
	public void onReceive(Object data) throws Exception {
		
		super.onReceive(data);
		if( batchJobActor != null){
			// Batch Job Actor contains Amazon Account Id
			
			try {
			
				int albumId = batchJobActor.getObjectId();
				
				
				processAlbum(albumId);
				
			} catch (Exception e){
				
				// e.printStackTrace();
				
				markFailed(500);
			}

		}
		
	}
	
	protected void processAlbum(int albumId){
		
		Album album = Album.find.byId(albumId);
		
		if(album != null ){
			
			List<Song> song = Song.find.where().eq("album", album).ne("echonestSong.coverArt", null).ne("echonestSong.coverArt", "").setMaxRows(1).findList();
			
			if( ( song != null ) && ( song.size() > 0 ) ){
				
				String albumArtUrl = song.get(0).getEchonestSong().getCoverArt();
				if( ( albumArtUrl != null ) && ( !albumArtUrl.isEmpty() )){ 
					
					try {
						URL url = new URL(albumArtUrl);
						
						InputStream sourceImage;

						sourceImage = url.openStream();
						
						album.updateImage(sourceImage);
						
						markCompleted();
						
						
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
						
						markFailed(3);
					} catch (IOException e) {
						markFailed(4);
					}
					
					
				}
				
			} else {
				markFailed(2);
			}
			
		} else {
			markFailed(1);
		}
	}
	
}
