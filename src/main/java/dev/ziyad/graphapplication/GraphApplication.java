package dev.ziyad.graphapplication;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

public class GraphApplication extends Application {

    // Fields and initial setup
    double initialX = 0;
    double initialY = 0;

    int windowWidth = 320;
    int windowHeight = 240;

    Graph graph = new Graph();
    CursorState cursorState = CursorState.PAN;
    ArrayList<Node> moveableNodes = new ArrayList<>();
    Vertex firstVertex = null;

    Label coordinatesLabel = new Label("X: 0, Y: 0");
    Label statsLabel = new Label("Vertices: 0, Edges: 0");
    double centerX = 0;
    double centerY = 0;

    Alert functionDialog = new Alert(Alert.AlertType.CONFIRMATION);

    @Override
    public void start(Stage stage) throws IOException {
        Pane graphPane = new Pane();

       initializeFunctionDialog();

        initializeCoordinateLabel(graphPane);

        addResizableGrid(graphPane);

        // Keyboard Event Handlers
        setupKeyboardEvents(graphPane);

        // Mouse Event Handlers
        setupMouseEvents(graphPane);

        Scene scene = new Scene(graphPane, windowWidth, windowHeight);
        stage.setTitle("Discrete Math: Undirected Graph");
        stage.setScene(scene);
        stage.show();

        graphPane.requestFocus();
    }

    // Initialize Stats
    private void initializeStatLabel(Pane graphPane) {
        statsLabel.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-padding: 5;");

        // Bind the label to the bottom right of the pane
        statsLabel.layoutXProperty().bind(graphPane.widthProperty().subtract(statsLabel.widthProperty()).subtract(10));
        statsLabel.layoutYProperty().bind(graphPane.heightProperty().subtract(statsLabel.heightProperty()).subtract(10));

        graphPane.getChildren().add(statsLabel);
    }
    // Initialize Coordinates
    private void initializeCoordinateLabel(Pane graphPane) {
        coordinatesLabel.setStyle("-fx-background-color: white; -fx-border-color: black; -fx-padding: 5;");

        // Bind the label to the bottom left of the pane
        coordinatesLabel.layoutXProperty().bind(graphPane.layoutXProperty().add(10));
        coordinatesLabel.layoutYProperty().bind(graphPane.heightProperty().subtract(coordinatesLabel.heightProperty()).subtract(10));

        graphPane.getChildren().add(coordinatesLabel);
    }


