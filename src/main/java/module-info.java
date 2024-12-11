module com.hiru.expense.tracker.smartexpensetracker {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.sql;

    opens com.hiru.smartexpensetracker to javafx.fxml;
    exports com.hiru.smartexpensetracker;
    exports com.hiru.smartexpensetracker.models;
    opens com.hiru.smartexpensetracker.models to javafx.fxml;
    exports com.hiru.smartexpensetracker.controllers;
    opens com.hiru.smartexpensetracker.controllers to javafx.fxml;
    exports com.hiru.smartexpensetracker.views;
    opens com.hiru.smartexpensetracker.views to javafx.fxml;
}