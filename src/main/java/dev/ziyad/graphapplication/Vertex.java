package dev.ziyad.graphapplication;

import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class Vertex {
    static int vertexCount = 0;
    int id;
    Circle vertexRepresentation;
    Text vertexIDText;

    public Vertex(Circle vertexRepresentation, Text vertexIDText) {
        vertexCount++;
        this.id = vertexCount;
        this.vertexRepresentation = vertexRepresentation;
        this.vertexIDText = vertexIDText;
    }

    public static int getVertexCount() {
        return vertexCount;
    }

    public Node getVertexRepresentation() {
        return vertexRepresentation;
    }

    public Text getVertexIDText() {
        return vertexIDText;
    }

    public static void decrementVertexCount() {
        vertexCount--;
    }

    public double getX() {
        return vertexRepresentation.getCenterX();
    }

    public double getY() {
        return vertexRepresentation.getCenterY();
    }
}
