package com.hydrogame.database;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "recipt_item")
public class ReciptItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipt_item_id")
    private int reciptItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipt_id", nullable = false)
    private Recipt recipt;

    @Column(name = "game_id", nullable = false)
    private int gameId;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "price", precision = 15, scale = 2, nullable = false)
    private BigDecimal price;

    public ReciptItem() {}

    // Getters and setters
    public int getReciptItemId() { return reciptItemId; }
    public void setReciptItemId(int reciptItemId) { this.reciptItemId = reciptItemId; }

    public Recipt getRecipt() { return recipt; }
    public void setRecipt(Recipt recipt) { this.recipt = recipt; }

    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
}
