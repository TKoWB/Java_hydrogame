package com.hydrogame.main;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import com.hydrogame.database.*;
import com.hydrogame.feature_admin.*;
import com.hydrogame.feature_all.*;
import com.hydrogame.hibernate_util.HibernateUtil;
import com.hydrogame.security_service.*;
import com.hydrogame.user_service.*;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.Month;
/**
 * JavaFX App
 * public class App extends Application {

    @Override
    public void start(Stage stage) {
        var javaVersion = SystemInfo.javaVersion();
        var javafxVersion = SystemInfo.javafxVersion();

        var label = new Label("Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".");
        var scene = new Scene(new StackPane(label), 640, 480);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}
 */

public class App {

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        RegisterService R = new RegisterService();
        LoginService L = new LoginService();
        AddGame_Service Add = new AddGame_Service();
        EditGame_Service E = new EditGame_Service();
        
        E.EditGame(1, "Teaching Feeling", "Just An Healing H-game", BigDecimal.valueOf(512999.99), 18, LocalDate.of(2015, Month.OCTOBER, 21), 0, "https://img.dlsite.jp/modpub/images2/work/doujin/RJ163000/RJ162718_img_main.webp");
    }
}