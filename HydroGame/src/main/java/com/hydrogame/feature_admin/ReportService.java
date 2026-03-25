package com.hydrogame.feature_admin;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Session;

import com.hydrogame.database.Game;
import com.hydrogame.database.User;
import com.hydrogame.hibernate_util.HibernateUtil;

public class ReportService {

    // =================== USER REPORTS ===================

    public void reportUserById(int uid) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, uid);
            if (user == null) {
                System.out.println("User not found.");
                return;
            }
            System.out.println("\n================================================================");
            System.out.println("              USER REPORT — " + user.getUsername());
            System.out.println("================================================================");
            System.out.println("ID       : " + user.getUid());
            System.out.println("Username : " + user.getUsername());
            System.out.println("Email    : " + user.getEmail());
            System.out.println("Birthday : " + user.getBirthday());
            System.out.println("Age      : " + user.getAge());
            System.out.println("Balance  : " + user.getBalance());
            System.out.println("date join: " + user.getDateAdded());
            System.out.println("ban date : " + user.getBandate());
            System.out.println("role     : " + user.getRole());
            // Games bought (were in cart — already purchased, cart cleared on checkout)
            // We show current cart items as "games in cart"
            List<Game> cartGames = session.createNativeQuery(
                    "SELECT g.* FROM game g JOIN cart c ON g.game_id = c.game_id WHERE c.uid = :uid", Game.class)
                    .setParameter("uid", uid)
                    .list();
            System.out.println("\nGames in cart: " + cartGames.size());
            if (!cartGames.isEmpty()) {
                BigDecimal cartTotal = BigDecimal.ZERO;
                System.out.printf("  %-5s %-25s %-10s%n", "ID", "Title", "Price");
                System.out.println("  " + "-".repeat(42));
                for (Game g : cartGames) {
                    System.out.printf("  %-5d %-25s %-10s%n", g.getGameId(), g.getTitle(), g.getPrice());
                    cartTotal = cartTotal.add(g.getPrice());
                }
                System.out.println("  Cart total: " + cartTotal);
            }
            System.out.println("================================================================");
        } catch (Exception e) {
            System.err.println("Error generating user report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void reportAllUsers() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("\n================================================================");
            System.out.println("                  ALL USERS SUMMARY REPORT                     ");
            System.out.println("================================================================");

            Long totalUsers = (Long) session.createQuery("SELECT COUNT(u) FROM User u").uniqueResult();
            BigDecimal totalBalance = (BigDecimal) session.createQuery(
                    "SELECT COALESCE(SUM(u.balance), 0) FROM User u").uniqueResult();
            BigDecimal avgBalance = BigDecimal.ZERO;
            if (totalUsers > 0) {
                Number avgRaw = (Number) session.createQuery(
                        "SELECT COALESCE(AVG(u.balance), 0) FROM User u").uniqueResult();
                avgBalance = BigDecimal.valueOf(avgRaw.doubleValue());
            }

            System.out.println("Total users       : " + totalUsers);
            System.out.println("Total balance     : " + totalBalance);
            System.out.println("Average balance   : " + avgBalance);

            // All users table
            List<User> users = session.createQuery("FROM User u ORDER BY u.uid", User.class).list();
            if (!users.isEmpty()) {
                System.out.printf("\n%-5s %-20s %-30s %-5s %-10s%n",
                        "ID", "Username", "Email", "Age", "Balance");
                System.out.println("-".repeat(75));
                for (User u : users) {
                    System.out.printf("%-5d %-20s %-30s %-5s %-10s%n",
                            u.getUid(), u.getUsername(), u.getEmail(), u.getAge(), u.getBalance());
                }
            }

            // Top 5 by balance
            List<User> richUsers = session.createQuery("FROM User u ORDER BY u.balance DESC", User.class)
                    .setMaxResults(5).list();
            if (!richUsers.isEmpty()) {
                System.out.println("\nTop 5 users by balance:");
                System.out.printf("  %-5s %-20s %-12s%n", "ID", "Username", "Balance");
                for (User u : richUsers) {
                    System.out.printf("  %-5d %-20s %-12s%n", u.getUid(), u.getUsername(), u.getBalance());
                }
            }

            // Users with most cart items
            @SuppressWarnings("unchecked")
            List<Object[]> cartStats = session.createNativeQuery(
                    "SELECT u.uid, u.username, COUNT(c.game_id) AS cnt " +
                    "FROM users u LEFT JOIN cart c ON u.uid = c.uid " +
                    "GROUP BY u.uid, u.username ORDER BY cnt DESC")
                    .setMaxResults(5).list();
            if (!cartStats.isEmpty()) {
                System.out.println("\nTop 5 users by cart items:");
                System.out.printf("  %-5s %-20s %-10s%n", "ID", "Username", "Cart Items");
                for (Object[] row : cartStats) {
                    System.out.printf("  %-5s %-20s %-10s%n", row[0], row[1], row[2]);
                }
            }

            System.out.println("================================================================");
        } catch (Exception e) {
            System.err.println("Error generating users summary: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =================== GAME REPORTS ===================

    public void reportGameById(int gameId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Game game = session.get(Game.class, gameId);
            if (game == null) {
                System.out.println("Game not found.");
                return;
            }
            System.out.println("\n================================================================");
            System.out.println("              GAME REPORT — " + game.getTitle());
            System.out.println("================================================================");
            System.out.println("ID         : " + game.getGameId());
            System.out.println("Title      : " + game.getTitle());
            System.out.println("Description: " + game.getDescription());
            System.out.println("Price      : " + game.getPrice());
            System.out.println("Age Cap    : " + game.getAgeCap() + "+");
            System.out.println("Released   : " + game.getReleaseDate());
            System.out.println("Stock      : " + game.getStock());
            System.out.println("Image URL  : " + game.getImgUrl());
            System.out.println("date add   : " + game.getDescription());
            System.out.println("added by   : " + game.getUser());

            // Genres
            @SuppressWarnings("unchecked")
            List<Object[]> genres = session.createNativeQuery(
                    "SELECT g.genre_id, g.genrename FROM genre g " +
                    "JOIN link_genre lg ON g.genre_id = lg.genre_id WHERE lg.game_id = :gid")
                    .setParameter("gid", gameId).list();
            System.out.print("Genres     : ");
            if (genres.isEmpty()) {
                System.out.println("None");
            } else {
                for (int i = 0; i < genres.size(); i++) {
                    System.out.print(genres.get(i)[1]);
                    if (i < genres.size() - 1) System.out.print(", ");
                }
                System.out.println();
            }

            // How many users have this in cart
            Long inCarts = ((Number) session.createNativeQuery(
                    "SELECT COUNT(*) FROM cart WHERE game_id = :gid")
                    .setParameter("gid", gameId).uniqueResult()).longValue();
            System.out.println("In carts   : " + inCarts + " user(s)");

            System.out.println("================================================================");
        } catch (Exception e) {
            System.err.println("Error generating game report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void reportAllGames() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            System.out.println("\n================================================================");
            System.out.println("                  ALL GAMES SUMMARY REPORT                     ");
            System.out.println("================================================================");

            Long totalGames = (Long) session.createQuery("SELECT COUNT(g) FROM Game g").uniqueResult();
            BigDecimal avgPrice = BigDecimal.ZERO;
            if (totalGames > 0) {
                Number avgPriceRaw = (Number) session.createQuery(
                        "SELECT COALESCE(AVG(g.price), 0) FROM Game g").uniqueResult();
                avgPrice = BigDecimal.valueOf(avgPriceRaw.doubleValue());
            }
            Number totalStockRaw = (Number) session.createQuery(
                    "SELECT COALESCE(SUM(g.stock), 0) FROM Game g").uniqueResult();
            Long totalStock = totalStockRaw.longValue();

            System.out.println("Total games       : " + totalGames);
            System.out.println("Average price     : " + avgPrice);
            System.out.println("Total stock       : " + totalStock);

            // Games by age cap
            @SuppressWarnings("unchecked")
            List<Object[]> ageCounts = session.createQuery(
                    "SELECT g.ageCap, COUNT(g) FROM Game g GROUP BY g.ageCap ORDER BY g.ageCap").list();
            if (!ageCounts.isEmpty()) {
                System.out.println("\nGames by age cap:");
                for (Object[] row : ageCounts) {
                    System.out.printf("  Age %2d+ : %d games%n", row[0], row[1]);
                }
            }

            // Games by genre
            @SuppressWarnings("unchecked")
            List<Object[]> genreCounts = session.createNativeQuery(
                    "SELECT g.genrename, COUNT(lg.game_id) AS cnt " +
                    "FROM genre g LEFT JOIN link_genre lg ON g.genre_id = lg.genre_id " +
                    "GROUP BY g.genre_id, g.genrename ORDER BY cnt DESC").list();
            if (!genreCounts.isEmpty()) {
                System.out.println("\nGames by genre:");
                System.out.printf("  %-25s %-10s%n", "Genre", "Count");
                System.out.println("  " + "-".repeat(37));
                for (Object[] row : genreCounts) {
                    System.out.printf("  %-25s %-10s%n", row[0], row[1]);
                }
            }

            // List all games with their genres
            @SuppressWarnings("unchecked")
            List<Object[]> gamesWithGenres = session.createNativeQuery(
                    "SELECT g.game_id, g.title, g.price, g.stock, " +
                    "COALESCE(GROUP_CONCAT(gn.genrename SEPARATOR ', '), 'None') AS genres " +
                    "FROM game g LEFT JOIN link_genre lg ON g.game_id = lg.game_id " +
                    "LEFT JOIN genre gn ON lg.genre_id = gn.genre_id " +
                    "GROUP BY g.game_id, g.title, g.price, g.stock ORDER BY g.game_id").list();
            if (!gamesWithGenres.isEmpty()) {
                System.out.printf("\n%-5s %-25s %-10s %-6s %-20s%n",
                        "ID", "Title", "Price", "Stock", "Genres");
                System.out.println("-".repeat(70));
                for (Object[] row : gamesWithGenres) {
                    System.out.printf("%-5s %-25s %-10s %-6s %-20s%n",
                            row[0], row[1], row[2], row[3], row[4]);
                }
            }

            // Low stock
            List<Game> lowStock = session.createQuery(
                    "FROM Game g WHERE g.stock <= 5 ORDER BY g.stock ASC", Game.class)
                    .setMaxResults(10).list();
            if (!lowStock.isEmpty()) {
                System.out.println("\nLow stock games (<=5):");
                System.out.printf("  %-5s %-25s %-6s%n", "ID", "Title", "Stock");
                for (Game g : lowStock) {
                    System.out.printf("  %-5d %-25s %-6d%n", g.getGameId(), g.getTitle(), g.getStock());
                }
            }

            // Most popular (by cart)
            @SuppressWarnings("unchecked")
            List<Object[]> cartStats = session.createNativeQuery(
                    "SELECT g.game_id, g.title, COUNT(c.uid) AS add_count " +
                    "FROM game g JOIN cart c ON g.game_id = c.game_id " +
                    "GROUP BY g.game_id, g.title ORDER BY add_count DESC")
                    .setMaxResults(5).list();
            if (!cartStats.isEmpty()) {
                System.out.println("\nMost popular games (by cart adds):");
                System.out.printf("  %-5s %-25s %-10s%n", "ID", "Title", "In Carts");
                for (Object[] row : cartStats) {
                    System.out.printf("  %-5s %-25s %-10s%n", row[0], row[1], row[2]);
                }
            }

            System.out.println("================================================================");
        } catch (Exception e) {
            System.err.println("Error generating games summary: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
