package com.hydrogame.user_service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.hydrogame.database.User;
import com.hydrogame.hibernate_util.HibernateUtil;
import com.hydrogame.security_service.EncryptionService;

public class UserCrudService {

    private final EncryptionService encryptionService = new EncryptionService();

    public List<User> listAllUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM User", User.class).list();
        } catch (Exception e) {
            System.err.println("Error listing users: " + e.getMessage());
            return List.of();
        }
    }

    public User getUserById(int uid) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, uid);
        } catch (Exception e) {
            System.err.println("Error fetching user: " + e.getMessage());
            return null;
        }
    }

    public void updateUser(int uid, String email, String username, LocalDate birthday, String newPassword, BigDecimal balance) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, uid);
            if (user == null) {
                System.err.println("User with ID " + uid + " not found.");
                return;
            }

            if (email != null && !email.isEmpty()) user.setEmail(email);
            if (username != null && !username.isEmpty()) user.setUsername(username);
            if (birthday != null) user.setBirthday(birthday);
            if (newPassword != null && !newPassword.isEmpty()) {
                user.setPassword(encryptionService.Encrypt(newPassword));
            }
            if (balance != null) user.setBalance(balance);

            session.merge(user);
            transaction.commit();
            System.out.println("User updated successfully.");
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
            System.err.println("Error updating user: " + e.getMessage());
        }
    }

    public void deleteUser(int uid) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            // Clear cart entries first to avoid FK constraint violation
            session.createNativeQuery("DELETE FROM cart WHERE uid = :uid")
                   .setParameter("uid", uid)
                   .executeUpdate();
            User user = session.get(User.class, uid);
            if (user != null) {
                session.delete(user);
                transaction.commit();
                System.out.println("User deleted successfully.");
            } else {
                System.err.println("User with ID " + uid + " not found.");
            }
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
            System.err.println("Error deleting user: " + e.getMessage());
        }
    }
}
