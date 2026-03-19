-- 1. Tạo Database
CREATE DATABASE IF NOT EXISTS HydroGame;
USE HydroGame;
-- ------------------------------------------------------------------------------- --
-- 2. Tạo bảng users
CREATE TABLE IF NOT EXISTS users (
	uid INT(20) PRIMARY KEY AUTO_INCREMENT,
    email varchar(100) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    birthday DATE NOT NULL,
    password VARCHAR(512) NOT NULL,
    age INT NULL,
    balance decimal(15,2) default 0,
    joindate date not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DELIMITER //

CREATE TRIGGER TRG_CHECK_AGE
BEFORE INSERT ON users
FOR EACH ROW
BEGIN
    SET NEW.age = TIMESTAMPDIFF(YEAR, NEW.birthday, CURDATE());
END //

DELIMITER ;
-- ------------------------------------------------------------------------------- --

CREATE TABLE IF NOT EXISTS game (
    game_id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT null,
    price DECIMAL(15,2) DEFAULT 0.00,
    age_cap TINYINT NOT NULL,
    release_date DATE NULL,
    stock int default 0,
    img_url varchar(1000) null,
    
    -- Ràng buộc độ tuổi
    CONSTRAINT check_age_cap CHECK (age_cap IN (3, 7, 12, 16, 18))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

create table if not exists genre(
	genre_id int primary key auto_increment,
    genrename varchar(50) unique not null
) engine=InnoDB default charset=utf8mb4;

create table if not exists link_genre(
	game_id int,
    genre_id int,
    primary key(game_id, genre_id),
    constraint FK_gameID_link_genre foreign key (game_id) references game(game_id),
    constraint FK_genre_link_genre foreign key (genre_id) references genre(genre_id)
) engine=InnoDB default charset=utf8mb4;

-- ------------------------------------------------------------------------------- --

-- ĐÚNG: Gom 2 cột lại thành 1 cụm khóa chính
CREATE TABLE cart (
    uid INT NOT NULL,
    game_id INT NOT NULL,
    PRIMARY KEY (uid, game_id),
	constraint FK_users foreign key (uid) references users(uid),
    constraint FK_game foreign key (game_id) references game(game_id)
) engine=InnoDB default charset=utf8mb4;

-- ------------------------------------------------------------------------------- --
-- CHO DE SUA SQL --

-- ------------------------------------------------------------------------------- --
    select * from users;
    select * from game;
    select * from cart;
    