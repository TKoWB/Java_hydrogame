package com.hydrogame.main;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

import com.hydrogame.database.Game;
import com.hydrogame.database.Genre;
import com.hydrogame.database.User;
import com.hydrogame.feature_admin.AddGame_Service;
import com.hydrogame.feature_admin.EditGame_Service;
import com.hydrogame.feature_admin.GameCrudService;
import com.hydrogame.feature_admin.GenreCrudService;
import com.hydrogame.feature_admin.ReportService;
import com.hydrogame.user_service.CartService;
import com.hydrogame.user_service.RegisterService;
import com.hydrogame.user_service.UserCrudService;

public class ConsoleMenu {

    private final Scanner scanner = new Scanner(System.in);
    private final String loggedInUsername;
    private final int loggedInUid;
    private final boolean isAdmin;

    private final UserCrudService userCrud = new UserCrudService();
    private final GameCrudService gameCrud = new GameCrudService();
    private final CartService cartService = new CartService();
    private final RegisterService registerService = new RegisterService();
    private final AddGame_Service addGameService = new AddGame_Service();
    private final EditGame_Service editGameService = new EditGame_Service();
    private final GenreCrudService genreCrud = new GenreCrudService();
    private final ReportService reportService = new ReportService();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ConsoleMenu(List<Object> loginData, boolean isAdmin) {
        this.loggedInUid = (loginData != null && !loginData.isEmpty())
                ? ((Number) loginData.get(0)).intValue() : -1;
        this.loggedInUsername = (loginData != null && loginData.size() >= 2)
                ? loginData.get(1).toString() : "Unknown";
        this.isAdmin = isAdmin;
    }

    public void start() {
        System.out.println("\n========================================");
        System.out.println("   Welcome to HydroGame Console Menu");
        System.out.println("   Logged in as: " + loggedInUsername + " (" + (isAdmin ? "Admin" : "Customer") + ")");
        System.out.println("========================================");

        if (isAdmin) {
            adminLoop();
        } else {
            customerLoop();
        }
        System.out.println("Goodbye!");
    }

    // ======================== CUSTOMER LOOP ========================

