CREATE TABLE IF NOT EXISTS `albums` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `image` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `artist_id` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `copyright_year` smallint(6) NOT NULL,
  `created_date` date NOT NULL,
  `keywords` text COLLATE utf8_unicode_ci NOT NULL,
  `genre_id` int(11) DEFAULT NULL,
  `description` text COLLATE utf8_unicode_ci NOT NULL,
  `album_art_storage_object_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `artist_id` (`artist_id`,`name`),
  KEY `fk_albums_genres1` (`genre_id`),
  KEY `fk_albums_storage_object1` (`album_art_storage_object_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=7624 ;

CREATE TABLE IF NOT EXISTS `amazon_accounts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `access_key_id` varchar(45) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'AWS access key\n',
  `secret_access_key` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=2 ;

CREATE TABLE IF NOT EXISTS `artists` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `description` text COLLATE utf8_unicode_ci NOT NULL,
  `url` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=4755 ;

CREATE TABLE IF NOT EXISTS `buckets` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8_unicode_ci NOT NULL,
  `amazon_account_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_aws_buckets_amazon_s3_accounts1` (`amazon_account_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=2 ;

CREATE TABLE IF NOT EXISTS `cloudfront_distributions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `distribution_id` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT 'AWS Cloudfront Distribution Id''',
  `domain_name` varchar(45) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Cloudfront domain name prefix\n',
  `last_modification_date` datetime NOT NULL,
  `status` enum('InProgress','Deployed') COLLATE utf8_unicode_ci NOT NULL DEFAULT 'InProgress' COMMENT 'AWS Cloudfront distribution status\n',
  `filename` varchar(255) COLLATE utf8_unicode_ci NOT NULL COMMENT 'AWS Clodufront filename',
  `storage_object_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_cloudfront_distribution_song_storages1` (`storage_object_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

CREATE TABLE IF NOT EXISTS `genres` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

CREATE TABLE IF NOT EXISTS `music_categories` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(200) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `parent_id` smallint(5) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `parent_id` (`parent_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

CREATE TABLE IF NOT EXISTS `playlists` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `status` enum('Public','Private','Draft') COLLATE utf8_unicode_ci NOT NULL DEFAULT 'Draft',
  `name` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `description` text COLLATE utf8_unicode_ci NOT NULL,
  `user_id` int(11) NOT NULL,
  `genre_id` int(11) DEFAULT NULL,
  `music_category_id` int(11) NOT NULL,
  `created_date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `status` (`status`),
  KEY `fk_playlists_users1` (`user_id`),
  KEY `fk_playlists_music_categories1` (`music_category_id`),
  KEY `fk_playlists_genres1` (`genre_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

CREATE TABLE IF NOT EXISTS `playlist_ratings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `user_id` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `type` enum('like','dislike') COLLATE utf8_unicode_ci NOT NULL DEFAULT 'like',
  `playlist_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_user_ratings_users1` (`user_id`),
  KEY `fk_object` (`playlist_id`),
  KEY `fk_user` (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

CREATE TABLE IF NOT EXISTS `playlist_songs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playlist_id` int(11) NOT NULL,
  `song_id` int(11) NOT NULL,
  `created_date` datetime NOT NULL,
  `order` int(11) NOT NULL DEFAULT '0',
  `likes_count` int(11) NOT NULL DEFAULT '0',
  `dislikes_count` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_playlist_songs_playlists1` (`playlist_id`),
  KEY `fk_playlist_songs_songs1` (`song_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

CREATE TABLE IF NOT EXISTS `play_evolutions` (
  `id` int(11) NOT NULL,
  `hash` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `applied_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `apply_script` text COLLATE utf8_unicode_ci,
  `revert_script` text COLLATE utf8_unicode_ci,
  `state` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `last_problem` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `songs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(300) COLLATE utf8_unicode_ci NOT NULL,
  `album_id` int(11) NOT NULL,
  `keywords` text COLLATE utf8_unicode_ci NOT NULL,
  `duration` int(11) NOT NULL,
  `storage_object_id` int(11) NOT NULL,
  `status` enum('visible','hidden') COLLATE utf8_unicode_ci NOT NULL DEFAULT 'visible',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`,`album_id`),
  KEY `fk_songs_albums1` (`album_id`),
  KEY `fk_songs_storage_object1` (`storage_object_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=36328 ;

CREATE TABLE IF NOT EXISTS `songs_music_categories` (
  `song_id` int(11) NOT NULL,
  `music_category_id` int(11) NOT NULL,
  PRIMARY KEY (`song_id`,`music_category_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE IF NOT EXISTS `storage_objects` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bucket_id` int(11) NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'AWS S3 object name',
  `filesize` bigint(20) NOT NULL,
  `created_date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_song_storages_aws_buckets1` (`bucket_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=36328 ;

CREATE TABLE IF NOT EXISTS `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  `email` varchar(40) COLLATE utf8_unicode_ci NOT NULL,
  `password` varchar(48) COLLATE utf8_unicode_ci NOT NULL COMMENT 'sha1',
  `registered_date` datetime NOT NULL,
  `last_login_date` datetime DEFAULT NULL,
  `type` enum('admin','user') COLLATE utf8_unicode_ci NOT NULL DEFAULT 'user',
  `image_storage_object_id` int(11) DEFAULT NULL COMMENT 'User image\n',
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`,`password`),
  KEY `type` (`type`),
  KEY `fk_users_storage_object1` (`image_storage_object_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;

CREATE TABLE IF NOT EXISTS `user_playlist_activities` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `created_date` datetime NOT NULL,
  `type` enum('play','pause','skip') COLLATE utf8_unicode_ci NOT NULL,
  `playlist_song_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_user_playlist_activity_users1` (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci AUTO_INCREMENT=1 ;
