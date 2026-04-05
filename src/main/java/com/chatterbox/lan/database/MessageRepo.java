package com.chatterbox.lan.database;

import com.chatterbox.lan.models.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.UpdateResult;
import org.bson.BsonBinary;
import org.bson.Document;
import org.bson.types.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class MessageRepo {
    private final MongoCollection<Document> messageCollection;
    private final UserRepo userRepo;

    public MessageRepo() {
        MongoDatabase database = db.getDatabase();
        this.messageCollection = database.getCollection("messages");
        this.userRepo = new UserRepo();
    }

    public void saveMessage(Message message) {
        Date timestamp = Date.from(message.getTimestamp()
                .atZone(ZoneId.systemDefault())
                .toInstant());

        Document doc = new Document("conversationId", message.getConversationId())
                .append("sender", message.getSender().getUsername())
                .append("text", message.getText())
                .append("messageType", message.getMessageType())
                .append("fileName", message.getFileName())
                .append("fileData", message.getFileData() == null ? null : new BsonBinary(message.getFileData()))
                .append("timestamp", timestamp)
                .append("isDeleted", false);

        messageCollection.insertOne(doc);
        Object id = doc.get("_id");
        if (id != null) {
            message.setId(id.toString());
        }
    }

    public boolean deleteMessage(String conversationId, String messageId) {
        UpdateResult result = messageCollection.updateOne(
                Filters.and(
                        Filters.eq("_id", new ObjectId(messageId)),
                        Filters.eq("conversationId", conversationId)
                ),
                Updates.combine(
                        Updates.set("isDeleted", true),
                        Updates.set("text", ""),
                        Updates.set("fileName", null),
                        Updates.set("fileData", null)
                )
        );
        return result.getModifiedCount() > 0;
    }

    public List<Message> getMessages(String conversationId) {
        List<Message> messages = new ArrayList<>();

        for (Document doc : messageCollection.find(Filters.eq("conversationId", conversationId))
                .sort(Sorts.ascending("timestamp"))) {

            String senderUsername = doc.getString("sender");
            String id = doc.getObjectId("_id").toHexString();
            String text = doc.getString("text");
            String messageType = doc.getString("messageType");
            String fileName = doc.getString("fileName");
            Binary fileBinary = doc.get("fileData", Binary.class);
            byte[] fileData = fileBinary == null ? null : fileBinary.getData();
            Date timestamp = doc.getDate("timestamp");

            LocalDateTime localDateTime = timestamp.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            User sender = userRepo.getUserByUsername(senderUsername);
            if (sender == null) {
                sender = new User(senderUsername, "/avatars/default.png");
            }

            Message message = new Message(id, sender, null, text, conversationId, localDateTime, messageType, fileName, fileData);
            Boolean deleted = doc.getBoolean("isDeleted", false);
            message.setDeleted(Boolean.TRUE.equals(deleted));
            messages.add(message);
        }

        return messages;
    }
    public Message getMessageById(String messageId) {
        Document doc = messageCollection.find(Filters.eq("_id", new ObjectId(messageId))).first();
        if (doc == null) return null;

        String senderUsername = doc.getString("sender");
        String conversationId = doc.getString("conversationId");
        String text = doc.getString("text");
        String messageType = doc.getString("messageType");
        String fileName = doc.getString("fileName");
        Binary fileBinary = doc.get("fileData", Binary.class);
        byte[] fileData = fileBinary == null ? null : fileBinary.getData();
        Date timestamp = doc.getDate("timestamp");

        LocalDateTime localDateTime = timestamp.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        User sender = userRepo.getUserByUsername(senderUsername);
        if (sender == null) {
            sender = new User(senderUsername, "/avatars/default.png");
        }

        return new Message(messageId, sender, null, text, conversationId, localDateTime, messageType, fileName, fileData);
    }
}
