package danmarksKort;

import danmarksKort.address.Address;
import danmarksKort.drawables.*;
import danmarksKort.mapelements.*;
import danmarksKort.routefinder.Dijkstra;
import danmarksKort.routefinder.RoadEdge;
import danmarksKort.routefinder.RoadType;
import danmarksKort.utility.EWDigraph;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static danmarksKort.mapelements.Elemtype.*;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

public class Model implements Serializable {

    public static Model instance = null;
    KDTree islandTree;

    /*
    Understående er de forskellige feltvariabler, disse er også de elementer der bliver gemt i vores binære-fil.
    */

    KDTree highwayTree;
    KDTree buildingTree;
    KDTree motorWayTree;
    KDTree footwayTree;
    KDTree grassTree;
    KDTree waterTree;
    danmarksKort.AddressArray addressArray;
    Dijkstra shortestPath;
    ArrayList<POI> POIs = new ArrayList<>();
    int toNodeID = -1, fromNodeID = -2;
    EWDigraph g;
    float minlat, minlon, maxlat, maxlon;
    float biggestXDifferenceHighway, biggestYDifferenceHighway,
            biggestXDifferenceMotorway, biggestYDifferenceMotorway,
            biggestXDifferenceBuilding, biggestYDifferenceBuilding,
            biggestXDifferenceIsland, biggestYDifferenceIsland,
            biggestXDifferenceGrass, biggestYDifferenceGrass,
            biggestXDifferenceWater, biggestYDifferenceWater;
    OSMWay pathresult = new OSMWay();
    LinePath patheresultline = new LinePath(pathresult);



