package com.example.javafxapp;

import javafx.scene.shape.Polygon;
import javafx.scene.shape.Line;
import javafx.beans.value.ChangeListener;

public class Arrow extends Polygon {

  private final Line line;
  private final double arrowLength;
  private final double arrowWidth;
  private final ChangeListener<Number> listener;

  public Arrow(Line line, double arrowLength, double arrowWidth) {
    this.line = line;
    this.arrowLength = arrowLength;
    this.arrowWidth = arrowWidth;

    fillProperty().bind(line.strokeProperty());
    strokeProperty().bind(line.strokeProperty());

    listener = (obs, oldVal, newVal) -> updateVertices();
    line.startXProperty().addListener(listener);
    line.startYProperty().addListener(listener);
    line.endXProperty().addListener(listener);
    line.endYProperty().addListener(listener);

    updateVertices();
  }

  private void updateVertices() {
    double startX = line.getStartX();
    double startY = line.getStartY();
    double endX = line.getEndX();
    double endY = line.getEndY();

    double dx = endX - startX;
    double dy = endY - startY;
    double length = Math.sqrt(dx * dx + dy * dy);

    if (length < 0.001) {
      getPoints().setAll(endX, endY, endX, endY, endX, endY);
      return;
    }

    double normX = dx / length;
    double normY = dy / length;

    double perpX = -normY;
    double perpY = normX;

    double tipX = endX;
    double tipY = endY;

    double leftX = endX - arrowLength * normX + (arrowWidth / 2) * perpX;
    double leftY = endY - arrowLength * normY + (arrowWidth / 2) * perpY;

    double rightX = endX - arrowLength * normX - (arrowWidth / 2) * perpX;
    double rightY = endY - arrowLength * normY - (arrowWidth / 2) * perpY;

    getPoints().setAll(
        tipX, tipY,
        leftX, leftY,
        rightX, rightY);
  }
}