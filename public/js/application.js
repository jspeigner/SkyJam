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
		$.event.trigger("application:user", [ this.user ]);
	};
	
	this.autocompleteItemRender = function( ul, item ) {
		
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
	
	this.disableForm = function($form)
	{
		$form = $($form);
		$("input, select, textarea").each( function(){
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
		$("input, select, textarea").each(function(){
			var $this = $(this);
			
			if( !$this.data("previous-disabled-state") )
			{
				$this.prop("disabled", false);
			}
				
		});
		
	};
	
	this.stripPjaxParam = function (url) {
		  return url
		    .replace(/\?_pjax=[^&]+&?/, '?')
		    .replace(/_pjax=[^&]+&?/, '')
		    .replace(/[\?&]$/, '')
	}
	
	
	this.onFormBeforeSubmit = function(formFields, $form, options)
	{
		self.jCurrentForm = $form;
		
		self.disableForm($form);
		
		return true;
	};
	
	this.processPjaxRedirect = function(xhr)
	{
		var redirectUrl = xhr.getResponseHeader('X-PJAX-REDIRECT');
		
		
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
			
			var container = $(self.bodyContentSelector);
			// container.on('pjax:complete', self.onPjaxComplete);		
			
			// ajax navigation
				
			$.pjax.click(event, container, {fragment : self.bodyContentSelector });
			
			
			
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
		
		/*
		if( self.processPjaxRedirect(xhr) )
		{
			return true;
		}
		*/
		
		self.onPjaxComplete(null, xhr, statusText);
		
		// push state
		if($.support.pjax)
		{
			
			var formActionUrl = $form.attr("action");
			
			var pageUrl = self.stripPjaxParam(xhr.getResponseHeader('X-PJAX-URL') || formActionUrl );
			
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
		
		
		if( responseText && this.fragment )
		{
			var jResponseText = $(responseText);
			
			var jResponseTextFragment = $(this.fragment, jResponseText );
			
			var formActionUrl = self.jCurrentForm ? self.jCurrentForm.attr("action") : null;
			
			if(jResponseTextFragment.length)
			{
				$( self.bodyContentSelector ).replaceWith( jResponseTextFragment );
			}
			
			var strResponseTitle = $("title", jResponseText ).text();
			if( strResponseTitle ) 
			{
				$("head title").text(strResponseTitle);
			}

			
			
		}
		
		self.jCurrentForm = null;
		
	};
	
	this.updatePageFragments = function(responseText)
	{
		var jResponseText = $(responseText);
		
		// update the user menu if exists
		if( jResponseText.length && self.pjaxAdditionalFragments )
		{
			for(var i=0; i<self.pjaxAdditionalFragments.length; i++)
			{
				var $fragment = $( self.pjaxAdditionalFragments[i], jResponseText );
				if($fragment.length)
				{
					$( self.pjaxAdditionalFragments[i]).replaceWith($fragment);
				}
			}
		}		
	}
	
	this.onPjaxComplete = function(event, xhr, textStatus, options)
	{

		if( ( textStatus == "success" ) )
		{
		
			if( self.processPjaxRedirect(xhr) )
			{
				return true;
			}
			
			self.updatePageFragments(xhr.responseText);

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
		
		
	};
	
	this.onPjaxError = function(e, xhr, textStatus, errorThrown, options)
	{
		e.preventDefault();
		
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
					
					window.location = self.config.urls.playlist.replace("0", ui.item.id);
					
					return false;
				}
			});
			
			inputHeaderFormSearchJq.data( "autocomplete" )._renderItem = this.autocompleteItemRender;			
		}
		
		var pjaxContainer = $(self.bodyContentSelector);
		pjaxContainer.on('pjax:complete', self.onPjaxComplete);		
		pjaxContainer.on('pjax:error', self.onPjaxError);
		
		// ajax navigation
		$(document).on('click', 'a:not(.no-pjax):not(#player > *)', function(event) {
			
			$.pjax.click(event, pjaxContainer, {
				fragment : self.bodyContentSelector
			});
			
			return false;
		});		
		
		
		// on logout click
		$(document).on( "click", "#user-top-menu .logout", function(event){
			
			var logoutUrl = $(this).attr("href");
			
			$.get( logoutUrl, {}, function(data, textStatus, jqXHR){
				self.updatePageFragments(data);
				self.refreshAuthUser();
			});
			
			/*
			var container = $(self.bodyContentSelector);
			container.on('pjax:complete', function(){
				// on logout done
				self.setUser(null);
			});
			$.pjax.click(event, container, {
				fragment : self.bodyContentSelector
			});
			*/
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
		

	};
	
	if( config.user )
	{
		this.setUser(config.user);
	}
};

