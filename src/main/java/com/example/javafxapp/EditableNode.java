package com.example.javafxapp;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import javafx.geometry.Point2D;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

/**
 * Панель, представляющая редактируемый узел блок-схемы.
 * Поддерживает перетаскивание, изменение текста, отображение портов и связывание.
 */
public class EditableNode extends Pane {

  /**
   * Типы фигур, доступные для узла.
   */
  public enum NodeType {
    RECTANGLE, ROUNDED_RECT, ELLIPSE, RHOMBUS, PARALLELOGRAM,
    TRAPEZOID, INVERTED_TRAPEZOID, HEXAGON
  }

  private final Shape shape;
  private final Text textLabel;
  private final TextField editField;
  private final NodeType nodeType;

  private Circle portTop;
  private Circle portBottom;
  private Circle portLeft;
  private Circle portRight;
  private List<Circle> ports;

  private double dragStartX;
  private double dragStartY;
  private double nodeStartX;
  private double nodeStartY;

  private boolean isConnecting = false;
  private Consumer<Circle> onDragStarted;

  private EditableNode(Shape shape, String initialText, NodeType type) {
    this.nodeType = type;
    this.shape = shape;
    shape.setFill(Color.LIGHTBLUE);
    shape.setStroke(Color.BLACK);
    shape.setStrokeWidth(2);

    StackPane contentPane = new StackPane();
    contentPane.setPrefSize(shape.getBoundsInLocal().getWidth(),
            shape.getBoundsInLocal().getHeight());

    textLabel = new Text(initialText);
    textLabel.setFill(Color.BLACK);
    textLabel.setMouseTransparent(true);

    editField = new TextField(initialText);
    editField.setVisible(false);
    editField.setMaxWidth(shape.getBoundsInLocal().getWidth() - 20);

    contentPane.getChildren().addAll(shape, textLabel, editField);
    getChildren().add(contentPane);

    setPrefSize(shape.getBoundsInLocal().getWidth(),
            shape.getBoundsInLocal().getHeight());

    createPorts();
    setupDrag();
    setupEditing();

    setOnMouseEntered(e -> {
      if (!isConnecting) {
        ports.forEach(p -> p.setVisible(true));
      }
    });
    setOnMouseExited(e -> {
      if (!isConnecting) {
        ports.forEach(p -> p.setVisible(false));
      }
    });
  }

  /**
   * Фабричный метод для создания узла заданного типа с начальным текстом.
   *
   * @param type тип фигуры
   * @param text начальный текст
   * @return готовый объект EditableNode
   */
  public static EditableNode createNode(NodeType type, String text) {
    Shape shape;
    switch (type) {
      case RECTANGLE:
        shape = new Rectangle(200, 100);
        break;

      case ROUNDED_RECT:
        Rectangle roundRect = new Rectangle(200, 100);
        roundRect.setArcWidth(30);
        roundRect.setArcHeight(30);
        shape = roundRect;
        break;

      case ELLIPSE:
        shape = new Ellipse(100, 50);
        break;

      case RHOMBUS:
        Polygon rhombus = new Polygon();
        rhombus.getPoints().addAll(0.0, -50.0, 100.0, 0.0, 0.0, 50.0, -100.0, 0.0);
        shape = rhombus;
        break;

      case PARALLELOGRAM:
        Polygon paral = new Polygon();
        paral.getPoints().addAll(
                -70.0, -50.0,
                30.0, -50.0,
                70.0, 50.0,
                -30.0, 50.0);
        shape = paral;
        break;

      case TRAPEZOID:
        Polygon trap = new Polygon();
        trap.getPoints().addAll(
                -100.0, -50.0,
                100.0, -50.0,
                60.0, 50.0,
                -60.0, 50.0);
        shape = trap;
        break;

      case INVERTED_TRAPEZOID:
        Polygon invTrap = new Polygon();
        invTrap.getPoints().addAll(
                -60.0, -50.0,
                60.0, -50.0,
                100.0, 50.0,
                -100.0, 50.0);
        shape = invTrap;
        break;

      case HEXAGON:
        Polygon hex = new Polygon();
        hex.getPoints().addAll(
                -50.0, -50.0,
                50.0, -50.0,
                100.0, 0.0,
                50.0, 50.0,
                -50.0, 50.0,
                -100.0, 0.0);
        shape = hex;
        break;

      default:
        throw new IllegalArgumentException("Неизвестный тип: " + type);
    }
    return new EditableNode(shape, text, type);
  }

  private void createPorts() {
    double width = shape.getBoundsInLocal().getWidth();
    double height = shape.getBoundsInLocal().getHeight();
    double radius = 6;

    portTop = new Circle(width / 2, 0, radius);
    portBottom = new Circle(width / 2, height, radius);
    portLeft = new Circle(0, height / 2, radius);
    portRight = new Circle(width, height / 2, radius);

    ports = Arrays.asList(portTop, portBottom, portLeft, portRight);
    for (Circle port : ports) {
      port.setFill(Color.LIGHTGREEN);
      port.setVisible(false);
      port.setMouseTransparent(false);
      port.setOnMousePressed(this::onPortPressed);
      getChildren().add(port);
    }
  }

