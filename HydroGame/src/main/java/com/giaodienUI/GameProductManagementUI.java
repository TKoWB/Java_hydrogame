package com.giaodienUI;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableRow;
import javafx.scene.control.TextArea;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.hydrogame.database.Game;
import com.hydrogame.mvc.ProductController;

// ===== PHẦN 3: CLASS CHÍNH CỦA CHƯƠNG TRÌNH =====
// Class này kế thừa Application để chạy JavaFX
public class GameProductManagementUI extends Application {
    // ===== PRODUCT DETAIL DIALOG =====
    private void showProductDetailDialog(ProductRow product) {
        Stage detailStage = new Stage();
        detailStage.setTitle("Product Details");

        VBox content = new VBox(18);
        content.setPadding(new Insets(24));
        content.setStyle(glassPanelStyle("28") + "-fx-background-radius: 18; -fx-min-width: 540px;");

        // Product info section
        VBox info = new VBox(8);
        info.getChildren().addAll(
            createSectionTitle(product.getName()),
            createSubTitle("Game ID: " + product.getId()),
            new Label("Genre: " + product.getCategory()),
            new Label("Price: " + product.getPriceFormatted()),
            new Label("Stock: " + product.getStock()),
            new Label("Status: " + product.getStatus())
        );
        for (javafx.scene.Node n : info.getChildren()) {
            if (n instanceof Label) {
                ((Label)n).setTextFill(Color.WHITE);
                ((Label)n).setFont(Font.font(FONT_UI, 15));
            }
        }

        // Revenue chart for this game
        VBox chartCard = new VBox(10);
        chartCard.setPadding(new Insets(16));
        chartCard.setStyle(glassPanelStyle("22"));
        chartCard.getChildren().addAll(
            createSectionTitle("Revenue for this Game"),
            createProductRevenueChart(product.getId())
        );

        content.getChildren().addAll(info, chartCard);

        // Add a close button at the bottom
        Button closeBtn = new Button("Close");
        closeBtn.setStyle(primaryButtonStyle());
        closeBtn.setOnAction(e -> detailStage.close());
        HBox closeBox = new HBox(closeBtn);
        closeBox.setAlignment(Pos.CENTER_RIGHT);
        closeBox.setPadding(new Insets(10, 0, 0, 0));
        content.getChildren().add(closeBox);

        Scene scene = new Scene(content);
        detailStage.setScene(scene);
        detailStage.initOwner(root.getScene().getWindow());
        detailStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
        detailStage.setResizable(false);
        detailStage.showAndWait();
    }

