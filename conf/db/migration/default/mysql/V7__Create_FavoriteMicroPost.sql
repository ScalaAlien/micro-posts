CREATE TABLE `favorite_micro_posts` (
  `id`             BIGINT   AUTO_INCREMENT,
  `user_id`         BIGINT  NOT NULL,
  `micro_posts_id`  BIGINT  NOT NULL,
  `create_at` TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_at` TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY(`id`),
  FOREIGN KEY (`user_id`) REFERENCES users(`id`),
  FOREIGN KEY (`micro_posts_id`) REFERENCES users(`id`),
  UNIQUE(`user_id`, `micro_posts_id`)
) ENGINE=InnoDB;