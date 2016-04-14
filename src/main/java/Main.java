import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.EntityType;
import de.topobyte.osm4j.core.model.iface.OsmNode;
import de.topobyte.osm4j.core.model.iface.OsmWay;
import de.topobyte.osm4j.pbf.seq.PbfIterator;
import jdk.internal.org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;


/**
 * Created by Dominik on 07.04.2016.
 */
public class Main {

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException {
        java.util.Scanner scanner = new java.util.Scanner(System.in);

        System.out.println("Zadejte klíč:");
        String key = scanner.nextLine();

        System.out.println("Zadejte hodnotu:");
        String value = scanner.nextLine();

        long stime = System.currentTimeMillis();
        OsmWay[] ways = new OsmWay[200000];
        Set<Long> nodesId = new TreeSet<>();

        int wayCounter = 0;

        File file = new File("C:/Users/Dominik/Documents/czech-republic-snapshot.osm.pbf");
        InputStream input = new FileInputStream(file);
        OsmIterator iterator = new PbfIterator(input, false);

        // nacitani ways a nodes id
        for (EntityContainer container : iterator) {
            if (container.getType() == EntityType.Way) {
                OsmWay way = (OsmWay) container.getEntity();

                for (int i = 0; i < way.getNumberOfTags(); i++) {
                    if(way.getTag(i).getKey().equals(key) && way.getTag(i).getValue().equals(value)){
                        ways[wayCounter++] = way;
                        for (int j = 0; j < way.getNumberOfNodes(); j++) {
                            nodesId.add(way.getNodeId(j));
                        }
                    }
                }
            }
        }
        System.out.println("Počet ways: " + wayCounter);
        System.out.println("Počet nodes: " + nodesId.size());

        long[] nodes = new long[nodesId.size()];
        double[]nodesLat = new double[nodesId.size()];
        double[]nodesLong = new double[nodesId.size()];

        //nacitani nodes
        input = new FileInputStream(file);
        iterator = new PbfIterator(input, false);

        int pointer = 0;

        for(long nodeId : nodesId){
            while(true){
                EntityContainer container = iterator.next();
                if (container.getType() == EntityType.Node && container.getEntity().getId() == nodeId) {
                    nodes[pointer] = nodeId;
                    OsmNode node = (OsmNode)container.getEntity();
                    nodesLat[pointer] = node.getLatitude();
                    nodesLong[pointer] = node.getLongitude();
                    pointer++;
                    break;
                }
            }
        }

        double total = 0;
        double total2 = 0;

        //prochazeni ways
        for (int i = 0; i < wayCounter - 1; i++) {
            OsmWay way = ways[i];
            double length = 0;
            double length2 = 0;

            long nodeId = way.getNodeId(0);
            double lon = 0;
            double lat = 0;

            for (int j = 0; j < nodes.length; j++) {
                if(nodeId==nodes[j]){
                    lon = nodesLong[j];
                    lat = nodesLat[j];
                    break;
                }
            }

            for (int j = 1; j < way.getNumberOfNodes(); j++) {
                long nodeId2 = way.getNodeId(j);
                double lon2 = 0;
                double lat2 = 0;

                for (int k = 0; k < nodes.length; k++) {
                    if(nodeId2==nodes[k]){
                        lon2 = nodesLong[k];
                        lat2 = nodesLat[k];
                        break;
                    }
                }

                length += distance(lat, lon, lat2, lon2);
                length2 += distance2(lat, lon, lat2, lon2);
                lon = lon2;
                lat = lat2;
            }
            total += length;
            total2 += length2;
        }

        System.out.println("Celková vzdálenost pro key: " + key + " value: " + value + " = " + total/1000 + " km");
        System.out.println("Celková vzdálenost pro key: " + key + " value: " + value + " = " + total2*1.609344 + " km");
        long etime = System.currentTimeMillis();
        System.out.println("Celkový čas = " + (etime-stime)/1000 + " vteřin");
    }

    public static double distance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadius * c;
    }

    private static double distance2(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }
    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

}


