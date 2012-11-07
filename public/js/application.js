/**
 * 
 * Global Events:
 * $(document).on("app:page_load")
 * 
 * @param config
 * @returns Object
 */

function Application(config)
{
	
	this.config = config;
	
	var self = this;
	
	this.searchInterval = null;
	
	this.previousHeaderFormSearch = null; 
	
	this.bodyContentSelector = "#body-content";
	
	this.pjaxAdditionalFragments = [ "#user-top-menu" ];
	
	this.jCurrentForm = null;
	
	this.user = null;
	
	this.getUser = function ()
	{
		return self.user;
	};
	
	this.setUser = function (newUser)
	{
		this.user = newUser;
		
		// update the bottom menu
		if( this.user ){
			$("footer .user-logged-menu").removeClass("hidden");
			$("footer .user-unlogged-menu").addClass("hidden");
		} else {
			$("footer .user-logged-menu").addClass("hidden");
			$("footer .user-unlogged-menu").removeClass("hidden");
		}
		
		$.event.trigger("application:user", [ this.user ]);
	};
	
	this.autocompleteItemRender = function( ul, item ) {
		
		var maxDescriptionChars = 100;
		var description = item.description.substring(0,maxDescriptionChars);
		
		if( item.description.length > maxDescriptionChars)
		{
			description += "...";
		}
		
		var artistsMaxChars = 80;
		
		var artists = item.artists.join(", ");
		
		var artistsText = "With Artists: "+(  artists.length > artistsMaxChars ? ( artists.substring(0, artistsMaxChars)+"..." ) : artists );
		
		return $( "<li></li>" )
			.addClass("playlistSearchDropdown")
			.data( "item.autocomplete", item )
			.append( 
					$("<a>").attr("href", self.config.urls.playlist.replace( application.config.urls.idPlaceholder , item.id) )
						.append( $("<span>").addClass("name").text( item.label ) )
						.append( $("<span>").addClass("artists").text(artistsText) )
						.append( $("<span>").addClass("description").text( description) )
			)
			
			.appendTo( ul );
		
	};		
	
	this.disableForm = function($form)
	{
		$form = $($form);
		$("input, select, textarea", $form).each( function(){
			var $this = $(this);

			$this.data("previous-disabled-state", $this.prop("disabled") );

			$this.prop("disabled", true);
		});
		
		
		
	};
	
	this.refreshAuthUser = function()
	{
		$.get( self.config.urls.getAuthUser, {}, function(data, textStatus, jqXHR){
			
			self.setUser(data);
			
		}, "json" );
	}
	
	this.enableForm = function($form)
	{
		$form = $($form);
		$("input, select, textarea", $form).each(function(){
			var $this = $(this);
			
			if( !$this.data("previous-disabled-state") )
			{
				$this.prop("disabled", false);
			}
				
		});
		
	};
	
	this.stripPjaxParam = function (url) {
		  return url
		    .replace(/\?_pjax=[^&]+&?/, "?")
		    .replace(/_pjax=[^&]+&?/, "")
		    .replace(/[\?&]$/, "")
	}
	
	
	this.onFormBeforeSubmit = function(formFields, $form, options)
	{
		self.jCurrentForm = $form;
		
		self.disableForm($form);
		
		return true;
	};
	
	this.processPjaxRedirect = function(xhr)
	{
		var redirectUrl = xhr.getResponseHeader("X-PJAX-REDIRECT");
		
		
		if( redirectUrl && ( !xhr.pjaxRedirected  ) )
		{
			// emulate pjax click
			
			xhr.pjaxRedirected = true;
			
			var a = $("<a>").attr("href", redirectUrl).click( 
					function(e){ 
						e.preventDefault(); 
						return false; 
					}
			);
			
			var event = jQuery.Event("click");
			event.currentTarget = a[0];
			
			
			var self = this;
			$("form", $( self.bodyContentSelector) ).each(function(){
				self.disableForm($(this));
			});
			
			
			// ajax navigation
				
			$.pjax.click(event, $(self.bodyContentSelector), {fragment : self.bodyContentSelector, timeout:0 });
			
			
			
			return true;
			
		}	
		
		return false;
	}

	
	this.onFormSubmitSuccess = function(responseText, statusText, xhr, $form)
	{
		self.enableForm($form);

		if( $form.attr("id") == "user-sign-in" )
		{
			// refresh the user data
			self.refreshAuthUser();
		}
		
		// process redirect and page fragments
		self.onPjaxComplete(null, xhr, statusText);
		
		// push state
		if($.support.pjax)
		{
			
			var formActionUrl = $form.attr("action");
			
			var pageUrl = self.stripPjaxParam(xhr.getResponseHeader("X-PJAX-URL") || formActionUrl );
			
			var strResponseTitle = "";
			
			var pjaxState = {
				      id: "form_"+(new Date).getTime(),
				      url: pageUrl,
				      title: strResponseTitle,
				      container: this.target,
				      fragment: this.fragment,
				      timeout: 0
				   
			};

			
			window.history.pushState(pjaxState, strResponseTitle, pageUrl);
			
		}		
		
		self.jCurrentForm = null;
		
		$(document).trigger( Application.events.PAGE_LOAD );
		
	};
	
	this.onPjaxComplete = function(event, xhr, textStatus, options)
	{

		if( ( textStatus == "success" ) )
		{
		
			if( self.processPjaxRedirect(xhr) )
			{
				return true;
			}
			
			jQuery.updatePageFragments(xhr.responseText, ["title", self.bodyContentSelector ].concat(self.pjaxAdditionalFragments));

		}
		
		
		// logout
		if( event )
		{
			event.preventDefault();
			
			if( event.relatedTarget && $(event.relatedTarget).is(".logout") )
			{
				self.refreshAuthUser();
			}
			
		}
		
		$(document).trigger( Application.events.PAGE_LOAD );
	};
	
	this.onPjaxError = function(e, xhr, textStatus, errorThrown, options)
	{
		e.preventDefault();
		
		if( xhr.responseText )
		{
			jQuery.updatePageFragments(xhr.responseText, ["title", self.bodyContentSelector ], ["title", "body" ] );
			
		}
		
		
		return false;
	};
	
	
	// this is quick and dirty implementation
	this.onDomReady = function()
	{
		
		
		var inputHeaderFormSearch = "header form input:text" ;
		
		if( $(inputHeaderFormSearch).length )
		{
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
					
					// window.location = self.config.urls.playlist.replace( application.config.urls.idPlaceholder , ui.item.id);
					
					return false;
				}
			});
			
			inputHeaderFormSearchJq.data( "autocomplete" )._renderItem = this.autocompleteItemRender;			
		}
		
		
		$(document).on("click", ".player-controls-save-as-favorite", function(){
			
			if( !$(this).is(".disabled") )
			{
				PlayerControlInterface.saveUserPlaylist( $(this).data("playlist-id") );
			}
			
		});
		
		$(document).on("click", ".player-controls-save", function(){
			
			if( !$(this).is(".disabled") )
			{
				PlayerControlInterface.saveUserPlaylist( $(this).data("playlist-id") );
			}
			
		});		
		
		
		if( application.config.pjax )
		{
			
			var pjaxContainer = $(self.bodyContentSelector);
			pjaxContainer.on("pjax:complete", self.onPjaxComplete);		
			pjaxContainer.on("pjax:error", self.onPjaxError);
			
			// ajax navigation
			$(document).on("click", "a:not(.no-pjax):not(#player-container > *)", function(event) {
				
				$.pjax.click(event, pjaxContainer, {
					fragment : self.bodyContentSelector,	
					timeout: 0
				});
				
				return false;
			});			
			
			
			// on logout click
			$(document).on( "click", "#user-top-menu .logout", function(event){
				
				var logoutUrl = $(this).attr("href");
				
				$.get( logoutUrl, {}, function(data, textStatus, jqXHR){
					
					jQuery.updatePageFragments(data, ["title", self.bodyContentSelector ].concat(self.pjaxAdditionalFragments) );
					self.refreshAuthUser();
					
				});
				
				return false;
			});
			
			$(document).on("submit", "form:not(.no-pjax)", function(event){
		        	
		            event.preventDefault();
		            
		            var pjaxAction = URI($(this).attr("action"));
		            pjaxAction.addSearch("_pjax", "form");
		            
		            $(this).attr("action", pjaxAction.toString() );
		            
		            $(this).ajaxSubmit({
		    			replaceTarget: 	true,
		    			// delegation: 	true,
		    			dataType:		"html",
		    			target: 		self.bodyContentSelector,
		    			fragment: 		self.bodyContentSelector,

		    			beforeSubmit: 	self.onFormBeforeSubmit,
		    			success: 		self.onFormSubmitSuccess

		            });
		        			
			});			
		}
		

		$(document).trigger( Application.events.PAGE_LOAD );

	};
	
	if( config.user )
	{
		this.setUser(config.user);
	}
};

Application.events = {
		PAGE_LOAD : "app:page_load"
}


