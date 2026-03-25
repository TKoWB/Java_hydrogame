/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.hydrogame.database;

import javax.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.Set;
import java.util.HashSet;
import org.hibernate.annotations.NaturalId;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "uid")
    private int uid;
    

    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;
    
    @NaturalId(mutable = true)
    @Column(name = "username", length = 50, unique = true, nullable = false)
    private String username;

    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;

    @Column(name = "password", length = 512, nullable = false)
    private String password;

    //read only
    @Column(name = "age", insertable = false, updatable = false)
    private Integer age;
    //defaut = 0.00
    @Column(name = "balance", precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "date_users_added", nullable = false)
    private LocalDate date_user_added;
    
    @Column(name = "ban_date", nullable = true)
    private LocalDate ban_date;
    
    @Column(name = "role", nullable = false)
    private boolean role;
    public User() {
    }

    public User(String email, String username, LocalDate birthday, String password, boolean role) {
        this.email = email;
        this.username = username;
        this.birthday = birthday;
        this.password = password;
        this.date_user_added = LocalDate.now();
        this.ban_date = null;
        this.role = role;
        
    }
    @ManyToMany
    @JoinTable(
            name = "cart",
            joinColumns = @JoinColumn(name = "uid"),
            inverseJoinColumns = @JoinColumn(name = "game_id")
    )
    
    private Set<Game> games = new HashSet<>();
    
    public int getUid() { return uid; }
    public void setUid(int uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Integer getAge() { return age; }
    public LocalDate getDateAdded() { return date_user_added; }
    public boolean getRole() { return role; }
    
    public LocalDate getBandate() { return ban_date; }
    public void setBandate(boolean role) { this.role = role; }
    
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    
    
    
    @Override
    public String toString() {
        return "User{" +
                "uid=" + uid +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", birthday=" + birthday +
                ", age=" + age +
                ", balance=" + balance +
                ", date join=" + date_user_added +
                ", date ban=" + ban_date +
                ", role=" + role +
                '}';
    }
}
