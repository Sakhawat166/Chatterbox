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
        root.setMaxWidth(Double.MAX_VALUE);

        VBox messageBox = new VBox(4);
        messageBox.getStyleClass().add("message-box");

        if (showSenderNames.getAsBoolean() && message.getSender() != null) {
            Label usernameLabel = new Label(message.getSender().getUsername());
            usernameLabel.getStyleClass().add("username-label");
            messageBox.getChildren().add(usernameLabel);
        }

        if (message.isDeleted()) {
            String deletedText = (message.getText() == null || message.getText().isBlank())
                    ? "Message unsent"
                    : message.getText();
            Label deleted = new Label(deletedText);            deleted.getStyleClass().add("message-deleted-text");
            messageBox.getChildren().add(deleted);
            messageBox.getStyleClass().add("message-deleted-box");
        } else if (message.isFileMessage()) {
            Button save = new Button("Save: " + (message.getFileName() == null ? "File" : message.getFileName()));
            save.getStyleClass().add("message-file-button");
            save.setOnAction(e -> saveFile(message));
            messageBox.getChildren().add(save);
            messageBox.getStyleClass().add(isMine ? "message-me" : "message-other");
        } else {
            Text text = new Text(message.getText() == null ? "" : message.getText());
            text.setWrappingWidth(300);
            text.getStyleClass().add("message-text");
            messageBox.getChildren().add(text);
            messageBox.getStyleClass().add(isMine ? "message-me" : "message-other");
        }

        Button actions = new Button("⋯");
        actions.getStyleClass().add("message-options-btn");
        actions.setFocusTraversable(false);

        // FIX 1: Set opacity to 0 by default instead of setVisible(false)
        actions.setOpacity(0);
        // Keep it managed so the layout space is always reserved
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

        // FIX 2: Add hover listeners to the root HBox
        root.setOnMouseEntered(e -> { if(isMine && !message.isDeleted()) actions.setOpacity(1); });
        root.setOnMouseExited(e -> actions.setOpacity(0));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (isMine) {
            root.setAlignment(Pos.CENTER_RIGHT);
            // FIX 3: Put actions BEFORE messageBox to have it appear on the left
            root.getChildren().addAll(spacer, actions, messageBox);
        } else {
            root.setAlignment(Pos.CENTER_LEFT);
            root.getChildren().addAll(messageBox, spacer);
        }

        setGraphic(root);
        setText(null);
    }


    private void saveFile(Message message) {
        if (message.getFileData() == null || getScene() == null) return;
        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName(message.getFileName() == null ? "file" : message.getFileName());
        File file = chooser.showSaveDialog(getScene().getWindow());
        if (file != null) {
            try {
                Files.write(file.toPath(), message.getFileData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}