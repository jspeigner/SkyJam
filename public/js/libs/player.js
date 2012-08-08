PlayerControl = {
	playerId : "#player",
	playerTemplateId : "#player-template",
	setCurrentUrl : function(){
		
	},
	loadCurrentPlaylist : function(){
		$.when( Playlist.findOne( {} ) ).then(
				  
			    function(playlistResponse) {
			    	
			      var playlist = playlistResponse;
			      new Player( PlayerControl.playerId , { playlist: playlist });
			      
			    }
		);		
	}

	
};

Player = can.Control({
  init: function(playlist){
	  
	var self = this;  
	
    this.element.html(can.view( PlayerControl.playerTemplateId, { 
    	playlist: playlist 
    }));
    
  },
  
})

Playlist = can.Model({
	findOne : application.config.player.urls.get_current_playlist
	
},{});

PlaylistSong = can.Model({

});

$(document).ready(function(){
	
  if( $("#player").length )
  {
	  PlayerControl.loadCurrentPlaylist();
	  
	  	  
  }
	

});

