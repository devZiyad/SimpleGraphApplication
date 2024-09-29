package dev.ziyad.graphapplication;

import javafx.scene.shape.Line;

public class Edge {
    private Vertex vertex1;
    private Vertex vertex2;
    private Line edgeLine;

    public Edge(Vertex vertex1, Vertex vertex2, Line edgeLine) {
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
        this.edgeLine = edgeLine;
    }

    public Vertex getVertex1() { return vertex1; }
    public Vertex getVertex2() { return vertex2; }
    public Line getEdgeLine() { return edgeLine; }
}
