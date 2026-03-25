package com.hydrogame.controller;

import javafx.animation.*;
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
import java.time.LocalDate;
import java.util.ResourceBundle;
import com.hydrogame.user_service.RegisterService;

public class RegisterController implements Initializable {
    RegisterService R = new RegisterService();

    @FXML private TextField     usernameField;
    @FXML private TextField     emailField;
    @FXML private DatePicker    dobPicker;
    @FXML private PasswordField passwordField;
    @FXML private TextField     passwordVisible;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField     confirmPasswordVisible;
    @FXML private Label         errorLabel;
    @FXML private Button        registerButton;
    @FXML private ToggleButton  userToggle;
    @FXML private ToggleButton  adminToggle;
    @FXML private VBox          formContainer;
    @FXML private Button        eyeBtn;
    @FXML private Button        eyeBtnConfirm;

    private boolean passwordShown = false;
    private boolean confirmShown  = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());
        confirmPasswordVisible.textProperty().bindBidirectional(confirmPasswordField.textProperty());

        dobPicker.setPromptText("MM/DD/YYYY");
        dobPicker.getEditor().setPromptText("MM/DD/YYYY");

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
        passwordVisible.setVisible(passwordShown); passwordVisible.setManaged(passwordShown);
        passwordField.setVisible(!passwordShown);  passwordField.setManaged(!passwordShown);
        eyeBtn.setText(passwordShown ? "🙈" : "👁");
    }

    @FXML
    private void toggleConfirmVisibility() {
        confirmShown = !confirmShown;
        confirmPasswordVisible.setVisible(confirmShown); confirmPasswordVisible.setManaged(confirmShown);
        confirmPasswordField.setVisible(!confirmShown);  confirmPasswordField.setManaged(!confirmShown);
        eyeBtnConfirm.setText(confirmShown ? "🙈" : "👁");
    }

    @FXML
    private void handleRegister() {
        String    username = usernameField.getText().trim();
        String    email    = emailField.getText().trim();
        LocalDate dob      = dobPicker.getValue();
        String    password = passwordField.getText();
        String    confirm  = confirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || dob == null
                || password.isEmpty() || confirm.isEmpty()) {
            showError("Please fill in all required fields."); return;
        }
        if (username.length() < 4 || !username.matches("^[A-Za-z0-9_]+$")) {
            showError("Username: min 4 chars, letters/numbers/underscore only."); return;
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            showError("Invalid email address format."); return;
        }
        if (dob.isAfter(LocalDate.now().minusYears(6))) {
            showError("Please enter a valid date of birth."); return;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters."); return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match."); return;
        }
        boolean isAdmin = adminToggle.isSelected();
        R.Register(email, username, dob, password, isAdmin);
        
        ScaleTransition st = new ScaleTransition(Duration.millis(90), registerButton);
        st.setToX(0.95); st.setToY(0.95);
        st.setAutoReverse(true); st.setCycleCount(2);
        st.play();
    }

    @FXML
    private void goToLogin() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Scene scene = new Scene(root, 860, 620);
            Stage stage = (Stage) emailField.getScene().getWindow();
            FadeTransition out = new FadeTransition(Duration.millis(180), emailField.getScene().getRoot());
            out.setFromValue(1); out.setToValue(0);
            out.setOnFinished(e -> {
                stage.setScene(scene);
                stage.setTitle("HydroGames — Sign In");
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