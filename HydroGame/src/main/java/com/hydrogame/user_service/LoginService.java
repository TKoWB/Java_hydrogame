/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/File.java to edit this template
 */
package com.hydrogame.user_service;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.hibernate.Session;
import com.hydrogame.security_service.DecryptionService;
import com.hydrogame.hibernate_util.HibernateUtil;
import com.hydrogame.database.User;

/**
 *
 * @author Admin
 */
public class LoginService {
    DecryptionService D = new DecryptionService();
    
    //check email
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@" + 
        "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    
    public static boolean isEmail(String email) {
        if (email == null) return false;
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }    
    

    
    public List<Object> Login(String logintext, String password ){
        List<Object> Result = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            User u = null;

        if (isEmail(logintext)) {
            // Tìm bằng Email (Dùng HQL vì email giờ là Column thường)
            u = session.createQuery("FROM User WHERE email = :email", User.class)
                       .setParameter("email", logintext)
                       .uniqueResult();
        } else {
            // Tìm bằng Username (Dùng NaturalId vì username vẫn giữ @NaturalId)
            u = session.byNaturalId(com.hydrogame.database.User.class)
                       .using("username", logintext)
                       .load();
        }
            if(u != null && D.Decrypt(password,u.getPassword() )){
                Result.add(u.getUid());
                Result.add(u.getUsername());
                Result.add(u.getBalance());
                
                // tuổi thì sẽ dùng để vali mấy thứ cần độ tuổi, hoặc display lên cũng được
                Result.add(u.getAge());
                // email thì để đây, muốn làm gì thì làm
                Result.add(u.getEmail());
                
            } else {System.out.println("Incorrect login information");}
            
            
        } catch (NullPointerException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
                
        return Result;
    }
}
