PlayerControlInterface = {
		
	playerId : "#player-container",
	playerTemplateId : "#player-template",
	playerControl: null,
	volume: null,
	defaultVolume:50,
	userAvailableButtons: [ 
        ".player-controls-save-as-favorite" 
	],
	
	setPlaylist : function(playlistId){
		
	},
	loadPlaylist : function( playlistId ){
		
		var playlistId = playlistId ? playlistId : 0; 
		var self = this; 
		
		
		$.when( PlaylistModel.findOne( { id: playlistId } ) ).then(
			    function(playlistResponse) {
			    	
			    	if( self.playerControl )
			    	{
			    		self.playerControl.clear();
			    		
			    	}
			    	
					var deviceAgent = navigator.userAgent.toLowerCase();
					var agentID = deviceAgent.match(/(iphone|ipod|ipad)/);
					var disableVolumeControl = false;
					if (agentID) {
						
				        // disable the volume control. It is not supported on iOS devices
						disableVolumeControl = true;
					}		
			    	
			    	
			    	self.playerControl = new PlayerControl( PlayerControlInterface.playerId , { 
			    		playlist : playlistResponse,
			    		volume : self.volume,
			    		disableVolumeControl : disableVolumeControl
			    	} );
			    	
			    	self.playerControl.setUser(application.user);
			    }
		).fail( function(){
				// playlist is empty
		});
		
	},
	
	init: function()
	{
		this.volume = this.defaultVolume;

		$(document).on("application:user", this.onUserChange );
		$(document).on(PlayerControl.event.VOLUME_CHANGED, this.onVolumeChange );
		
		PlayerControlInterface.loadPlaylist();
	},

	onVolumeChange: function(event, volume){
		PlayerControlInterface.volume = volume;
	},
	
	
	onSoundManagerTimeout: function()
	{
		
	},
	onUserChange: function( event, userData )
	{
		if( PlayerControlInterface.playerControl )
		{
			PlayerControlInterface.playerControl.setUser(userData);
		}
		
		
		for(var i =0; i < PlayerControlInterface.userAvailableButtons.length; i++)
		{
			if( userData )
			{
				$(PlayerControlInterface.userAvailableButtons[i]).removeClass("disabled").removeClass("hide");
			}
			else
			{
				$(PlayerControlInterface.userAvailableButtons[i]).addClass("disabled");
			}
			
		}

		
	},
	
	saveUserPlaylist: function(playlistId){
		
		var selector = ".player-controls-save-as-favorite[data-playlist-id="+playlistId+"]";
		var deleteSelector = ".player-controls-delete-from-favorite[data-playlist-id="+playlistId+"]";
		
		$(selector).addClass("disabled");
		
		$.get( application.config.urls.saveFavoritePlaylist.replace(application.config.urls.idPlaceholder,playlistId ) , { }, function(){
			
			$(selector).text("Saved");
			
			
			setTimeout(function(){
				$(selector).fadeOut("slow");
				
				
			}, 1000);
		} );
	}
	
	
	
};




