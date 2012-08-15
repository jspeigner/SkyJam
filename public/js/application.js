function Application(config)
{
	
	this.config = config;
	
	var self = this;
	
	this.searchInterval = null;
	
	this.previousHeaderFormSearch = null; 
	
	this.bodyContentSelector = "#body-content";
	
	this.jCurrentForm = null;
	
	
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
	
	

	
	this.onFormSubmitSuccess = function(responseText, statusText, xhr, $form)
	{
		self.enableForm($form);
		
		// for normal html responses, the first argument to the success callback 
	    // is the XMLHttpRequest object's responseText property 
	 
	    // if the ajaxForm method was passed an Options Object with the dataType 
	    // property set to 'xml' then the first argument to the success callback 
	    // is the XMLHttpRequest object's responseXML property 
	 
	    // if the ajaxForm method was passed an Options Object with the dataType 
	    // property set to 'json' then the first argument to the success callback 
	    // is the json data object returned by the server 		
		
		// alert('status: ' + statusText + '\n\nresponseText: \n' + responseText +  '\n\nThe output div should have already been updated with the responseText.');
		
		var redirectUrl = xhr.getResponseHeader('X-PJAX-REDIRECT');
		
		
		if( redirectUrl )
		{
			// emulate pjax click
			
			var a = $("<a>").attr("href", redirectUrl);
			
			var opts = {
				fragment : self.bodyContentSelector
			};
			
			var event = jQuery.Event("click");
			event.currentTarget = a[0];
			
			$.pjax.click(event, self.bodyContentSelector, opts);
			
			return true;
			
		}

		
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
	
	this.onPjaxEnd = function(event, xhrDeferred,options)
	{
		// update the user menu if exists
		
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
		
		
		// ajax navigation
		$(document).on('click', 'a', function(event) {
			
			var container = $(self.bodyContentSelector);
			container.on('pjax:end', self.onPjaxEnd);
			
			return $.pjax.click(event, container, {
				fragment : self.bodyContentSelector
			});
		});		
		
		
		
		$("form").ajaxForm({
			replaceTarget: 	true,
			delegation: 	true,
			dataType:		"html",
			target: 		self.bodyContentSelector,
			beforeSubmit: 	self.onFormBeforeSubmit,
			success: 		self.onFormSubmitSuccess,
			fragment: 		self.bodyContentSelector
			
		});
		
	};
};

