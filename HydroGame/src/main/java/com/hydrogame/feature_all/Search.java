package com.hydrogame.feature_all;

import java.util.List;

import org.hibernate.Session;

import com.hydrogame.database.Game;
import com.hydrogame.hibernate_util.HibernateUtil;

/**
 * Search service — finds games by title keyword and/or genre name.
 * Used by both the admin console and the REST API layer.
 */
public class Search {

    /**
     * Search games whose title contains the given keyword (case-insensitive).
     * Only returns games that have not been soft-deleted.
     */
    public List<Game> searchByTitle(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT g FROM Game g WHERE " +
                    "LOWER(g.title) LIKE LOWER(:kw) AND g.date_game_deleted IS NULL",
                    Game.class)
                    .setParameter("kw", "%" + keyword.trim() + "%")
                    .list();
        } catch (Exception e) {
            System.err.println("Search error: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Return all games that belong to a specific genre (case-insensitive match).
     */
    public List<Game> searchByGenre(String genreName) {
        if (genreName == null || genreName.isBlank()) {
            return List.of();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                    "SELECT DISTINCT g FROM Game g JOIN g.genres gn WHERE " +
                    "LOWER(gn.genreName) = LOWER(:genre) AND g.date_game_deleted IS NULL",
                    Game.class)
                    .setParameter("genre", genreName.trim())
                    .list();
        } catch (Exception e) {
            System.err.println("Search error: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Combined search: title keyword + genre filter.
     * Either parameter may be null/blank to skip that filter.
     */
    public List<Game> search(String keyword, String genreName) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasGenre   = genreName != null && !genreName.isBlank();

        if (!hasKeyword && !hasGenre) {
            return List.of();
        }

        StringBuilder hql = new StringBuilder(
                "SELECT DISTINCT g FROM Game g LEFT JOIN g.genres gn WHERE g.date_game_deleted IS NULL");

        if (hasGenre)   hql.append(" AND LOWER(gn.genreName) = LOWER(:genre)");
        if (hasKeyword) hql.append(" AND LOWER(g.title) LIKE LOWER(:kw)");

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            var query = session.createQuery(hql.toString(), Game.class);
            if (hasGenre)   query.setParameter("genre", genreName.trim());
            if (hasKeyword) query.setParameter("kw", "%" + keyword.trim() + "%");
            return query.list();
        } catch (Exception e) {
            System.err.println("Search error: " + e.getMessage());
            return List.of();
        }
    }
}
