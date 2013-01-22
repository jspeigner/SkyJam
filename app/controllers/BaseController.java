package controllers;



import global.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;

import models.AppModel;
import models.StorageObject;
import models.behavior.ImageMetadata;

import com.typesafe.plugin.*;
import controllers.components.ControllerFilter;

import play.Play;
import play.mvc.*;
import play.mvc.Http.Context;
import play.mvc.Http.MultipartFormData;
import play.mvc.Http.MultipartFormData.FilePart;

@With(ControllerFilter.class)
public class BaseController extends Controller {

	public static final String PJAX_RESPONSE_HEADER  = "X-PJAX-URL";
	public static final String PJAX_RESPONSE_REDIRECT_HEADER = "X-PJAX-REDIRECT";
	public static final String PJAX_REQUEST_HEADER = "X-PJAX";
	
	public static long DAY_IN_MS = 1000 * 60 * 60 * 24;
	
	public static boolean setPjaxUrl(String url)
	{
		response().setHeader( PJAX_RESPONSE_HEADER, url );
		return true;
	}
	
	public static boolean setPjaxUrl(Call url)
	{
		response().setHeader( PJAX_RESPONSE_HEADER, url.toString() );
		return true;
	}
	
	public static Result pjaxRedirect( Call url )
	{
		
		String isPjax =  ( request().queryString().containsKey("_pjax") ) ? request().queryString().get("_pjax")[0] : "";
		
		if( isPjax != "" ){
			response().setHeader( PJAX_RESPONSE_REDIRECT_HEADER, url.toString() );
			setPjaxUrl(url);
			
			return ok("");
		}
		else
		{
			return redirect(url);
		}

	}
	
	public static boolean email(String to, String subject, String message ){
		
		try {
		
		
			MailerAPI mail = play.Play.application().plugin(MailerPlugin.class).email();
		    mail.setSubject(subject);
		    mail.addRecipient(to);
		    mail.addFrom( play.Play.application().configuration().getString("smtp.from") );
		    mail.send( message );
		    
		    return true;
		    
		} catch(Exception e){
			
		}
	    
		
		return false;
	}
	
	protected static boolean processImageUpload(AppModel model, String modelFieldAccessor, ImageMetadata imageMetadata ) {
		return processImageUpload(model, modelFieldAccessor, imageMetadata, "image");
	}
	
	/**
	 * Image file upload and model field update. It flashes notifications under "image_error" or "image_success" flash() call. 
	 * @param model
	 * @param modelFieldAccessor
	 * @param imageMetadata
	 * @param imageFilename
	 * @return
	 */
	protected static boolean processImageUpload(AppModel model, String modelFieldAccessor, ImageMetadata imageMetadata, String imageFilename ) {
		  
		  	MultipartFormData body = request().body().asMultipartFormData();
		  	FilePart picture = body.getFile(imageFilename);
			  
		  	boolean savedSuccessfully = false;
		  	Method imageFieldAccessor = null;
		  	
		  	
		  	
		  	try {
		  		Class<? extends AppModel> c = model.getClass();
				imageFieldAccessor = c.getMethod(modelFieldAccessor, StorageObject.class);
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		  	
		  	if (picture != null) {
				
			    File imageFile = picture.getFile();
			    
			    try {
			        long filesizeLimit = Play.application().configuration().getInt("application.thumbnail.max_filesize");
			    	
			    	if(imageFile.length() > filesizeLimit) {
			    		flash("image_error", "Image size exceeds the "+ Utils.humanReadableByteCount(filesizeLimit, true)+" limit");
			    	} else {
			    		
			    		StorageObject s = StorageObject.updateStorageObjectWithImage( imageMetadata.getFilename(model.getId()), new FileInputStream(imageFile), imageMetadata);
			    		imageFieldAccessor.invoke(model, s);
			    		model.update();
			    		model.refresh();
			    		
			    		savedSuccessfully = (s!=null);
			    		
		    	        if(savedSuccessfully){
		    	        	flash("image_success", "Your photo was updated successfully");
		    	        } else {
		    	        	flash("image_error", "There was an error changing the image.");
		    	        }
			    	}
			        
			    }
			    catch (Exception e) {
			    	
			    }
			    
			    imageFile.delete();
			    
			    
			}	  
		  	
		  	return savedSuccessfully;
		  	
		  }	

}
