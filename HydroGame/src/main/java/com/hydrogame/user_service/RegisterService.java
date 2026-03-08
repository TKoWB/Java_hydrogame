/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.hydrogame.user_service;
import java.time.LocalDate;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.hydrogame.hibernate_util.HibernateUtil;
import com.hydrogame.database.User;
import com.hydrogame.security_service.EncryptionService;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


/**
 *
 * @author Admin
 */
public class RegisterService {
    EncryptionService E = new EncryptionService(); 
    //check username
    public boolean isValidUsername(String str) {
        return str != null && str.matches("^[a-zA-Z0-9_\\.@\\-]+$");
    }
    
    //Check email
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" + 
        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    
    public static boolean isEmail(String email) {
        if (email == null) return false;
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }     
    
    public void Register(String email, String username, LocalDate birthday, String password){
        Transaction transaction = null;
        
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            
            String hash = E.Encrypt(password);
            User u = (isEmail(email) && isValidUsername(username)) ?
                    new User(email, username, birthday, hash): null;            
            
            if(u == null){
                System.err.print("Err: Incorrect registration information");
                return;
            }
            transaction = session.beginTransaction();

            session.save(u);
            transaction.commit();

            System.out.println("your account have been created");
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
            System.err.println("Err: Cant create account: " + e.getMessage());
            e.printStackTrace();
        }
        
        
    }
}
