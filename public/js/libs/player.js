
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
			    		self.playerControl.destroy();
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
	
	playerState: null,
	
	init: function( element, options ){
	  
		var self = this;
		
		this.playlistData = options.playlist;
		
		this.playerState = this.PlayerState.STOP;
	    
	    if( this.playlistData.PlaylistSong && ( this.playlistData.PlaylistSong.length > 0 ) )
	    {
	    	if( options.default_playlist_song_id )
	    	{
	    		for(var playlistSongId in this.playlistData.PlaylistSong)
	    		{
	    			if( playlistData.PlaylistSong[playlistSongId].id == options.default_playlist_song_id )
	    			{
	    				this.currentSong = playlistData.PlaylistSong[playlistSongId];
	    				break;
	    			}
	    		}
	    	}
	    	else
	    	{
	    		this.currentSong = this.playlistData.PlaylistSong[0];
	    	}
	    }
	    
	    this.element.html( can.view( PlayerControlInterface.playerTemplateId, { playlist: this.playlistData, song: this.currentSong }) );
    
	    /*
	    if( this.defaultSong )
	    {
	    	this.playSong(this.defaultSong);
	    }
	    */
  } ,
  playSong: function(playlistSongData)
  {
	  if( this.playerState != this.PlayerState.PLAY )
	  {
		  if( this.currentSong.id != playlistSongData.id)
		  {
			  this.clearCurrentSong();
			  
			  this.currentSong = playlistSongData;
			  
		  }
		  
		  if( !this.currentSongSMSound ){
			  this.currentSongSMSound = soundManager.createSound( { 
				  id: 'PlaylistSong.'+this.currentSong.id , 
				  url: this.currentSong.Song.song_url,
				  autoPlay: false
			  });
		  }
			  
		  
		  
		  this.currentSongSMSound.play();
		  
		  $(".name .song").text( this.currentSong.Song.name );
		  
		  this.playerState = this.PlayerState.PLAY;
		  
		  $( ".play", this.element).hide();
		  $( ".pause", this.element).show();
	  }
	  
	  
  },
  clearCurrentSong: function()
  {
	  // TBD
	  if( this.currentSong ){
		  this.currentSong = null;
	  }
	  
	  if( this.currentSongSMSound ){
		  this.currentSongSMSound.destruct();
		  this.currentSongSMSound = null;
	  }
		  
  },
  
  pauseSong: function()
  {
	  if( this.playerState == this.PlayerState.PLAY)
	  {
		  this.playerState = this.PlayerState.PAUSE;
		  
		  this.currentSongSMSound.pause();
		  
		  $( ".play", this.element).show();
		  $( ".pause", this.element).hide();
	  }
  },
  
  ".play a click" : function(el , event){
	  
	  if( this.currentSong )
      {
		  
		  this.playSong( this.currentSong );
		  // console.log("virtually playing "+this.currentSong.Song.song_url);
      }

	  
	  
  },
  
  
  ".pause a click" : function(el , event){
	  this.pauseSong();
  },
  
  ".next-song a click" : function(el , event){
	  
  },
  
  ".volume a click" : function(el , event){
	  
  },  
  ".like a click" : function(el , event){
	  
  },  
  ".dislike a click" : function(el , event){
	  
  },  
  ".share a click" : function(el , event){
	  
  }      
  
});



PlaylistModel = can.Model({
	
	// update with id templates
	findOne : application.config.player.urls.get_current_playlist.replace("0","{id}")
	
},{});

PlaylistSongModel = can.Model({
	
});


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




