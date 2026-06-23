package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.logging.Level;

import static com.example.service.DatabaseService.logger;

public class App extends Application {

  private StackPane rootContainer;

  @Override
  public void start(Stage stage) {
    rootContainer = new StackPane();
    Scene scene = new Scene(rootContainer);
    stage.setScene(scene);
    stage.setTitle("Текстовый редактор");
    stage.show();

    loadView("/views/auth.fxml");
  }

  public void loadView(String fxmlPath) {
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
      Parent view = loader.load();
      rootContainer.getChildren().setAll(view);
    } catch (Exception e) {
      logger.log(Level.SEVERE, "Ошибка", e);
    }
  }

  public static void main(String[] args) {
    launch(args);
  }
}
