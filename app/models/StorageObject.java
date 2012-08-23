package models;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import models.behavior.ImageMetadata;

import org.imgscalr.Scalr;
import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.acl.AccessControlList;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;

import com.amazonaws.services.s3.model.S3Object;
import com.avaje.ebean.validation.Length;

import play.data.format.Formats;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="storage_objects")
public class StorageObject extends AppModel {
	
	public static String AMAZON_S3_URL = "https://s3.amazonaws.com/";
	
	@Length(max=255)
	private String name;
	
	private Long filesize;
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	private Date createdDate;
	
	@ManyToOne
	private Bucket bucket;
	

	public static Model.Finder<Integer,StorageObject> find = new Finder<Integer, StorageObject>(Integer.class, StorageObject.class);
	
	public static StorageObject getByName(String name)
	{
		return StorageObject.find.where().eq("name", name).findUnique();
	}

	public String getUrl()
	{
		Bucket b  = getBucket();
		
		if( ( b != null ) )
		{
			return AMAZON_S3_URL + b.getName() + "/" + getName() ;				
		}
		
		return null;
	}

	public Bucket getBucket() {
		return bucket;
	}

	public void setBucket(Bucket bucket) {
		this.bucket = bucket;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Long getFilesize() {
		return filesize;
	}

	public void setFilesize(Long filesize) {
		this.filesize = filesize;
	}
	
	public static void writeInputStream(String filename, Bucket b, String contentType, InputStream i, Long size) throws S3ServiceException
	{
		
			
		S3Service s3Service = new RestS3Service(b.getAmazonAccount().getJets3tAWSCredentials());
		
		S3Bucket s3bucket = s3Service.getBucket(b.getName());
		
		org.jets3t.service.model.S3Object obj = new org.jets3t.service.model.S3Object(filename);
		obj.setDataInputStream(i);
		obj.setContentType(contentType);
		obj.setContentLength(size);
		
		obj.setAcl(AccessControlList.REST_CANNED_PUBLIC_READ);
		
		s3Service.putObject(s3bucket, obj);
	}
	
	public static StorageObject updateStorageObjectByName( String name, String contentType, InputStream i,long size) throws S3ServiceException
	{
		return StorageObject.updateStorageObjectByName( name, contentType, i, size, Bucket.getDefault() );
	}
	
	public static StorageObject updateStorageObjectByName( String name, String contentType, InputStream i, long size, Bucket b ) throws S3ServiceException
	{
		
		StorageObject s = StorageObject.find.where().eq("bucket", b).eq("name", name).findUnique();
		
		if( s == null)
		{
			try
			{
				s = new StorageObject();
				
				s.setCreatedDate(new Date());
				s.setBucket( b );
				s.setName( name );
				s.setFilesize(size);
				s.save();
				
			}
			catch(Exception e)
			{
				s = null;
			}
			
		}
		
		if( s != null )
		{
			StorageObject.writeInputStream(name, b, contentType, i, size);
		}
		
		return s;
		
		
	}
	
	public static StorageObject updateStorageObjectWithImage( String name, InputStream i, ImageMetadata imageMetadata )
	{
		return updateStorageObjectWithImage(name,  i, imageMetadata, Bucket.getDefault());
	}
	
	public static StorageObject updateStorageObjectWithImage( String name, InputStream i, ImageMetadata imageMetadata, Bucket b )
	{
		
	    try
	    {
	    	BufferedImage sourceImage = ImageIO.read( i );
	        
	    	BufferedImage resizedImage = Scalr.resize(sourceImage,  Scalr.Method.BALANCED, Scalr.Mode.AUTOMATIC, User.imageMetadata.width, User.imageMetadata.height, Scalr.OP_ANTIALIAS );
	        
	        ByteArrayOutputStream encodedImageStream = new ByteArrayOutputStream();
	        
	        ImageIO.write(resizedImage, imageMetadata.imageType , encodedImageStream);
	        
	        String imageName = name;
	        
	        byte[] encodedImageByteArray = encodedImageStream.toByteArray();
	        
	        ByteArrayInputStream inputStreamByteArray = new ByteArrayInputStream(encodedImageByteArray);
	        
	        long size = encodedImageByteArray.length;
	        
	        encodedImageStream.close();
	        
	        StorageObject s = StorageObject.updateStorageObjectByName(imageName, imageMetadata.contentType , inputStreamByteArray, size);
	        
	        return s;
	        
	        
	    } 
	    catch (Exception e) 
	    {
	    	
	    }		
		
	    return null;
	}
	
	

}
