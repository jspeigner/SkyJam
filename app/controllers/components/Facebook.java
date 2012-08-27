package controllers.components;

public class Facebook {

	
	public enum UserImageType
	{
		LARGE("large"),
		SQUARE("square"),
		SMALL("small");
		
		private String urlParam;
		
		UserImageType(String urlParam){
			this.setUrlParam(urlParam);
		}

		public String getUrlParam() {
			return urlParam;
		}

		private void setUrlParam(String urlParam) {
			this.urlParam = urlParam;
		}
	};
	
	
	public static String getUserImageUrl(String userId, UserImageType type){
		
		return "http://graph.facebook.com/"+userId+"/picture?type="+type.getUrlParam();
		
	}
	
	
}
