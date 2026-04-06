package com.chatterbox.lan.controllers;

import com.chatterbox.lan.models.Message;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class MessageCell extends ListCell<Message> {
    private final BooleanSupplier showSenderNames;
    private final Consumer<Message> onUnsend;

    public MessageCell(BooleanSupplier showSenderNames, Consumer<Message> onUnsend) {
        this.showSenderNames = showSenderNames;
        this.onUnsend = onUnsend;
    }

    @Override
    protected void updateItem(Message message, boolean empty) {
        super.updateItem(message, empty);

        if (empty || message == null) {
            setGraphic(null);
            setText(null);
            return;
        }

        boolean isMine = message.getSender() != null && message.getSender().isMe();

        HBox root = new HBox(8);
        root.setMaxWidth(Region.USE_PREF_SIZE);

        VBox messageBox = new VBox(4);
        messageBox.getStyleClass().add("message-box");

        if (showSenderNames.getAsBoolean() && message.getSender() != null) {
            Label usernameLabel = new Label(message.getSender().getUsername());
            usernameLabel.getStyleClass().add("username-label");
            messageBox.getChildren().add(usernameLabel);
        }

        if (message.isDeleted()) {
            String deletedText = "Message Unsent";
            Label deleted = new Label(deletedText);
            deleted.getStyleClass().add("message-deleted-text");
            messageBox.getChildren().add(deleted);
            messageBox.getStyleClass().add("message-deleted-box");
        } else if (message.isFileMessage()) {
            Label fileLabel = new Label(message.getFileName() == null ? "File" : message.getFileName());
            fileLabel.getStyleClass().add("message-file-name");
            HBox.setHgrow(fileLabel, Priority.ALWAYS);
            fileLabel.setMaxWidth(Double.MAX_VALUE);

            Button downloadButton = new Button("Save File");
            downloadButton.getStyleClass().add("message-file-button");
            downloadButton.setOnAction(event -> saveFile(message));

            HBox fileRow = new HBox(8, fileLabel, downloadButton);
            fileRow.setAlignment(Pos.CENTER_LEFT);
            messageBox.getChildren().add(fileRow);
        } else {
            Text text = new Text(message.getText() == null ? "" : message.getText());
            text.setWrappingWidth(300);
            text.getStyleClass().add("message-text");
            messageBox.getChildren().add(text);
            messageBox.getStyleClass().add(isMine ? "message-me" : "message-other");
        }

        //unsend
        Button actions = new Button("⋯");
        actions.getStyleClass().add("message-options-btn");
        actions.setFocusTraversable(false);

        actions.setOpacity(0);
        actions.setManaged(isMine && !message.isDeleted());

        ContextMenu menu = new ContextMenu();
        if (!message.isDeleted()) {
            MenuItem reply = new MenuItem("Reply");
            reply.setOnAction(e -> {});
            menu.getItems().add(reply);

            if (isMine) {
                MenuItem unsend = new MenuItem("Unsend");
                unsend.setOnAction(e -> onUnsend.accept(message));
                menu.getItems().add(unsend);
            }
        }

        actions.setOnAction(e -> {
            if (!menu.getItems().isEmpty()) {
                menu.show(actions, javafx.geometry.Side.BOTTOM, 0, 4);
            }
        });

        root.setOnMouseEntered(e -> { if(isMine && !message.isDeleted()) actions.setOpacity(1); });
        root.setOnMouseExited(e -> actions.setOpacity(0));

        //end

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        root.getChildren().clear();
        if (isMine) {
            messageBox.getStyleClass().add("message-me");
            root.getChildren().addAll(spacer, actions, messageBox);
            root.setAlignment(Pos.CENTER_RIGHT);
            setAlignment(Pos.CENTER_RIGHT);
        } else {
            messageBox.getStyleClass().add("message-other");
            root.getChildren().addAll(messageBox, spacer);
            root.setAlignment(Pos.CENTER_LEFT);
            setAlignment(Pos.CENTER_RIGHT);
        }

        setGraphic(root);
        setText(null);
    }


    private void saveFile(Message message) {
        if (message.getFileData() == null || message.getFileData().length == 0 || getScene() == null) {
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save File");
        chooser.setInitialFileName(message.getFileName() == null ? "attachment" : message.getFileName());
        File file = chooser.showSaveDialog(getScene().getWindow());
        if (file == null) {
            return;
        }

        try {
            Files.write(file.toPath(), message.getFileData());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save file", e);
        }
    }

}