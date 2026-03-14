package com.hydrogame.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        try {
            // Khởi tạo scene với file primary.fxml
            scene = new Scene(loadFXML("primary"), 640, 480);
            stage.setTitle("HydroGame Store");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace(); // In lỗi chi tiết ra console để debug
        }
    }

    public static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        // ĐƯỜNG DẪN CHUẨN: Dùng dấu "/" và nối đúng biến fxml
        String path = "/com/hydrogame/" + fxml + ".fxml";
        URL fxmlLocation = App.class.getResource(path);

        if (fxmlLocation == null) {
            System.err.println(">>> LỖI: Không tìm thấy file: " + path);
            System.err.println(">>> Kiểm tra lại thư mục: src/main/resources/com/hydrogame/main/");
            throw new IOException("Location not found: " + path);
        }

        FXMLLoader fxmlLoader = new FXMLLoader(fxmlLocation);
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}