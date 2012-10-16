CreatePlaylistControl = can.Control({
	
	songSerachQueryDelay: 700,
	songSearchQueryIntevalId: null,
	songSearchQuery: null,
	
	searchSongList: [],
	selectedSongList: [],
	
	init: function( element, options ){
		
		var self = this;
		
		if(options.songs && options.songs.length )
		{
			for(var i=0; i< options.songs.length; i++)
			{
				this.addSongToSelected( options.songs[i] );
			}
		}
		
		if(options.playlistSongs && options.playlistSongs.length )
		{
			for(var i=0; i< options.playlistSongs.length; i++)
			{
				this.addPlaylistSongToSelected( options.playlistSongs[i] );
			}
		}
		
		$( ".playlist-songs-container ul", this.element)
			.sortable()
			.bind( "sortstop", function(event,ui){  self.onSortOrderChange(event, ui); } );
		
		this.onPlaylistSongsChange();
		
	},
	
	onSortOrderChange: function(event, ui) {
		
		var i = 0;
		
		$(".playlist-songs-container ul li", this.element).each(function(){
			$("input.position", this ).val( i++ );
		});
		
		this.onPlaylistSongsChange();
	},
	
	onPlaylistSongsChange: function(){
		var self = this;
		
		var albumArtUrls = {};
		
		$(".playlist-songs-container ul li", this.element).each( function(){
			
			var data = $(this).data("playlistSong");
			
			if( data.song && data.song.album ){
				albumArtUrls[ data.song.album.id ] = data.song.album;
			}
			
		});
		
		var currentIndex = 0;
		var maxIndex = 4;
		var albums = _.values(albumArtUrls);
		
		$(".albumPreview td", this.element).html("");
		
		for( albumId in albums){
			if( currentIndex < maxIndex){
				
				var albumArtImage = $("<img>").attr( { alt : albums[albumId].name , src: albums[albumId].albumArtUrl  });
				
				$(".albumPreview .album"+currentIndex, this.element).html("").append(albumArtImage);
				
				currentIndex++;
			} else {
				break;
			}
			
		}
		

			

	},
	
	addPlaylistSongToSelected: function(playlistSong)
	{
		this.addSongToSelected( playlistSong.song, playlistSong.id);
	},
	
	updateSongList: function(searchString){
		
		
		var self = this;
		
		if( searchString != "" )
		{
		
			$.get( application.config.urls.searchSongs , { term: searchString }, function(result){
				self.onSongSearchComplete(result);
			});
		}
	},
	
	addSongToSelected: function(song, playlistSongId){
		
		playlistSongId = playlistSongId || null;
		
		this.selectedSongList.push( song );
		
		
		var list = $(".playlist-songs-container ul", this.element);
		
		var position = $("li", list).length;
		
		var elem = can.view("#create-playlist-selected-song-element", { song: song, playlistSongId : playlistSongId, position: position });
		 
		list.append( elem );

		$( "li[data-song-id="+ song.id +"]", list)
			.data( "playlistSong", { id:null, song: song })
			.effect("highlight", {}, 800);
		
		this.onPlaylistSongsChange();
		
		
	},
	
	deleteSongFromSelected: function(song){
		
		
		
	},
	
	onSongSearchComplete: function(resultSongs){
		
		$(".songs-search-results-container", this.element ).html( can.view("#create-playlist-search-song-list", { songs: resultSongs }) );
		
		this.searchSongList = resultSongs;
	},
	
	".song-search keyup" : function(){
		var newValue = $(".song-search").val();
		var self = this;
		
		if( this.songSearchQuery != newValue )
		{
			this.songSearchQuery = newValue;
			
			if( this.songSearchQueryIntevalId )
			{
				clearTimeout( this.songSearchQueryIntevalId );
				this.songSearchQueryIntevalId = null;
			}
		
			this.songSearchQueryIntevalId = setTimeout( function(){
				
				self.updateSongList( newValue );
				
			}, this.songSerachQueryDelay);
		}
	},
	
	".add-song-to-playlist click" : function(element, event){
		
		var songId = element.data("song-id");
		
		var parent = element.parents("li");
		
		var song = _.find( this.searchSongList, function(song){ return song.id == songId;  } );
		
		if( song )
		{
			this.addSongToSelected( song );
			
			parent.hide();
		}
		
		
	},
	".remove-song-from-playlist click": function(element, event){
		
		var songId = element.data("song-id");
		
		var parent = element.parents("li");
		
		parent.remove();
		
		this.selectedSongList = _.filter( this.selectedSongList, function(song){ return !( song.id == songId ) } );
		
		$(".songs-search-results-container ul li[data-song-id="+songId+"]", this.element).show();
		
		this.onPlaylistSongsChange();
		
	}

	
	
});


$(document).on( Application.events.PAGE_LOAD, function(){
	
	var createPlaylistId = "#create-playlist"; 
	
	if($(createPlaylistId).length)
	{
		var  createPlaylistControl = new CreatePlaylistControl( createPlaylistId, {} );
	}
	
});