module lk.ijse {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens lk.ijse to javafx.fxml;
    exports lk.ijse;

    opens lk.ijse.gear_rent_pro.controller to javafx.fxml;
    exports lk.ijse.gear_rent_pro.controller;
}
