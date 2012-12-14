package actors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;

import com.avaje.ebean.Ebean;

import models.Genre;
import models.SongMetadata;

public class GenreUpdateActor extends BaseActor {
	
	@Override
	public void onReceive(Object data) throws Exception {
		
		super.onReceive(data);
			
		List<SongMetadata> genres = SongMetadata.find.where()
					.ne("genre", "")
					.ne("genre", "<error decoding frame>")
					.ne("genre", null)
					.findList();
		
		Map<String, String> missingGenreNames = new HashMap<String, String>(); 
		for(SongMetadata genre : genres){
			String s = genre.getGenre();
			String[] genreParts = s.split("[;,/]");
			for (String genrePart : genreParts) {
				String genrePartClean = genrePart.trim();
				
				if( !genrePartClean.isEmpty() && !missingGenreNames.containsKey(genrePartClean.toLowerCase() )){
					// if( Genre.find.where().eq("name", genrePartClean).findRowCount() == 0  ){
						missingGenreNames.put(genrePartClean.toLowerCase(), genrePartClean );
					// }
				}
			}
		}
		
		List<Genre> existingGenres = Genre.find.where().in("name",  missingGenreNames.keySet() ).findList();
		
		for( Genre genre : existingGenres ){
			String genreName = genre.getName();
			missingGenreNames.remove(genreName.toLowerCase());
		}
		
		Ebean.beginTransaction();  
		
		try {
		
			for(String genreName : missingGenreNames.keySet()){
				
				try {
					Genre genre = new Genre();
					genre.setName(missingGenreNames.get(genreName));
					genre.save();
				} catch(Exception e){
					
				}
			}
		
			Ebean.commitTransaction();
		} finally {
			Ebean.endTransaction();
		}
		
		markCompleted();
		
	}	

}