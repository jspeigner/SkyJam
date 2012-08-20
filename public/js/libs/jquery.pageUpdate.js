// jquery.pageUdate.js
// copyright igor mordashev aloise@aloise.name


(function($){
	
	$.parseHtmlPage = function(htmlString){
		
		  // empty set
		  var results = $();
		
		  var reBody = /<body[\s\S]*\/body>/;
		  var body=htmlString.match(reBody);
		  
		  if(body && body.length>0) 
		  {
			  body=body[0].replace(/^<body/, '<div');
			  body=body.replace(/body>$/, 'div>');

		  } 
		  else 
		  {
			  body=htmlString;  
		  }
		  
		  results = results.add( $(body).addClass("bodyTag") );
		
		  var reHead = /<head[\s\S]*\/head>/;
		  var head=htmlString.match(reHead);
		  
		  if(head && head.length>0) 
		  {
			  head=head[0].replace(/^<head/, '<div');
			  head=head.replace(/head>$/, 'div>');
			  
			  results = results.add( $(head).addClass("headTag") );
			   

		  } 
		  else 
		  {
			    
		  }
		  
		  return results;
		
	};

	$.updatePageFragments = function(responseText, targetPageFragments, sourcePageFragments )
	{
		sourcePageFragments = sourcePageFragments || targetPageFragments;
		
		var jResponseText = jQuery.parseHtmlPage(responseText);
		
		// update the user menu if exists
		if( targetPageFragments )
		{
			for(var i=0; i<targetPageFragments.length; i++)
			{
				var $fragment = $( sourcePageFragments[i], jResponseText );
				
				if($fragment.length)
				{
					$( targetPageFragments[i]).html( $fragment.html() );
				}
			}
		}		
	};
	
})(jQuery);