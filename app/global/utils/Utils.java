package global.utils;

import java.util.ArrayList;
import java.util.List;

public class Utils {

	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}	
	
	public static List<Integer> extractIntsFromString(String list){
		String[] items = list.split(",");
		if( items.length > 0 ){
			List<Integer> ids = new ArrayList<Integer>();
				
			for(String s : items){
				try {
					
					Integer i = Integer.parseInt(s);
					
					ids.add(i);
					
				} catch (Exception e){
					
				}
			}
			
			return ids.size() > 0 ? ids : null;
			
			
		}
		
		return null;
		
	}
	
}