    // Create a line chart for revenue of a single game by gameId
    private LineChart<String, Number> createProductRevenueChart(String gameId) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.setPrefHeight(220);
        chart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        // Query revenue by day for this game (replace with your actual query)
        java.util.Map<String, java.math.BigDecimal> map = com.hydrogame.database.RevenueService.getDailyRevenueForGame(gameId);
        for (String day : map.keySet()) {
            series.getData().add(new XYChart.Data<>(day, map.get(day)));
        }
        chart.getData().add(series);
        return chart;
    }

    // ===== PHẦN 4: BIẾN TOÀN CỤC =====

    // Font mặc định cho UI
    private static final String FONT_UI = "Poppins";

    // Layout chính của toàn bộ ứng dụng
    private BorderPane root;

    // View trang Products
    private VBox productsView;

    // Vùng nội dung trung tâm (đổi trang theo menu)
    private VBox contentArea;

    // Danh sách các nút menu bên sidebar
    private final List<Button> menuButtons = new ArrayList<>();

    // Nút đang active
    private Button activeButton;

    // ===== PHẦN 5: DỮ LIỆU MẪU SẢN PHẨM =====
    // ObservableList is populated from the database at startup via loadProductsFromDB()
    private final ObservableList<ProductRow> productList = FXCollections.observableArrayList();

    // ===== MVC CONTROLLER =====
    // The single controller instance used by all UI actions in this window.
    private final ProductController productController = new ProductController();

    /** The username of the admin who logged in — used as FK when adding games. */
    private String loggedInUsername = "";

    public void setLoggedInUsername(String username) {
        this.loggedInUsername = username != null ? username : "";
    }

    // ===== PHẦN 6: CÁC THÀNH PHẦN GIAO DIỆN CHÍNH =====
    private TableView<ProductRow> productTable;
    private FlowPane featuredFlow;

    // ===== PHẦN 7: CÁC Ô NHẬP LIỆU FORM =====
    private TextField txtName;
    private FlowPane categoryChips;
    private VBox categoryDropdown;
    private VBox categoryPickerBox;
    private final ObservableList<String> allCategories = FXCollections.observableArrayList(
            "Action", "Adventure", "RPG", "Sports", "Sandbox", "Horror");
    private final ObservableList<String> selectedCategories = FXCollections.observableArrayList();
    private TextField txtPrice;
    private TextField txtStock;
    private ComboBox<String> cboStatus;
    private TextArea txtDesc;

    // ===== PHẦN 8: HÀM KHỞI ĐỘNG CHƯƠNG TRÌNH =====
    // Hàm start() sẽ chạy đầu tiên khi ứng dụng được mở
    @Override
    public void start(Stage stage) {
        Font.loadFont(getClass().getResourceAsStream("/fonts/Radley-Sans.ttf"), 14);

        // Tạo layout gốc
        root = new BorderPane();

        // Style nền tổng
        root.setStyle(
         "-fx-background-color: linear-gradient(to bottom right, #08111f, #0b1220 40%, #111827 100%);" +
         "-fx-font-family: 'Radley';"
        );

        // Gắn sidebar bên trái
        root.setLeft(createSidebar());

        // Mặc định mở Dashboard
        contentArea = createDashboardView();
        root.setCenter(contentArea);

        // Tạo scene
        Scene scene = new Scene(root, 1540, 920);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        // Thiết lập stage
        stage.setTitle("Game Product Management UI");
        stage.setScene(scene);
        stage.show();

        // Load real game data from the database on a background thread
        loadProductsFromDB();
    }

    // ===== PHẦN 9: TẠO SIDEBAR MENU =====
    // Đây là menu điều hướng bên trái
    private VBox createSidebar() {
        VBox sidebar = new VBox(14);
        sidebar.setPrefWidth(255);
        sidebar.setPadding(new Insets(22, 18, 22, 18));
        sidebar.setStyle(
                "-fx-background-color: linear-gradient(to bottom, rgba(5,10,18,0.98), rgba(13,18,28,0.98));" +
                "-fx-border-color: rgba(255,255,255,0.08); -fx-border-width: 0 1 0 0;"
        );

        // ===== THƯƠNG HIỆU / LOGO =====
        VBox brandBox = new VBox(6);
        brandBox.setPadding(new Insets(0, 0, 20, 0));

        Region topLine = createAccentLine();

        ImageView logo = new ImageView(new Image(getClass().getResourceAsStream("/images/LOGO.png")));
        logo.setFitHeight(135);
        logo.setPreserveRatio(true);
        logo.setSmooth(true);

        Label brand = new Label("X GAME SHOP");
        brand.setTextFill(Color.WHITE);
        brand.setFont(Font.font("Radley Sans", 30));

        Label sub = new Label("premium game admin dashboard");
        sub.setTextFill(Color.web("#8ea4c7"));
        sub.setFont(Font.font(FONT_UI, 12));

        brandBox.getChildren().addAll(logo, brand, sub);

        // ===== CÁC NÚT MENU =====
        Button btnDashboard = createMenuButton("Dashboard");
        Button btnProducts = createMenuButton("Products");
        Button btnLibrary = createMenuButton("Library");
        Button btnReports = createMenuButton("Reports");
        Button btnSettings = createMenuButton("Settings");

        // ===== GÁN CHỨC NĂNG CHUYỂN TRANG =====
        btnDashboard.setOnAction(e -> switchPage(btnDashboard, createDashboardView()));
        btnProducts.setOnAction(e -> switchPage(btnProducts, createProductsView()));
        btnLibrary.setOnAction(e -> switchPage(btnLibrary, createLibraryView()));
        btnReports.setOnAction(e -> switchPage(btnReports, createReportsView()));
        btnSettings.setOnAction(e -> switchPage(btnSettings, createShowcasePage(
                "Settings", "Manage accounts, appearance, access roles, and general platform configuration."
        )));

        // Mặc định Dashboard đang active
        setActiveButton(btnDashboard);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        // ===== KHUNG PROFILE ADMIN =====
        VBox profile = new VBox(8);
        profile.setPadding(new Insets(16));
        profile.setStyle(glassPanelStyle("18"));

        Label user = new Label(loggedInUsername.isEmpty() ? "Admin Account" : loggedInUsername);
        user.setTextFill(Color.WHITE);
        user.setFont(Font.font(FONT_UI, FontWeight.BOLD, 16));

        Label role = new Label("System Manager");
        role.setTextFill(Color.web("#94a3b8"));
        role.setFont(Font.font(FONT_UI, 13));

        Button logout = new Button("Sign Out");
        logout.setMaxWidth(Double.MAX_VALUE);
        logout.setStyle(primaryButtonStyle());
        logout.setOnAction(e -> {
            try {
                Stage loginStage = new Stage();
                javafx.scene.Parent loginRoot = javafx.fxml.FXMLLoader.load(
                        getClass().getResource("/fxml/Login.fxml"));
                loginStage.setScene(new javafx.scene.Scene(loginRoot, 860, 620));
                loginStage.setTitle("HyDro Games — Sign In");
                loginStage.setResizable(false);
                loginStage.show();
                ((Stage) logout.getScene().getWindow()).close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        profile.getChildren().addAll(user, role, logout);

        // ===== THÊM TẤT CẢ VÀO SIDEBAR =====
        sidebar.getChildren().addAll(
                brandBox,
                btnDashboard, btnProducts, btnLibrary, btnReports, btnSettings,
                spacer,
                profile
        );

        return sidebar;
    }

    // ===== PHẦN 10: TRANG DASHBOARD =====
    // Trang tổng quan hiển thị thông tin chính của hệ thống
    private VBox createDashboardView() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24, 24, 24, 26));

        content.getChildren().addAll(
                createTopHeader("Dashboard Overview", "Steam-inspired layout with polished cards, analytics, and premium presentation."),
                createHeroBanner(),
                createStatRow(),
                createChartsRow(),
                createBottomDashboardRow()
        );

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

        VBox wrapper = new VBox(scroll);
        VBox.setVgrow(scroll, Priority.ALWAYS);
        return wrapper;
    }

    // ===== PHẦN 11: TRANG QUẢN LÝ SẢN PHẨM =====
    // Đây là màn hình CRUD sản phẩm
    private VBox createProductsView() {
        if (productsView != null) {
            return productsView;
        }

        VBox wrapper = new VBox(20);
        wrapper.setPadding(new Insets(24, 24, 24, 26));

        HBox body = new HBox(20, createProductFormPanel(), createProductTablePanel());
        HBox.setHgrow(body.getChildren().get(1), Priority.ALWAYS);

        wrapper.getChildren().addAll(
            createTopHeader("Product Management", "Create, update, delete, and monitor games with a modern launcher-style layout."),
            createFeaturedStrip(),
            body
        );

        ScrollPane scrollPane = new ScrollPane(wrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: linear-gradient(to bottom right, #08111f, #0b1220 40%, #111827 100%);");
        VBox outer = new VBox(scrollPane);
        outer.setStyle("-fx-background-color: linear-gradient(to bottom right, #08111f, #0b1220 40%, #111827 100%);");
        productsView = outer;
        return outer;
    }

    // ===== PHẦN 12: TRANG THƯ VIỆN GAME =====
    // Hiển thị game theo dạng card / poster
    private VBox createLibraryView() {
        VBox wrapper = new VBox(20);
        wrapper.setPadding(new Insets(24, 24, 24, 26));

        FlowPane gameGrid = new FlowPane();
        gameGrid.setHgap(18);
        gameGrid.setVgap(18);

        for (ProductRow row : productList) {
            gameGrid.getChildren().add(createLibraryCard(row));
        }

        VBox container = new VBox(18);
        container.setPadding(new Insets(20));
        container.setStyle(glassPanelStyle("22"));
        container.getChildren().addAll(createSectionTitle("Game Collection"), gameGrid);

        wrapper.getChildren().addAll(
                createTopHeader("Game Library", "Poster-based collection view inspired by modern game launchers."),
                createHeroBanner(),
                container
        );

        ScrollPane scrollPane = new ScrollPane(wrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: linear-gradient(to bottom right, #08111f, #0b1220 40%, #111827 100%);");
        VBox outer = new VBox(scrollPane);
        outer.setStyle("-fx-background-color: linear-gradient(to bottom right, #08111f, #0b1220 40%, #111827 100%);");
        return outer;
    }

    // ===== PHẦN 13: TRANG BÁO CÁO / THỐNG KÊ =====
    private VBox createReportsView() {
        VBox wrapper = new VBox(20);
        wrapper.setPadding(new Insets(24, 24, 24, 26));

        HBox top = createTopHeader("Reports", "Track revenue by day, month, and year.");

        // Fetch dynamic values
        String todayRevenue = formatMoney(com.hydrogame.database.RevenueService.getTodayRevenue());
        String monthRevenue = formatMoney(com.hydrogame.database.RevenueService.getMonthlyRevenue());
        String yearRevenue = formatMoney(com.hydrogame.database.RevenueService.getYearlyRevenue());
        int activeUsers = com.hydrogame.user_service.UserCrudService.class.isAssignableFrom(com.hydrogame.user_service.UserCrudService.class)
            ? new com.hydrogame.user_service.UserCrudService().listAllUsers().size() : 0;

        HBox summary = new HBox(16,
                createMetricCard("Today Revenue", todayRevenue, "Completed orders for today", "#22c55e"),
                createMetricCard("Monthly Revenue", monthRevenue, "Current month performance", "#60a5fa"),
                createMetricCard("Yearly Revenue", yearRevenue, "Accumulated revenue for this year", "#a855f7"),
                createMetricCard("Active Users", String.valueOf(activeUsers), "Users with recent interaction", "#f59e0b")
        );

        // Chart and filter
        ComboBox<String> filter = new ComboBox<>();
        filter.getItems().addAll("Day", "Month", "Year");
        filter.setValue("Month");
        filter.setStyle(comboStyle());

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.setPrefHeight(320);
        chart.setStyle("-fx-background-color: transparent;");

        // Date picker shown when "Day" is selected — no default value so rolling data loads first
        javafx.scene.control.DatePicker datePicker = new javafx.scene.control.DatePicker();
        datePicker.setPromptText("Pick month to filter");
        datePicker.setStyle(inputStyle());
        datePicker.setVisible(false);
        datePicker.setManaged(false);

        // Year picker shown when "Month" is selected — no default so rolling data loads first
        ComboBox<Integer> yearPicker = new ComboBox<>();
        int nowYear = LocalDate.now().getYear();
        for (int y = nowYear; y >= nowYear - 9; y--) yearPicker.getItems().add(y);
        yearPicker.setPromptText("Pick year to filter");
        yearPicker.setValue(null);
        yearPicker.setStyle(comboStyle());
        yearPicker.setVisible(false);
        yearPicker.setManaged(false);

        HBox pickerRow = new HBox(10, datePicker, yearPicker);
        pickerRow.setAlignment(Pos.CENTER_LEFT);

        // Helper to load chart data:
        //   Day   + no date picked  → last 30 days (rolling)
        //   Day   + date picked     → all days in that month
        //   Month + no year picked  → last 12 months (rolling)
        //   Month + year picked     → all months in that year
        //   Year                    → last 5 years
        Runnable updateChart = () -> {
            chart.getData().clear();
            XYChart.Series<String, Number> s = new XYChart.Series<>();
            if ("Day".equals(filter.getValue())) {
                LocalDate picked = datePicker.getValue();
                java.util.Map<String, java.math.BigDecimal> map = picked != null
                    ? com.hydrogame.database.RevenueService.getDailyRevenueForMonth(picked.getYear(), picked.getMonthValue())
                    : com.hydrogame.database.RevenueService.getDailyRevenueLastDays(30);
                for (String k : map.keySet()) s.getData().add(new XYChart.Data<>(k, map.get(k)));
            } else if ("Month".equals(filter.getValue())) {
                Integer yr = yearPicker.getValue();
                java.util.Map<String, java.math.BigDecimal> map = yr != null
                    ? com.hydrogame.database.RevenueService.getMonthlyRevenueForYear(yr)
                    : com.hydrogame.database.RevenueService.getMonthlyRevenueLastMonths(12);
                for (String k : map.keySet()) s.getData().add(new XYChart.Data<>(k, map.get(k)));
            } else {
                java.util.Map<String, java.math.BigDecimal> map =
                    com.hydrogame.database.RevenueService.getYearlyRevenueForChart(5);
                for (String yr : map.keySet()) s.getData().add(new XYChart.Data<>(yr, map.get(yr)));
            }
            chart.getData().add(s);
        };
        updateChart.run();
        filter.setOnAction(e -> {
            boolean isDay = "Day".equals(filter.getValue());
            boolean isMo  = "Month".equals(filter.getValue());
            // Reset pickers so rolling data shows when switching filters
            datePicker.setValue(null);
            yearPicker.setValue(null);
            datePicker.setVisible(isDay);
            datePicker.setManaged(isDay);
            yearPicker.setVisible(isMo);
            yearPicker.setManaged(isMo);
            updateChart.run();
        });
        datePicker.setOnAction(e -> updateChart.run());
        yearPicker.setOnAction(e -> updateChart.run());

        VBox chartCard = new VBox(14);
        chartCard.setPadding(new Insets(20));
        chartCard.setStyle(glassPanelStyle("24"));
        chartCard.getChildren().addAll(
                createSectionTitle("Revenue Statistics"),
                createSubTitle("View revenue by day, month, or year"),
                filter,
                pickerRow,
                chart
        );

        HBox bottom = new HBox(18, createTopGamesReportCard(), createUserActionReportCard());
        HBox.setHgrow(bottom.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(bottom.getChildren().get(1), Priority.ALWAYS);

        wrapper.getChildren().addAll(top, summary, chartCard, bottom);
        ScrollPane scrollPane = new ScrollPane(wrapper);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: linear-gradient(to bottom right, #08111f, #0b1220 40%, #111827 100%);");
        VBox outer = new VBox(scrollPane);
        outer.setStyle("-fx-background-color: linear-gradient(to bottom right, #08111f, #0b1220 40%, #111827 100%);");
        return outer;
    }

    // ===== PHẦN 14: HEADER TRÊN MỖI TRANG =====

        // Helper to format money
        private String formatMoney(java.math.BigDecimal value) {
            if (value == null) return "0đ";
            java.text.DecimalFormat df = new java.text.DecimalFormat("###,###,###");
            return df.format(value) + "đ";
        }
    // Gồm tiêu đề, mô tả, ô search, nút thêm game
    private HBox createTopHeader(String titleText, String descText) {
        HBox top = new HBox(18);
        top.setAlignment(Pos.CENTER_LEFT);

        VBox left = new VBox(4);
        Label title = new Label(titleText);
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(FONT_UI, FontWeight.EXTRA_BOLD, 36));

        Label desc = new Label(descText);
        desc.setTextFill(Color.web("#aec4e4"));
        desc.setFont(Font.font(FONT_UI, 14));

        left.getChildren().addAll(title, desc);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField search = new TextField();
        search.setPromptText("Search games, IDs, categories...");
        search.setPrefWidth(300);
        search.setStyle(inputStyle());

        // Press Enter in the search field → filter product list via ProductController
        search.setOnAction(e -> {
            String kw = search.getText().trim();
            if (kw.isEmpty()) loadProductsFromDB();
            else              filterProductList(kw);
        });

        Button add = new Button("+ New Release");
        add.setStyle(primaryButtonStyle());

        // Mở popup thêm game mới
        add.setOnAction(e -> showNewReleaseDialog());

        top.getChildren().addAll(left, spacer, search, add);
        return top;
    }

    // ===== PHẦN 15: POPUP THÊM GAME MỚI =====
    // Khi nhấn nút "+ New Release" sẽ hiện hộp thoại nhập dữ liệu
    private void showNewReleaseDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New Release");
        dialog.setHeaderText("Enter game information");

        ButtonType addButtonType = new ButtonType("Add Game", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        TextField idField = new TextField();
        idField.setPromptText("Game ID");

        TextField nameField = new TextField();
        nameField.setPromptText("Game Name");

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.getItems().addAll("Action", "Adventure", "RPG", "Sports", "Sandbox", "Horror");
        categoryBox.setPromptText("Category");
        categoryBox.setMaxWidth(Double.MAX_VALUE);

        TextField priceField = new TextField();
        priceField.setPromptText("Price");

        TextField stockField = new TextField();
        stockField.setPromptText("Stock Quantity");

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("In Stock", "Low Stock", "Out of Stock");
        statusBox.setPromptText("Status");
        statusBox.setMaxWidth(Double.MAX_VALUE);

        TextArea descField = new TextArea();
        descField.setPromptText("Short description...");
        descField.setPrefRowCount(3);
        descField.setWrapText(true);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Game ID:"), 0, 0);
        grid.add(idField, 1, 0);

        grid.add(new Label("Game Name:"), 0, 1);
        grid.add(nameField, 1, 1);

        grid.add(new Label("Category:"), 0, 2);
        grid.add(categoryBox, 1, 2);

        grid.add(new Label("Price:"), 0, 3);
        grid.add(priceField, 1, 3);

        grid.add(new Label("Stock:"), 0, 4);
        grid.add(stockField, 1, 4);

        grid.add(new Label("Status:"), 0, 5);
        grid.add(statusBox, 1, 5);

        grid.add(new Label("Description:"), 0, 6);
        grid.add(descField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(nameField::requestFocus);

        dialog.showAndWait().ifPresent(result -> {
            if (result == addButtonType) {
                // ===== LẤY DỮ LIỆU NGƯỜI DÙNG NHẬP =====
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                String category = categoryBox.getValue();
                String priceText = priceField.getText().trim();
                String stockText = stockField.getText().trim();
                String status = statusBox.getValue();
                String desc = descField.getText().trim();

                // ===== KIỂM TRA DỮ LIỆU RỖNG =====
                if (id.isEmpty() || name.isEmpty() || category == null ||
                    priceText.isEmpty() || stockText.isEmpty() || status == null) {
                    showWarning("Please fill in all required fields.");
                    return;
                }

                int price;
                int stock;

                // ===== KIỂM TRA GIÁ VÀ TỒN KHO CÓ PHẢI SỐ =====
                try {
                    price = Integer.parseInt(priceText);
                    stock = Integer.parseInt(stockText);
                } catch (Exception e) {
                    showWarning("Price and stock must be numeric values.");
                    return;
                }

                // ===== THÊM VÀO CƠ SỞ DỮ LIỆU QUA PRODUCTCONTROLLER =====
                final String fName  = name;
                final String fCat   = category;
                final int    fPrice = price;
                final int    fStock = stock;
                final String fDesc  = desc.isEmpty() ? "New release" : desc;

                Thread t = new Thread(() -> {
                    Game saved = productController.addGame(
                            fName, fDesc, new BigDecimal(fPrice),
                            18, LocalDate.now(), fStock, "/images/LOGO.png", loggedInUsername);
                    if (saved != null && fCat != null && !fCat.isBlank()) {
                        productController.setGenresForGame(saved.getGameId(), List.of(fCat));
                        Game withGenres = productController.getGameById(saved.getGameId());
                        if (withGenres != null) {
                            final Game finalSaved = withGenres;
                            Platform.runLater(() -> {
                                productList.add(gameToRow(finalSaved));
                                if (productTable != null) productTable.refresh();
                                refreshDynamicSections();
                                showInfo("Added successfully!");
                            });
                            return;
                        }
                    }
                    final Game finalSaved = saved;
                    Platform.runLater(() -> {
                        if (finalSaved != null) {
                            ProductRow row = gameToRow(finalSaved);
                            productList.add(row);
                        }
                        if (productTable != null) productTable.refresh();
                        refreshDynamicSections();
                        showInfo("Added successfully!");
                    });
                });
                t.setDaemon(true);
                t.start();
            }
        });
    }

    // ===== PHẦN 16: TRANG TÙY CHỈNH / SETTINGS DEMO =====
    private VBox createShowcasePage(String titleText, String descText) {
        VBox wrapper = new VBox(20);
        wrapper.setPadding(new Insets(24, 24, 24, 26));

        HBox cards = new HBox(18,
                createInfoCard("UI Status", "Premium Ready", "The interface is fully styled and ready for data binding."),
                createInfoCard("Theme", "Steam Inspired", "Dark blue gradients, glass cards, and launcher-style spacing."),
                createInfoCard("Next Step", "Bind Data", "Connect a database, game covers, authentication, and permissions.")
        );

        VBox panel = new VBox(18);
        panel.setPadding(new Insets(22));
        panel.setStyle(glassPanelStyle("24"));
        panel.getChildren().addAll(
                createSectionTitle(titleText),
                createSubTitle(descText),
                cards,
                createHeroBanner()
        );

        wrapper.getChildren().addAll(createTopHeader(titleText, descText), panel);
        return wrapper;
    }

    // ===== PHẦN 17: HERO BANNER =====
    // Banner lớn ở đầu trang
    private HBox createHeroBanner() {
        HBox banner = new HBox();
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(26));
        banner.setMinHeight(220);
        banner.setStyle(
                "-fx-background-color: linear-gradient(to right, rgba(15,23,42,0.95), rgba(16,24,39,0.78), rgba(37,99,235,0.35));" +
                "-fx-background-radius: 28; -fx-border-radius: 28;" +
                "-fx-border-color: rgba(255,255,255,0.08); -fx-border-width: 1;"
        );

        VBox left = new VBox(10);
        left.setMaxWidth(560);

        Label badge = new Label("FEATURED COLLECTION");
        badge.setTextFill(Color.WHITE);
        badge.setFont(Font.font(FONT_UI, FontWeight.BOLD, 11));
        badge.setStyle("-fx-background-color: rgba(59,130,246,0.28); -fx-background-radius: 999; -fx-padding: 7 12;");

        Label title = new Label("Build a premium Steam-style game management system");
        title.setWrapText(true);
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(FONT_UI, FontWeight.EXTRA_BOLD, 30));

        Label desc = new Label("Dashboard charts, library cards, inventory states, featured products, and a premium glass-style admin layout.");
        desc.setWrapText(true);
        desc.setTextFill(Color.web("#c9d8ee"));
        desc.setFont(Font.font(FONT_UI, 14));

        HBox actions = new HBox(12);
        Button viewLibrary = new Button("Open Library");
        viewLibrary.setStyle(primaryButtonStyle());
        viewLibrary.setOnAction(e -> switchPage(findButtonByText("Library"), createLibraryView()));

        Button analytics = new Button("View Analytics");
        analytics.setStyle(secondaryButtonStyle());
        analytics.setOnAction(e -> switchPage(findButtonByText("Reports"), createReportsView()));

        actions.getChildren().addAll(viewLibrary, analytics);
        left.getChildren().addAll(badge, title, desc, actions);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox right = new HBox(14);
        int count = 0;
        for (ProductRow row : productList) {
            if (count >= 3) break;
            right.getChildren().add(createHeroMiniPoster(row.getName(), row.getCategory()));
            count++;
        }
        if (productList.isEmpty()) {
            right.getChildren().addAll(
                createHeroMiniPoster("No Games", "Add some!"),
                createHeroMiniPoster("...", "..."),
                createHeroMiniPoster("...", "...")
            );
        }

        banner.getChildren().addAll(left, spacer, right);
        return banner;
    }

    // ===== PHẦN 18: THẺ NHỎ TRONG HERO BANNER =====
    private VBox createHeroMiniPoster(String name, String genre) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.BOTTOM_LEFT);
        card.setPadding(new Insets(12));
        card.setPrefSize(138, 165);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom, rgba(255,255,255,0.10), rgba(255,255,255,0.02));" +
                "-fx-background-radius: 18; -fx-border-radius: 18;" +
                "-fx-border-color: rgba(255,255,255,0.12);"
        );

        Region top = new Region();
        VBox.setVgrow(top, Priority.ALWAYS);

        Label title = new Label(name);
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(FONT_UI, FontWeight.BOLD, 14));

        Label sub = new Label(genre);
        sub.setTextFill(Color.web("#c5d4eb"));
        sub.setFont(Font.font(FONT_UI, 12));

        card.getChildren().addAll(top, title, sub);
        return card;
    }

    // ===== PHẦN 19: THỐNG KÊ NHANH TRÊN DASHBOARD =====
    private HBox createStatRow() {
        HBox row = new HBox(16);
        row.getChildren().addAll(
                createMetricCard("Total Games", String.valueOf(productList.size()), "Number of managed products", "#60a5fa"),
                createMetricCard("Low Stock", String.valueOf(countLowStock()), "Items below 10 units", "#f59e0b"),
                createMetricCard("Inventory Value", formatMoney(calculateInventoryValue()), "Current total stock value", "#22c55e"),
                createMetricCard("Categories", String.valueOf(countCategories()), "Total genres in collection", "#a855f7")
        );
        return row;
    }

    // ===== PHẦN 20: THẺ THỐNG KÊ NHỎ =====
    private VBox createMetricCard(String titleText, String valueText, String noteText, String accent) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(18));
        card.setPrefWidth(280);
        card.setStyle(glassPanelStyle("22"));

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(titleText);
        title.setTextFill(Color.web("#c2d0e6"));
        title.setFont(Font.font(FONT_UI, 13));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label dot = new Label("  ");
        dot.setStyle("-fx-background-color: " + accent + "; -fx-background-radius: 999; -fx-padding: 4;");

        top.getChildren().addAll(title, spacer, dot);

        Label value = new Label(valueText);
        value.setTextFill(Color.WHITE);
        value.setFont(Font.font(FONT_UI, FontWeight.EXTRA_BOLD, 28));

        Label note = new Label(noteText);
        note.setTextFill(Color.web("#8ea4c7"));
        note.setFont(Font.font(FONT_UI, 12));

        card.getChildren().addAll(top, value, note);
        return card;
    }

    // ===== PHẦN 21: HÀNG BIỂU ĐỒ DASHBOARD =====
    private HBox createChartsRow() {
        HBox charts = new HBox(18);
        charts.getChildren().addAll(createRevenueChartCard(), createCategoryBarChartCard());
        HBox.setHgrow(charts.getChildren().get(0), Priority.ALWAYS);
        HBox.setHgrow(charts.getChildren().get(1), Priority.ALWAYS);
        return charts;
    }

    // ===== PHẦN 22: CARD BIỂU ĐỒ DOANH THU =====
    private VBox createRevenueChartCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setPrefWidth(720);
        card.setStyle(glassPanelStyle("24"));

        card.getChildren().addAll(
                createSectionTitle("Revenue Trend"),
                createSubTitle("Revenue across the last six months"),
                createRevenueChart()
        );
        return card;
    }

    // ===== PHẦN 23: CARD BIỂU ĐỒ TỒN KHO THEO THỂ LOẠI =====
    private VBox createCategoryBarChartCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setPrefWidth(470);
        card.setStyle(glassPanelStyle("24"));

        card.getChildren().addAll(
                createSectionTitle("Stock by Genre"),
                createSubTitle("Inventory amount by game category"),
                createBarChart()
        );
        return card;
    }

    // ===== PHẦN 24: HÀNG DƯỚI DASHBOARD =====
    private HBox createBottomDashboardRow() {
        HBox row = new HBox(18);
        row.getChildren().addAll(createPieCard(), createFeaturedListCard());
        HBox.setHgrow(row.getChildren().get(1), Priority.ALWAYS);
        return row;
    }

    // ===== PHẦN 25: CARD BIỂU ĐỒ TRẠNG THÁI SẢN PHẨM =====
    private VBox createPieCard() {
        VBox card = new VBox(16);
        card.setPadding(new Insets(20));
        card.setPrefWidth(380);
        card.setStyle(glassPanelStyle("24"));

        card.getChildren().addAll(
                createSectionTitle("Product Status"),
                createSubTitle("Distribution of available, low-stock, and out-of-stock products"),
                createPieChart()
        );
        return card;
    }

    // ===== PHẦN 26: DANH SÁCH GAME NỔI BẬT =====
    private VBox createFeaturedListCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle(glassPanelStyle("24"));

        VBox list = new VBox(12);
        for (ProductRow row : productList) {
            list.getChildren().add(createFeaturedRow(row));
        }

        card.getChildren().addAll(
                createSectionTitle("Featured Games"),
                createSubTitle("Highlighted products displayed in a launcher-inspired layout"),
                list
        );
        return card;
    }

    // ===== PHẦN 27: DẢI FEATURED GAME TRÊN PRODUCTS =====
    private HBox createFeaturedStrip() {
        featuredFlow = new FlowPane();
        featuredFlow.setHgap(14);
        featuredFlow.setVgap(14);
        refreshFeaturedStrip();

        VBox card = new VBox(14);
        card.setPadding(new Insets(18));
        card.setStyle(glassPanelStyle("22"));
        card.getChildren().addAll(
                createSectionTitle("Featured Covers"),
                createSubTitle("Game cards with image covers"),
                featuredFlow
        );

        HBox wrapper = new HBox(card);
        HBox.setHgrow(card, Priority.ALWAYS);
        return wrapper;
    }

    // ===== PHẦN 28: FORM NHẬP / SỬA THÔNG TIN SẢN PHẨM =====
    private VBox createProductFormPanel() {
        VBox panel = new VBox(14);
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(370);
        panel.setStyle(glassPanelStyle("24"));

        // Khởi tạo các ô nhập liệu
        txtName = new TextField();
        categoryPickerBox = createCategoryPicker();
        txtPrice = new TextField();
        txtStock = new TextField();
        cboStatus = new ComboBox<>();
        txtDesc = new TextArea();

        // Prompt text
        txtName.setPromptText("Game Name");
        txtPrice.setPromptText("Price");
        txtStock.setPromptText("Stock Quantity");
        txtDesc.setPromptText("Short description...");

        // Style
        txtName.setStyle(inputStyle());
        txtPrice.setStyle(inputStyle());
        txtStock.setStyle(inputStyle());
        txtDesc.getStyleClass().add("dark-text-area");
        txtDesc.setStyle("-fx-background-color: #162032; -fx-background-radius: 16; -fx-border-radius: 16; "
                + "-fx-border-color: rgba(255,255,255,0.08); -fx-text-fill: white; -fx-control-inner-background: #162032;");
        txtDesc.setPrefRowCount(2);
        txtDesc.setMaxHeight(60);
        txtDesc.setPrefHeight(60);
        txtDesc.setWrapText(true);

        // Category picker is already created above

        cboStatus.getItems().addAll("In Stock", "Low Stock", "Out of Stock");
        cboStatus.setPromptText("Status");
        cboStatus.setPrefWidth(Double.MAX_VALUE);
        cboStatus.setStyle(comboStyle());
        styleComboBox(cboStatus);
        cboStatus.setDisable(true);

        // Auto-compute status when stock changes
        txtStock.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                int stockVal = Integer.parseInt(newVal.trim());
                String autoStatus = stockVal <= 0 ? "Out of Stock" : stockVal < 10 ? "Low Stock" : "In Stock";
                cboStatus.setValue(autoStatus);
            } catch (NumberFormatException ex) {
                cboStatus.setValue(null);
            }
        });

        // ===== CÁC NÚT THAO TÁC =====
        HBox actions = new HBox(10);
        Button add = new Button("Add");
        Button update = new Button("Update");
        Button clear = new Button("Reset");

        add.setStyle(primaryButtonStyle());
        update.setStyle(secondaryButtonStyle());
        clear.setStyle(ghostButtonStyle());

        // Gắn sự kiện
        add.setOnAction(e -> addProduct());
        update.setOnAction(e -> updateProduct());
        clear.setOnAction(e -> clearForm());

        actions.getChildren().addAll(add, update, clear);

        panel.getChildren().addAll(
                createSectionTitle("Product Details"),
                createSubTitle("Clean form layout designed to match the launcher theme"),
                txtName, categoryPickerBox, txtPrice, txtStock, txtDesc,
                actions
        );

        return panel;
    }

    // ===== PHẦN 29: BẢNG DỮ LIỆU SẢN PHẨM =====
    private VBox createProductTablePanel() {
        VBox panel = new VBox(14);
        panel.setPadding(new Insets(20));
        panel.setStyle(glassPanelStyle("24"));

        HBox header = new HBox();
        VBox left = new VBox(4,
                createSectionTitle("Product Table"),
                createSubTitle("Select a row to edit product information")
        );

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button delete = new Button("Delete Product");
        delete.setStyle(secondaryButtonStyle());
        delete.setOnAction(e -> deleteProduct());

        header.getChildren().addAll(left, spacer, delete);

        // ===== TẠO TABLEVIEW =====
        productTable = new TableView<>();
        productTable.setItems(productList);
        productTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        productTable.setStyle(tableStyle());
        VBox.setVgrow(productTable, Priority.ALWAYS);

        // ===== TẠO CÁC CỘT =====
        TableColumn<ProductRow, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> cell.getValue().idProperty());

        TableColumn<ProductRow, String> colName = new TableColumn<>("Game Name");
        colName.setCellValueFactory(cell -> cell.getValue().nameProperty());

        TableColumn<ProductRow, String> colCategory = new TableColumn<>("Category");
        colCategory.setCellValueFactory(cell -> cell.getValue().categoryProperty());

        TableColumn<ProductRow, String> colPrice = new TableColumn<>("Price");
        colPrice.setCellValueFactory(cell -> cell.getValue().priceFormattedProperty());

        TableColumn<ProductRow, String> colStock = new TableColumn<>("Stock");
        colStock.setCellValueFactory(cell -> cell.getValue().stockProperty());

        TableColumn<ProductRow, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(cell -> cell.getValue().statusProperty());

        productTable.getColumns().addAll(colId, colName, colCategory, colPrice, colStock, colStatus);

        // Make table cell text white so it's visible on dark background
        for (TableColumn<ProductRow, ?> col : productTable.getColumns()) {
            @SuppressWarnings("unchecked")
            TableColumn<ProductRow, String> strCol = (TableColumn<ProductRow, String>) col;
            strCol.setCellFactory(c -> new TableCell<ProductRow, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty ? null : item);
                    setTextFill(Color.WHITE);
                }
            });
        }

        // Khi chọn 1 dòng trong bảng => đổ dữ liệu lên form
        productTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selected) -> fillForm(selected));

        // Double-click to open product detail dialog
        productTable.setRowFactory(tv -> {
            TableRow<ProductRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    ProductRow clicked = row.getItem();
                    showProductDetailDialog(clicked);
                }
            });
            return row;
        });

        panel.getChildren().addAll(header, productTable);
        return panel;
    }

    // ===== PHẦN 30: CARD DOANH THU THEO GIAI ĐOẠN =====
    private VBox createRevenueByPeriodCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setPrefWidth(720);
        card.setStyle(glassPanelStyle("24"));

        card.getChildren().addAll(
                createSectionTitle("Revenue by Period"),
                createSubTitle("Expandable for day, month, year, or custom range filters"),
                createRevenuePeriodChart(),

                createReportInfoBox(
                        "Recommended Metrics",
                        "• Daily revenue\n• Monthly revenue\n• Yearly revenue\n• Previous period comparison"
                )
        );
        return card;
    }

    // ===== PHẦN 31: CARD HÀNH VI NGƯỜI DÙNG =====
    private VBox createUserBehaviorCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setPrefWidth(470);
        card.setStyle(glassPanelStyle("24"));

        card.getChildren().addAll(
                createSectionTitle("User Reports"),
                createSubTitle("Track what users do inside the platform and how they interact with products."),
                createUserBehaviorChart(),
                createReportInfoBox(
                        "Behavior to Track",
                        "• Login count\n• Product views\n• Add to cart\n• Completed orders\n• New vs returning users"
                )
        );
        return card;
    }

    // ===== PHẦN 32: TOP GAME BÁN CHẠY =====
    private VBox createTopGamesReportCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setStyle(glassPanelStyle("24"));

        VBox list = new VBox(12);
        // Top 5 games by unit price from the loaded productList
        List<ProductRow> top = productList.stream()
                .sorted((a, b) -> b.getPrice() - a.getPrice())
                .limit(5)
                .collect(java.util.stream.Collectors.toList());

        if (top.isEmpty()) {
            list.getChildren().add(createReportRow("No data", "Load products first", "—"));
        } else {
            for (ProductRow row : top) {
                list.getChildren().add(createReportRow(
                        row.getName(),
                        row.getCategory(),
                        row.getPriceFormatted()
                ));
            }
        }

        card.getChildren().addAll(
                createSectionTitle("Top Products by Price"),
                createSubTitle("Highest-priced games in the catalog"),
                list
        );
        return card;
    }

    // ===== PHẦN 33: TÓM TẮT BÁO CÁO ADMIN =====
    private VBox createUserActionReportCard() {
        VBox card = new VBox(14);
        card.setPadding(new Insets(20));
        card.setPrefWidth(430);
        card.setStyle(glassPanelStyle("24"));

        VBox list = new VBox(12,
                createReportInfoBox("Order Summary", "Orders today: 42\nSuccessful: 35\nPending: 5\nCancelled: 2"),
                createReportInfoBox("Account Summary", "New users: 18\nReturning users: 73\nOnline now: 56"),
                createReportInfoBox("Interaction Summary", "Product views: 1,284\nAdd to cart: 219\nConversion rate: 8.7%")
        );

        card.getChildren().addAll(
                createSectionTitle("Admin Report Summary"),
                createSubTitle("Quick management insights for monitoring and presentations"),
                list
        );
        return card;
    }

    // ===== PHẦN 34: CARD GAME TRONG LIBRARY =====
    private VBox createLibraryCard(ProductRow row) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(14));
        card.setPrefSize(245, 260);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + row.getAccent() + "55, " + row.getShade() + ");" +
                "-fx-background-radius: 24; -fx-border-radius: 24;" +
                "-fx-border-color: rgba(255,255,255,0.10);"
        );

        // Add click handler to open product detail dialog
        card.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                event.consume(); // Prevent event from bubbling and causing layout issues
                showProductDetailDialog(row);
            }
        });

        StackPane poster = new StackPane();
        poster.setPrefHeight(135);

        ImageView cover = new ImageView(loadImage(row.getImagePath()));
        cover.setFitWidth(215);
        cover.setFitHeight(135);
        cover.setPreserveRatio(false);
        cover.setSmooth(true);

        Rectangle clip = new Rectangle(215, 135);
        clip.setArcWidth(28);
        clip.setArcHeight(28);
        cover.setClip(clip);

        poster.getChildren().add(cover);

        Label title = new Label(row.getName());
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(FONT_UI, FontWeight.BOLD, 18));

        Label meta = new Label(row.getCategory() + " · " + row.getTagline());
        meta.setTextFill(Color.web("#e2ebfb"));
        meta.setWrapText(true);
        meta.setFont(Font.font(FONT_UI, 12));

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);

        Label price = new Label(row.getPriceFormatted());
        price.setTextFill(Color.WHITE);
        price.setFont(Font.font(FONT_UI, FontWeight.BOLD, 14));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label(row.getStatus());
        status.setTextFill(Color.WHITE);
        status.setFont(Font.font(FONT_UI, 12));
        status.setStyle(statusChipStyle(row.getStatus()));

        footer.getChildren().addAll(price, spacer, status);

        card.getChildren().addAll(poster, title, meta, footer);
        return card;
    }

    // ===== PHẦN 35: CARD THÔNG TIN NHỎ =====
    private VBox createInfoCard(String titleText, String valueText, String descText) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(18));
        card.setPrefWidth(300);
        card.setStyle(glassPanelStyle("22"));

        Label title = new Label(titleText);
        title.setTextFill(Color.web("#bfd0e8"));
        title.setFont(Font.font(FONT_UI, 12));

        Label value = new Label(valueText);
        value.setTextFill(Color.WHITE);
        value.setFont(Font.font(FONT_UI, FontWeight.EXTRA_BOLD, 24));

        Label desc = new Label(descText);
        desc.setTextFill(Color.web("#93a9cb"));
        desc.setWrapText(true);
        desc.setFont(Font.font(FONT_UI, 12));

        card.getChildren().addAll(title, value, desc);
        return card;
    }

    // ===== PHẦN 36: DÒNG GAME NỔI BẬT =====
    private HBox createFeaturedRow(ProductRow row) {
        HBox item = new HBox(14);
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(12, 14, 12, 14));
        item.setStyle(
                "-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 18; -fx-border-radius: 18;" +
                "-fx-border-color: rgba(255,255,255,0.06);"
        );

        ImageView thumb = new ImageView(loadImage(row.getImagePath()));
        thumb.setFitWidth(54);
        thumb.setFitHeight(54);
        thumb.setPreserveRatio(false);
        thumb.setSmooth(true);

        Rectangle clip = new Rectangle(54, 54);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        thumb.setClip(clip);

        VBox info = new VBox(4);
        Label name = new Label(row.getName());
        name.setTextFill(Color.WHITE);
        name.setFont(Font.font(FONT_UI, FontWeight.BOLD, 15));

        Label meta = new Label(row.getCategory() + " · " + row.getTagline());
        meta.setTextFill(Color.web("#9bb2d5"));
        meta.setFont(Font.font(FONT_UI, 12));

        info.getChildren().addAll(name, meta);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label price = new Label(row.getPriceFormatted());
        price.setTextFill(Color.WHITE);
        price.setFont(Font.font(FONT_UI, FontWeight.BOLD, 14));

        item.getChildren().addAll(thumb, info, spacer, price);
        return item;
    }

    // ===== PHẦN 37: BIỂU ĐỒ ĐƯỜNG DOANH THU =====
    private LineChart<String, Number> createRevenueChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setTickLabelFill(Color.web("#c6d5ec"));
        yAxis.setTickLabelFill(Color.web("#c6d5ec"));

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.setPrefHeight(290);
        chart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        // Load real revenue data from DB
        List<Object[]> revenueData = productController.getMonthlyRevenue();
        if (revenueData != null && !revenueData.isEmpty()) {
            for (Object[] row : revenueData) {
                String month = String.valueOf(row[0]);
                Number amount = (Number) row[1];
                series.getData().add(new XYChart.Data<>(month, amount));
            }
        } else {
            // Fallback: no revenue data yet
            series.getData().add(new XYChart.Data<>("No data", 0));
        }
        chart.getData().add(series);

        return chart;
    }

    // ===== PHẦN 38: BIỂU ĐỒ CỘT TỒN KHO THEO THỂ LOẠI =====
    private BarChart<String, Number> createBarChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setTickLabelFill(Color.web("#c6d5ec"));
        yAxis.setTickLabelFill(Color.web("#c6d5ec"));

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setPrefHeight(290);
        chart.setStyle("-fx-background-color: transparent;");

        // Compute stock per genre from the loaded productList
        // A row's category can be "Action, RPG" (multi-genre) — split each one
        java.util.Map<String, Integer> stockByGenre = new java.util.LinkedHashMap<>();
        for (ProductRow row : productList) {
            if (row.getCategory() == null || row.getCategory().isBlank()) continue;
            for (String genre : row.getCategory().split(",")) {
                String g = genre.trim();
                if (!g.isEmpty()) stockByGenre.merge(g, row.getStock(), Integer::sum);
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        if (stockByGenre.isEmpty()) {
            series.getData().add(new XYChart.Data<>("No data", 0));
        } else {
            stockByGenre.forEach((genre, stock) ->
                series.getData().add(new XYChart.Data<>(genre, stock)));
        }
        chart.getData().add(series);

        return chart;
    }

    // ===== PHẦN 39: BIỂU ĐỒ DOANH THU THEO THỜI GIAN =====
    private LineChart<String, Number> createRevenuePeriodChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setTickLabelFill(Color.web("#c6d5ec"));
        yAxis.setTickLabelFill(Color.web("#c6d5ec"));

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.setPrefHeight(290);
        chart.setStyle("-fx-background-color: transparent;");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().add(new XYChart.Data<>("03/01", 8));
        series.getData().add(new XYChart.Data<>("03/05", 12));
        series.getData().add(new XYChart.Data<>("03/10", 15));
        series.getData().add(new XYChart.Data<>("03/15", 11));
        series.getData().add(new XYChart.Data<>("03/20", 18));
        series.getData().add(new XYChart.Data<>("03/25", 21));
        chart.getData().add(series);

        return chart;
    }

    // ===== PHẦN 40: BIỂU ĐỒ HÀNH VI NGƯỜI DÙNG =====
    private StackPane createUserBehaviorChart() {
        PieChart pie = new PieChart(FXCollections.observableArrayList(
                new PieChart.Data("Product Views", 42),
                new PieChart.Data("Add to Cart", 26),
                new PieChart.Data("Purchases", 18),
                new PieChart.Data("Logins", 14)
        ));
        pie.setLegendVisible(false);
        pie.setLabelsVisible(true);
        pie.setPrefHeight(290);
        pie.setStyle("-fx-background-color: transparent;");

        Region center = new Region();
        center.setPrefSize(110, 110);
        center.setMaxSize(110, 110);
        center.setStyle("-fx-background-color: rgba(15,23,42,0.95); -fx-background-radius: 999;");

        StackPane stack = new StackPane(pie, center);
        stack.setPrefHeight(290);
        return stack;
    }

    // ===== PHẦN 41: HỘP THÔNG TIN BÁO CÁO =====
    private VBox createReportInfoBox(String titleText, String contentText) {
        VBox box = new VBox(8);
        box.setPadding(new Insets(14));
        box.setStyle(
                "-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 18; -fx-border-radius: 18;" +
                "-fx-border-color: rgba(255,255,255,0.06);"
        );

        Label title = new Label(titleText);
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(FONT_UI, FontWeight.BOLD, 14));

        Label content = new Label(contentText);
        content.setTextFill(Color.web("#a9bfdc"));
        content.setWrapText(true);
        content.setFont(Font.font(FONT_UI, 12));

        box.getChildren().addAll(title, content);
        return box;
    }

    // ===== PHẦN 42: DÒNG BÁO CÁO =====
    private HBox createReportRow(String nameText, String noteText, String valueText) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12, 14, 12, 14));
        row.setStyle(
                "-fx-background-color: rgba(255,255,255,0.04); -fx-background-radius: 18; -fx-border-radius: 18;" +
                "-fx-border-color: rgba(255,255,255,0.06);"
        );

        VBox info = new VBox(4);
        Label name = new Label(nameText);
        name.setTextFill(Color.WHITE);
        name.setFont(Font.font(FONT_UI, FontWeight.BOLD, 15));

        Label note = new Label(noteText);
        note.setTextFill(Color.web("#9bb2d5"));
        note.setFont(Font.font(FONT_UI, 12));
        info.getChildren().addAll(name, note);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label value = new Label(valueText);
        value.setTextFill(Color.WHITE);
        value.setFont(Font.font(FONT_UI, FontWeight.BOLD, 14));

        row.getChildren().addAll(info, spacer, value);
        return row;
    }

    // ===== PHẦN 43: BIỂU ĐỒ TRÒN TRẠNG THÁI SẢN PHẨM =====
    private PieChart createPieChart() {
        PieChart chart = new PieChart();
        chart.setLegendVisible(false);
        chart.setLabelsVisible(true);
        chart.setPrefHeight(280);
        chart.setStyle("-fx-background-color: transparent;");

        int inStock = 0;
        int lowStock = 0;
        int outOfStock = 0;

        for (ProductRow row : productList) {
            switch (row.getStatus()) {
                case "In Stock":
                    inStock++;
                    break;
                case "Low Stock":
                    lowStock++;
                    break;
                case "Out of Stock":
                    outOfStock++;
                    break;
            }
        }

        chart.setData(FXCollections.observableArrayList(
                new PieChart.Data("In Stock", inStock),
                new PieChart.Data("Low Stock", lowStock),
                new PieChart.Data("Out of Stock", outOfStock)
        ));

        return chart;
    }

    // ===== PHẦN 44: PANEL PHỤ TRONG SIDEBAR =====
    private VBox createSidebarPanel(String titleText, Region... tags) {
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(16));
        panel.setStyle(glassPanelStyle("18"));

        Label title = new Label(titleText);
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(FONT_UI, FontWeight.BOLD, 13));

        FlowPane flow = new FlowPane();
        flow.setHgap(8);
        flow.setVgap(8);
        flow.getChildren().addAll(tags);

        panel.getChildren().addAll(title, flow);
        return panel;
    }

    // ===== PHẦN 45: TAG NHỎ =====
    private Label createMiniTag(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font(FONT_UI, 11));
        label.setStyle(
                "-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 999; -fx-padding: 6 10;" +
                "-fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 999;"
        );
        return label;
    }

    // ===== PHẦN 46: THANH NHẤN MÀU =====
    private Region createAccentLine() {
        Region line = new Region();
        line.setPrefSize(54, 4);
        line.setStyle("-fx-background-color: linear-gradient(to right, #60a5fa, #22d3ee); -fx-background-radius: 999;");
        return line;
    }

    // ===== PHẦN 47: TITLE CHUNG =====
    private Label createSectionTitle(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.WHITE);
        label.setFont(Font.font(FONT_UI, FontWeight.EXTRA_BOLD, 20));
        return label;
    }

    // ===== PHẦN 48: SUBTITLE CHUNG =====
    private Label createSubTitle(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web("#9db2d4"));
        label.setWrapText(true);
        label.setFont(Font.font(FONT_UI, 12));
        return label;
    }

    // ===== PHẦN 49: TẠO NÚT MENU =====
    private Button createMenuButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPrefHeight(44);
        button.setStyle(menuButtonStyle(false));
        button.setFont(Font.font(FONT_UI, FontWeight.SEMI_BOLD, 14));
        menuButtons.add(button);
        return button;
    }

    // ===== PHẦN 50: CHUYỂN TRANG =====
    // Dùng khi bấm menu sidebar
    private void switchPage(Button button, VBox view) {
        if (button != null) {
            setActiveButton(button);
        }
        contentArea = view;
        root.setCenter(contentArea);
    }

    // ===== PHẦN 51: ĐÁNH DẤU NÚT MENU ĐANG ACTIVE =====
    private void setActiveButton(Button target) {
        activeButton = target;
        for (Button button : menuButtons) {
            button.setStyle(menuButtonStyle(button == target));
        }
    }

    // ===== PHẦN 52: TÌM NÚT MENU THEO TÊN =====
    private Button findButtonByText(String text) {
        for (Button button : menuButtons) {
            if (button.getText().equals(text)) return button;
        }
        return null;
    }

    // ===== PHẦN 53: ĐỔ DỮ LIỆU TỪ BẢNG LÊN FORM =====
    // Khi click 1 dòng trong bảng, dữ liệu sẽ hiện lên form
    private void fillForm(ProductRow row) {
        if (row == null) return;
        txtName.setText(row.getName());
        selectedCategories.clear();
        if (row.getCategory() != null && !row.getCategory().isEmpty()) {
            for (String cat : row.getCategory().split(", ")) selectedCategories.add(cat.trim());
        }
        refreshCategoryChips();
        txtPrice.setText(String.valueOf(row.getPrice()));
        txtStock.setText(String.valueOf(row.getStock()));
        cboStatus.setValue(row.getStatus());
        txtDesc.setText(row.getTagline());
    }

    // ===== PHẦN 54: THÊM SẢN PHẨM =====
    // Chức năng CRUD - CREATE: View → ProductController → AddGame_Service → DB
    private void addProduct() {
        if (!validateForm()) return;

        String     title   = txtName.getText().trim();
        String     desc    = txtDesc.getText().trim();
        BigDecimal price   = new BigDecimal(txtPrice.getText().trim());
        int        stock   = Integer.parseInt(txtStock.getText().trim());
        String     imgUrl  = "/images/LOGO.png"; // default; extend form to collect from user
        List<String> categories = new ArrayList<>(selectedCategories);

        // Run DB write on a background thread so the UI stays responsive
        Thread t = new Thread(() -> {
            Game saved = productController.addGame(
                    title, desc, price,
                    18, LocalDate.now(),   // default ageCap — extend the form to collect this
                    stock, imgUrl, loggedInUsername
            );
            if (saved != null && !categories.isEmpty()) {
                productController.setGenresForGame(saved.getGameId(), categories);
                Game withGenres = productController.getGameById(saved.getGameId());
                if (withGenres != null) {
                    final Game finalSaved = withGenres;
                    Platform.runLater(() -> {
                        ProductRow row = gameToRow(finalSaved);
                        productList.add(row);
                        if (productTable != null) productTable.refresh();
                        refreshDynamicSections();
                        clearForm();
                        showInfo("Added successfully!");
                    });
                    return;
                }
            }
            final Game finalSaved = saved;
            Platform.runLater(() -> {
                if (finalSaved == null) {
                    showWarning("Failed to save game to database. Check the console for details.");
                    return;
                }
                ProductRow row = gameToRow(finalSaved);
                productList.add(row);
                if (productTable != null) productTable.refresh();
                refreshDynamicSections();
                clearForm();
                showInfo("Added successfully!");
            });
        });
        t.setDaemon(true);
        t.start();
    }

    // ===== PHẦN 55: CẬP NHẬT SẢN PHẨM =====
    // Chức năng CRUD - UPDATE: View → ProductController → EditGame_Service → DB
    private void updateProduct() {
        ProductRow selected = productTable == null ? null
                : productTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showWarning("Please select a product from the table before updating.");
            return;
        }
        if (!validateForm()) return;
        if (selected.getGameId() < 0) {
            showWarning("This row has no database ID. Reload the product list and try again.");
            return;
        }

        int        gameId = selected.getGameId();
        String     title  = txtName.getText().trim();
        String     desc   = txtDesc.getText().trim();
        BigDecimal price  = new BigDecimal(txtPrice.getText().trim());
        int        stock  = Integer.parseInt(txtStock.getText().trim());

        // Optimistic update: reflect changes in the UI immediately
        selected.setName(title);
        selected.setCategory(String.join(", ", selectedCategories));
        selected.setPrice(Integer.parseInt(txtPrice.getText().trim()));
        selected.setStock(stock);
        selected.setStatus(cboStatus.getValue());
        if (!desc.isEmpty()) selected.setTagline(desc);
        if (productTable != null) productTable.refresh();
        refreshDynamicSections();

        final List<String> categories = new ArrayList<>(selectedCategories);

        // Persist to DB in background
        Thread t = new Thread(() -> {
            boolean ok = productController.updateGame(
                    gameId, title, desc, price,
                    18, LocalDate.now(), stock, selected.getImagePath());
            if (ok && !categories.isEmpty()) {
                productController.setGenresForGame(gameId, categories);
            }
            Platform.runLater(() -> {
                if (ok) showInfo("Product updated successfully.");
                else    showWarning("UI updated but failed to persist to database.");
            });
        });
        t.setDaemon(true);
        t.start();
    }

    // ===== PHẦN 56: XOÁ SẢN PHẨM =====
    // Chức năng CRUD - DELETE: View → ProductController → GameCrudService → DB
    private void deleteProduct() {
        if (productTable == null) {
            showWarning("Product table is not ready.");
            return;
        }
        ProductRow selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarning("Choose Product!");
            return;
        }

        // Remove from the UI immediately for a snappy feel
        int gameId = selected.getGameId();
        productList.remove(selected);
        productTable.refresh();
        refreshDynamicSections();
        clearForm();

        if (gameId >= 0) {
            // Persist deletion in background
            Thread t = new Thread(() -> {
                boolean ok = productController.deleteGame(gameId);
                Platform.runLater(() -> {
                    if (ok) showInfo("Product Deleted!");
                    else    showWarning("Removed from view but could not delete from database.");
                });
            });
            t.setDaemon(true);
            t.start();
        } else {
            showInfo("Product Deleted!");
        }
    }

    // ===== PHẦN 57: KIỂM TRA DỮ LIỆU FORM =====
    // Kiểm tra dữ liệu có hợp lệ trước khi thêm / sửa
    private boolean validateForm() {
        if (txtName.getText().trim().isEmpty() ||
            selectedCategories.isEmpty() ||
            txtPrice.getText().trim().isEmpty() ||
            txtStock.getText().trim().isEmpty()) {
            showWarning("Please fill in all required fields.");
            return false;
        }

        try {
            Integer.parseInt(txtPrice.getText().trim());
            Integer.parseInt(txtStock.getText().trim());
        } catch (Exception e) {
            showWarning("Price and stock must be numeric values.");
            return false;
        }

        return true;
    }

    // ===== PHẦN 58: RESET FORM =====
    // Xoá dữ liệu đang nhập trên form
    private void clearForm() {
        if (txtName != null) txtName.clear();
        if (txtName != null) txtName.clear();
        selectedCategories.clear();
        refreshCategoryChips();
        if (categoryDropdown != null) categoryDropdown.setVisible(false);
        if (txtPrice != null) txtPrice.clear();
        if (txtStock != null) txtStock.clear();
        if (cboStatus != null) cboStatus.setValue(null);
        if (txtDesc != null) txtDesc.clear();
        if (productTable != null) productTable.getSelectionModel().clearSelection();
    }

    // ===== PHẦN 59: REFRESH GIAO DIỆN ĐỘNG =====
    // Sau khi thêm / sửa / xoá thì cập nhật các vùng hiển thị
    private void refreshDynamicSections() {
        if (featuredFlow != null) {
            refreshFeaturedStrip();
        }

        // Default to Dashboard when no button has been clicked yet (initial load)
        String page = activeButton != null ? activeButton.getText() : "Dashboard";
        if ("Dashboard".equals(page)) {
            root.setCenter(createDashboardView());
        } else if ("Library".equals(page)) {
            root.setCenter(createLibraryView());
        }
    }

    // ===== PHẦN 60: REFRESH DẢI FEATURED GAME =====
    private void refreshFeaturedStrip() {
        featuredFlow.getChildren().clear();
        for (ProductRow row : productList) {
            featuredFlow.getChildren().add(createLibraryCardMini(row));
        }
    }

    // ====================================================================
    //  DATABASE ↔ UI BRIDGE  (MVC glue — called by all CRUD operations)
    // ====================================================================

    /**
     * Load all games from the DB on a background thread and populate
     * productList so every view (Table, Library, Dashboard) refreshes.
     */
    private void loadProductsFromDB() {
        Thread t = new Thread(() -> {
            List<Game> games = productController.getAllGames();
            Platform.runLater(() -> {
                productList.clear();
                for (Game g : games) productList.add(gameToRow(g));
                refreshDynamicSections();
            });
        });
        t.setDaemon(true);
        t.start();
    }

    /**
     * Convert a Hibernate Game entity → a UI ProductRow.
     * Picks the first genre as the "category" displayed in the table.
     */
    private ProductRow gameToRow(Game g) {
        // Derive display category from genres
        String category = "Unknown";
        if (g.getGenres() != null && !g.getGenres().isEmpty()) {
            category = g.getGenres().stream()
                    .map(gr -> gr.getGenreName())
                    .collect(java.util.stream.Collectors.joining(", "));
        }
        // Derive stock status
        int    stock  = g.getStock();
        String status = stock >= 10 ? "In Stock" : stock > 0 ? "Low Stock" : "Out of Stock";
        // Shorten description to a card-friendly tagline
        String desc   = g.getDescription() != null ? g.getDescription() : "";
        String tagline = desc.length() > 60 ? desc.substring(0, 57) + "..." : desc;
        String imgPath = g.getImgUrl() != null && !g.getImgUrl().isBlank()
                         ? g.getImgUrl() : "/images/LOGO.png";

        ProductRow row = new ProductRow(
                String.valueOf(g.getGameId()),
                g.getTitle(),
                category,
                g.getPrice().intValue(),
                stock,
                status,
                tagline,
                "#38bdf8",  // default accent; extend to pick per genre
                "#0f172a",  // default shade
                imgPath
        );
        // Link the row to its DB record so updates/deletes know the PK
        row.setGameId(g.getGameId());
        return row;
    }

    /**
     * Filter the product list by title keyword (calls ProductController → Search service).
     * Results replace whatever is currently shown in the table.
     */
    private void filterProductList(String keyword) {
        Thread t = new Thread(() -> {
            List<Game> results = productController.searchByTitle(keyword);
            Platform.runLater(() -> {
                productList.clear();
                for (Game g : results) productList.add(gameToRow(g));
                if (productTable != null) productTable.refresh();
            });
        });
        t.setDaemon(true);
        t.start();
    }

    /**
     * Image loader that works for both classpath resources ("/images/foo.png")
     * and external URLs ("https://...").  Falls back to the project logo on error.
     */
    private Image loadImage(String path) {
        if (path == null || path.isBlank()) return fallbackImage();
        // Classpath resource
        if (path.startsWith("/")) {
            try {
                var stream = getClass().getResourceAsStream(path);
                if (stream != null) return new Image(stream);
            } catch (Exception ignored) {}
        }
        // External URL (stored in DB)
        try {
            return new Image(path, true); // background loading
        } catch (Exception ignored) {}
        return fallbackImage();
    }

    /** Fallback image when a cover cannot be loaded. */
    private Image fallbackImage() {
        try {
            var stream = getClass().getResourceAsStream("/images/LOGO.png");
            if (stream != null) return new Image(stream);
        } catch (Exception ignored) {}
        return new Image("data:image/png;base64,"); // empty transparent placeholder
    }

    // ===== PHẦN 61: CARD NHỎ GAME =====
    private VBox createLibraryCardMini(ProductRow row) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(12));
        card.setPrefSize(190, 150);
        card.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " + row.getAccent() + "44, " + row.getShade() + ");" +
                "-fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: rgba(255,255,255,0.08);"
        );

        ImageView cover = new ImageView(loadImage(row.getImagePath()));
        cover.setFitWidth(166);
        cover.setFitHeight(72);
        cover.setPreserveRatio(false);
        cover.setSmooth(true);

        Rectangle clip = new Rectangle(166, 72);
        clip.setArcWidth(18);
        clip.setArcHeight(18);
        cover.setClip(clip);

        Label name = new Label(row.getName());
        name.setTextFill(Color.WHITE);
        name.setFont(Font.font(FONT_UI, FontWeight.BOLD, 15));

        Label meta = new Label(row.getCategory() + " · " + row.getStatus());
        meta.setTextFill(Color.web("#d7e3f6"));
        meta.setFont(Font.font(FONT_UI, 12));

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label price = new Label(row.getPriceFormatted());
        price.setTextFill(Color.WHITE);
        price.setFont(Font.font(FONT_UI, FontWeight.BOLD, 13));

        card.getChildren().addAll(cover, name, meta, spacer, price);
        return card;
    }

    // ===== PHẦN 62: HÀM THỐNG KÊ - ĐẾM GAME SẮP HẾT =====
    private int countLowStock() {
        int count = 0;
        for (ProductRow row : productList) {
            if (row.getStock() < 10) count++;
        }
        return count;
    }

    // ===== PHẦN 63: HÀM THỐNG KÊ - ĐẾM SỐ THỂ LOẠI =====
    private int countCategories() {
        Set<String> set = new LinkedHashSet<>();
        for (ProductRow row : productList) {
            set.add(row.getCategory());
        }
        return set.size();
    }

    // ===== PHẦN 64: HÀM THỐNG KÊ - TÍNH TỔNG GIÁ TRỊ TỒN KHO =====
    private int calculateInventoryValue() {
        int total = 0;
        for (ProductRow row : productList) {
            total += row.getPrice() * row.getStock();
        }
        return total;
    }

    // ===== PHẦN 65: FORMAT TIỀN TỆ =====
    private String formatMoney(int value) {
        return String.format("%,dđ", value).replace(',', '.');
    }

    // ===== PHẦN 66: STYLE NÚT MENU =====
    private String menuButtonStyle(boolean active) {
        if (active) {
            return "-fx-background-color: linear-gradient(to right, rgba(59,130,246,0.35), rgba(34,211,238,0.18));" +
                   "-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: 700;" +
                   "-fx-background-radius: 14; -fx-border-radius: 14;" +
                   "-fx-border-color: rgba(255,255,255,0.08); -fx-padding: 0 0 0 16; -fx-cursor: hand;";
        }

        return "-fx-background-color: transparent; -fx-text-fill: #c7d5ea; -fx-font-size: 14px; -fx-font-weight: 600;" +
               "-fx-background-radius: 14; -fx-border-radius: 14; -fx-padding: 0 0 0 16; -fx-cursor: hand;";
    }

    // ===== PHẦN 67: STYLE PANEL KÍNH =====
    private String glassPanelStyle(String radius) {
        return "-fx-background-color: linear-gradient(to bottom right, rgba(15,23,42,0.82), rgba(17,24,39,0.65));" +
               "-fx-background-radius: " + radius + "; -fx-border-radius: " + radius + ";" +
               "-fx-border-color: rgba(255,255,255,0.08); -fx-border-width: 1;";
    }

    // ===== CATEGORY PICKER: MULTI-SELECT WITH + BUTTONS =====
    private VBox createCategoryPicker() {
        VBox wrapper = new VBox(0);

        // --- Chips area + toggle button ---
        categoryChips = new FlowPane(6, 6);
        categoryChips.setPadding(new Insets(0));

        Label placeholder = new Label("Category");
        placeholder.setTextFill(Color.web("#92a8cb"));
        placeholder.setFont(Font.font("Segoe UI", 13));

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 14, 10, 14));
        header.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 16;"
                + " -fx-border-radius: 16; -fx-border-color: rgba(255,255,255,0.08); -fx-cursor: hand;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label arrow = new Label("\u25BC");
        arrow.setTextFill(Color.web("#92a8cb"));
        arrow.setFont(Font.font(10));

        header.getChildren().addAll(categoryChips, placeholder, spacer, arrow);

        // --- Dropdown list ---
        categoryDropdown = new VBox(2);
        categoryDropdown.setPadding(new Insets(6));
        categoryDropdown.setStyle("-fx-background-color: rgba(15,23,42,0.95); -fx-background-radius: 12;"
                + " -fx-border-radius: 12; -fx-border-color: rgba(255,255,255,0.1);");
        categoryDropdown.setVisible(false);
        categoryDropdown.setManaged(false);

        rebuildCategoryDropdown();

        // Toggle dropdown on click
        header.setOnMouseClicked(e -> {
            boolean show = !categoryDropdown.isVisible();
            categoryDropdown.setVisible(show);
            categoryDropdown.setManaged(show);
        });

        wrapper.getChildren().addAll(header, categoryDropdown);
        refreshCategoryChips();
        return wrapper;
    }

    private void rebuildCategoryDropdown() {
        categoryDropdown.getChildren().clear();
        for (String cat : allCategories) {
            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(6, 10, 6, 10));
            row.setStyle("-fx-background-radius: 8; -fx-cursor: hand;");
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 8; -fx-cursor: hand;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-radius: 8; -fx-cursor: hand;"));

            Label plus = new Label(selectedCategories.contains(cat) ? "\u2713" : "+");
            plus.setTextFill(selectedCategories.contains(cat) ? Color.web("#22d3ee") : Color.web("#60a5fa"));
            plus.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            plus.setMinWidth(18);
            plus.setAlignment(Pos.CENTER);

            Label name = new Label(cat);
            name.setTextFill(Color.WHITE);
            name.setFont(Font.font("Segoe UI", 13));

            row.getChildren().addAll(plus, name);
            row.setOnMouseClicked(e -> {
                if (selectedCategories.contains(cat)) {
                    selectedCategories.remove(cat);
                } else {
                    selectedCategories.add(cat);
                }
                refreshCategoryChips();
                rebuildCategoryDropdown();
            });
            categoryDropdown.getChildren().add(row);
        }

        // --- "Add New Category" button at the bottom ---
        HBox addRow = new HBox(8);
        addRow.setAlignment(Pos.CENTER_LEFT);
        addRow.setPadding(new Insets(8, 10, 4, 10));
        addRow.setStyle("-fx-border-color: rgba(255,255,255,0.06); -fx-border-width: 1 0 0 0; -fx-cursor: hand;");
        addRow.setOnMouseEntered(e -> addRow.setStyle("-fx-background-color: rgba(255,255,255,0.08);"
                + " -fx-border-color: rgba(255,255,255,0.06); -fx-border-width: 1 0 0 0; -fx-cursor: hand;"));
        addRow.setOnMouseExited(e -> addRow.setStyle("-fx-border-color: rgba(255,255,255,0.06); -fx-border-width: 1 0 0 0; -fx-cursor: hand;"));

        Label plusIcon = new Label("+");
        plusIcon.setTextFill(Color.web("#22d3ee"));
        plusIcon.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        Label addLabel = new Label("Add New Category");
        addLabel.setTextFill(Color.web("#22d3ee"));
        addLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

        addRow.getChildren().addAll(plusIcon, addLabel);
        addRow.setOnMouseClicked(e -> showAddCategoryDialog());
        categoryDropdown.getChildren().add(addRow);
    }

    private void showAddCategoryDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("New Category");
        dialog.setHeaderText("Enter a new category name:");
        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);
        TextField input = new TextField();
        input.setPromptText("Category name");
        dialog.getDialogPane().setContent(input);
        dialog.setResultConverter(bt -> bt == addBtn ? input.getText().trim() : null);
        dialog.showAndWait().ifPresent(name -> {
            if (!name.isEmpty() && !allCategories.contains(name)) {
                allCategories.add(name);
                selectedCategories.add(name);
                refreshCategoryChips();
                rebuildCategoryDropdown();
            }
        });
    }

    private void refreshCategoryChips() {
        categoryChips.getChildren().clear();
        for (String cat : selectedCategories) {
            HBox chip = new HBox(4);
            chip.setAlignment(Pos.CENTER);
            chip.setPadding(new Insets(3, 10, 3, 10));
            chip.setStyle("-fx-background-color: rgba(96,165,250,0.18); -fx-background-radius: 12;"
                    + " -fx-border-radius: 12; -fx-border-color: rgba(96,165,250,0.3);");

            Label text = new Label(cat);
            text.setTextFill(Color.web("#93c5fd"));
            text.setFont(Font.font("Segoe UI", 11));

            Label remove = new Label("\u2715");
            remove.setTextFill(Color.web("#f87171"));
            remove.setFont(Font.font(10));
            remove.setStyle("-fx-cursor: hand;");
            remove.setOnMouseClicked(e -> {
                selectedCategories.remove(cat);
                refreshCategoryChips();
                rebuildCategoryDropdown();
            });

            chip.getChildren().addAll(text, remove);
            categoryChips.getChildren().add(chip);
        }
        // Show/hide placeholder
        if (categoryPickerBox != null) {
            HBox header = (HBox) categoryPickerBox.getChildren().get(0);
            Label placeholderLabel = null;
            for (javafx.scene.Node node : header.getChildren()) {
                if (node instanceof Label && "Category".equals(((Label) node).getText())) {
                    placeholderLabel = (Label) node;
                    break;
                }
            }
            if (placeholderLabel != null) {
                placeholderLabel.setVisible(selectedCategories.isEmpty());
                placeholderLabel.setManaged(selectedCategories.isEmpty());
            }
        }
    }

    // ===== PHẦN 68: STYLE INPUT =====
    private String inputStyle() {
        return "-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: white; -fx-prompt-text-fill: #92a8cb;" +
               "-fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: rgba(255,255,255,0.08);" +
               "-fx-padding: 12 14;";
    }

    // ===== PHẦN 69: STYLE COMBOBOX =====
    private String comboStyle() {
        return "-fx-background-color: rgba(255,255,255,0.05); -fx-text-fill: white; -fx-prompt-text-fill: #92a8cb;" +
               "-fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: rgba(255,255,255,0.08);";
    }

    // ===== HELPER: STYLE COMBOBOX TEXT WHITE =====
    private void styleComboBox(ComboBox<String> combo) {
        combo.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setTextFill(Color.WHITE);
                setStyle("-fx-background-color: transparent;");
            }
        });
        combo.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                setTextFill(Color.WHITE);
                setStyle("-fx-background-color: rgba(15,23,42,0.95);");
            }
        });
    }

    // ===== PHẦN 70: STYLE TEXTAREA =====
    private String textAreaStyle() {
        return "-fx-control-inner-background: rgba(255,255,255,0.05); -fx-text-fill: white; -fx-prompt-text-fill: #92a8cb;" +
               "-fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: rgba(255,255,255,0.08);" +
               "-fx-padding: 8;";
    }

    // ===== PHẦN 71: STYLE NÚT CHÍNH =====
    private String primaryButtonStyle() {
        return "-fx-background-color: linear-gradient(to right, #60a5fa, #22d3ee); -fx-text-fill: #07111d;" +
               "-fx-font-weight: bold; -fx-background-radius: 16; -fx-padding: 11 18; -fx-cursor: hand;";
    }

    // ===== PHẦN 72: STYLE NÚT PHỤ =====
    private String secondaryButtonStyle() {
        return "-fx-background-color: rgba(255,255,255,0.07); -fx-text-fill: white; -fx-font-weight: bold;" +
               "-fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: rgba(255,255,255,0.08);" +
               "-fx-padding: 11 18; -fx-cursor: hand;";
    }

    // ===== PHẦN 73: STYLE NÚT GHOST =====
    private String ghostButtonStyle() {
        return "-fx-background-color: transparent; -fx-text-fill: #c6d5ec; -fx-font-weight: bold;" +
               "-fx-background-radius: 16; -fx-border-radius: 16; -fx-border-color: rgba(255,255,255,0.10);" +
               "-fx-padding: 11 18; -fx-cursor: hand;";
    }

    // ===== PHẦN 74: STYLE BẢNG =====
    private String tableStyle() {
        return "-fx-background-color: transparent; -fx-control-inner-background: rgba(255,255,255,0.03);" +
               "-fx-table-cell-border-color: rgba(255,255,255,0.06); -fx-padding: 10;" +
               "-fx-selection-bar: rgba(96,165,250,0.55); -fx-selection-bar-text: white; -fx-background-radius: 20;";
    }

    // ===== PHẦN 75: STYLE NHÃN TRẠNG THÁI =====
    private String statusChipStyle(String status) {
        String bg = "rgba(96,165,250,0.25)";
        if ("Low Stock".equals(status)) bg = "rgba(245,158,11,0.28)";
        if ("Out of Stock".equals(status)) bg = "rgba(239,68,68,0.28)";
        return "-fx-background-color: " + bg + "; -fx-background-radius: 999; -fx-padding: 6 10;";
    }

    // ===== PHẦN 76: THÔNG BÁO THÀNH CÔNG =====
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ===== PHẦN 77: THÔNG BÁO CẢNH BÁO =====
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ===== PHẦN 78: HÀM MAIN - CHẠY CHƯƠNG TRÌNH =====
    public static void main(String[] args) {
        launch(args);
    }

    // ===== PHẦN 79: CLASS MODEL DỮ LIỆU SẢN PHẨM =====
    // Lớp này đại diện cho 1 sản phẩm game trong hệ thống
    public static class ProductRow {
        private final SimpleStringProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty category;
        private final SimpleStringProperty stock;
        private final SimpleStringProperty status;
        private final SimpleStringProperty priceFormatted;

        // Giá trị thật của dữ liệu
        private String idValue;
        private String nameValue;
        private String categoryValue;
        private int priceValue;
        private int stockValue;
        private String statusValue;
        private String tagline;
        private final String accent;
        private final String shade;
        private final String imagePath;
        /** DB primary key — -1 when not yet linked to a database row */
        private int gameId = -1;

        // ===== CONSTRUCTOR KHỞI TẠO SẢN PHẨM =====
        public ProductRow(String id, String name, String category, int price, int stock,
                          String status, String tagline, String accent, String shade,
                          String imagePath) {
            this.idValue = id;
            this.nameValue = name;
            this.categoryValue = category;
            this.priceValue = price;
            this.stockValue = stock;
            this.statusValue = status;
            this.tagline = tagline;
            this.accent = accent;
            this.shade = shade;
            this.imagePath = imagePath;

            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.category = new SimpleStringProperty(category);
            this.stock = new SimpleStringProperty(String.valueOf(stock));
            this.status = new SimpleStringProperty(status);
            this.priceFormatted = new SimpleStringProperty(String.format("%,dđ", price).replace(',', '.'));
        }

        // ===== PROPERTY CHO TABLEVIEW =====
        public SimpleStringProperty idProperty() { return id; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleStringProperty categoryProperty() { return category; }
        public SimpleStringProperty stockProperty() { return stock; }
        public SimpleStringProperty statusProperty() { return status; }
        public SimpleStringProperty priceFormattedProperty() { return priceFormatted; }

        // ===== GETTER =====
        public String getId() { return idValue; }
        public String getName() { return nameValue; }
        public String getCategory() { return categoryValue; }
        public int getPrice() { return priceValue; }
        public int getStock() { return stockValue; }
        public String getStatus() { return statusValue; }
        public String getTagline() { return tagline; }
        public String getAccent() { return accent; }
        public String getShade() { return shade; }
        public String getImagePath() { return imagePath; }
        public String getPriceFormatted() { return String.format("%,dđ", priceValue).replace(',', '.'); }

        // ===== SETTER =====
        public void setId(String value) { idValue = value; id.set(value); }
        public void setName(String value) { nameValue = value; name.set(value); }
        public void setCategory(String value) { categoryValue = value; category.set(value); }
        public void setPrice(int value) { priceValue = value; priceFormatted.set(String.format("%,dđ", value).replace(',', '.')); }
        public void setStock(int value) {
            stockValue = value;
            stock.set(String.valueOf(value));
            String autoStatus = value <= 0 ? "Out of Stock" : value < 10 ? "Low Stock" : "In Stock";
            setStatus(autoStatus);
        }
        public void setStatus(String value) { statusValue = value; status.set(value); }
        public void setTagline(String value) { tagline = value; }
        /** Link this row to its database record. */
        public int  getGameId()              { return gameId; }
        public void setGameId(int gameId)    { this.gameId = gameId; }
    }
}