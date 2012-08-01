package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import play.db.ebean.Model;

@Entity
@Table(name="user_playlist_activities")
public class UserPlaylistActivity extends Model {

	@Id
	public Integer id;
	
	
}
