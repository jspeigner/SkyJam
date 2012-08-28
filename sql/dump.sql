CREATE TABLE `albums` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
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
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `amazon_accounts` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `access_key_id` varchar(45) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'AWS access key\n',
  `secret_access_key` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `artists` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `description` text COLLATE utf8_unicode_ci NOT NULL,
  `url` varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `buckets` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) COLLATE utf8_unicode_ci NOT NULL,
  `amazon_account_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_aws_buckets_amazon_s3_accounts1` (`amazon_account_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `cloudfront_distributions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `distribution_id` varchar(32) COLLATE utf8_unicode_ci NOT NULL COMMENT 'AWS Cloudfront Distribution Id''',
  `domain_name` varchar(45) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Cloudfront domain name prefix\n',
  `last_modification_date` datetime NOT NULL,
  `status` enum('InProgress','Deployed') COLLATE utf8_unicode_ci NOT NULL DEFAULT 'InProgress' COMMENT 'AWS Cloudfront distribution status\n',
  `filename` varchar(255) COLLATE utf8_unicode_ci NOT NULL COMMENT 'AWS Clodufront filename',
  `storage_object_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_cloudfront_distribution_song_storages1` (`storage_object_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `genres` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `music_categories` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `parent_id` smallint(5) unsigned NOT NULL DEFAULT '0',
  `image_storage_object_id` int(11) DEFAULT NULL,
  `type` enum('activity','popular') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `parent_id` (`parent_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=latin1;

CREATE TABLE `playlists` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `status` enum('Public','Private','Draft') COLLATE utf8_unicode_ci NOT NULL DEFAULT 'Draft',
  `name` varchar(200) COLLATE utf8_unicode_ci NOT NULL,
  `description` text COLLATE utf8_unicode_ci NOT NULL,
  `user_id` int(11) NOT NULL,
  `genre_id` int(11) DEFAULT NULL,
  `created_date` datetime NOT NULL,
  `loaded_times` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `status` (`status`),
  KEY `fk_playlists_users1` (`user_id`),
  KEY `fk_playlists_genres1` (`genre_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `playlists_music_categories` (
  `playlists_id` int(11) NOT NULL,
  `music_categories_id` int(11) NOT NULL,
  PRIMARY KEY (`playlists_id`,`music_categories_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `playlist_songs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playlist_id` int(11) NOT NULL,
  `song_id` int(11) NOT NULL,
  `created_date` datetime NOT NULL,
  `position` int(11) NOT NULL DEFAULT '0',
  `likes_count` int(11) NOT NULL DEFAULT '0',
  `dislikes_count` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `fk_playlist_songs_playlists1` (`playlist_id`),
  KEY `fk_playlist_songs_songs1` (`song_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `playlist_song_ratings` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `created_date` datetime NOT NULL,
  `user_id` varchar(45) COLLATE utf8_unicode_ci NOT NULL,
  `type` enum('like','dislike') COLLATE utf8_unicode_ci NOT NULL DEFAULT 'like',
  `playlist_song_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_user_ratings_users1` (`user_id`),
  KEY `fk_object` (`playlist_song_id`),
  KEY `fk_user` (`user_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `play_evolutions` (
  `id` int(11) NOT NULL,
  `hash` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `applied_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `apply_script` text COLLATE utf8_unicode_ci,
  `revert_script` text COLLATE utf8_unicode_ci,
  `state` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `last_problem` text COLLATE utf8_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `songs` (
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
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `storage_objects` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `bucket_id` int(11) NOT NULL,
  `name` varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'AWS S3 object name',
  `filesize` bigint(20) NOT NULL,
  `created_date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `bucket_id` (`bucket_id`,`name`),
  KEY `fk_song_storages_aws_buckets1` (`bucket_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `users` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(30) COLLATE utf8_unicode_ci NOT NULL,
  `email` varchar(40) COLLATE utf8_unicode_ci NOT NULL,
  `password` varchar(48) COLLATE utf8_unicode_ci NOT NULL COMMENT 'sha1',
  `registered_date` datetime NOT NULL,
  `last_login_date` datetime DEFAULT NULL,
  `image_storage_object_id` int(11) DEFAULT NULL COMMENT 'User image\n',
  `facebook_user_id` varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`,`password`),
  KEY `fk_users_storage_object1` (`image_storage_object_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `users_user_roles` (
  `users_id` int(11) NOT NULL,
  `user_roles_id` int(11) NOT NULL,
  PRIMARY KEY (`users_id`,`user_roles_id`)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `user_playlist_activities` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) DEFAULT NULL,
  `created_date` datetime NOT NULL,
  `type` enum('play','pause','skip') COLLATE utf8_unicode_ci NOT NULL,
  `playlist_song_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_user_playlist_activity_users1` (`user_id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `user_roles` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(32) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

CREATE TABLE `user_saved_playlists` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `playlist_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `created_date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `playlist_id` (`playlist_id`,`user_id`),
  KEY `created_date` (`created_date`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

