package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import models.UserPlaylistActivity.Type;

import anorm.SqlRow;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.RawSql;
import com.avaje.ebean.RawSqlBuilder;

import play.data.format.Formats;
import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
@Table(name="user_playlist_activities")
public class UserPlaylistActivity extends AppModel {


	@ManyToOne
	public User user;
	
	@Formats.DateTime(pattern="yyyy-MM-dd")
	public Date createdDate;
	
	public enum Type{
			play,
			pause,
			skip
	};
	
	@Enumerated(EnumType.STRING)
	public Type type;
	
	@ManyToOne
	public PlaylistSong playlistSong;
	
	public static Model.Finder<Integer,UserPlaylistActivity> find = new Finder<Integer, UserPlaylistActivity>(Integer.class, UserPlaylistActivity.class);
	
	
	public static List<UserPlaylistActivity> getActivityByPlaylist(Playlist p, Integer limit )
	{
		return getActivityByPlaylist(p, null, limit);
	}

	public static List<UserPlaylistActivity> getActivityByPlaylist(Playlist p, Type t, Integer limit )
	{
		Expression e, playlistExpr = Expr.eq("playlistSong.playlist", p);
		
		if( t != null)
		{
			e = Expr.and(playlistExpr, Expr.eq("type", t));
		}
		else
		{
			e = playlistExpr;
		}
		
		return find.where(e).setMaxRows(limit).findList(); 
	}
	
	public static List<User> getUsersOnPlaylist(Playlist p, Integer limit)
	{
		  String sql = "SELECT a.user_id " +
		  		" FROM user_playlist_activities a, playlist_songs ps" +
		  		" WHERE " +
		  			" ( a.playlist_song_id = ps.id ) AND " +
		  			" ( ps.playlist_id = :playlist_id ) " +
		  		" GROUP BY a.user_id ";
		  
		  RawSql rawSql = 
				  RawSqlBuilder.parse(sql)
				      // map the sql result columns to bean properties
				      .columnMapping("a.user_id", "id")
				      // we don't need to map this one due to the sql column alias
				      //.columnMapping("sum(d.order_qty*d.unit_price)", "totalAmount")
				      .create();		  
		  
	      return User.find.setRawSql(rawSql)
	    		  	.setParameter("playlist_id", p.getId())
	    		  	.orderBy("a.created_date DESC")
	    		  	.setMaxRows(limit)
	    		  	.findList();
	        		
	        		
	}

	
}
