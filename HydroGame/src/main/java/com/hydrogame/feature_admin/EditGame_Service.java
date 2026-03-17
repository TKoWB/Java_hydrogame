/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.hydrogame.feature_admin;
import java.math.BigDecimal;
import java.time.LocalDate;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.hydrogame.database.Game;
import com.hydrogame.hibernate_util.HibernateUtil;



/**
 *
 * @author Admin
 */
public class EditGame_Service {
    public void EditGame(int gameid, String title, String des, BigDecimal price, int agecap, LocalDate release, int stock, String imgurl){
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            transaction = session.beginTransaction();
            Game game = session.get(com.hydrogame.database.Game.class, gameid);
            
            if(game != null){
                game.setTitle(title);
                game.setDescription(des);
                game.setPrice(price);
                game.setAgeCap(agecap);
                game.setReleaseDate(release);
                game.setStock(stock);
                game.setImgUrl(imgurl);
                
                session.merge(game);
                transaction.commit();
                System.out.println("Edit game succesfully");
            } else {
                System.err.println("game is not exsist");
            }
            
            
        }catch (Exception e){
            if(transaction != null){transaction.rollback();}
            e.printStackTrace();
        }
    }
}
