package danmarksKort;

import danmarksKort.address.Address;
import danmarksKort.drawables.*;
import danmarksKort.mapelements.Highway;
import danmarksKort.routefinder.Dijkstra;
import danmarksKort.routefinder.RoadType;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.FileChooser;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import static javafx.scene.shape.FillRule.EVEN_ODD;
import static javafx.scene.shape.FillRule.NON_ZERO;

public class ViewController {

    private final long[] frameTimes = new long[100];
    public MenuItem defaultbtn;
    public MenuItem redBtn;
    public Button UdBtn;
    public Button IndBtn;
    @FXML
    URL location;
    @FXML
    MenuItem loadBtn;
    @FXML
    private Label zoomLevelLabel;
    @FXML
    private Label FpsCounterLabel;
    @FXML
    private Label AddressLabel;
    private double startFactor;
    private double zoomLevel;
    private double maxZoom;
    private double minZoom;
    @FXML
    private VBox navigatonVbox;
    @FXML
    private Button cycleBtn;
    @FXML
    private Button carBtn;
    @FXML
    private AnchorPane root;
    @FXML
    private Canvas canvas;
    @FXML
    private boolean colorBlindMode;
    @FXML
    private NavigationTextField fromDestination;
    @FXML
    private NavigationTextField toDestination;
    private GraphicsContext gc;
    private Affine trans;
    private Model model;
    private Point2D lastmouse;
    private Boolean isDistance = true;
    private RoadType roadType = RoadType.BOTH;
    private int frameTimeIndex = 0;
    private boolean arrayFilled = false;
    private CustomCircle circleFrom;
    private CustomCircle circleTo;
    //taget fra stackoverflow:
    // https://stackoverflow.com/questions/28287398/what-is-the-preferred-way-of-getting-the-frame-rate-of-a-javafx-application
    private AnimationTimer frameRateMeter = new AnimationTimer() {
        @Override
        public void handle(long now) {
            long oldFrameTime = frameTimes[frameTimeIndex];
            frameTimes[frameTimeIndex] = now;
            frameTimeIndex = (frameTimeIndex + 1) % frameTimes.length;
            if (frameTimeIndex == 0) {
                arrayFilled = true;
            }
            if (arrayFilled) {
                long elapsedNanos = now - oldFrameTime;
                long elapsedNanosPerFrame = elapsedNanos / frameTimes.length;
                double frameRate = 1_000_000_000.0 / elapsedNanosPerFrame;
                UpdateFpscounterLabel((int) frameRate);
            }
        }
    };

    public ViewController() {
        model = Model.getInstance();
        trans = new Affine();
    }

