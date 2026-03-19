package com.hydrogame.user_service;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.hydrogame.database.Game;
import com.hydrogame.database.User;
import com.hydrogame.hibernate_util.HibernateUtil;

public class CartService {

    public List<Game> getCartItems(int uid) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Game> games = session.createNativeQuery(
                    "SELECT g.* FROM game g JOIN cart c ON g.game_id = c.game_id WHERE c.uid = :uid", Game.class)
                    .setParameter("uid", uid)
                    .list();
            return games;
        } catch (Exception e) {
            System.err.println("Error fetching cart: " + e.getMessage());
            return List.of();
        }
    }

    public void addToCart(int uid, int gameId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Check if already in cart
            Long count = ((Number) session.createNativeQuery(
                    "SELECT COUNT(*) FROM cart WHERE uid = :uid AND game_id = :gid")
                    .setParameter("uid", uid)
                    .setParameter("gid", gameId)
                    .uniqueResult()).longValue();
            if (count > 0) {
                System.out.println("Game is already in your cart.");
                return;
            }
            // Check game exists
            Game game = session.get(Game.class, gameId);
            if (game == null) {
                System.out.println("Game not found.");
                return;
            }
            transaction = session.beginTransaction();
            session.createNativeQuery("INSERT INTO cart (uid, game_id) VALUES (:uid, :gid)")
                    .setParameter("uid", uid)
                    .setParameter("gid", gameId)
                    .executeUpdate();
            transaction.commit();
            System.out.println("Added \"" + game.getTitle() + "\" to cart.");
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
            System.err.println("Error adding to cart: " + e.getMessage());
        }
    }

    public void removeFromCart(int uid, int gameId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            int rows = session.createNativeQuery("DELETE FROM cart WHERE uid = :uid AND game_id = :gid")
                    .setParameter("uid", uid)
                    .setParameter("gid", gameId)
                    .executeUpdate();
            transaction.commit();
            if (rows > 0) {
                System.out.println("Removed from cart.");
            } else {
                System.out.println("Game was not in your cart.");
            }
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
            System.err.println("Error removing from cart: " + e.getMessage());
        }
    }

    public void checkout(int uid) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Game> cartItems = getCartItems(uid);
            if (cartItems.isEmpty()) {
                System.out.println("Your cart is empty.");
                return;
            }

            User user = session.get(User.class, uid);
            if (user == null) {
                System.out.println("User not found.");
                return;
            }

            // Calculate total
            BigDecimal total = BigDecimal.ZERO;
            for (Game g : cartItems) {
                total = total.add(g.getPrice());
            }

            if (user.getBalance().compareTo(total) < 0) {
                System.out.println("Insufficient balance. You need " + total + " but have " + user.getBalance());
                return;
            }

            // Check stock
            for (Game g : cartItems) {
                Game fresh = session.get(Game.class, g.getGameId());
                if (fresh.getStock() <= 0) {
                    System.out.println("\"" + fresh.getTitle() + "\" is out of stock.");
                    return;
                }
            }

            transaction = session.beginTransaction();

            // Deduct balance
            user.setBalance(user.getBalance().subtract(total));
            session.merge(user);

            // Decrease stock for each game
            for (Game g : cartItems) {
                Game fresh = session.get(Game.class, g.getGameId());
                fresh.setStock(fresh.getStock() - 1);
                session.merge(fresh);
            }

            // Clear cart
            session.createNativeQuery("DELETE FROM cart WHERE uid = :uid")
                    .setParameter("uid", uid)
                    .executeUpdate();

            transaction.commit();
            System.out.println("Purchase successful! Total: " + total);
            System.out.println("Remaining balance: " + user.getBalance());
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
            System.err.println("Error during checkout: " + e.getMessage());
        }
    }

    public void topUp(int uid, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, uid);
            if (user == null) {
                System.out.println("User not found.");
                return;
            }
            user.setBalance(user.getBalance().add(amount));
            session.merge(user);
            transaction.commit();
            System.out.println("Top up successful! New balance: " + user.getBalance());
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
            System.err.println("Error topping up: " + e.getMessage());
        }
    }
}
