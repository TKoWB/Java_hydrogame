/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.hydrogame.database;

import javax.persistence.*;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "genre")
public class Genre {

    @Id
    @Column(name = "genre_id")
    private int genreId;

    @Column(name = "genrename", length = 50, unique = true, nullable = false)
    private String genreName;

    //construc
    public Genre() {}

    public Genre(int genreId, String genreName) {
        this.genreId = genreId;
        this.genreName = genreName;
    }
    
    //mappedto
    @ManyToMany(mappedBy = "genres")
    private Set<Game> games = new HashSet<>();
    
    //get set
    public int getGenreId() {return genreId;}
    public void setGenreId(int genreId) {this.genreId = genreId;}

    public String getGenreName() {return genreName;}
    public void setGenreName(String genreName) {this.genreName = genreName;}

    //tostring
    @Override
    public String toString() {
        return "Genre{" +
                "id=" + genreId +
                ", name='" + genreName + '\'' +
                '}';
    }
}