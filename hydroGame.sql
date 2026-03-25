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
    date_user_added DATE NOT NULL,
    ban_date DATE NULL,
    role BOOLEAN NOT NULL 
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
    img_url varchar(10000) null,
    instore int DEFAULT 0,
    date_game_addad date not null,
    date_game_deleted date null,
	username varchar(50) not null,
    -- Ràng buộc độ tuổi
    CONSTRAINT check_age_cap CHECK (age_cap IN (3, 7, 12, 16, 18)),
    constraint FK_game_uid foreign key (username) references users(username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ------------------------------------------------------------------------------- --

create table if not exists genre(
	genre_id int primary key auto_increment,
    genrename varchar(50) unique not null
) engine=InnoDB default charset=utf8mb4;

create table if not exists link_genre(
	game_id int,
    genre_id int,
    primary key(game_id, genre_id),
    constraint FK_linkgenre_game_id foreign key (game_id) references game(game_id),
    constraint FK_linkgenre_genre_id foreign key (genre_id) references genre(genre_id)
) engine=InnoDB default charset=utf8mb4;

-- ------------------------------------------------------------------------------- --

CREATE TABLE cart (
    uid INT primary key,
    total_price decimal(15,2),
	constraint FK_cart_uid foreign key (uid) references users(uid)
) engine=InnoDB default charset=utf8mb4;

create table cart_item (
	game_id int not null,
    uid int not null,
    amount int default 0,
    primary key(uid, game_id),
    constraint FK_cart_item_cart foreign key (uid) references cart(uid),
    constraint FK_cart_item_game foreign key (game_id) references game(game_id)
) engine=InnoDB default charset=utf8mb4;

-- ----------------------------------------------------
DELIMITER //
create trigger TRG_ADD_CART
after insert on users
for each row
begin
	insert into cart (uid, total_price)
    values (new.uid, 0);
end //
DELIMITER ;
-- ----------------------------------------------------

DELIMITER //
create trigger TRG_CART_INSERT
after insert on cart_item
for each row
begin
	-- tao bien cuc bo
	declare game_price decimal(15,2);

	-- lay gia tien cua game
	select price into game_price
    from game	
    where game_id = new.game_id;
	
    -- nhe cong tong vao total
	update cart
    set total_price = total_price + (game_price * new.amount)
    where uid = new.uid;
end //
DELIMITER ;

-- ----------------------------------------------------

DELIMITER //
create trigger TRG_CART_UPDATE
after update on cart_item
for each row
begin
	declare game_price decimal(15,2);

	-- lay gia tien cua game
	select price into game_price
    from game	
    where game_id = new.game_id;
    
    -- nem tien khi da chi sau vao lai
	update cart
    set total_price = total_price - (game_price * old.amount) + (game_price * new.amount)
    where uid = new.uid;
end //
DELIMITER ;

-- ----------------------------------------------------

DELIMITER //
create trigger TRG_CART_DELETE
after delete on cart_item
for each row
begin
	declare game_price decimal(15,2);
    
    select price into game_price
    from game
    where game_id = old.game_id;

	update cart 
    set total_price = total_price - (game_price * old.amount)
    where uid = old.uid;
end //
DELIMITER ;

-- ------------------------------------------------------------------------------- --

CREATE Table recipt (
    recipt_id int primary key AUTO_INCREMENT,
    uid int not null,
    total_price decimal(15,2),
    recipt_date date not null,
    constraint FK_recipt_users foreign key (uid) references users(uid)
) engine=InnoDB default charset=utf8mb4;

create table recipt_item (
	game_id int not null,
    recipt_id int not null,
    amount int not null,
    primary key (recipt_id, game_id),
    constraint FK_recipt_item_recipt foreign key (recipt_id) references recipt(recipt_id)
) engine=InnoDB default charset=utf8mb4;

-- ------------------------------------------------------------------------------- --

CREATE Table reportUser (
	uid int not null,
    report_date date not null,
    money_spend decimal(15,2),
    primary key(uid, report_date),
    constraint FK_reportUser_users foreign key (uid) references users(uid)
) engine=InnoDB default charset=utf8mb4;

DELIMITER //
create trigger TRG_REPORT_USERS
after insert on recipt
for each row
begin
	insert into reportUser (uid, report_date, money_spend)
    values (new.uid, new.recipt_date, new.total_price)
    on duplicate key update 
		money_spend = money_spend + new.total_price;
end //
DELIMITER ;

CREATE TABLE total (
    report_date DATE PRIMARY KEY,
    total_money_earn DECIMAL(15,2) DEFAULT 0.00
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

DELIMITER //
CREATE TRIGGER trg_after_insert_total_daily
AFTER INSERT ON recipt
FOR EACH ROW
BEGIN
    INSERT INTO total (report_date, total_money_earn)
    VALUES (NEW.recipt_date, NEW.total_price)
    ON DUPLICATE KEY UPDATE 
        total_money_earn = total_money_earn + NEW.total_price;
END //
DELIMITER ;

-- ------------------------------------------------------------------------------- --
-- CHO DE SUA SQL --

INSERT INTO recipt (uid, total_price, recipt_date)
VALUES 
(2, 3000000.00, Curdate());
-- ------------------------------------------------------------------------------- --
    select * from users;
    select * from game;
    select * from cart;
    select * from cart_item;
    select * from reportUser;
    
    
    
    
    
-- ------------------------------------------------------------------------------- --
-- admin --
INSERT IGNORE INTO users (email, username, birthday, password, date_user_added, role) 
VALUES ('vana@gmail.com', 'vana_hydro', '2000-01-01', '123456', CURDATE(), 1);
    
    -- cac gia tri tham khao
    
    INSERT INTO game (title, description, price, age_cap, release_date, stock, img_url, instore, date_game_addad, username)
VALUES 
('Elden Ring', 'Hành động nhập vai thế giới mở cực khó.', 1200000.00, 16, '2022-02-25', 100, 'https://link-anh-1.jpg', 1, CURDATE(), 'vana_hydro'),

('Minecraft', 'Xây dựng thế giới từ những khối vuông.', 500000.00, 7, '2011-11-18', 500, 'https://link-anh-2.jpg', 1, CURDATE(), 'vana_hydro'),

('Grand Theft Auto V', 'Thế giới ngầm tội phạm tại Los Santos.', 800000.00, 18, '2013-09-17', 200, 'https://link-anh-3.jpg', 1, CURDATE(), 'vana_hydro'),

('Stardew Valley', 'Game nông trại thư giãn và nhẹ nhàng.', 165000.00, 3, '2016-02-26', 1000, 'https://link-anh-4.jpg', 1, CURDATE(), 'vana_hydro'),

('The Witcher 3: Wild Hunt', 'Cuộc phiêu lưu của thợ săn quái vật Geralt.', 600000.00, 18, '2015-05-19', 150, 'https://link-anh-5.jpg', 1, CURDATE(), 'vana_hydro'),

('Overwatch 2', 'Game bắn súng đồng đội phong cách anh hùng.', 0.00, 12, '2022-10-04', 9999, 'https://link-anh-6.jpg', 1, CURDATE(), 'vana_hydro'),

('Resident Evil Village', 'Kinh dị sinh tồn trong một ngôi làng hẻo lánh.', 950000.00, 18, '2021-05-07', 80, 'https://link-anh-7.jpg', 1, CURDATE(), 'vana_hydro'),

('Super Mario Odyssey', 'Hành động phiêu lưu giải cứu công chúa Peach.', 1350000.00, 3, '2017-10-27', 60, 'https://link-anh-8.jpg', 1, CURDATE(), 'vana_hydro'),

('FIFA 24 (FC 24)', 'Game bóng đá đỉnh cao thế giới.', 1500000.00, 3, '2023-09-29', 300, 'https://link-anh-9.jpg', 1, CURDATE(), 'vana_hydro'),

('Hollow Knight', 'Khám phá vương quốc côn trùng đổ nát.', 188000.00, 7, '2017-02-24', 400, 'https://link-anh-10.jpg', 1, CURDATE(), 'vana_hydro');

-- ------------------------------------------------------------------------------- --


-- users --
INSERT INTO users (email, username, birthday, password, balance, date_user_added, role) 
VALUES 
('vanas@gmail.com', 'vana_hysdro', '2000-05-15', 'hash_pass_1', 500000.00, CURDATE(), 0),
('thi_b@yahoo.com', 'thib_gamer', '1998-10-20', 'hash_pass_2', 120000.00, CURDATE(), 0),
('tran_c@outlook.com', 'claire_tran', '2005-02-14', 'hash_pass_3', 0.00, CURDATE(), 0),
('le_d@gmail.com', 'danny_le', '1992-12-30', 'hash_pass_4', 2500000.00, CURDATE(), 0),
('hoang_e@hotmail.com', 'eric_hoang', '2008-07-07', 'hash_pass_5', 45000.00, CURDATE(), 0),
('pham_f@gmail.com', 'felix_pham', '1995-03-25', 'hash_pass_6', 1000000.00, CURDATE(), 0),
('nguyen_g@gmail.com', 'giang_star', '2002-11-11', 'hash_pass_7', 300000.00, CURDATE(), 0),
('huynh_h@gmail.com', 'harry_h', '1989-01-01', 'hash_pass_8', 850000.00, CURDATE(), 0),
('dang_i@yahoo.com', 'iris_dang', '2004-09-09', 'hash_pass_9', 0.00, CURDATE(), 0),
('bui_k@gmail.com', 'kevin_bui', '1997-06-18', 'hash_pass_10', 50000.00, CURDATE(), 0);

-- ------------------------------------------------------------------------------- --