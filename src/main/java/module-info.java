module com.example.demo1 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens com.chatterbox.lan to javafx.fxml;
    exports com.chatterbox.lan;
    exports com.chatterbox.lan.controllers;
    opens com.chatterbox.lan.controllers to javafx.fxml;
}