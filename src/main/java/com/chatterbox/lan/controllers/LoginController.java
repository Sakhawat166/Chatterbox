package com.chatterbox.lan.controllers;

import com.chatterbox.lan.models.User;
import com.chatterbox.lan.network.Client;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private ComboBox<String> userComboBox;

    @FXML
    private Label errorLabel;

    @FXML
    public void initialize() {
        // When user selects from dropdown, populate the text field
        userComboBox.setOnAction(e -> {
            String selected = userComboBox.getValue();
            if (selected != null) {
                usernameField.setText(selected);
            }
        });
    }

    @FXML
    public void onLogin() {
        String username = usernameField.getText().trim();

        if (username.isEmpty()) {
            errorLabel.setText("Please enter a username");
            errorLabel.setVisible(true);
            return;
        }

        try {
            // Create client instance with username
            Client client = new Client();

            // Create current user
            User currentUser = new User(username, "/avatars/avatar1.png");
            currentUser.setMe(true);

            // Login to server
            client.login(username);

            // Load chat page
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/chatterbox/lan/ChatPage-view.fxml"));
            Parent root = loader.load();

            // Get controller and set current user
            ChatPageController chatController = loader.getController();
            chatController.setCurrentUser(currentUser, client);

            // Switch to chat scene
            Stage stage = (Stage) usernameField.getScene().getWindow();
            Scene scene = new Scene(root, 800, 600);
            stage.setScene(scene);
            stage.setTitle("Chatterbox - " + username);
        } catch (IOException e) {
            errorLabel.setText("Failed to load chat page: " + e.getMessage());
            errorLabel.setVisible(true);
            e.printStackTrace();
        } catch (Exception e) {
            errorLabel.setText("Connection failed: " + e.getMessage());
            errorLabel.setVisible(true);
            e.printStackTrace();
        }
    }
}