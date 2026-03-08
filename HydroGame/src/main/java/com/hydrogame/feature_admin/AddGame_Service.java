/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.hydrogame.feature_admin;
import javax.persistence.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.hydrogame.hibernate_util.HibernateUtil;
import com.hydrogame.database.Game;
/**
 *
 * @author Admin
 */
public class AddGame_Service {

    public void addGame(String title, String description, BigDecimal price, int agecap, LocalDate releasedate, int stock, String imgurl) {
        
        boolean isValidAge = (agecap == 3 || agecap == 7 || agecap == 12 || agecap == 16 || agecap == 18);
        
        if (!isValidAge) {
            System.err.println("Err: Ege Cap only support 3 7 12 16 18");
            return;
        }

        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();

            Game game = new Game(title, description, price, agecap, releasedate, stock, imgurl);
            
            session.persist(game); 

            transaction.commit();
            System.out.println("Thêm game thành công: " + title);

        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
            System.err.println("Err: Cant Add Game: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
