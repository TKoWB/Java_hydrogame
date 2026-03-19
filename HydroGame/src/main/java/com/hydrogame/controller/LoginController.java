package com.hydrogame.controller;

import java.util.List;
import java.util.ArrayList;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.net.URL;
import java.util.ResourceBundle;
import com.hydrogame.main.ConsoleMenu;
import com.hydrogame.user_service.LoginService;

public class LoginController implements Initializable {
    LoginService L = new LoginService();
    List<Object> list = new ArrayList<>();

    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField     passwordVisible;
    @FXML private CheckBox      rememberMeCheck;
    @FXML private Label         errorLabel;
    @FXML private Button        loginButton;
    @FXML private ToggleButton  userToggle;
    @FXML private ToggleButton  adminToggle;
    @FXML private VBox          formContainer;
    @FXML private Button        eyeBtn;

    private boolean passwordShown = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());

        userToggle.selectedProperty().addListener((obs, was, isNow) -> {
            if (isNow) adminToggle.setSelected(false);
            else if (!adminToggle.isSelected()) userToggle.setSelected(true);
        });
        adminToggle.selectedProperty().addListener((obs, was, isNow) -> {
            if (isNow) userToggle.setSelected(false);
            else if (!userToggle.isSelected()) adminToggle.setSelected(true);
        });

        animateEntry(formContainer);
    }

    @FXML
    private void togglePasswordVisibility() {
        passwordShown = !passwordShown;
        passwordVisible.setVisible(passwordShown);
        passwordVisible.setManaged(passwordShown);
        passwordField.setVisible(!passwordShown);
        passwordField.setManaged(!passwordShown);
        eyeBtn.setText(passwordShown ? "🙈" : "👁");
    }

    @FXML
    private void handleLogin() {
        String email    = emailField.getText().trim();
        String password = passwordField.getText();

                    
//            if (email.isEmpty() || password.isEmpty()) {
//                showError("Please fill in all required fields."); return;
//            }
//            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
//                showError("Invalid email address format."); return;
//            }   
//            if (password.length() < 6) {
//                showError("Password must be at least 6 characters."); return;
//            }

        
        list = L.Login(email, password);
        if(!L.getCheck()){
            showError("Incorrect login information");
        } else {
            // Login successful — close JavaFX and launch console menu
            List<Object> userData = new ArrayList<>(list);
            boolean isAdmin = adminToggle.isSelected();
            new Thread(() -> new ConsoleMenu(userData, isAdmin).start()).start();
            Platform.exit();
            return;
        }
        
        ScaleTransition st = new ScaleTransition(Duration.millis(90), loginButton);
        st.setToX(0.95); st.setToY(0.95);
        st.setAutoReverse(true); st.setCycleCount(2);
        st.play();
    }

    @FXML
    private void goToRegister() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Register.fxml"));
            Scene scene = new Scene(root, 860, 620);
            Stage stage = (Stage) emailField.getScene().getWindow();
            FadeTransition out = new FadeTransition(Duration.millis(180), emailField.getScene().getRoot());
            out.setFromValue(1); out.setToValue(0);
            out.setOnFinished(e -> {
                stage.setScene(scene);
                stage.setTitle("HydroGames — Create Account");
            });
            out.play();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.getStyleClass().setAll("message-label", "msg-error");
        errorLabel.setVisible(true);
        PauseTransition pt = new PauseTransition(Duration.seconds(3.5));
        pt.setOnFinished(e -> errorLabel.setVisible(false));
        pt.play();
    }

    private void animateEntry(javafx.scene.Node node) {
        node.setOpacity(0); node.setTranslateY(20);
        FadeTransition ft = new FadeTransition(Duration.millis(480), node);
        ft.setFromValue(0); ft.setToValue(1);
        TranslateTransition tt = new TranslateTransition(Duration.millis(480), node);
        tt.setFromY(20); tt.setToY(0);
        new ParallelTransition(ft, tt).play();
    }
}