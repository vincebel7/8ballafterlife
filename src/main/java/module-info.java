module com.example.eightballgamerevived {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.eightballgamerevived to javafx.fxml;
    exports com.example.eightballgamerevived;
}