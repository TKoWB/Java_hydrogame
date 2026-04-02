package com.hydrogame.feature_admin;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.hydrogame.database.Game;
import com.hydrogame.hibernate_util.HibernateUtil;

public class GameCrudService {

    public List<Game> listAllGames() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Game", Game.class).list();
        } catch (Exception e) {
            System.err.println("Error listing games: " + e.getMessage());
            return List.of();
        }
    }

    public Game getGameById(int gameId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Game.class, gameId);
        } catch (Exception e) {
            System.err.println("Error fetching game: " + e.getMessage());
            return null;
        }
    }

    public void deleteGame(int gameId) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            // Clear junction table entries to avoid FK constraint violations
            session.createNativeQuery("DELETE FROM cart_item WHERE game_id = :gid")
                   .setParameter("gid", gameId)
                   .executeUpdate();
            session.createNativeQuery("DELETE FROM link_genre WHERE game_id = :gid")
                   .setParameter("gid", gameId)
                   .executeUpdate();
            Game game = session.get(Game.class, gameId);
            if (game != null) {
                session.delete(game);
                transaction.commit();
                System.out.println("Game deleted successfully.");
            } else {
                System.err.println("Game with ID " + gameId + " not found.");
            }
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
            System.err.println("Error deleting game: " + e.getMessage());
        }
    }
}
