package com.hydrogame.database;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "recipt")
public class Recipt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipt_id")
    private int reciptId;

    @Column(name = "uid", nullable = false)
    private int uid;

    @Column(name = "total_price", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "recipt_date", nullable = false)
    private LocalDateTime reciptDate;

    @OneToMany(mappedBy = "recipt", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ReciptItem> items = new HashSet<>();

    public Recipt() {}

    // Getters and setters
    public int getReciptId() { return reciptId; }
    public void setReciptId(int reciptId) { this.reciptId = reciptId; }

    public int getUid() { return uid; }
    public void setUid(int uid) { this.uid = uid; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }

    public LocalDateTime getReciptDate() { return reciptDate; }
    public void setReciptDate(LocalDateTime reciptDate) { this.reciptDate = reciptDate; }

    public Set<ReciptItem> getItems() { return items; }
    public void setItems(Set<ReciptItem> items) { this.items = items; }
}
