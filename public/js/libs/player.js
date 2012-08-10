
PlayerControlInterface = {
		
	playerId : "#player",
	playerTemplateId : "#player-template",
	playerControl: null,
	
	
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
			    	
			    	self.playerControl = new PlayerControl( PlayerControlInterface.playerId , { 
			    		playlist : playlistResponse 
			    	} );
			    }
		);		
	},
	
	init: function()
	{
		PlayerControlInterface.loadPlaylist();
	},
	
	onSoundManagerTimeout: function()
	{
		
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
	
	init: function( element, options ){
	  
		var self = this;
		
		this.playlistData = options.playlist;
		
		this.currentPlayerState = this.PlayerState.STOP;
		
		var defaultSong = null;
	    
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
    
	    
	    if( defaultSong )
	    {
	    	this.loadSong( defaultSong );
	    	this.playSong();
	    }
	    
  } ,
  
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
	  
	  this.currentSongSMSound = soundManager.createSound({ 
		  id: id, 
		  url: url,
		  autoPlay: false,
		  autoLoad: true,
		  isMovieStar: null,
		  
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
  
  getNextSong: function(playlist, playlistSongId)
  {
	  
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
  
  ".volume a click" : function(el , event){
	  if( this.currentSongSMSound )
	  {
		  this.currentSongSMSound.mute();
		  $(".volume", this.element).hide();
		  $(".volume-off", this.element).show();
	  }
  },  
  
  ".volume-off a click" : function(el , event){
	  if( this.currentSongSMSound )
	  {
		  this.currentSongSMSound.unmute();
		  $(".volume", this.element).show();
		  $(".volume-off", this.element).hide();
		  
	  }	  
  },  
  
  ".like a click" : function(el , event){
	  var a = new PlaylistSongRatingModel({ playlist_song_id:  this.currentSong.id , type : PlaylistSongRatingModel.TYPE.LIKE });
	  a.save();
	  $(".like a").addClass("disabled");
	  $(".dislike a").addClass("disabled");
	  
  },  
  ".dislike a click" : function(el , event){
	  var a = new PlaylistSongRatingModel({ playlist_song_id:  this.currentSong.id , type : PlaylistSongRatingModel.TYPE.DISLIKE });
	  a.save(); 
	  $(".like a").addClass("disabled");
	  $(".dislike a").addClass("disabled");
  },  
  ".share a click" : function(el , event){
	  
  }      
  
});



PlaylistModel = can.Model({
	
	// update with id templates
	findOne : application.config.player.urls.get_current_playlist.replace("0","{id}")
	
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


// resolve on both dom ready and sound manager ready

var df1 = $.Deferred(), df2 = $.Deferred();

$.when( df1, df2 ).done( function(){
	
	  if( $("#player").length )
	  {
		  PlayerControlInterface.init();
	  
	  
		  $(".player-controls-play").on("click", function(){
			  
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




