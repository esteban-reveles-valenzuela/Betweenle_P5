module dev.esteban.betweenle {
    requires javafx.controls;
    requires javafx.fxml;


    opens dev.esteban.betweenle to javafx.fxml;
    exports dev.esteban.betweenle;
}