    //initialiserer UI'en og diverse elementer
    public void initialize() {
        trans = new Affine();
        gc = canvas.getGraphicsContext2D();
        fromDestination.setListener(model.addressArray);
        toDestination.setListener(model.addressArray);
        frameRateMeter.start();
        resetView();
        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());
        canvas.widthProperty().addListener((a, b, c) -> {
            repaint();
        });
        canvas.heightProperty().addListener((a, b, c) -> {
            repaint();
        });
        zoomLevel = startFactor;
        maxZoom = zoomLevel * ((model.maxlat - model.minlat) * 80);
        minZoom = zoomLevel * 0.2;
    }

    // Canvas - pan and zoom controls
    @FXML
    void canvasMousePressed(MouseEvent e) throws NonInvertibleTransformException {
        lastmouse = new Point2D(e.getX(), e.getY());
        Point2D mouseCOORDS = trans.inverseTransform(e.getSceneX(), e.getSceneY());
        canvas.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                if (model.getFromNodeID() < 0) {
                    model.setFromNodeID(model.highwayTree.getNearestNodeId((float) mouseCOORDS.getX(),
                            (float) mouseCOORDS.getY(), model.biggestXDifferenceHighway,
                            model.biggestYDifferenceHighway));
                    circleFrom = new CustomCircle(mouseCOORDS.getX(), mouseCOORDS.getY(), 0.0010);
                    circleTo = null;
                    model.patheresultline.coords = new OSMNode[0];
                    repaint();

                } else {
                    model.setToNodeID(model.highwayTree.getNearestNodeId((float) mouseCOORDS.getX(),
                            (float) mouseCOORDS.getY(), model.biggestXDifferenceHighway,
                            model.biggestYDifferenceHighway));
                    model.shortestPath = new Dijkstra(model.g, model.getToNodeID(), model.getFromNodeID(),
                            isDistance, roadType);
                    String[] pathDir = model.getRoute(model.shortestPath.pathTo(model.getFromNodeID()));
                    setNavDirections(pathDir);
                    circleTo = new CustomCircle(mouseCOORDS.getX(), mouseCOORDS.getY(), 0.0010);
                    model.setFromNodeID(-1);
                    repaint();
                }
            }
            if (e.getButton().name().equals("SECONDARY") && !event.isAltDown()) {

                boolean removed = deletePoiClicked(mouseCOORDS);
                double rectRadius = 0.0005;
                POI poi = new POI((float) (mouseCOORDS.getX() - rectRadius), (float) (mouseCOORDS.getX() + rectRadius),
                        (float) (mouseCOORDS.getY() - rectRadius), (float) (mouseCOORDS.getY() + rectRadius));
                poi.setPoiSelected("default");
                if (!removed)
                    model.POIs.add(poi);

                repaint();
            }

        });
    }

    private boolean deletePoiClicked(Point2D click) {
        ArrayList<POI> ClosestPOIs = new ArrayList<>();
        Integer remover = 0;
        ArrayList<Integer> removers = new ArrayList<>();
        for (POI poi : model.POIs) {
            OSMNode centerOfPoi = POI.getLinePath(poi.getXmax(), poi.getYmax(), poi.getYmin(),
                    poi.getXmin()).getcenter();
            double newDistance = OSMNode.computeDistance(centerOfPoi.getx(), click.getX(), centerOfPoi.gety(),
                    click.getY());
            if (ClosestPOIs.size() == 0) {
                ClosestPOIs.add(poi);
                poi.setDistance(newDistance);
            }
            if (newDistance <= ClosestPOIs.get(ClosestPOIs.size() - 1).getDistance()) {
                ClosestPOIs.add(poi);
                removers.add(remover);
                poi.setDistance(newDistance);
            }
            remover++;
        }
        if (ClosestPOIs.size() > 0) {
            if (ClosestPOIs.get(ClosestPOIs.size() - 1).getDistance() < .3) {
                ClosestPOIs.remove(ClosestPOIs.size() - 1);
                model.POIs.remove(Integer.parseInt(String.valueOf(removers.get(removers.size() - 1))));
                return true;
            }
        }
        return false;
    }

    @FXML
    void canvasMouseDragged(MouseEvent e) {
        pan(e.getX() - lastmouse.getX(), e.getY() - lastmouse.getY());
        lastmouse = new Point2D(e.getX(), e.getY());
    }

    @FXML
    void canvasScroll(ScrollEvent e) {
        double factor = Math.pow(1.001, e.getDeltaY());
        if (zoomLevel * factor <= maxZoom && zoomLevel * factor >= minZoom) {
            zoomLevel = zoomLevel * factor;
            Zoom(factor, e.getX(), e.getY());
        }
        UpdateZoomLevel();
    }

    private void UpdateZoomLevel() {
        String s = ((getZoomPercent())) + " %";
        zoomLevelLabel.setText(s);
    }

    private int getZoomPercent() {
        return (int) (zoomLevel / maxZoom * 100);
    }

    @FXML
    private void resetView() {
        pan(-model.minlon, -model.minlat);
        this.startFactor = canvas.getWidth() / (model.maxlat - model.minlat);
        Zoom(startFactor, 0, 0);
        repaint();
    }

    @FXML
    private void Zoom(double factor, double x, double y) {
        trans.prependScale(factor, factor, x, y);
        repaint();
        UpdateZoomLevel();
    }

    @FXML
    private void pan(double dx, double dy) {
        trans.prependTranslation(dx, dy);
        repaint();
    }

    @FXML
    public void ZoomIn(MouseEvent e) {
        if (zoomLevel * 1.2 <= maxZoom && zoomLevel * 1.2 >= minZoom) {
            zoomLevel = zoomLevel * 1.2;
            Zoom(1.2, canvas.getHeight() / 2, canvas.getWidth() / 2);
            UpdateZoomLevel();
        }
    }

    @FXML
    public void ZoomOut(MouseEvent e) {
        if (zoomLevel * 0.9 <= maxZoom && zoomLevel * 0.9 >= minZoom) {
            zoomLevel = zoomLevel * 0.9;
            Zoom(0.9, canvas.getHeight() / 2, canvas.getWidth() / 2);
            UpdateZoomLevel();
        }
    }

    private void UpdateFpscounterLabel(int Fps) {
        FpsCounterLabel.setText(Fps + "");
    }

    private void UpdateAddresseLabel(String Address) {
        AddressLabel.setText(Address);
    }

    private RectHV viewRect(float biggestXDif, float biggestYdif) {
        try {
            Point2D min = trans.inverseTransform(0, 0);
            Point2D max = trans.inverseTransform(canvas.getWidth(), canvas.getHeight());
            float maxX, minX, maxY, minY;
            maxX = (float) max.getX() + biggestXDif / 2;
            minX = (float) min.getX() - biggestXDif / 2;
            maxY = (float) max.getY() + biggestYdif / 2;
            minY = (float) min.getY() - biggestYdif / 2;
            return new RectHV(maxX, minX, maxY, minY);
        } catch (NonInvertibleTransformException e) {
            return new RectHV(model.maxlon, model.minlon, model.maxlat, model.minlat);
        }
    }

    @FXML
    private void repaint() {
        gc.setTransform(new Affine());
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setTransform(trans);
        gc.setFillRule(NON_ZERO);
        double pixelwidth = 1 / Math.sqrt(Math.abs(trans.determinant()));
        gc.setLineWidth(pixelwidth);
        gc.setStroke(Color.GREY);

        if (colorBlindMode) gc.setFill(Color.rgb(100, 100, 100));
        else {
            gc.setFill(Color.rgb(239, 240, 213));
        }
        if (getZoomPercent() < 50) {
            for (var island : model.islandTree.range(viewRect(model.biggestXDifferenceIsland * 2,
                    model.biggestYDifferenceIsland * 2))) {
                LinePath islandLinpath = (LinePath) island;
                islandLinpath.draweveryother(gc);
                gc.fill();
            }
        } else {
            for (var island : model.islandTree.range(viewRect(model.biggestXDifferenceIsland * 2,
                    model.biggestYDifferenceIsland * 2))) {
                island.draw(gc);
                gc.fill();
            }
        }

        if (getZoomPercent() > 60) {
            if (colorBlindMode) gc.setFill(Color.rgb(120, 0, 250));
            else {
                gc.setFill(Color.LIGHTGREEN);
            }
            gc.setFillRule(EVEN_ODD);
            for (Drawable d : model.grassTree.range(viewRect(model.biggestXDifferenceGrass,
                    model.biggestYDifferenceGrass))) {
                d.draw(gc);
            }
            gc.setFill(Color.LIGHTBLUE);
            for (Drawable d : model.waterTree.range(viewRect(model.biggestXDifferenceWater,
                    model.biggestYDifferenceWater))) {
                d.draw(gc);
            }
        }

        if (getZoomPercent() > 50) {
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(pixelwidth * 1.3);
            if (model.highwayTree.root != null) {
                for (Drawable d : model.highwayTree.range(viewRect(model.biggestXDifferenceHighway,
                        model.biggestYDifferenceHighway))) {
                    d.draw(gc);
                }
            }

        }

        if (getZoomPercent() > 60) {
            if (colorBlindMode) {
                gc.setFill(Color.rgb(0, 0, 240));
            } else {
                gc.setFill(Color.GREY);
                gc.setStroke(Color.GREY);
            }
            if (model.buildingTree.root != null) {
                for (Drawable d : model.buildingTree.range(viewRect(model.biggestXDifferenceBuilding,
                        model.biggestYDifferenceBuilding))) {
                    d.draw(gc);
                }
            }
            if (colorBlindMode) {
                gc.setFill(Color.BEIGE);
            } else {
                gc.setStroke(Color.SADDLEBROWN);
            }
            gc.setLineWidth(pixelwidth * 0.7);
            if (model.footwayTree.root != null) {
                for (Drawable d : model.footwayTree.range(viewRect(model.biggestXDifferenceHighway,
                        model.biggestYDifferenceHighway))) {
                    d.draw(gc);
                }
            }
        }
        if (colorBlindMode) {
            gc.setFill(Color.BLACK);
        } else {
            gc.setStroke(Color.YELLOW);
        }
        gc.setLineWidth(pixelwidth * 2);
        if (model.motorWayTree.root != null) {
            for (Drawable d : model.motorWayTree.range(viewRect(model.biggestXDifferenceMotorway,
                    model.biggestYDifferenceMotorway))) {
                d.draw(gc);
            }
        }


        gc.setLineWidth(pixelwidth * 3);
        int poiCnt = 0;
        for (var poi : model.POIs) {

            if (model.POIs.get(poiCnt).getPoiSelected().equals("default")) {
                gc.setStroke(Color.PINK);
                gc.setLineWidth(pixelwidth * 5);
            }
            if (model.POIs.get(poiCnt).getPoiSelected().equals("home")) {
                gc.setStroke(Color.GREEN);
            }
            if (model.POIs.get(poiCnt).getPoiSelected().equals("school")) {
                gc.setStroke(Color.GREEN);
            }
            if (model.POIs.get(poiCnt).getPoiSelected().equals("work")) {
                gc.setStroke(Color.GREEN);
            }
            if (model.POIs.get(poiCnt).getPoiSelected().equals("friend")) {
                gc.setStroke(Color.GREEN);
            }
            poi.getRect(poi).draw(gc);
            poiCnt++;
        }
        gc.setStroke(Color.RED);
        if (model.patheresultline.getcoords().length > 0) {
            model.patheresultline.draw(gc);
        }
        if (circleFrom != null) {
            gc.setFill(Color.RED);
            double radius = circleFrom.getRadius() / (zoomLevel / 10000);
            gc.fillOval(circleFrom.getCenterX() - radius, circleFrom.getCenterY() - radius, 2 * radius,
                    2 * radius);
        }
        if (circleTo != null) {
            gc.setFill(Color.BLUE);
            double radius = circleTo.getRadius() / (zoomLevel / 10000);
            gc.fillOval(circleTo.getCenterX() - radius, circleTo.getCenterY() - radius, 2 * radius,
                    2 * radius);
        }
    }

    private void markAddress(boolean from) {
        if (from) {
            Address fromAddress = parseAddress(fromDestination);
            circleFrom = new CustomCircle(fromAddress.getLon(), fromAddress.getLat(), 0.0010);
            circleFrom.setAddress("From: " + fromAddress.toString());
            circleFrom.setFrom(true);

        } else {
            Address toAddress = parseAddress(toDestination);
            circleTo = new CustomCircle(toAddress.getLon(), toAddress.getLat(), 0.0010);
            circleTo.setAddress("To: " + toAddress.toString());
        }

    }

    private Address parseAddress(TextField inputField) {
        String input = inputField.getText();
        Address address = Address.parse(input);
        return model.addressArray.search(address);
    }

    @FXML
    void canvasMouseMoved(MouseEvent e) throws NonInvertibleTransformException {
        lastmouse = new Point2D(e.getX(), e.getY());
        if (getZoomPercent() > 80) {
            Point2D mouseCOORDS = trans.inverseTransform(e.getSceneX(), e.getSceneY());
            canvas.addEventHandler(MouseEvent.MOUSE_MOVED, mouseEvent -> UpdateAddresseLabel(
                    model.highwayTree.getNearestRoad((float) mouseCOORDS.getX(),
                            (float) mouseCOORDS.getY(), model.biggestXDifferenceHighway, model.biggestYDifferenceHighway)));
        }

    }

    private OSMNode addressGetNode(TextField inputField) {
        Address foundAddress = parseAddress(inputField);
        OSMNode shortestNode = null;
        double shortestDistance = Double.NaN;
        boolean notFound = true;
        for (Drawable d : model.highwayTree.range(new RectHV(foundAddress.getLon() +
                model.biggestXDifferenceHighway, foundAddress.getLon() - model.biggestXDifferenceHighway,
                foundAddress.getLat() + model.biggestYDifferenceHighway, foundAddress.getLat() -
                model.biggestYDifferenceHighway))) {
            if (foundAddress.getStreet().equals(((Highway) d).getName())) {
                notFound = false;
                Highway highway = (Highway) d;
                if (Double.isNaN(shortestDistance)) {
                    shortestDistance = highway.getLength(highway.getfirst(),
                            new OSMNode(foundAddress.getLon(), foundAddress.getLat()));
                }
                for (OSMNode node : highway.getcoords()) {
                    double distance = highway.getLength(node, new OSMNode(foundAddress.getLon(), foundAddress.getLat()));
                    if (shortestDistance >= distance) {
                        shortestDistance = distance;
                        shortestNode = node;
                    }
                }
            }

        }
        if (notFound) {
            shortestNode = model.highwayTree.getNearestNode(foundAddress.getLon(), foundAddress.getLat(),
                    model.biggestXDifferenceHighway, model.biggestYDifferenceHighway);
        }
        return shortestNode;
    }


    public void calcWay() {
        if (toDestination.getText().isEmpty() && !fromDestination.getText().isEmpty()) {
            markAddress(true);
            model.patheresultline.coords = new OSMNode[0];
            circleTo = null;
            Point2D centrum = new Point2D(canvas.getWidth() / 2, canvas.getHeight() / 2);
            Point2D fromDestinationPoint = trans.transform(addressGetNode(fromDestination).getx(),
                    addressGetNode(fromDestination).gety());
            pan(centrum.getX() - fromDestinationPoint.getX(), centrum.getY() - fromDestinationPoint.getY());

        } else if (fromDestination.getText().isEmpty() && !toDestination.getText().isEmpty()) {
            markAddress(false);
            circleFrom = null;
            model.patheresultline.coords = new OSMNode[0];
            Point2D centrum = new Point2D(canvas.getWidth() / 2, canvas.getHeight() / 2);
            Point2D toDestinationPoint = trans.transform(addressGetNode(toDestination).getx(),
                    addressGetNode(toDestination).gety());
            pan(centrum.getX() - toDestinationPoint.getX(), centrum.getY() - toDestinationPoint.getY());
        } else {
            OSMNode fromAddresse = addressGetNode(fromDestination);
            OSMNode toAddresse = addressGetNode(toDestination);
            model.fromNodeID = fromAddresse.getId();
            model.toNodeID = toAddresse.getId();
            model.shortestPath = new Dijkstra(model.g, model.getFromNodeID(), model.getToNodeID(), isDistance, roadType);
            String[] pathDir = model.getRoute(model.shortestPath.pathTo(model.getToNodeID()));
            setNavDirections(pathDir);

            markAddress(false);
            markAddress(true);
            Address address = parseAddress(fromDestination);
            Point2D center = new Point2D(canvas.getWidth() / 2, canvas.getHeight() / 2);
            Point2D newCenter = trans.transform(address.getLon(), address.getLat());
            pan(center.getX() - newCenter.getX(), center.getY() - newCenter.getY());
            repaint();
        }
    }

    @FXML
    public void load() {
        File file = new FileChooser().showOpenDialog(View.getStage());
        if (file != null)
            try {
                model.load(file);
                initialize();
            } catch (IOException | XMLStreamException | FactoryConfigurationError e1) {
                new Alert(Alert.AlertType.ERROR, "Could not load file");
            }
    }


    @FXML
    public void ColorBlindMode(javafx.event.ActionEvent e) {
        if (e.getSource().equals(redBtn)) {
            colorBlindMode = true;
            repaint();
        } else {
            colorBlindMode = false;
            repaint();
        }
    }

    @FXML
    public void save() {
        model.binSave();
    }

    private void setNavigationLabel(String direction) {
        Label navLabel = new Label(direction);
        navLabel.prefWidthProperty().bind(navigatonVbox.widthProperty());
        navLabel.setText(direction);
        navLabel.setWrapText(true);
        navLabel.setStyle("-fx-border-color: LightBlue");
        if (direction.length() > 34) {
            navLabel.prefHeight(navLabel.getHeight() * 2);
        }
        TextFlow tf = new TextFlow(navLabel);
        navigatonVbox.setAlignment(Pos.TOP_LEFT);
        navigatonVbox.getChildren().add(tf);
    }

    private void resetNavigationBox() {
        navigatonVbox.getChildren().clear();
    }

    private void setNavDirections(String[] pathDir) {
        resetNavigationBox();
        for (int i = 1; i < pathDir.length - 1; i++) {
            if (pathDir[i].length() > 34) {
                String[] specificDir = pathDir[i].split("to", 2);
                setNavigationLabel(specificDir[0] + "to" + specificDir[1]);
            } else {
                setNavigationLabel(pathDir[i]);
            }
        }
        if (pathDir[pathDir.length - 1].length() > 34) {
            String[] destination = pathDir[pathDir.length - 1].split("destination");
            setNavigationLabel(destination[0] + "destination" + destination[1]);
        } else {
            setNavigationLabel(pathDir[pathDir.length - 1]);
        }
    }

    @FXML
    public void setCyclingRouting() {
        isDistance = true;
        roadType = RoadType.PEDESTRIAN;
        cycleBtn.setStyle("-fx-border-color: red");
        carBtn.setStyle("-fx-border-color: grey");
    }

    @FXML
    public void setDrivingRouting() {
        isDistance = false;
        roadType = RoadType.CAR;
        carBtn.setStyle("-fx-border-color: red");
        cycleBtn.setStyle("-fx-border-color: grey");
    }
}