    // Setup keyboard event handles
    private void setupKeyboardEvents(Pane graphPane) {
        graphPane.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case SHIFT:
                    firstVertex = null;
                    break;
            }
        });
    }

    // Setup mouse event handlers
    private void setupMouseEvents(Pane graphPane) {
        graphPane.setOnMousePressed(e -> {
            initialX = e.getSceneX();
            initialY = e.getSceneY();

            if (e.isPrimaryButtonDown()) {
                if (e.isControlDown()) {
                    cursorState = CursorState.VERTEX;
                } else if (e.isShiftDown()) {
                    cursorState = CursorState.EDGE;
                }
            } else if (e.isSecondaryButtonDown()) {
                cursorState = CursorState.DELETE_VERTEX;
            } else if (e.isMiddleButtonDown()) {
                cursorState = CursorState.VERTEX_FUNCTION;
            } else {
                cursorState = CursorState.PAN;
            }

            handleMouseEvent(e);
        });

        graphPane.setOnMouseDragged(this::handleMouseEvent);

        graphPane.setOnMouseReleased(e -> {
            initialX = e.getSceneX();
            initialY = e.getSceneY();
            cursorState = CursorState.PAN;
            if (!e.isShiftDown()) firstVertex = null;
        });
    }

    // Grid drawing methods
    private void addResizableGrid(Pane pane) {
        // Add listeners to the width and height properties to redraw grid on resize
        pane.widthProperty().addListener((observable, oldValue, newValue) -> drawGrid(pane));
        pane.heightProperty().addListener((observable, oldValue, newValue) -> drawGrid(pane));

        // Initial grid drawing
        drawGrid(pane);
    }

    private void drawGrid(Pane pane) {
        ArrayList<Node> nodesToRemove = new ArrayList<>();
        // Clear existing grid lines
        for (Node n : pane.getChildren()) {
            boolean hasID = n.getId() != null;
            boolean isLine = n instanceof Line line;
            boolean isGridLine = hasID && isLine && (n.getId().contains("horizontal") || n.getId().contains("vertical"));

            if (isGridLine) {
                nodesToRemove.add(n);
            }
        }

        for (Node n : nodesToRemove) {
            pane.getChildren().remove(n);
        }

        nodesToRemove.clear();

        int gridSize = 20;
        Color gridColor = Color.LIGHTGRAY;

        // Get current pane dimensions
        double width = pane.getWidth();
        double height = pane.getHeight();

        // Create vertical lines
        for (int x = 0; x < width; x += gridSize) {
            Line line = new Line(x, 0, x, height);
            line.setStroke(gridColor);
            pane.getChildren().add(line);
        }

        // Create horizontal lines
        for (int y = 0; y < height; y += gridSize) {
            Line line = new Line(0, y, width, y);
            line.setStroke(gridColor);
            pane.getChildren().add(line);
        }

        redrawNodes(pane);
    }

    // Main mouse event handler method
    public void handleMouseEvent(MouseEvent event) {
        switch (cursorState) {
            case PAN:
                if (event.isPrimaryButtonDown() && !event.isControlDown() && !event.isShiftDown())
                    panPane(event);
                break;
            case VERTEX:
                addVertex(event);
                break;
            case EDGE:
                addEdge(event);
                break;
            case DELETE_VERTEX:
                deleteVertex(event);
                break;
            case VERTEX_FUNCTION:
                runVertexFunction(event);
                break;
        }
    }

    // Vertex methods
    private void addVertex(MouseEvent event) {
        if (event.getSource() instanceof Pane p) {
            double newX = event.getSceneX();
            double newY = event.getSceneY();

            // Check for overlap with existing vertices
            for (Vertex v : graph.getVertices()) {
                Circle existingVertex = (Circle) v.getVertexRepresentation();
                double distance = Math.sqrt(Math.pow(newX - existingVertex.getCenterX(), 2) +
                        Math.pow(newY - existingVertex.getCenterY(), 2));

                // If the distance is smaller than a threshold (e.g., twice the radius of a vertex), skip adding
                if (distance < existingVertex.getRadius() * 2) {
                    return;  // Exit the method and don't add the vertex
                }
            }

            // If no overlap, proceed to add the new vertex
            Circle vertexRepresentation = new Circle(newX, newY, 15);
            Text vertexIDText = new Text("");
            Vertex vertex = new Vertex(vertexRepresentation, vertexIDText);
            vertexIDText.setFont(Font.font("Arial", FontWeight.BOLD, FontPosture.REGULAR, 32));
            vertexIDText.setFill(Color.TEAL);
            vertexIDText.setX(newX - 20);
            vertexIDText.setY(newY - 20);

            p.getChildren().add(vertexRepresentation);
            p.getChildren().add(vertexIDText);
            vertexIDText.toFront();

            graph.addVertex(vertex);
            moveableNodes.add(vertexRepresentation);
            moveableNodes.add(vertexIDText);
            cursorState = CursorState.PAN;

            statsLabel.setText(String.format("Vertices: %d, Edges: %d", graph.getVertices().size(), graph.getEdges().size()));
        }
    }

    private void deleteVertex(MouseEvent event) {
        if (event.getSource() instanceof Pane p) {
            ArrayList<Vertex> verticesToRemove = new ArrayList<>();
            ArrayList<Edge> edgesToRemove = new ArrayList<>();
            for (Vertex v : graph.getVertices()) {
                Node n = v.getVertexRepresentation();
                if (!(n instanceof Circle c)) continue;

                double centerX = c.getCenterX();
                double centerY = c.getCenterY();
                double radius = c.getRadius();

                double distanceSquared = Math.pow(initialX - centerX, 2) + Math.pow(initialY - centerY, 2);
                boolean overlapping = (distanceSquared <= Math.pow(radius, 2));

                if (overlapping) {
                    p.getChildren().remove(c);
                    p.getChildren().remove(v.vertexIDText);
                    moveableNodes.remove(c);
                    moveableNodes.remove(v.vertexIDText);
                    verticesToRemove.add(v);

                    for (Edge e : graph.getEdges()) {
                        if (e.getVertex1() == v || e.getVertex2() == v) {
                            p.getChildren().remove(e.getEdgeLine());
                            moveableNodes.remove(e.getEdgeLine());
                            edgesToRemove.add(e);
                        }
                    }
                }
            }

            for (Vertex v : verticesToRemove) {
                graph.removeVertex(v);
            }

            for (Edge e : edgesToRemove) {
                graph.removeEdge(e);
            }

            verticesToRemove.clear();
            edgesToRemove.clear();
        }

        statsLabel.setText(String.format("Vertices: %d, Edges: %d", graph.getVertices().size(), graph.getEdges().size()));
    }

    private Vertex getVertexFromCircle(Circle circle) {
        for (Vertex v : graph.getVertices()) {
            if (v.getVertexRepresentation().equals(circle)) {
                return v;
            }
        }
        return null;
    }

    private void runVertexFunction(MouseEvent event) {
        if (event.getSource() instanceof Pane p) {
            for (Vertex v : graph.getVertices()) {
                Node n = v.getVertexRepresentation();
                if (!(n instanceof Circle c)) continue;

                double centerX = c.getCenterX();
                double centerY = c.getCenterY();
                double radius = c.getRadius();

                double distanceSquared = Math.pow(initialX - centerX, 2) + Math.pow(initialY - centerY, 2);
                boolean overlapping = (distanceSquared <= Math.pow(radius, 2));

                if (overlapping) {
                    String function = showFunctionDialog();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);

                    switch (function) {
                        case "Neighbor":
                            alert.setHeaderText("The neighbors of the selected vertex are: " + graph.getNeighbors(v));
                            alert.showAndWait();
                            break;
                        case "Degree":
                            alert.setHeaderText("The degree of the selected vertex is: " + graph.getDegree(v));
                            alert.showAndWait();
                            break;
                        case "Label":
                            TextInputDialog td = new TextInputDialog();
                            td.setHeaderText("Enter vertex label");
                            td.showAndWait();
                            graph.getVertexAt(event.getSceneX(), event.getSceneY()).vertexIDText.setText(td.getEditor().getText());
                            break;
                    }

                }
            }
        }
    }

    // Edge methods
    private void addEdge(MouseEvent event) {
        cursorState = CursorState.PAN;
        if (event.getSource() instanceof Pane p) {
            Circle clickedVertex = null;

            // Check if the clicked point is near any existing vertex
            for (Vertex v : graph.getVertices()) {
                Circle vertexRepresentation = (Circle) v.getVertexRepresentation();

                double centerX = vertexRepresentation.getCenterX();
                double centerY = vertexRepresentation.getCenterY();
                double radius = vertexRepresentation.getRadius();

                double distanceSquared = Math.pow(initialX - centerX, 2) + Math.pow(initialY - centerY, 2);
                boolean overlapping = (distanceSquared <= Math.pow(radius, 2));

                if (overlapping) {
                    clickedVertex = vertexRepresentation;
                    break;
                }
            }

            // If a vertex is clicked
            if (clickedVertex != null) {
                // If this is the first vertex
                if (firstVertex == null) {
                    firstVertex = getVertexFromCircle(clickedVertex);
                } else {
                    // This is the second vertex, draw an edge
                    Vertex secondVertex = getVertexFromCircle(clickedVertex);
                    if (secondVertex != null && secondVertex != firstVertex) {
                        for (Edge e : graph.getEdges()) {
                            if (e.getVertex1() == firstVertex && e.getVertex2() == secondVertex) return;
                        }

                        // Draw the line between the two vertices
                        Line edgeLine = new Line(firstVertex.getX(), firstVertex.getY(),
                                secondVertex.getX(), secondVertex.getY());
                        edgeLine.setStrokeWidth(5);

                        p.getChildren().add(edgeLine);
                        moveableNodes.add(edgeLine);

                        // Add the edge to the graph
                        Edge edge = new Edge(firstVertex, secondVertex, edgeLine);
                        graph.addEdge(edge);

                        // Reset for next edge creation
                        firstVertex = null;

                        // Send Vertex text to front
                        for (Vertex v : graph.vertices) v.vertexIDText.toFront();

                        // Update stats label
                        statsLabel.setText(String.format("Vertices: %d, Edges: %d", graph.getVertices().size(), graph.getEdges().size()));
                    }
                }
            }
        }
    }

    // Utility methods
    private void panPane(MouseEvent event) {
        double deltaX = event.getSceneX() - initialX;
        double deltaY = event.getSceneY() - initialY;

        centerX -= deltaX;  // Update center X coordinate
        centerY += deltaY;  // Update center Y coordinate

        for (Node n : moveableNodes) {
            if (n instanceof Circle c) {
                c.setCenterX(c.getCenterX() + deltaX);
                c.setCenterY(c.getCenterY() + deltaY);
            }

            if (n instanceof Line l) {
                l.setStartX(l.getStartX() + deltaX);
                l.setStartY(l.getStartY() + deltaY);
                l.setEndX(l.getEndX() + deltaX);
                l.setEndY(l.getEndY() + deltaY);
            }

            if (n instanceof Text t) {
                t.setX(t.getX() + deltaX);
                t.setY(t.getY() + deltaY);
            }
        }

        initialX = event.getSceneX();
        initialY = event.getSceneY();
        coordinatesLabel.setText(String.format("X: %.2f, Y: %.2f", centerX, centerY));
    }

    private void redrawNodes(Pane p) {
        for (Node n : moveableNodes) {
            p.getChildren().remove(n);
            p.getChildren().add(n);

            if (n instanceof Text t) t.toFront();
        }

        p.getChildren().remove(coordinatesLabel);
        p.getChildren().add(coordinatesLabel);

        p.getChildren().remove(statsLabel);
        p.getChildren().add(statsLabel);
    }

    private void initializeFunctionDialog() {
        functionDialog.setTitle("Vertex Functions");
        functionDialog.setHeaderText("Pick a function to perform on the selected vertex");

        ButtonType neighborButton = new ButtonType("Neighbor");
        ButtonType degreeButton = new ButtonType("Degree");
        ButtonType labelButton = new ButtonType("Label");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        functionDialog.getButtonTypes().setAll(neighborButton, degreeButton, labelButton, cancelButton);
    }

    private String showFunctionDialog() {
        Optional<ButtonType> result = functionDialog.showAndWait();

        if (result.isEmpty()) return "";
        return result.get().getText();
    }

    public static void main(String[] args) {
        launch();
    }
}