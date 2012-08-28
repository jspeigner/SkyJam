package models.behavior;

import models.Bucket;
import models.StorageObject;
import play.Play;
import controllers.routes;

public class ImageMetadata {
	
	public static final String IMAGE_TYPE_PNG = "png";
	public static final String IMAGE_TYPE_JPG = "jpeg";
	
	
	
	public int width;
	public int height;
	
	public String imageType = null;
	public String contentType = null;
	public String filenameFormat = null;
	public String defaultImageUrl = null;
	
	public ImageMetadata(int width, int height, String imageType, String contentType, String filenameFormat, String defaultImageUrl )
	{
		this.width = width;
		this.height = height;
		this.imageType = imageType;
		this.contentType = contentType;
		this.filenameFormat = filenameFormat;
		
		
		this.defaultImageUrl = ( defaultImageUrl == null ) ? getFilename(new Integer(0)) : defaultImageUrl ;
		
	}

	public ImageMetadata(int width, int height, String imageType, String imagePathFormat, String defaultImageUrl )
	{
		this(width, height, imageType, "image/"+imageType, imagePathFormat, defaultImageUrl);
	}

	public ImageMetadata(int width, int height, String imageType, String imagePathFormat )
	{
		this(width, height, imageType, "image/"+imageType, imagePathFormat, null);
	}	
	
	public String getFilename(Object... params)
	{
		return String.format(filenameFormat, params);
	}
	
	public String getImageUrlFromStorageObject(StorageObject s)
	{
		if( s != null )
		{
			return s.getUrl();
		}
		else
		{
			boolean isAsset = Play.application().configuration().getBoolean("application.default_image.is_stored_in_assets");
			
			return  isAsset ?
						routes.Assets.at( this.defaultImageUrl).toString() :
						StorageObject.getObjectUrl( this.defaultImageUrl, Bucket.getDefault());
		}		
	}

	
}