PlayerControl = can.Control({
	
	PlayerState : {
		PLAY : "play",
		PAUSE : "pause",
		STOP : "stop"
	},
	
	currentSong : null,
	currentSongSMSound: null,
	currentPlayerState: null,
	playFailedCount: 0,
	user:null,
	userPlaylistData:null,
	volume:50,
	
	init: function( element, options ){
	  
		var self = this;
		
		this.volume = options.volume === undefined ? this.volume : options.volume;
		
		this.playlistData = options.playlist;
		
		

		
		
		this.currentPlayerState = this.PlayerState.STOP;
		
		var defaultSong = null;
		
		if( options.playlist )
		{
	    
		    if( this.playlistData.PlaylistSong && ( this.playlistData.PlaylistSong.length > 0 ) )
		    {
		    	if( options.default_playlist_song_id )
		    	{
		    		for(var playlistSongId in this.playlistData.PlaylistSong)
		    		{
		    			if( playlistData.PlaylistSong[playlistSongId].id == options.default_playlist_song_id )
		    			{
		    				defaultSong = playlistData.PlaylistSong[playlistSongId];
		    				
		    				break;
		    			}
		    		}
		    	}
		    	else
		    	{
		    		defaultSong = this.playlistData.PlaylistSong[0];
		    	}
		    }
		    
		    this.element.html( can.view( PlayerControlInterface.playerTemplateId, { playlist: this.playlistData, song: null }) );
	    
			if( options.disableVolumeControl )
			{
				$(".volume", this.element).hide();
			}		    
		    
		    if( defaultSong )
		    {
		    	this.loadSong( defaultSong );
		    	this.playSong();
		    }
		}
		
		$( ".volume .volume-slider-vertical", this.element ).slider({
			orientation: "vertical",
			range: "min",
			min: 0,
			max: 100,
			value: this.volume,
			slide: function( event, ui ) {
				
				// $( "#amount" ).val( ui.value );
				
				self.onVolumeChange( ui.value );
				
			}
		});
		
		
		this.refreshUserButtons();
	    
  } ,
  
  onVolumeChange: function(volume){
	  
	  volume = Math.max( 0, Math.min(100, volume) );
   	  this.volume = volume;
	  
	  if( this.currentSongSMSound )
	  {
		
		this.currentSongSMSound.setVolume( volume );

	  }
	$(document).trigger(PlayerControl.event.VOLUME_CHANGED, [ volume ]);
	  
	  
  },
  
  setUser: function(userData)
  {
	  this.user = userData;
	  
	  if( this.user != null  )
	  {
		  
		  this.refreshUserPlaylistData();
		  
	  }
	  else
	  {
		  this.userPlaylistData = null;
		  
	  }
	  
	  this.refreshUserButtons();
	  
	  
  },
  
  refreshUserPlaylistData: function()
  {
	  var self = this;
	  
	  if( this.options.playlist )
	  {
		  
		  $.get( application.config.player.urls.get_user_playlist_data.replace( application.config.urls.idPlaceholder , this.options.playlist.id ), {}, function(data, xhr, status){
			  
			  self.onUserPlaylistDataLoaded(data, self.user );
			  
		  }).error( function(){
			  
			  self.userPlaylistData = null;
			  
		  });
		  
		  return true;
	  }	  
	  
	  return false;
	  
	  	  
  },
  
  onUserPlaylistDataLoaded: function(data, user)
  {
	  this.userPlaylistData = data;
	  
	  this.refreshUserButtons();
	  
  },
  
  getUserPlaylistDataByPlaylistSongId : function(playlistSongId)
  {
	  if( this.userPlaylistData )
	  {
		  var playlistSongData = {};
		  
		  $.each( this.userPlaylistData , function(innerModelName, innerModel ){
			 
			  playlistSongData[innerModelName] = [];
			 
			 $.each( innerModel, function(entryIndex, entry){
				 if( entry.playlist_song_id == playlistSongId ){
					 playlistSongData[ innerModelName ].push( entry);
				 }
					
			 } );
			 
		  } );
		  
		  return playlistSongData;
		  
	  }
	  else 
	  {
		  return null;
	  }
		  
	
  },
  
  loadSong: function(playlistSongData)
  {
	  
	  if( this.currentSong && ( this.currentSong.id != playlistSongData.id ) )
	  {
		  this.clearCurrentSong();
		  
	  }
	  
	  this.setProgressIndicator(0);
	  
	  this.currentSong = playlistSongData;
	  
	  // this.currentPlayerState = this.PlayerState.STOP;
	  
	  if( !this.currentSongSMSound )
	  {
		   
		this._createCurrentSMSong('PlaylistSong.'+this.currentSong.id, this.currentSong.Song.song_url);
		  
	  }
	  
	  $(".name .song").text( this.currentSong.Song.name );
	  
	  this.refreshUserButtons();
	  
	  return true;
	  
  },
  /**
   * Progress indicator [0,1]
   */
  setProgressIndicator:function(value)
  {
	  value = ( value > 1 ) ? 1 : ( value < 0 ? 0 : value ); 
	  
	  $("li.name .song-progress").css("width",  ( value*100 ) +"%");
  },
  
  _createCurrentSMSong: function(id, url)
  {
	  var self = this;
	  
	  // var defaultVolume = $( ".volume .volume-slider-vertical", this.element ).slider("value");
	  
	  this.currentSongSMSound = soundManager.createSound({ 
		  id: id, 
		  url: url,
		  autoPlay: false,
		  autoLoad: true,
		  isMovieStar: null,
		  volume: this.volume,
		  
		  // events
		  
		  onload: function(loadSuccess){ 
			  	self.eventCallbacks.onSMSongLoad( self, this, loadSuccess);  
		  },
		  onfinish: function(){ 
			  self.eventCallbacks.onSMSongFinish( self,this);
		  },
		  whileplaying: function(){
			  self.eventCallbacks.onSMSongWhilePlaying(self,this);
		  },
		  onfailure : function(){ 
			  self.eventCallbacks.onSMSongFailure(self,this);
		  }
		  
		  
	  });
	  
  },
  onListFinished: function()
  {
	  // zero the fail counter
	  if( this.playFailedCount < this.options.playlist.PlaylistSong.length )
	  {
		  this.playFailedCount = 0;
	  }
	  
  },
  
  onPlayFailed: function()
  {
	  if( ++this.playFailedCount < this.options.playlist.PlaylistSong.length )
	  {
		  this.goToNextSong( );
	  }		  
	  
  },
  
  eventCallbacks: 
  {
	  
	  onSMSongLoad: function(control, smSong, loadSuccess)
	  {
		  if( ! loadSuccess )
		  {
			  // skip to next song on error  
			  control.onPlayFailed( smSong );
		  }
		  else
		  {
			  --control.playFailedCount;
		  }
			 
	  },
	  
	  onSMSongFinish:function(control, smSong)
	  {
		  control.goToNextSong( );
	  },
	  
	  onSMSongWhilePlaying: function(control,smSong)
	  {
		  // console.log( 'sound '+smSong.id+' playing, '+smSong.position+' of '+smSong.duration );
		  if( smSong.duration && ( smSong.duration > 0 ))
		  {
			  control.setProgressIndicator( smSong.position / smSong.duration );
		  }
	  },
	  
	  onSMSongFailure: function(control, smSong)
	  {
		  control.onPlayFailed( smSong );
	  }
  },
  
  playSong: function()
  {
	
	  
	  
	  if( this.currentSongSMSound )
	  {
		  this.currentSongSMSound.play();
		  
		  this.currentPlayerState = this.PlayerState.PLAY;
		  
		  $( ".play", this.element).hide();
		  $( ".pause", this.element).show();
		  

		  
		  var activity = new UserPlaylistActivityModel({ playlist_song_id:  this.currentSong.id , type : UserPlaylistActivityModel.ACTIVITY_TYPE.PLAY }); 
		  activity.save();
		  
	  }
	  


  },
	  
  clear: function(){
	  
	  this.clearCurrentSong();
	  this.destroy();
	  
	  
	  
  },
	 
  clearCurrentSong: function()
  {
	  if( this.currentSong )
	  {
		  this.currentSong = null;
	  }
	  
	  if( this.currentSongSMSound )
	  {
		  this.currentSongSMSound.destruct();
		  this.currentSongSMSound = null;
	  }
		  
  },
  
  pauseSong: function()
  {
	  if( this.currentPlayerState == this.PlayerState.PLAY)
	  {
		  this.currentPlayerState = this.PlayerState.PAUSE;
		  
		  this.currentSongSMSound.pause();
		  
		  $( ".play", this.element).show();
		  $( ".pause", this.element).hide();
		  

		  var activity = new UserPlaylistActivityModel({ playlist_song_id:  this.currentSong.id , type : UserPlaylistActivityModel.ACTIVITY_TYPE.PAUSE }); 
		  activity.save();
		  
	  }
  },
  
  refreshUserButtons: function(){
	  
	  if( this.user != null ){
		  
		  if( this.options.playlist ) {
			  
			  if( this.currentSong ){
				  var userPlaylistSongData = this.getUserPlaylistDataByPlaylistSongId( this.currentSong.id );
				  
				  if ( userPlaylistSongData )
				  {
					  if( userPlaylistSongData.PlaylistSongRating && userPlaylistSongData.PlaylistSongRating.length ){
						  this.disableRatingButtons();
					  } else {
						  this.enableRatingButtons();
					  }
					  
					  
				  }
				  else
				  {
					  this.disableRatingButtons();
				  }
				  
				  this.enableShareButton();
				  
			  } else {
				  this.disableUserButtons();
			  }
			  
			  
			  
			  
		  } else {
			  this.disableUserButtons();
		  }
		  
			 
		  
		  
		  
	  }  else {
		  this.disableUserButtons();
	  }
		  
  },
  
  goToNextSong: function(play)
  {
	  play = ( play === undefined ) ? ( this.currentPlayerState == this.PlayerState.PLAY ) : play ;
	  
	  if( this.options.playlist )
	  {
		  var nextSong = null;
		  
		  if( this.currentSong )
		  {
			  nextSong = this.getNextSong( this.options.playlist, this.currentSong.id ); 
				  
			  this.clearCurrentSong();
		  }
		  else
		  {
			  nextSong = this.options.playlist.PlaylistSong[0];
		  }
		  
		  if( nextSong )
		  {
			  this.loadSong(nextSong);
			  
			  if( play )
			  {
				  this.playSong();
			  }
		  }
			  
	  }
  },
  
  disableUserButtons:function(){
	  this.disableShareButton();
	  this.disableRatingButtons();
  },
  
  enableUserButtons:function(){
	  this.enableShareButton();
	  this.enableRatingButtons();
  },
  disableRatingButtons: function(){
	  $( ".like a, .dislike a",this.element).addClass("disabled");
  },
  enableRatingButtons: function()
  {
	  $( ".like a, .dislike a",this.element).removeClass("disabled");
  },
  enableShareButton: function(){
	  $( ".share a",this.element).removeClass("disabled");
  },
  disableShareButton: function(){
	  $(".share a" ,this.element).addClass("disabled");
  },
  
  
  getNextSong: function(playlist, playlistSongId){
	  
	  var count = playlist.PlaylistSong.length;
	  
	  for( i = 0; i < count ; i++)
	  {
		  if( playlist.PlaylistSong[i].id == playlistSongId )
		  {
			  if( i+1 >= count )
			  {
				  this.onListFinished();
			  }
			  
			  return playlist.PlaylistSong[ (i + 1) % count ]; 
		  }
			  
	  }
	  
	  return null;
  },
  
  ".play a click" : function(el , event){
	  	  
	  		
		  this.playSong();
	  
  },
  
  
  ".pause a click" : function(el , event){
	  this.pauseSong();
  },
  
  ".next-song a click" : function(el , event){
	  this.goToNextSong(  );
	  
	  var activity = new UserPlaylistActivityModel({ playlist_song_id:  this.currentSong.id , type : UserPlaylistActivityModel.ACTIVITY_TYPE.SKIP }); 
	  activity.save();
	  
	  
  },
  
  ".volume mouseover" : function(el , event){
	  if( this.currentSongSMSound )
	  {
		  $(".volume-control", this.element).show();
		  
		  /*
		  this.currentSongSMSound.mute();
		  $(".volume", this.element).hide();
		  $(".volume-off", this.element).show();
		  */
	  }
	  
	  return false;
  },  
  
  ".volume mouseout" : function(el, event){
	  $(".volume-control", this.element).hide();
  },
  
  ".volume-off a click" : function(el , event){
	  if( this.currentSongSMSound )
	  {
		  this.currentSongSMSound.unmute();
		  $(".volume", this.element).show();
		  $(".volume-off", this.element).hide();
		  
	  }	  
	  
	  return false;
  },  
  
  ".like a click" : function(el , event){
	  
	  if(!el.is(".disabled")){
		  
		  var a = new PlaylistSongRatingModel({ playlist_song_id:  this.currentSong.id , type : PlaylistSongRatingModel.TYPE.LIKE });
		  
		  var self = this;
		  $.when( a.save()).then( function(){ 
			  self.refreshUserPlaylistData();
		  });
		  
		  // update the playlist data
		  
		  
		  this.disableRatingButtons();
		  
		  
	  }
		 
	  return false;
	  
  },  
  ".dislike a click" : function(el , event){
	  
	  if(!el.is(".disabled")){
		  var a = new PlaylistSongRatingModel({ playlist_song_id:  this.currentSong.id , type : PlaylistSongRatingModel.TYPE.DISLIKE });
		  a.save(); 
		  
		  var self = this;
		  $.when( a.save()).then( function(){ 
			  self.refreshUserPlaylistData();
		  });
		  
		  this.disableRatingButtons();
		  
	  }
	  
	  return false;
  },  
  ".share a click" : function(el , event){
	  if(!el.is(".disabled")){
		  
		  $("#player-share-modal", this.element).modal("show");
		  
	  }
	  
	  return false;
  },
  
  "#player-share-modal a.facebook click" : function(el, event){
	  var playlistUrl = application.config.urls.playlist.replace( application.config.urls.idPlaceholder, this.options.playlist.id );
	  var description = 'Playing ' + this.options.playlist.name + " " + this.currentSong.song + " " + this.currentSong.Song.Album.Artist.name + " - " + this.currentSong.Song.name;
	  // var playlist = ;
	  
      // calling the API ...
      var obj = {
        method: 		'feed',
        name: 			'Skyjam',
        description:	description,
        link: 			playlistUrl

      };

      function callback(response) {
        // document.getElementById('msg').innerHTML = "Post ID: " + response['post_id'];
      }

      FB.ui(obj, callback);	  
      
      $("#player-share-modal", this.element).modal("hide");
  } ,
  "#player-share-modal a.twitter click" : function(el, event){
	  
	  // $("#player-share-modal", this.element).modal("hide");
	  
	  var playlistUrl = application.config.urls.playlist.replace( application.config.urls.idPlaceholder, this.options.playlist.id );
	  var description = 'Listening to ' + this.options.playlist.name + " " + this.currentSong.song + " " + this.currentSong.Song.Album.Artist.name + " - " + this.currentSong.Song.name+ " #skyjam";
	  
	  var twittBox =$("#player-share-twitter-modal");
	  
	  $("#player-share-modal", this.element).modal("hide");
	  
	  twttr.anywhere(function (T) {
		  
		    T("#player-share-modal-modal-body").tweetBox({
		    	height: 100,
		      	width: 500,
		      	defaultContent: description,
		      	onTweet: function(){
		      		twittBox.modal("hide");
		      }
		    });
		    
		    
		 
	  });	  
	  
	  twittBox.modal("show");
	  
	  /*
	  <a href="https://twitter.com/share" class="twitter-share-button" data-url="http://xxx/" data-text="yyy" data-size="large">Tweet</a>
	  <script>!function(d,s,id){var js,fjs=d.getElementsByTagName(s)[0];if(!d.getElementById(id)){js=d.createElement(s);js.id=id;js.src="//platform.twitter.com/widgets.js";fjs.parentNode.insertBefore(js,fjs);}}(document,"script","twitter-wjs");</script>
	  */
  }   
  
});

