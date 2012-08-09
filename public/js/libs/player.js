PlayerControlInterface = {
	playerId : "#player",
	playerTemplateId : "#player-template",
	
	setPlaylist : function(playlistId){
		
	},
	loadPlaylist : function( playlistId ){
		
		var playlistId = playlistId ? playlistId : 0; 
		
		$.when( PlaylistModel.findOne( { id: playlistId } ) ).then(
				
			    function(playlistResponse) {
			    	new PlayerControl( PlayerControlInterface.playerId , { 
			    		playlist : playlistResponse 
			    	} );
			    }
			    
		);		
	},
	

	
};

PlayerControl = can.Control({
	
  init: function(element, options ){
	  
	var self = this;
	
	this.playlistData = options.playlist;
	
    var defaultSong = null;
    
    if( playlistData.PlaylistSong && ( playlistData.PlaylistSong.length > 0 ) )
    {
    	if( options.default_playlist_song_id )
    	{
    		for(var playlistSongId in playlistData.PlaylistSong)
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
    		defaultSong = playlistData.PlaylistSong[0];
    	}
    }
    
    this.element.html(can.view( PlayerControlInterface.playerTemplateId, { playlist: this.playlistData, song: defaultSong }));
    
    console.log(this.playlistData);
    
  },
  
  play: function(playlistSong){
	  //
	  var url = playlistSong.Song.song_url;
	  console.log("virtually playing "+url);
  }
  
})

PlaylistModel = can.Model({
	
	// update with id templates
	findOne : application.config.player.urls.get_current_playlist.replace("0","{id}")
	
},{});

PlaylistSongModel = can.Model({

});


// page hooks

$(document).ready(function(){
	
  if( $("#player").length )
  {
	  PlayerControlInterface.loadPlaylist();
  
  
	  $(".player-controls-play").on("click", function(){
		  
		  var playlistId = $(this).data("playlist-id");
		  
		  PlayerControlInterface.loadPlaylist( playlistId );
		  
		  return false;
	  });
  }

});

