package com.hydrogame.mvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;

import com.hydrogame.database.Game;
import com.hydrogame.feature_admin.AddGame_Service;
import com.hydrogame.feature_admin.EditGame_Service;
import com.hydrogame.feature_admin.GameCrudService;
import com.hydrogame.feature_all.Search;
import com.hydrogame.hibernate_util.HibernateUtil;

/**
 * MVC Controller — Products
 *
 * Acts as the bridge between the View (GameProductManagementUI) and the
 * existing Service layer.  The View never talks to services directly.
 *
 * Flow:
 *   View (button click)
 *     → ProductController  (this class)
 *       → AddGame_Service / EditGame_Service / GameCrudService / Search
 *         → Hibernate ↔ MySQL
 *
 * All methods return plain Java objects so the View can stay UI-agnostic.
 */
public class ProductController {

    // ── Services (already exist in the project) ─────────────────────────
    private final GameCrudService  gameCrud    = new GameCrudService();
    private final AddGame_Service  addService  = new AddGame_Service();
    private final EditGame_Service editService = new EditGame_Service();
    private final Search           search      = new Search();

    // ====================================================================
    // READ
    // ====================================================================

    /**
     * Return every active (non-deleted) game from the database.
     * Called when the Products / Library view opens.
     */
    public List<Game> getAllGames() {
        try {
            return gameCrud.listAllGames();
        } catch (Exception e) {
            System.err.println("[ProductController] getAllGames: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /** Return a single game by its primary key, or null if not found. */
    public Game getGameById(int id) {
        try {
            return gameCrud.getGameById(id);
        } catch (Exception e) {
            System.err.println("[ProductController] getGameById: " + e.getMessage());
            return null;
        }
    }

    // ====================================================================
    // SEARCH
    // ====================================================================

    /**
     * Search by title keyword.
     * Called from the header search field.
     */
    public List<Game> searchByTitle(String keyword) {
        if (keyword == null || keyword.isBlank()) return getAllGames();
        try {
            return search.searchByTitle(keyword);
        } catch (Exception e) {
            System.err.println("[ProductController] searchByTitle: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Combined search: optional title keyword + optional genre filter.
     * Called from the sidebar genre filter and search bar together.
     */
    public List<Game> search(String keyword, String genre) {
        try {
            return search.search(keyword, genre);
        } catch (Exception e) {
            System.err.println("[ProductController] search: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    // ====================================================================
    // CREATE
    // ====================================================================

    /**
     * Persist a new game and return the saved entity (with its generated
     * gameId) so the UI can create a matching ProductRow immediately.
     * Returns null if the insert fails.
     */
    public Game addGame(String title, String description, BigDecimal price,
                        int ageCap, LocalDate releaseDate, int stock,
                        String imgUrl, String username) {
        try {
            addService.addGame(title, description, price,
                               ageCap, releaseDate, stock, imgUrl, username);

            // Retrieve the newly persisted record (highest gameId with this title).
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                return session.createQuery(
                        "FROM Game g WHERE g.title = :title ORDER BY g.gameId DESC",
                        Game.class)
                        .setParameter("title", title)
                        .setMaxResults(1)
                        .uniqueResult();
            }
        } catch (Exception e) {
            System.err.println("[ProductController] addGame: " + e.getMessage());
            return null;
        }
    }

    // ====================================================================
    // UPDATE
    // ====================================================================

    /**
     * Update an existing game.
     * Returns true on success, false on failure.
     */
    public boolean updateGame(int gameId, String title, String description,
                               BigDecimal price, int ageCap, LocalDate releaseDate,
                               int stock, String imgUrl) {
        try {
            editService.EditGame(gameId, title, description,
                                 price, ageCap, releaseDate, stock, imgUrl);
            return true;
        } catch (Exception e) {
            System.err.println("[ProductController] updateGame: " + e.getMessage());
            return false;
        }
    }

    // ====================================================================
    // DELETE
    // ====================================================================

    /**
     * Hard-delete a game and its related cart / genre junction rows.
     * Returns true on success, false on failure.
     */
    public boolean deleteGame(int gameId) {
        try {
            gameCrud.deleteGame(gameId);
            return true;
        } catch (Exception e) {
            System.err.println("[ProductController] deleteGame: " + e.getMessage());
            return false;
        }
    }

    // ====================================================================
    // STATS  (used by Dashboard metric cards)
    // ====================================================================

    /** Total number of active (non-deleted) games in the DB. */
    public long countActiveGames() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Number n = (Number) session.createQuery(
                    "SELECT COUNT(g) FROM Game g WHERE g.date_game_deleted IS NULL")
                    .uniqueResult();
            return n != null ? n.longValue() : 0L;
        } catch (Exception e) { return 0L; }
    }

    /** Games with stock below 10. */
    public long countLowStockGames() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Number n = (Number) session.createQuery(
                    "SELECT COUNT(g) FROM Game g " +
                    "WHERE g.stock < 10 AND g.date_game_deleted IS NULL")
                    .uniqueResult();
            return n != null ? n.longValue() : 0L;
        } catch (Exception e) { return 0L; }
    }

    /** Sum of (price × stock) for all active games — total inventory value. */
    public BigDecimal getInventoryValue() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            BigDecimal val = (BigDecimal) session.createQuery(
                    "SELECT COALESCE(SUM(g.price * g.stock), 0) FROM Game g " +
                    "WHERE g.date_game_deleted IS NULL")
                    .uniqueResult();
            return val != null ? val : BigDecimal.ZERO;
        } catch (Exception e) { return BigDecimal.ZERO; }
    }

    /** Distinct number of genres registered in the DB. */
    public long countGenres() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Number n = (Number) session.createQuery(
                    "SELECT COUNT(gn) FROM Genre gn")
                    .uniqueResult();
            return n != null ? n.longValue() : 0L;
        } catch (Exception e) { return 0L; }
    }

    /**
     * Return monthly revenue calculated from game sales (cart × price),
     * grouped by the month the game was added.
     * Each row is Object[]{ monthLabel (String), totalAmount (Number) }.
     */
    @SuppressWarnings("unchecked")
    public List<Object[]> getMonthlyRevenue() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createNativeQuery(
                    "SELECT DATE_FORMAT(g.date_game_addad, '%b %Y') AS month_label, " +
                    "SUM(g.price) AS total " +
                    "FROM cart c " +
                    "JOIN game g ON c.game_id = g.game_id " +
                    "GROUP BY YEAR(g.date_game_addad), MONTH(g.date_game_addad), month_label " +
                    "ORDER BY MIN(g.date_game_addad)")
                    .list();
        } catch (Exception e) {
            System.err.println("[ProductController] getMonthlyRevenue: " + e.getMessage());
            return Collections.emptyList();
        }
    }
}
