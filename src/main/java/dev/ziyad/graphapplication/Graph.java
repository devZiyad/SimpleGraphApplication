package dev.ziyad.graphapplication;

import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.Collections;

public class Graph {
    ArrayList<Vertex> vertices = new ArrayList<Vertex>();
    ArrayList<Edge> edges = new ArrayList<Edge>();

    public Graph() {

    }

    public void addVertex(Vertex v) {
        vertices.add(v);
    }

    public void removeVertex(Vertex v) {
        vertices.remove(v);
        Vertex.decrementVertexCount();
    }

    public ArrayList<Vertex> getVertices() {
        return vertices;
    }

    public void addEdge(Edge e) {
        edges.add(e);
    }

    public void removeEdge(Edge e) {
        edges.remove(e);
    }

    public ArrayList<Edge> getEdges() {
        return edges;
    }

    public int getDegree(Vertex v) {
        int degreeCount = 0;

        for (Edge edge : edges) {
            if (edge.getVertex1() == v || edge.getVertex2() == v) degreeCount++;
        }

        return degreeCount;
    }

    public ArrayList<String> getNeighbors(Vertex v) {
        ArrayList<String> neighbors = new ArrayList<>();

        for (Edge edge : edges) {
            if (edge.getVertex1() == v) {
                neighbors.add(edge.getVertex2().getVertexIDText().getText());
            } else if (edge.getVertex2() == v) {
                neighbors.add(edge.getVertex1().getVertexIDText().getText());
            }
        }

        return neighbors;
    }

    public Vertex getVertexAt(double x, double y) {
        for (Vertex v : vertices) {
            double distanceSquared = Math.pow(x - v.getX(), 2) + Math.pow(y - v.getY(), 2);
            boolean overlapping = false;
            if (v.getVertexRepresentation() instanceof Circle c)
                overlapping = (distanceSquared <= Math.pow(c.getRadius(), 2));

            if (overlapping) return v;
        }

        return null;
    }

    public Vertex getVertexFromLabel(String label) {
        for (Vertex v : vertices) {
            if (v.getVertexIDText().getText().equalsIgnoreCase(label)) return v;
        }
        return null;
    }

    public ArrayList<String> getVerticesLabels() {
        ArrayList<String> verticesLabels = new ArrayList<>();

        for (Vertex v : vertices) {
            verticesLabels.add(v.getVertexIDText().getText());
        }

        verticesLabels.sort(null);
        return verticesLabels;
    }

    public ArrayList<ArrayList<Integer>> getAdjacencyMatrix() {
        ArrayList<ArrayList<Integer>> adjacencyMatrix = new ArrayList<>(new ArrayList<>());



        return adjacencyMatrix;
    }
}