    public Model() {
        try {
            ClassLoader classloader = Thread.currentThread().getContextClassLoader();
            InputStream input = classloader.getResourceAsStream("OSM.bin");
            File tempfile = File.createTempFile("test",".bin");
            FileOutputStream out = new FileOutputStream(tempfile);
            byte[] buffer = new byte[8 * 1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            input.close();
            load(tempfile);
            tempfile.delete();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Model getInstance() {
        if (instance == null) {
            instance = new Model();
        }
        return instance;
    }

    public int getFromNodeID() {
        return fromNodeID;
    }

    public void setFromNodeID(int fromNodeID) {
        this.fromNodeID = fromNodeID;
    }

    public int getToNodeID() {
        return toNodeID;
    }

    public void setToNodeID(int toNodeID) {
        this.toNodeID = toNodeID;
    }

    /**
     * Indlæser en fil, baseret på dens filtype. Virker på følgenede filtyper: .bin, .osm, .zip
     *
     * @param file Kan være ovenstående filtyper
     * @throws IOException
     * @throws XMLStreamException
     * @throws FactoryConfigurationError
     */

    public void load(File file) throws IOException, XMLStreamException, FactoryConfigurationError {
        String filename = file.getName();
        String fileExt = filename.substring(filename.lastIndexOf("."));
        switch (fileExt) {
            case ".bin":
                loadBinary(file);
                break;
            case ".osm":
                loadOSM(file);
                break;
            case ".zip":
                loadZip(file);
                break;
        }
    }


    /*
     * Parser en osm-fil, hertil laver de forskellige KD-træer, samt finder de største x og y forskelle for de
     * forskellige elementer (som f.eks. veje, bygninger og gras arealer)
     *
     * */
    private void loadOSM(File file) throws FileNotFoundException, XMLStreamException {
        var reader = XMLInputFactory.newFactory().createXMLStreamReader(new FileReader(file));
        int nmbrofnodes = 0;

        List<Drawable> islands = new ArrayList<>();
        List<Drawable> footwayList = new ArrayList<>();
        List<Drawable> motorWayList = new ArrayList<>();
        List<Drawable> builidngList = new ArrayList<>();
        List<Drawable> highwayList = new ArrayList<>();
        List<Drawable> grassList = new ArrayList<>();
        List<Drawable> waterList = new ArrayList<>();

        Map<Long, OSMNode> idToNode = new HashMap<>();
        Map<Long, OSMWay> idToWay = new HashMap<>();
        List<Address> addressTemp = new ArrayList<>();
        ArrayList<RoadEdge> edgelist;

        Boolean isOneWay = false;
        Boolean isMotorway = false;
        Boolean isFootway = false;
        double maxspeed = 50;

        Map<OSMNode, OSMWay> nodeToCoastline = new HashMap<>();

        OSMRelation currentRelation = null;
        OSMWay currentWay = null;
        Address currentAddress = null;
        String currentStreetName = "";
        Elemtype type = DEFAULT;
        long id = 0;
        edgelist = new ArrayList<>();
        while (reader.hasNext()) {
            reader.next();
            switch (reader.getEventType()) {
                case START_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "bounds":
                            minlat = -Float.parseFloat(reader.getAttributeValue(null, "maxlat"));
                            maxlon = 0.56f * Float.parseFloat(reader.getAttributeValue(null, "maxlon"));
                            maxlat = -Float.parseFloat(reader.getAttributeValue(null, "minlat"));
                            minlon = 0.56f * Float.parseFloat(reader.getAttributeValue(null, "minlon"));
                            break;

                        case "node":
                            id = Long.parseLong(reader.getAttributeValue(null, "id"));
                            float lat = Float.parseFloat(reader.getAttributeValue(null, "lat"));
                            float lon = Float.parseFloat(reader.getAttributeValue(null, "lon"));
                            var node = new OSMNode(0.56f * lon, -lat);
                            idToNode.put(id, node);
                            break;

                        case "way":
                            long ref = Long.parseLong(reader.getAttributeValue(null, "id"));
                            isOneWay = false;
                            isMotorway = false;
                            isFootway = false;
                            maxspeed = 50;
                            currentWay = new OSMWay();
                            idToWay.put(ref, currentWay);
                            type = DEFAULT;
                            break;

                        case "nd":
                            ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                            try {
                                currentWay.add(idToNode.get(ref));
                            } catch (NullPointerException e) {
                                System.out.println("Exception at handling <nd>");
                            }
                            break;

                        case "relation":
                            currentRelation = new OSMRelation();
                            type = DEFAULT;
                            break;

                        case "member":
                            var memberType = reader.getAttributeValue(null, "type");
                            ref = Long.parseLong(reader.getAttributeValue(null, "ref"));
                            if (memberType.equals("way")) {
                                if (idToWay.get(ref) != null) {
                                    currentRelation.add(idToWay.get(ref));
                                }

                            }
                            break;

                        case "tag":
                            var k = reader.getAttributeValue(null, "k");
                            var v = reader.getAttributeValue(null, "v");
                            if (v.equals("coastline")) {
                                type = COASTLINE;
                            }
                            switch (k) {
                                case "addr:city":
                                    currentAddress = new Address();
                                    currentAddress.setLat(idToNode.get(id).gety());
                                    currentAddress.setLon(idToNode.get(id).getx());
                                    currentAddress.city = v;
                                    break;
                                case "addr:housenumber":
                                    currentAddress.housenumber = v;
                                    break;
                                case "addr:municipality":
                                    currentAddress.municipality = v;
                                    break;
                                case "addr:postcode":
                                    currentAddress.postcode = v;
                                    break;
                                case "addr:street":
                                    currentAddress.street = v;
                                    addressTemp.add(currentAddress);
                                    break;
                                case "oneway":
                                    if (v == "yes") {
                                        isOneWay = true;
                                    }
                                    break;
                                case "name":
                                    currentStreetName = v;
                                    break;
                                case "highway":
                                    switch (v) {
                                        case "track":
                                            type = HIGHWAY;
                                            isFootway = true;
                                            break;
                                        case "path":
                                            type = HIGHWAY;
                                            isFootway = true;
                                            break;
                                        case "motorway_link":
                                            type = HIGHWAY;
                                            isMotorway = true;
                                            break;
                                        case "motorway":
                                            type = HIGHWAY;
                                            isMotorway = true;
                                            break;
                                        case "footway":
                                            isFootway = true;
                                            type = HIGHWAY;
                                            break;
                                        case "cycleway":
                                            type = DONTDRAW;
                                            break;
                                        default:
                                            type = HIGHWAY;
                                    }
                                    break;
                                case "natural":
                                    switch (v) {
                                        case "reef":
                                            type = WATER;
                                            break;
                                        case "water":
                                            type = WATER;
                                            break;
                                        case "grassland":
                                            type = GRASSLAND;
                                            break;
                                        case "wood":
                                            type = FOREST;
                                            break;


                                    }
                                    break;
                                case "landuse":
                                    switch (v) {
                                        case "recreation_ground":
                                            type = GRASS;
                                            break;
                                        case "forest":
                                            type = FOREST;
                                            break;
                                        case "farmland":
                                            type = FARMLAND;
                                            break;
                                        case "meadow":
                                            type = GRASS;
                                            break;
                                        case "farmyard":
                                            type = FOREST;
                                            break;
                                        case "grass":
                                            type = GRASS;
                                            break;
                                    }
                                    break;
                                case "building":
                                    type = BUILDING;

                                case "route":
                                    switch (v) {
                                        case "bicycle":
                                            type = DONTDRAW;
                                            break;
                                        case "ferry":
                                            type = DONTDRAW;
                                            break;
                                        case "power":
                                            type = DONTDRAW;
                                            break;
                                    }
                                    break;
                                case "maritime":
                                    type = DONTDRAW;
                                    break;
                                case "boundary":
                                    switch (v) {
                                        case "protected_area":
                                            type = DONTDRAW;
                                            break;
                                    }
                                    break;
                                case "maxspeed":
                                    try {
                                        maxspeed = (Double.parseDouble(v));
                                    } catch (NumberFormatException e) {

                                    }
                                    break;
                            }
                    }
                    break;
                case END_ELEMENT:
                    switch (reader.getLocalName()) {
                        case "way":
                            if (currentWay.size() != 0) {
                                switch (type) {
                                    case GRASS:
                                        grassList.add(new Grass(currentWay));
                                        break;
                                    case BUILDING:
                                        builidngList.add(new Building(currentWay));
                                        break;
                                    case HIGHWAY:

                                        Highway highway = new Highway(currentWay);
                                        highway.setName(currentStreetName);
                                        highway.setMotorWay(isMotorway);
                                        highway.setOneWay(isOneWay);
                                        highway.setMaxspeed(maxspeed);
                                        for (var node : highway.getcoords()) {
                                            if (node.getId() < 0) {
                                                node.setId(nmbrofnodes);
                                                nmbrofnodes++;
                                            }
                                        }
                                        if (isMotorway) {
                                            motorWayList.add(highway);
                                        }
                                        if (isFootway) {
                                            footwayList.add(highway);
                                        } else {
                                            highwayList.add(highway);
                                        }
                                        break;
                                    case FOREST:
                                        grassList.add(new Forest(currentWay));
                                        break;
                                    case GRASSLAND:
                                        grassList.add(new Grassland(currentWay));
                                        break;
                                    case WATER:
                                        waterList.add(new Water(currentWay));
                                        break;
                                    case COASTLINE:
                                        //Checking if there is a coastline before and after the current one (sharing same node).
                                        OSMWay coastLineBefore = null;
                                        if (nodeToCoastline.get(currentWay.firstPoint()) != null) {
                                            coastLineBefore = nodeToCoastline.remove(currentWay.firstPoint());
                                            nodeToCoastline.remove(coastLineBefore.lastPoint());
                                        }
                                        OSMWay coastLineAfter = null;
                                        if (nodeToCoastline.get(currentWay.lastPoint()) != null) {
                                            coastLineAfter = nodeToCoastline.remove(currentWay.lastPoint());
                                            nodeToCoastline.remove(coastLineAfter.lastPoint());
                                        }

                                        //Mering coastlines and putting them in the nodeToCoastline map
                                        currentWay = OSMWay.merge(OSMWay.merge(coastLineBefore, currentWay), coastLineAfter);
                                        nodeToCoastline.put(currentWay.firstPoint(), currentWay);
                                        nodeToCoastline.put(currentWay.lastPoint(), currentWay);
                                        break;
                                    case DEFAULT:
                                        break;
                                    case DONTDRAW:
                                        break;

                                }
                            }

                            break;
                        case "relation":
                            switch (type) {
                                case GRASS:
                                    grassList.add(new Grass(currentRelation));
                                    break;
                                case BUILDING:
                                    builidngList.add(new Building(currentRelation));
                                    break;
                                case FOREST:
                                    grassList.add(new Forest(currentRelation));
                                    break;
                                case GRASSLAND:
                                    grassList.add(new Grassland(currentRelation));
                                    break;
                                case WATER:
                                    waterList.add(new Water(currentRelation));
                                    break;
                                case DEFAULT:
                                    break;
                                case DONTDRAW:
                                    break;
                            }
                            break;
                        default:
                    }
            }
        }
        for (var entry : nodeToCoastline.entrySet()) {
            if (entry.getKey() == entry.getValue().lastPoint()) {
                islands.add(new LinePath(entry.getValue()));
            }
        }

        for (Drawable footway1 : footwayList) {
            Highway highway = (Highway) footway1;
            var currentHighway = highway.getcoords();
            for (int i = 1; i < currentHighway.length; i++) {
                edgelist.add(new RoadEdge(currentHighway[i - 1], currentHighway[i], highway.getOneWay(), highway.getLength(currentHighway[i - 1], currentHighway[i]), RoadType.PEDESTRIAN, highway.getName(), highway.getMaxspeed()));
            }
        }
        for (Drawable highway1 : highwayList) {
            Highway highway = (Highway) highway1;
            var currentHighway = highway.getcoords();
            for (int i = 1; i < currentHighway.length; i++) {
                edgelist.add(new RoadEdge(currentHighway[i - 1], currentHighway[i], highway.getOneWay(), highway.getLength(currentHighway[i - 1], currentHighway[i]), highway.getRoadType(), highway.getName(), highway.getMaxspeed()));
            }
        }
        for (Drawable motorWayTemp : motorWayList) {
            Highway motorWay = (Highway) motorWayTemp;
            var currentHighway = motorWay.getcoords();
            for (int i = 1; i < currentHighway.length; i++) {
                edgelist.add(new RoadEdge(currentHighway[i - 1], currentHighway[i], motorWay.getOneWay(), motorWay.getLength(currentHighway[i - 1], currentHighway[i]), motorWay.getRoadType(), motorWay.getName(), 130.0));
            }
        }
        g = new EWDigraph(nmbrofnodes);
        for (var edge : edgelist) {
            g.addEdge(edge);
        }

        biggestXDifferenceHighway = 0;
        biggestYDifferenceHighway = 0;
        for (Drawable highway : highwayList) {
            var currentHighway = highway.getcoords();
            for (int currentnode = 0; currentnode < currentHighway.length - 2; currentnode++) {
                for (int nextnode = currentnode + 1; nextnode < currentHighway.length - 1; nextnode++) {
                    float xdif = Math.abs(currentHighway[currentnode].getx() - currentHighway[nextnode].getx());
                    float ydif = Math.abs(currentHighway[currentnode].gety() - currentHighway[nextnode].gety());
                    if (Double.compare(xdif, biggestXDifferenceHighway) > 0) {
                        biggestXDifferenceHighway = xdif;
                    }
                    if (ydif > biggestYDifferenceHighway) {
                        biggestYDifferenceHighway = ydif;
                    }
                }
            }
        }
        biggestXDifferenceGrass = 0;
        biggestYDifferenceGrass = 0;
        for (Drawable grass : grassList) {
            var currentGrass = grass.getcoords();
            for (int currentnode = 0; currentnode < currentGrass.length - 2; currentnode++) {
                for (int nextnode = currentnode + 1; nextnode < currentGrass.length - 1; nextnode++) {
                    float xdif = Math.abs(currentGrass[currentnode].getx() - currentGrass[nextnode].getx());
                    float ydif = Math.abs(currentGrass[currentnode].gety() - currentGrass[nextnode].gety());
                    if (Double.compare(xdif, biggestXDifferenceGrass) > 0) {
                        biggestXDifferenceGrass = xdif;
                    }
                    if (ydif > biggestYDifferenceGrass) {
                        biggestYDifferenceGrass = ydif;
                    }
                }
            }
        }
        biggestXDifferenceMotorway = 0;
        biggestYDifferenceMotorway = 0;
        for (Drawable motorway : motorWayList) {
            var currentHighway = motorway.getcoords();
            for (int currentnode = 0; currentnode < currentHighway.length - 2; currentnode++) {
                for (int nextnode = currentnode + 1; nextnode < currentHighway.length - 1; nextnode++) {
                    float xdif = Math.abs(currentHighway[currentnode].getx() - currentHighway[nextnode].getx());
                    float ydif = Math.abs(currentHighway[currentnode].gety() - currentHighway[nextnode].gety());
                    if (Double.compare(xdif, biggestXDifferenceMotorway) > 0) {
                        biggestXDifferenceMotorway = xdif;
                    }
                    if (ydif > biggestYDifferenceMotorway) {
                        biggestYDifferenceMotorway = ydif;
                    }
                }
            }
        }
        biggestXDifferenceBuilding = 0;
        biggestYDifferenceBuilding = 0;
        for (Drawable building : builidngList) {
            var currentBuilding = building.getcoords();
            for (int currentnode = 0; currentnode < currentBuilding.length - 2; currentnode++) {
                for (int nextnode = currentnode + 1; nextnode < currentBuilding.length - 1; nextnode++) {
                    float xdif = Math.abs(currentBuilding[currentnode].getx() - currentBuilding[nextnode].getx());
                    float ydif = Math.abs(currentBuilding[currentnode].gety() - currentBuilding[nextnode].gety());
                    if (Double.compare(xdif, biggestXDifferenceBuilding) > 0) {
                        biggestXDifferenceBuilding = xdif;
                    }
                    if (ydif > biggestYDifferenceBuilding) {
                        biggestYDifferenceBuilding = ydif;
                    }
                }
            }
        }
        biggestXDifferenceIsland = 0;
        biggestYDifferenceIsland = 0;
        for (Drawable island : islands) {
            var currentIsland = island.getcoords();
            for (int currentnode = 0; currentnode < currentIsland.length - 2; currentnode++) {
                for (int nextnode = currentnode + 1; nextnode < currentIsland.length - 1; nextnode++) {
                    float xdif = Math.abs(currentIsland[currentnode].getx() - currentIsland[nextnode].getx());
                    float ydif = Math.abs(currentIsland[currentnode].gety() - currentIsland[nextnode].gety());
                    if (Double.compare(xdif, biggestXDifferenceIsland) > 0) {
                        biggestXDifferenceIsland = xdif;
                    }
                    if (ydif > biggestYDifferenceIsland) {
                        biggestYDifferenceIsland = ydif;
                    }
                }
            }
        }

        biggestXDifferenceWater = 0;
        biggestYDifferenceWater = 0;
        for (Drawable water : waterList) {
            var currentWater = water.getcoords();
            for (int currentnode = 0; currentnode < currentWater.length - 2; currentnode++) {
                for (int nextnode = currentnode + 1; nextnode < currentWater.length - 1; nextnode++) {
                    float xdif = Math.abs(currentWater[currentnode].getx() - currentWater[nextnode].getx());
                    float ydif = Math.abs(currentWater[currentnode].gety() - currentWater[nextnode].gety());
                    if (Double.compare(xdif, biggestXDifferenceWater) > 0) {
                        biggestXDifferenceWater = xdif;
                    }
                    if (ydif > biggestYDifferenceWater) {
                        biggestYDifferenceWater = ydif;
                    }
                }
            }
        }

        islandTree = new KDTree(islands, 0, new RectHV(maxlon, minlon, maxlat, minlat));
        footwayTree = new KDTree(footwayList, 0, new RectHV(maxlon, minlon, maxlat, minlat));
        highwayTree = new KDTree(highwayList, 0, new RectHV(maxlon, minlon, maxlat, minlat));
        buildingTree = new KDTree(builidngList, 0, new RectHV(maxlon, minlon, maxlat, minlat));
        motorWayTree = new KDTree(motorWayList, 0, new RectHV(maxlon, minlon, maxlat, minlat));
        grassTree = new KDTree(grassList, 0, new RectHV(maxlon, minlon, maxlat, minlat));
        waterTree = new KDTree(waterList, 0, new RectHV(maxlon, minlon, maxlat, minlat));

        addressArray = new AddressArray(addressTemp);
        addressArray.sort();


    }


    /**
     * Laver en routevejledning basseret på vores shortestPath
     *
     * @param roadEdges En liste af edges fra shortestPath.
     * @return En String array, der indeholder en route vejledning, for vores nuværende shortestPath.
     */
    public String[] getRoute(ArrayList<RoadEdge> roadEdges) {
        Collections.reverse(roadEdges);
        pathresult = new OSMWay();
        StringBuilder pathDirection = new StringBuilder();
        double pathToDist = 0;
        double distanceToNextRoad = 0;
        for (int i = 0; i < roadEdges.size(); i++) {
            RoadEdge thisEdge = roadEdges.get(i);
            double currentEdgeLength = thisEdge.getValue(true);
            distanceToNextRoad = distanceToNextRoad + currentEdgeLength;
            if (i + 1 < roadEdges.size()) {
                RoadEdge nextEdge = roadEdges.get(i + 1);
                pathresult.add(thisEdge.getFrom());
                if (!thisEdge.getRoadName().equals(nextEdge.getRoadName())) {
                    pathToDist += distanceToNextRoad;
                    if (distanceToNextRoad > 1) {
                        pathDirection.append("In ").append((Math.round(distanceToNextRoad * 100.0)) / 100.0)
                                .append(" km turn to ").append(thisEdge.getRoadName()).append(",");
                    } else {
                        pathDirection.append("In ").append((Math.round(distanceToNextRoad * 1000 * .1)) / .1)
                                .append(" m turn to ").append(thisEdge.getRoadName()).append(",");
                    }
                    distanceToNextRoad = 0;
                }
            }
            if (i + 1 == roadEdges.size()) {
                double distanceToDestination = (shortestPath.distTo(getToNodeID())) - (shortestPath.distTo(thisEdge.getFrom().getId()) * thisEdge.getMaxspeed()) - pathToDist;
                if (distanceToDestination > 1) {
                    pathDirection.append("You will reach your destination in ").append((Math.round(distanceToNextRoad * 100.0)) / 100.0)
                            .append(" km").append(",");
                } else {
                    pathDirection.append("You will reach your destination in ").append((Math.round(distanceToNextRoad * 1000 * .1)) / .1)
                            .append(" m").append(",");
                }
            }
        }

        patheresultline = new LinePath(pathresult);
        return pathDirection.toString().split(",");
    }

    private void loadBinary(File file) throws IOException {
        try (var in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            try {
                islandTree = (KDTree) in.readObject();
                highwayTree = (KDTree) in.readObject();
                buildingTree = (KDTree) in.readObject();
                motorWayTree = (KDTree) in.readObject();
                footwayTree = (KDTree) in.readObject();
                grassTree = (KDTree) in.readObject();
                waterTree = (KDTree) in.readObject();
                addressArray = (AddressArray) in.readObject();
                shortestPath = (Dijkstra) in.readObject();
                POIs = (ArrayList<POI>) in.readObject();
                toNodeID = (int) in.readObject();
                fromNodeID = (int) in.readObject();
                g = (EWDigraph) in.readObject();
                minlat = (float) in.readObject();
                minlon = (float) in.readObject();
                maxlat = (float) in.readObject();
                maxlon = (float) in.readObject();
                biggestXDifferenceHighway = (float) in.readObject();
                biggestYDifferenceHighway = (float) in.readObject();
                biggestXDifferenceMotorway = (float) in.readObject();
                biggestYDifferenceMotorway = (float) in.readObject();
                biggestXDifferenceBuilding = (float) in.readObject();
                biggestYDifferenceBuilding = (float) in.readObject();
                biggestXDifferenceIsland = (float) in.readObject();
                biggestYDifferenceIsland = (float) in.readObject();
                biggestXDifferenceGrass = (float) in.readObject();
                biggestYDifferenceGrass = (float) in.readObject();
                biggestXDifferenceWater = (float) in.readObject();
                biggestYDifferenceWater = (float) in.readObject();
                pathresult = (OSMWay) in.readObject();
                patheresultline = (LinePath) in.readObject();

            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void loadZip(File zipFile) throws IOException {
        File tempFile = File.createTempFile("zipOSM", ".osm");
        byte[] buffer = new byte[1048];
        String zipLocation = zipFile.getAbsolutePath();
        FileInputStream input;
        try{
            input = new FileInputStream(zipLocation);
            ZipInputStream zinput = new ZipInputStream(input);
            ZipEntry ze = zinput.getNextEntry();
            while(ze != null){
                FileOutputStream output = new FileOutputStream(tempFile);
                int bytesRead;
                while ((bytesRead = zinput.read(buffer)) > 0){
                    output.write(buffer,0,bytesRead);
                }
                output.close();
                zinput.closeEntry();
                ze = zinput.getNextEntry();
            }
            loadOSM(tempFile);
            tempFile.delete();
            zinput.closeEntry();
            zinput.close();
            input.close();
        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }
    }


    /**
     * Kalder WriteObjectToFile metoden. som skriver til en .bin fil.
     */
    public void binSave() {
        WriteObjectToFile();
    }

    private void WriteObjectToFile() {
        String filename = "OSM.bin";
        String workingDirectory = System.getProperty("user.dir");
        File file = new File(workingDirectory, filename);
        try {
            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(islandTree);
            objectOut.writeObject(highwayTree);
            objectOut.writeObject(buildingTree);
            objectOut.writeObject(motorWayTree);
            objectOut.writeObject(footwayTree);
            objectOut.writeObject(grassTree);
            objectOut.writeObject(waterTree);
            objectOut.writeObject(addressArray);
            objectOut.writeObject(shortestPath);
            objectOut.writeObject(POIs);
            objectOut.writeObject(toNodeID);
            objectOut.writeObject(fromNodeID);
            objectOut.writeObject(g);
            objectOut.writeObject(minlat);
            objectOut.writeObject(minlon);
            objectOut.writeObject(maxlat);
            objectOut.writeObject(maxlon);
            objectOut.writeObject(biggestXDifferenceHighway);
            objectOut.writeObject(biggestYDifferenceHighway);
            objectOut.writeObject(biggestXDifferenceMotorway);
            objectOut.writeObject(biggestYDifferenceMotorway);
            objectOut.writeObject(biggestXDifferenceBuilding);
            objectOut.writeObject(biggestYDifferenceBuilding);
            objectOut.writeObject(biggestXDifferenceIsland);
            objectOut.writeObject(biggestYDifferenceIsland);
            objectOut.writeObject(biggestXDifferenceGrass);
            objectOut.writeObject(biggestYDifferenceGrass);
            objectOut.writeObject(biggestXDifferenceWater);
            objectOut.writeObject(biggestYDifferenceWater);
            objectOut.writeObject(pathresult);
            objectOut.writeObject(patheresultline);

            objectOut.close();
            System.out.println("Final filepath : " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}