  /**
   * Обработчик нажатия на порт – инициирует создание связи.
   */
  private void onPortPressed(MouseEvent event) {
    Circle port = (Circle) event.getSource();
    if (onDragStarted != null) {
      onDragStarted.accept(port);
    }
    isConnecting = true;
    event.consume();
  }

  /**
   * Устанавливает callback для события начала перетаскивания связи от порта.
   *
   * @param callback потребитель, принимающий Circle порта, с которого началось перетаскивание
   */
  public void setOnPortDragStarted(Consumer<Circle> callback) {
    this.onDragStarted = callback;
  }

  /**
   * Возвращает порт, центр которого находится ближе всего к заданным сценическим координатам.
   *
   * @param sceneX координата X в системе сцены
   * @param sceneY координата Y в системе сцены
   * @return ближайший Circle‑порт или null, если ни один не подходит
   */
  public Circle findPortAtScene(double sceneX, double sceneY) {
    for (Circle port : ports) {
      Point2D centerInScene = port.localToScene(port.getCenterX(), port.getCenterY());
      double distance = centerInScene.distance(sceneX, sceneY);
      if (distance <= port.getRadius() + 3) {
        return port;
      }
    }
    return null;
  }

  /**
   * Переключает режим соединения узла. Если соединительный режим выключается,
   * порты автоматически скрываются.
   *
   * @param connecting true, если начинается перетаскивание связи; false иначе
   */
  public void setConnecting(boolean connecting) {
    this.isConnecting = connecting;
    if (!connecting) {
      hidePorts();
    }
  }

  /**
   * Принудительно скрывает все порты.
   */
  public void hidePorts() {
    for (Circle p : ports) {
      p.setVisible(false);
    }
  }

  /**
   * Возвращает массив всех четырёх портов узла (TOP, BOTTOM, LEFT, RIGHT).
   *
   * @return массив объектов Circle
   */
  public Circle[] getAllPorts() {
    return new Circle[] { portTop, portBottom, portLeft, portRight };
  }

  private void setupDrag() {
    setOnMousePressed(event -> {
      if (isConnecting || (ports != null && ports.contains(event.getTarget()))) {
        event.consume();
        return;
      }
      if (event.getClickCount() == 1) {
        dragStartX = event.getSceneX();
        dragStartY = event.getSceneY();
        nodeStartX = getLayoutX();
        nodeStartY = getLayoutY();
        event.consume();
      }
    });

    setOnMouseDragged(event -> {
      if (isConnecting) {
        return;
      }
      double offsetX = event.getSceneX() - dragStartX;
      double offsetY = event.getSceneY() - dragStartY;
      setLayoutX(nodeStartX + offsetX);
      setLayoutY(nodeStartY + offsetY);
      event.consume();
    });
  }

  private void setupEditing() {
    setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        textLabel.setVisible(false);
        editField.setVisible(true);
        editField.setText(textLabel.getText());
        editField.requestFocus();
        event.consume();
      }
    });

    editField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
      if (!isNowFocused) {
        finishEditing();
      }
    });
    editField.setOnAction(e -> finishEditing());
  }

  /**
   * Завершает редактирование текста и обновляет надпись.
   */
  private void finishEditing() {
    textLabel.setText(editField.getText());
    textLabel.setVisible(true);
    editField.setVisible(false);
  }

  /**
   * Возвращает текущий текст узла.
   *
   * @return строка с текстом
   */
  public String getText() {
    return textLabel.getText();
  }

  /**
   * Устанавливает новый текст узла.
   *
   * @param text новая строка
   */
  public void setText(String text) {
    textLabel.setText(text);
    editField.setText(text);
  }

  /**
   * Возвращает имя порта (TOP, BOTTOM, LEFT, RIGHT) по объекту Circle.
   *
   * @param port объект Circle, представляющий порт
   * @return имя порта или null, если порт не найден
   */
  public String getPortName(Circle port) {
    if (port == portTop) {
      return "TOP";
    }
    if (port == portBottom) {
      return "BOTTOM";
    }
    if (port == portLeft) {
      return "LEFT";
    }
    if (port == portRight) {
      return "RIGHT";
    }
    return null;
  }

  /**
   * Возвращает порт по его имени.
   *
   * @param name одно из: "TOP", "BOTTOM", "LEFT", "RIGHT"
   * @return объект Circle или null, если имя некорректно
   */
  public Circle getPortByName(String name) {
    switch (name) {
      case "TOP":
        return portTop;
      case "BOTTOM":
        return portBottom;
      case "LEFT":
        return portLeft;
      case "RIGHT":
        return portRight;
      default:
        return null;
    }
  }

  /**
   * Возвращает тип фигуры данного узла.
   *
   * @return значение перечисления NodeType
   */
  public NodeType getNodeType() {
    return nodeType;
  }
}
