import java.util.*;

/**
 * This class provides a shortestPath method for finding routes between two points
 * on the map. Start by using Dijkstra's, and if your code isn't fast enough for your
 * satisfaction (or the autograder), upgrade your implementation by switching it to A*.
 * Your code will probably not be fast enough to pass the autograder unless you use A*.
 * The difference between A* and Dijkstra's is only a couple of lines of code, and boils
 * down to the priority you use to order your vertices.
 */
public class Router {
    static Map<Long,Double> priorityMap;
    /**
     * Return a LinkedList of <code>Long</code>s representing the shortest path from st to dest, 
     * where the longs are node IDs.
     */
    public static LinkedList<Long> shortestPath(GraphDB g, double stlon, double stlat, double destlon, double destlat) {
        PriorityQueue<Long> pq = new PriorityQueue<>(new Comp());
        priorityMap = new HashMap<>();
        long sId = g.closest(stlon,stlat);
        long tId = g.closest(destlon,destlat);
        Map<Long,Double> disTo = new HashMap<>();
        Map<Long,Long> vertexTo = new HashMap<>();
        Set<Long> markedVerteies = new HashSet<>();


        disTo.put(sId,0.0);
        vertexTo.put(sId,sId);
        double estimateDis = g.distance(sId, tId);
        double priority = estimateDis*1000000000;
        priorityMap.put(sId, priority);

        pq.add(sId);
        findShortestPath(disTo,vertexTo,markedVerteies,pq,g,tId);

        List<Long> shortPath = new LinkedList<>();
        long curr = tId;
        int totalLen = 0;
        while(curr!=sId){
            shortPath.add(curr);
            curr = vertexTo.get(curr);
            totalLen++;
            if(totalLen>1000){
                System.out.println("cycle");
            }
        }
        shortPath.add(curr);
        Collections.reverse(shortPath);
        return (LinkedList<Long>) shortPath;
    }

    private static void findShortestPath( Map<Long,Double> disTo, Map<Long,Long> vertexTo,
                                  Set<Long> markedVerteies,
                                  PriorityQueue<Long> pq,GraphDB g, Long tID
    ){
        while(priorityMap.size()!=0) {
            long vID = pq.poll();
            if (vID==tID) {
                return;
            }
            if(markedVerteies.contains(vID)){
                continue;
            }
            markedVerteies.add(vID);
            GraphDB.Vertex v = g.verticesMap.get(vID);
            for (long w : v.adj) {
                if(vID==53055000){
                    GraphDB.Vertex check = g.verticesMap.get(w);
                    Object o = check.adj;
                }
                if (w != vertexTo.get(vID)) {
                    double disFromVToW = g.distance(vID, w);
                    double disFromSToW = disTo.get(vID) + disFromVToW;

                    //update the new distance from s to w
                    if (!disTo.containsKey(w) || disTo.get(w) > disFromSToW) {
                        disTo.put(w, disFromVToW);
                        vertexTo.put(w, vID);
                        double estimateDis = g.distance(w, tID);
                        // check marked

                        double priority = (disFromSToW + estimateDis) * 1000000000;
                        priorityMap.put(w, priority);
                        pq.add(w);

                    }
                }
            }
        }
    }

    private static class Comp implements Comparator<Long> {
        @Override
        public int compare(Long v1, Long v2) {
            return (int) (priorityMap.get(v1)-priorityMap.get(v2));
        }
    }

}
