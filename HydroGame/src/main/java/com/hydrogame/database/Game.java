/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.hydrogame.database;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "game")
public class Game {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    private int gameId;
    
    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", precision = 15, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "age_cap", nullable = false)
    private int ageCap;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "stock")
    private int stock = 0;

    @Column(name = "img_url", length = 10000)
    private String imgUrl;
    
    @Column(name = "instore", nullable = false)
    private int instore;
    
    @Column(name = "date_game_addad", nullable = false)
    private LocalDate date_game_added;
    
    @Column(name = "date_game_deleted", nullable = true)
    private LocalDate date_game_deleted;
    
    @Column(name = "username", nullable = false)
    private String username;
    
    
    //construc
    public Game() {}

    public Game(String title, String description, BigDecimal price, int ageCap, LocalDate releaseDate, int stock, String imgUrl, String username) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.ageCap = ageCap;
        this.releaseDate = releaseDate;
        this.stock = stock;
        this.imgUrl = imgUrl;
        this.instore = 0;
        this.date_game_added = LocalDate.now();
        this.date_game_deleted = null;
        this.username = username;
    }
    
    //get set
    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getAgeCap() { return ageCap; }
    public void setAgeCap(int ageCap) { this.ageCap = ageCap; }

    public LocalDate getReleaseDate() { return releaseDate; }
    public void setReleaseDate(LocalDate releaseDate) { this.releaseDate = releaseDate; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImgUrl() { return imgUrl; }
    public void setImgUrl(String imgUrl) { this.imgUrl = imgUrl; }
    
    public int getInStore() { return instore; }
    public void setInStore(int instore) { this.instore = instore; }    
    
    public LocalDate getDateAdded() { return date_game_added; }
    
    public LocalDate getDateDel() { return date_game_deleted; }
    public void setDateDel(LocalDate del) { this.date_game_deleted = del; }
    
    public String getUser() { return username; }
    
    //to string
    @Override
    public String toString() {
        return "Game{" +
                "id=" + gameId +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", ageCap=" + ageCap +
                ", stock=" + stock +
                '}';
    }
}