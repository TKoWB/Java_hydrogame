package com.hydrogame.feature_admin;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.hydrogame.database.Genre;
import com.hydrogame.hibernate_util.HibernateUtil;

public class GenreCrudService {

    public List<Genre> listAllGenres() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Genre", Genre.class).list();
        } catch (Exception e) {
            System.err.println("Error listing genres: " + e.getMessage());
            return List.of();
        }
    }

    public Genre getGenreById(int genreId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Genre.class, genreId);
        } catch (Exception e) {
            System.err.println("Error fetching genre: " + e.getMessage());
            return null;
        }
    }

    public void addGenre(String genreName) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            Genre genre = new Genre();
            genre.setGenreName(genreName);
            session.persist(genre);
            transaction.commit();
            System.out.println("Genre added: " + genreName);
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
            System.err.println("Error adding genre: " + e.getMessage());
        }
    }

    public void addGenreToGame(int gameId, int genreId) { //add them list genre de chon tat ca genre de cho vao game!!!!
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Check if already linked
            Long count = ((Number) session.createNativeQuery(
                    "SELECT COUNT(*) FROM link_genre WHERE game_id = :gid AND genre_id = :genId")
                    .setParameter("gid", gameId)
                    .setParameter("genId", genreId)
                    .uniqueResult()).longValue();
            if (count > 0) {
                System.out.println("Genre is already assigned to this game.");
                return;
            }
            transaction = session.beginTransaction();
            session.createNativeQuery("INSERT INTO link_genre (game_id, genre_id) VALUES (:gid, :genId)")
                    .setParameter("gid", gameId)
                    .setParameter("genId", genreId)
                    .executeUpdate();
            transaction.commit();
            System.out.println("Genre assigned to game successfully.");
        } catch (Exception e) {
            if (transaction != null && transaction.getStatus().canRollback()) {
                transaction.rollback();
            }
            System.err.println("Error assigning genre: " + e.getMessage());
        }
    }
}
