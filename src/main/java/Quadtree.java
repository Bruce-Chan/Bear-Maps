import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Quadtree {
    public static final int TILE_SIZE = 256;

    protected static class Node{
        Double upleftY;
        Double upleftX;
        Double lowrightY;
        Double lowrightX;
        int index;
        Double XDisPerPixel;
        Node[] children;
        int childIndex;
        String dir;
        int level;
        public Node(int n, Double ulY,Double ulX,Double lrY, Double lrX, String rootDir,int depth){
            if(n==0){
                dir = rootDir+"root";
            } else{
                dir = rootDir+n;
            }
            upleftY = ulY;
            upleftX = ulX;
            lowrightY = lrY;
            lowrightX = lrX;
            index = n;
            XDisPerPixel=(lowrightX - upleftX)/TILE_SIZE;
            childIndex = 0;
            children = new Node[4];
            level = depth;
        }

        private void addChild (Node n){
            if(childIndex<=3){
                children[childIndex] = n;
                childIndex++;
            } else{
                System.out.println("cannot add more children in this node");
            }
        }
    }

    Node root;
    String imgAddress;

    public Quadtree(Node r, int level, String imgroot){
        imgAddress = imgroot;
        root = autoAddChildren(r,level);

    }

    private Node autoAddChildren(Node n, int level){
        if(level==1){
            return n;
        }
        else {
            //uper left child
            int index = n.index*10+1;
            Double yInterval = (n.upleftY - n.lowrightY)/2;
            Double xInterval = (n.lowrightX - n.upleftX)/2;
            Double upleftY = n.upleftY;
            Double upleftX = n.upleftX;
            Double lowrightY = n.lowrightY+yInterval;
            Double lowrightX = n.lowrightX-xInterval;
            Node child = new Node(index,upleftY,upleftX,lowrightY,lowrightX,imgAddress,level-1);
            n.addChild(autoAddChildren(child,level-1));

            //upper right child
            index++;
            lowrightX+=xInterval;
            upleftX+=xInterval;
            child = new Node(index,upleftY,upleftX,lowrightY,lowrightX,imgAddress,level-1);
            n.addChild(autoAddChildren(child,level-1));

            //lower left child
            index++;
            lowrightX-=xInterval;
            upleftX-=xInterval;
            lowrightY-=yInterval;
            upleftY-=yInterval;
            child = new Node(index,upleftY,upleftX,lowrightY,lowrightX,imgAddress,level-1);
            n.addChild(autoAddChildren(child,level-1));

            //lower right child
            index++;
            lowrightX+=xInterval;
            upleftX+=xInterval;
            child = new Node(index,upleftY,upleftX,lowrightY,lowrightX,imgAddress,level-1);
            n.addChild(autoAddChildren(child,level-1));
        }
        return n;
    }

    public void getRaster(Map<String, Double> params, Node n, Double queryxDisPerPixel,
                          Map<String, Object> result, List<List<Node>> nodesLst){
        Double queryLrX = params.get("lrlon");
        Double queryUlX = params.get("ullon");
        Double queryUlY = params.get("ullat");
        Double queryLrY = params.get("lrlat");
        // check if this node include any region of the query box
        if(queryLrX>=n.upleftX && queryLrY <= n.upleftY
                && queryUlX <= n.lowrightX && queryUlY>=n.lowrightY){

            // check if this node have the greast LonDPP
            // the lonDPP is not good enough
            if(n.level != 1 && n.XDisPerPixel>queryxDisPerPixel){
                for(Node child: n.children){ // find suitable node in the children node
                    getRaster(params,child,queryxDisPerPixel,result,nodesLst);
                }
            }else{ // the lonDPP is good enough

                // set quety_success to true
                result.put("query_success",true);
                // set depth
                result.put("depth",root.level-n.level);
                // set ul_lon, ul_lat, lr_lon, lr_lat
                if(!result.containsKey("raster_ul_lon")){
                    result.put("raster_ul_lon", n.upleftX);
                    result.put("raster_ul_lat", n.upleftY);
                    result.put("raster_lr_lon", n.lowrightX);
                    result.put("raster_lr_lat", n.lowrightY);
                } else{
                    if((Double) result.get("raster_ul_lon")>n.upleftX){
                        result.put("raster_ul_lon", n.upleftX);
                    }
                    if((Double) result.get("raster_ul_lat")<n.upleftY){
                        result.put("raster_ul_lat", n.upleftY);
                    }
                    if((Double) result.get("raster_lr_lon")<n.lowrightX){
                        result.put("raster_lr_lon", n.lowrightX);
                    }
                    if((Double) result.get("raster_lr_lat")>n.lowrightY){
                        result.put("raster_lr_lat", n.lowrightY);
                    }
                }
                // add node into List
                if(nodesLst.size()==0){
                    List<Node> row = new ArrayList<>();
                    row.add(n);
                    nodesLst.add(row);
                } else {
                    int size = nodesLst.size();
                    int index = 0;
                    boolean existRow = false;
                    while(index<size && !existRow){
                        List<Node> currRow = nodesLst.get(index);
                        Node firstNode = currRow.get(0);
                        // find the row to contain this node
                        if(firstNode.upleftY.equals(n.upleftY)){
                            insertNodeIntoRow(n,currRow);
                            existRow = true;
                        }
                        index++;
                    }
                    // do not find the row to contain this node
                    // add a new row into the list
                    if(!existRow){
                        List<Node> row = new ArrayList<>();
                        row.add(n);
                        nodesLst.add(row);
                    }
                }
            }

        } else{
            return; //does not include any region of the query box
        }
    }

    // insert node into the correct position of the row
    private void insertNodeIntoRow(Node n, List<Node> row){
        for(int i=0; i<row.size();i++){
            Node compare = row.get(i);
            if(compare.upleftX>n.upleftX){
                // add this node into this position
                row.add(i,n);
                return;
            }
        }
        // add this node into the last position
        row.add(n);
    }


}
