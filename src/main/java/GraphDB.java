import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.*;

/**
 * Graph for storing all of the intersection (vertex) and road (edge) information.
 * Uses your GraphBuildingHandler to convert the XML files into a graph. Your
 * code must include the vertices, adjacent, distance, closest, lat, and lon
 * methods. You'll also need to include instance variables and methods for
 * modifying the graph (e.g. addNode and addEdge).
 *
 * @author Alan Yao, Josh Hug
 */
public class GraphDB {
    /** Your instance variables for storing the graph. You should consider
     * creating helper classes, e.g. Node, Edge, etc. */
    public Map<Long,Vertex> verticesMap = new HashMap<>();
    private Map<Long,Edge> edgesMap = new HashMap<>();
    public TrieST<Location> locationTrieST = new TrieST<>();
    /**
     * Example constructor shows how to create and start an XML parser.
     * You do not need to modify this constructor, but you're welcome to do so.
     * @param dbPath Path to the XML file to be parsed.
     */
    public GraphDB(String dbPath) {
        try {
            File inputFile = new File(dbPath);
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            GraphBuildingHandler gbh = new GraphBuildingHandler(this);
            saxParser.parse(inputFile, gbh);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        clean();
    }


    /**
     * Helper to process strings into their "cleaned" form, ignoring punctuation and capitalization.
     * @param s Input string.
     * @return Cleaned string.
     */
    static String cleanString(String s) {
        return s.replaceAll("[^a-zA-Z ]", "").toLowerCase();
    }

    /**
     *  Remove nodes with no connections from the graph.
     *  While this does not guarantee that any two nodes in the remaining graph are connected,
     *  we can reasonably assume this since typically roads are connected.
     */
    private void clean() {
        //Vertex[] vertices = (Vertex[]) verticesMap.values().toArray();
        HashSet<Long> removeItemIDs = new HashSet<>();
        for(Vertex v : verticesMap.values()){
            // if nothing is connecting to this vertex, then delete it from the map
            if(v.adj.size()==0){
                removeItemIDs.add(v.id);
            }
        }
        verticesMap.keySet().removeAll(removeItemIDs);
    }

    /** Returns an iterable of all vertex IDs in the graph. */
    Iterable<Long> vertices() {
        //YOUR CODE HERE, this currently returns only an empty list.
        return verticesMap.keySet();
    }

    /** Returns ids of all vertices adjacent to v. */
    Iterable<Long> adjacent(long v) {
        return verticesMap.get(v).adj;
    }

    /** Returns the Euclidean distance between vertices v and w, where Euclidean distance
     *  is defined as sqrt( (lonV - lonV)^2 + (latV - latV)^2 ). */
    double distance(long v, long w) {
        Vertex vertexV = verticesMap.get(v);
        Vertex vertexW = verticesMap.get(w);
        double lonV = vertexV.lon;
        double lonW = vertexW.lon;
        double latV = vertexV.lat;
        double latW = vertexW.lat;
        return Math.sqrt( Math.pow((lonV-lonW),2) + Math.pow((latV-latW),2));
    }

    /** Returns the vertex id closest to the given longitude and latitude. */
    long closest(double lon, double lat) {
        if(verticesMap.keySet().size()==0){
            System.out.println("Nothing inside the Graph, cannot find the closet vertex");
        }
        List<Vertex> vertices = new ArrayList<>(verticesMap.values());
        Vertex first = vertices.get(0);
        long result = first.id;
        double lonDisPow = Math.pow(first.lon-lon,2);
        double latDisPow = Math.pow(first.lat-lat,2);
        for(Vertex v: vertices){
            double currLonDisPow = Math.pow((v.lon-lon),2);
            double currLatDisPow = Math.pow((v.lat-lat),2);
            if(v.id == 53042711){
            }
            double totalCurrDis = Math.sqrt(currLatDisPow+currLonDisPow);
            double totalLastDis = Math.sqrt(lonDisPow+latDisPow);
            if(totalCurrDis<totalLastDis){
                lonDisPow = currLonDisPow;
                latDisPow = currLatDisPow;
                result = v.id;
            }
        }
        return result;
    }

    /** Longitude of vertex v. */
    double lon(long v) {
        return verticesMap.get(v).lon;
    }

    /** Latitude of vertex v. */
    double lat(long v) {
        return verticesMap.get(v).lat;
    }

    void add_vertex(long id, double lon, double lat){
        Vertex v = new Vertex(id,lon,lat);
        verticesMap.put(v.id,v);
    }
    void delete_vertex(long id){
        if(verticesMap.containsKey(id)){
            verticesMap.remove(id);
        }
    }
    void add_edge(Long id, List<Long> vertexList){
        Edge e = new Edge(id,vertexList);
        edgesMap.put(id,e);
        for(int i = 0; i < vertexList.size()-1; i++){
            long v1ID = vertexList.get(i);
            long v2ID = vertexList.get(i+1);
            Vertex v1 = verticesMap.get(v1ID);
            Vertex v2 = verticesMap.get(v2ID);
            v1.connectTo(v2ID);
            v2.connectTo(v1ID);
        }
    }


    static class Vertex{
        long id;
        String name;
        double lon;
        double lat;
        List<Long> adj;

        Vertex(long id, double lon, double lat){
            this.id = id;
            this.lon = lon;
            this.lat = lat;
            this.adj = new ArrayList<>();
        }

        void setName(String name){
            this.name = name;
        }

        void connectTo(long vertexId){
            adj.add(vertexId);
        }
    }

    static class Edge{
        private long id;
        private String name;
        private List<Long> vertexList;
        Edge(long name, List<Long> vertexList){
            this.id = id;
            this.vertexList = vertexList;
        }
        void setName(String name){
            this.name = name;
        }
    }

    static class Location{
        long id;
        String name;
        double lon;
        double lat;
        Location(long id, double lon, double lat, String name){
            this.id = id;
            this.name = name;
            this.lon = lon;
            this.lat = lat;
        }
    }
}