PlayerControl.event = {
		PLAYLIST_LOADED : "player:playlist_loaded",
		SONG_STARTED : "player:song_started",
		VOLUME_CHANGED : "player:volume_changed"
};


// Models

PlaylistModel = can.Model({
	
	// update with id templates
	findOne : application.config.player.urls.get_current_playlist.replace(application.config.urls.idPlaceholder,"{id}")
	
},{});

PlaylistSongModel = can.Model({
	
}, {});

UserPlaylistActivityModel = can.Model({
	
	create: application.config.player.urls.save_playlist_song_activity
	
}, {});
UserPlaylistActivityModel.ACTIVITY_TYPE = {
		PLAY : "play",
		PAUSE : "pause",
		SKIP: "skip"
};

PlaylistSongRatingModel = can.Model({
	
	create: application.config.player.urls.save_playlist_song_rating
	
}, {});

PlaylistSongRatingModel.TYPE = {
		LIKE : "like",
		DISLIKE : "dislike"
		
};

// Init the player
// resolve on both dom ready and sound manager ready

var df1 = $.Deferred(), df2 = $.Deferred();

$.when( df1, df2 ).done( function(){
	
	  if( $("#player-container").length )
	  {
		  PlayerControlInterface.init();
		  PlayerControlInterface.onUserChange(null, application.user);
	  
	  
		  $(document).on("click", ".player-controls-play", function(){
			  
			  var playlistId = $(this).data("playlist-id");
			  
			  PlayerControlInterface.loadPlaylist( playlistId );
			  
			  return false;
		  });
	  }	
});


$(document).ready(function(){
	df1.resolve();
});

soundManager.setup({
	  url: application.config.player.soundmanager.swf_path,
	  flashVersion: 9, // optional: shiny features (default = 8)
	  useFlashBlock: false, // optionally, enable when you're ready to dive in/**
	  onready: function(){
		  df2.resolve();
	  },
	  ontimeout: function() {

		    // Hrmm, SM2 could not start. Missing SWF? Flash blocked? Show an error, etc.?
		    // See the flashblock demo when you want to start getting fancy.

		  PlayerControlInterface.onSoundManagerTimeout();
		  
	  }
});




