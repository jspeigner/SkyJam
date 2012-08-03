function Application(config)
{
	
	this.config = config;
	
	var self = this;
	
	this.searchInterval = null;
	
	this.previousHeaderFormSearch = null; 
	
	
	// this is quick and dirty implementation
	this.onDomReady = function()
	{
		
		var inputHeaderFormSearch = "header form input:text" ;
		
		var cache = {}, lastXhr;
		
		var inputHeaderFormSearchJq = $( inputHeaderFormSearch ).autocomplete({
			
			minLength: 0,
			source: function( request, response ) {
				
				var term = request.term;
				
				/*
				if ( term in cache ) {
					response( cache[ term ] );
					return;
				}
				*/

				lastXhr = $.getJSON( config.urls.headerFormSearch, request, function( data, status, xhr ) {
					
					// cache[ term ] = data;
					
					if ( xhr === lastXhr ) {
						response( data );
					}
				});
			},
				
				
			focus: function( event, ui ) {
				$( inputHeaderFormSearch ).val( ui.item.label );
				return false;
			},
			select: function( event, ui ) {
				
				window.location = self.config.urls.playlist.replace("0", ui.item.id);
				
				return false;
			}
		});
		
		inputHeaderFormSearchJq.data( "autocomplete" )._renderItem = function( ul, item ) {
			
			var maxDescriptionChars = 50;
			var description = item.description.substring(0,maxDescriptionChars);
			
			if( item.description.length > maxDescriptionChars)
			{
				description += "...";
			}
			
			return $( "<li></li>" )
				.addClass('playlistSearchDropdown')
				.data( "item.autocomplete", item )
				.append( "<a>" + item.label + "<br><small>" + description + "</small></a>" )
				.appendTo( ul );
			
		};		

	};
};