    private void customerLoop() {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Customer Menu ---");
            System.out.println("1. Browse Games");
            System.out.println("2. View Game Details");
            System.out.println("3. Add Game to Cart");
            System.out.println("4. View Cart");
            System.out.println("5. Remove from Cart");
            System.out.println("6. Checkout (Buy)");
            System.out.println("7. Top Up Balance");
            System.out.println("8. View My Profile");
            System.out.println("0. Exit");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": listAllGames(); break;
                case "2": viewGameAndAddToCart(); break;
                case "3": addToCart(); break;
                case "4": viewCart(); break;
                case "5": removeFromCart(); break;
                case "6": checkout(); break;
                case "7": topUp(); break;
                case "8": viewMyProfile(); break;
                case "0": running = false; break;
                default: System.out.println("Invalid option."); break;
            }
        }
    }

    private void viewGameAndAddToCart() {
        Integer gid = readInt("Enter game ID: ");
        if (gid == null) return;
        Game g = gameCrud.getGameById(gid);
        if (g == null) {
            System.out.println("Game not found.");
            return;
        }
        System.out.println("\n--- Game Details ---");
        System.out.println("ID         : " + g.getGameId());
        System.out.println("Title      : " + g.getTitle());
        System.out.println("Description: " + g.getDescription());
        System.out.println("Price      : " + g.getPrice());
        System.out.println("Age Cap    : " + g.getAgeCap() + "+");
        System.out.println("Released   : " + g.getReleaseDate());
        System.out.println("Stock      : " + g.getStock());
        System.out.println("Image URL  : " + g.getImgUrl());

        System.out.print("\nAdd this game to cart? (y/n): ");
        String answer = scanner.nextLine().trim();
        if (answer.equalsIgnoreCase("y")) {
            cartService.addToCart(loggedInUid, g.getGameId());
        }
    }

    private void addToCart() {
        Integer gid = readInt("Enter game ID to add to cart: ");
        if (gid == null) return;
        cartService.addToCart(loggedInUid, gid);
    }

    private void viewCart() {
        List<Game> items = cartService.getCartItems(loggedInUid);
        if (items.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        System.out.println("\n--- Your Cart ---");
        BigDecimal total = BigDecimal.ZERO;
        System.out.printf("%-5s %-25s %-10s%n", "ID", "Title", "Price");
        System.out.println("-".repeat(42));
        for (Game g : items) {
            System.out.printf("%-5d %-25s %-10s%n", g.getGameId(), g.getTitle(), g.getPrice());
            total = total.add(g.getPrice());
        }
        System.out.println("-".repeat(42));
        System.out.println("Total: " + total);
    }

    private void removeFromCart() {
        Integer gid = readInt("Enter game ID to remove from cart: ");
        if (gid == null) return;
        cartService.removeFromCart(loggedInUid, gid);
    }

    private void checkout() {
        viewCart();
        List<Game> items = cartService.getCartItems(loggedInUid);
        if (items.isEmpty()) return;
        System.out.print("Confirm purchase? (y/n): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("y")) {
            cartService.checkout(loggedInUid);
        } else {
            System.out.println("Cancelled.");
        }
    }

    private void topUp() {
        BigDecimal amount = readDecimal("Enter amount to top up: ");
        if (amount == null) return;
        cartService.topUp(loggedInUid, amount);
    }

    private void viewMyProfile() {
        User u = userCrud.getUserById(loggedInUid);
        if (u == null) {
            System.out.println("Could not load profile.");
            return;
        }
        System.out.println("\n--- My Profile ---");
        System.out.println("ID       : " + u.getUid());
        System.out.println("Username : " + u.getUsername());
        System.out.println("Email    : " + u.getEmail());
        System.out.println("Birthday : " + u.getBirthday());
        System.out.println("Age      : " + u.getAge());
        System.out.println("Balance  : " + u.getBalance());
    }

    // ======================== ADMIN LOOP ========================

    private void adminLoop() {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Admin Menu ---");
            System.out.println("1. User Management");
            System.out.println("2. Game Management");
            System.out.println("3. System Report");
            System.out.println("0. Exit");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": userMenu(); break;
                case "2": gameMenu(); break;
                case "3": reportMenu(); break;
                case "0": running = false; break;
                default: System.out.println("Invalid option."); break;
            }
        }
    }

    // ======================== USER MENU ========================

    private void userMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- User Management ---");
            System.out.println("1. List all users");
            System.out.println("2. View user by ID");
            System.out.println("3. Create new user");
            System.out.println("4. Update user");
            System.out.println("5. Delete user");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": listAllUsers(); break;
                case "2": viewUserById(); break;
                case "3": createUser(); break;
                case "4": updateUser(); break;
                case "5": deleteUser(); break;
                case "0": back = true; break;
                default: System.out.println("Invalid option."); break;
            }
        }
    }

    private void listAllUsers() {
        List<User> users = userCrud.listAllUsers();
        if (users.isEmpty()) {
            System.out.println("No users found.");
            return;
        }
        System.out.println("\n--- All Users ---");
        System.out.printf("%-5s %-20s %-30s %-12s %-5s %-10s%n",
                "ID", "Username", "Email", "Birthday", "Age", "Balance");
        System.out.println("-".repeat(85));
        for (User u : users) {
            System.out.printf("%-5d %-20s %-30s %-12s %-5s %-10s%n",
                    u.getUid(), u.getUsername(), u.getEmail(),
                    u.getBirthday(), u.getAge(), u.getBalance());
        }
    }

    private void viewUserById() {
        Integer uid = readInt("Enter user ID: ");
        if (uid == null) return;
        User u = userCrud.getUserById(uid);
        if (u == null) {
            System.out.println("User not found.");
        } else {
            System.out.println(u);
        }
    }

    private void createUser() {
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        LocalDate birthday = readDate("Birthday (yyyy-MM-dd): ");
        if (birthday == null) return;
        System.out.print("Password: ");
        String password = scanner.nextLine();

        registerService.Register(email, username, birthday, password, false);
    }

    private void updateUser() {
        Integer uid = readInt("Enter user ID to update: ");
        if (uid == null) return;
        User existing = userCrud.getUserById(uid);
        if (existing == null) {
            System.out.println("User not found.");
            return;
        }
        System.out.println("Current: " + existing);
        System.out.println("(Press Enter to keep current value)");

        System.out.print("New email [" + existing.getEmail() + "]: ");
        String email = scanner.nextLine().trim();
        System.out.print("New username [" + existing.getUsername() + "]: ");
        String username = scanner.nextLine().trim();
        System.out.print("New birthday [" + existing.getBirthday() + "] (yyyy-MM-dd): ");
        String bdayStr = scanner.nextLine().trim();
        LocalDate birthday = null;
        if (!bdayStr.isEmpty()) {
            try {
                birthday = LocalDate.parse(bdayStr, DATE_FMT);
            } catch (DateTimeParseException e) {
                System.out.println("Invalid date format, keeping current.");
            }
        }
        System.out.print("New password (leave blank to keep): ");
        String password = scanner.nextLine();
        System.out.print("New balance [" + existing.getBalance() + "]: ");
        String balStr = scanner.nextLine().trim();
        BigDecimal balance = null;
        if (!balStr.isEmpty()) {
            try {
                balance = new BigDecimal(balStr);
            } catch (NumberFormatException e) {
                System.out.println("Invalid balance, keeping current.");
            }
        }

        userCrud.updateUser(uid,
                email.isEmpty() ? null : email,
                username.isEmpty() ? null : username,
                birthday,
                password.isEmpty() ? null : password,
                balance);
    }

    private void deleteUser() {
        Integer uid = readInt("Enter user ID to delete: ");
        if (uid == null) return;
        System.out.print("Are you sure? (y/n): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("y")) {
            userCrud.deleteUser(uid);
        } else {
            System.out.println("Cancelled.");
        }
    }

    // ======================== GAME MENU ========================

    private void gameMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Game Management ---");
            System.out.println("1. List all games");
            System.out.println("2. View game by ID");
            System.out.println("3. Add new game");
            System.out.println("4. Edit game");
            System.out.println("5. Delete game");
            System.out.println("6. Genre Management");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": listAllGames(); break;
                case "2": viewGameById(); break;
                case "3": addGame(); break;
                case "4": editGame(); break;
                case "5": deleteGame(); break;
                case "6": genreMenu(); break;
                case "0": back = true; break;
                default: System.out.println("Invalid option."); break;
            }
        }
    }

    private void listAllGames() {
        List<Game> games = gameCrud.listAllGames();
        if (games.isEmpty()) {
            System.out.println("No games found.");
            return;
        }
        System.out.println("\n--- All Games ---");
        System.out.printf("%-5s %-25s %-10s %-8s %-12s %-6s%n",
                "ID", "Title", "Price", "AgeCap", "Released", "Stock");
        System.out.println("-".repeat(70));
        for (Game g : games) {
            System.out.printf("%-5d %-25s %-10s %-8d %-12s %-6d%n",
                    g.getGameId(), g.getTitle(), g.getPrice(),
                    g.getAgeCap(), g.getReleaseDate(), g.getStock());
        }
    }

    private void viewGameById() {
        Integer gid = readInt("Enter game ID: ");
        if (gid == null) return;
        Game g = gameCrud.getGameById(gid);
        if (g == null) {
            System.out.println("Game not found.");
        } else {
            System.out.println(g);
            System.out.println("  Description: " + g.getDescription());
            System.out.println("  Image URL  : " + g.getImgUrl());
        }
    }

    private void addGame() {
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Description: ");
        String desc = scanner.nextLine().trim();
        BigDecimal price = readDecimal("Price: ");
        if (price == null) return;
        Integer ageCap = readInt("Age cap (3/7/12/16/18): ");
        if (ageCap == null) return;
        LocalDate release = readDate("Release date (yyyy-MM-dd): ");
        Integer stock = readInt("Stock: ");
        if (stock == null) return;
        System.out.print("Image URL: ");
        String imgUrl = scanner.nextLine().trim();

        addGameService.addGame(title, desc, price, ageCap, release, stock, imgUrl, "them user name");

        // Ask to assign genres
        System.out.print("Assign genres to this game? (y/n): ");
        String genreAnswer = scanner.nextLine().trim();
        if (genreAnswer.equalsIgnoreCase("y")) {
            // Show available genres
            List<Genre> genres = genreCrud.listAllGenres();
            if (genres.isEmpty()) {
                System.out.println("No genres exist yet. Add genres from Genre Management first.");
            } else {
                System.out.println("Available genres:");
                for (Genre g : genres) {
                    System.out.println("  " + g.getGenreId() + ". " + g.getGenreName());
                }
                // Need the game ID — fetch the latest game by title
                List<Game> latest = gameCrud.listAllGames();
                Game newGame = null;
                for (Game gm : latest) {
                    if (gm.getTitle().equals(title)) newGame = gm;
                }
                if (newGame != null) {
                    System.out.println("Enter genre IDs separated by comma (e.g. 1,3,5): ");
                    String genreIds = scanner.nextLine().trim();
                    String[] ids = genreIds.split(",");
                    for (String idStr : ids) {
                        try {
                            int genreId = Integer.parseInt(idStr.trim());
                            genreCrud.addGenreToGame(newGame.getGameId(), genreId);
                        } catch (NumberFormatException e) {
                            System.out.println("Skipping invalid genre ID: " + idStr.trim());
                        }
                    }
                }
            }
        }
    }

    private void editGame() {
        Integer gid = readInt("Enter game ID to edit: ");
        if (gid == null) return;
        Game existing = gameCrud.getGameById(gid);
        if (existing == null) {
            System.out.println("Game not found.");
            return;
        }
        System.out.println("Current: " + existing);
        System.out.println("(Press Enter to keep current value)");

        System.out.print("New title [" + existing.getTitle() + "]: ");
        String title = scanner.nextLine().trim();
        System.out.print("New description: ");
        String desc = scanner.nextLine().trim();
        System.out.print("New price [" + existing.getPrice() + "]: ");
        String priceStr = scanner.nextLine().trim();
        System.out.print("New age cap [" + existing.getAgeCap() + "]: ");
        String ageCapStr = scanner.nextLine().trim();
        System.out.print("New release date [" + existing.getReleaseDate() + "] (yyyy-MM-dd): ");
        String relStr = scanner.nextLine().trim();
        System.out.print("New stock [" + existing.getStock() + "]: ");
        String stockStr = scanner.nextLine().trim();
        System.out.print("New image URL [" + existing.getImgUrl() + "]: ");
        String imgUrl = scanner.nextLine().trim();

        editGameService.EditGame(
                gid,
                title.isEmpty() ? existing.getTitle() : title,
                desc.isEmpty() ? existing.getDescription() : desc,
                priceStr.isEmpty() ? existing.getPrice() : new BigDecimal(priceStr),
                ageCapStr.isEmpty() ? existing.getAgeCap() : Integer.parseInt(ageCapStr),
                relStr.isEmpty() ? existing.getReleaseDate() : LocalDate.parse(relStr, DATE_FMT),
                stockStr.isEmpty() ? existing.getStock() : Integer.parseInt(stockStr),
                imgUrl.isEmpty() ? existing.getImgUrl() : imgUrl
        );
    }

    private void deleteGame() {
        Integer gid = readInt("Enter game ID to delete: ");
        if (gid == null) return;
        System.out.print("Are you sure? (y/n): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("y")) {
            gameCrud.deleteGame(gid);
        } else {
            System.out.println("Cancelled.");
        }
    }

    // ======================== GENRE MENU ========================

    private void genreMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Genre Management ---");
            System.out.println("1. List all genres");
            System.out.println("2. Add new genre");
            System.out.println("3. Assign genre to game");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": listAllGenres(); break;
                case "2": addGenre(); break;
                case "3": assignGenreToGame(); break;
                case "0": back = true; break;
                default: System.out.println("Invalid option."); break;
            }
        }
    }

    private void listAllGenres() {
        List<Genre> genres = genreCrud.listAllGenres();
        if (genres.isEmpty()) {
            System.out.println("No genres found.");
            return;
        }
        System.out.println("\n--- All Genres ---");
        System.out.printf("%-5s %-30s%n", "ID", "Name");
        System.out.println("-".repeat(37));
        for (Genre g : genres) {
            System.out.printf("%-5d %-30s%n", g.getGenreId(), g.getGenreName());
        }
    }

    private void addGenre() {
        System.out.print("Genre name: ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        genreCrud.addGenre(name);
    }

    private void assignGenreToGame() {
        Integer gid = readInt("Enter game ID: ");
        if (gid == null) return;
        listAllGenres();
        Integer genreId = readInt("Enter genre ID to assign: ");
        if (genreId == null) return;
        genreCrud.addGenreToGame(gid, genreId);
    }

    // ======================== REPORT MENU ========================

    private void reportMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Reports ---");
            System.out.println("1. User Report");
            System.out.println("2. Game Report");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1": userReportMenu(); break;
                case "2": gameReportMenu(); break;
                case "0": back = true; break;
                default: System.out.println("Invalid option."); break;
            }
        }
    }

    private void userReportMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- User Report ---");
            System.out.println("1. Report by User ID");
            System.out.println("2. Summary of all users");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    Integer uid = readInt("Enter user ID: ");
                    if (uid != null) reportService.reportUserById(uid);
                    break;
                case "2": reportService.reportAllUsers(); break;
                case "0": back = true; break;
                default: System.out.println("Invalid option."); break;
            }
        }
    }

    private void gameReportMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Game Report ---");
            System.out.println("1. Report by Game ID");
            System.out.println("2. Summary of all games (by genre)");
            System.out.println("0. Back");
            System.out.print("Choose: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    Integer gid = readInt("Enter game ID: ");
                    if (gid != null) reportService.reportGameById(gid);
                    break;
                case "2": reportService.reportAllGames(); break;
                case "0": back = true; break;
                default: System.out.println("Invalid option."); break;
            }
        }
    }

    // ======================== HELPERS ========================

    private Integer readInt(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return null;
        }
    }

    private BigDecimal readDecimal(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        try {
            return new BigDecimal(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number.");
            return null;
        }
    }

    private LocalDate readDate(String prompt) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) return null;
        try {
            return LocalDate.parse(input, DATE_FMT);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format. Use yyyy-MM-dd.");
            return null;
        }
    }
}
