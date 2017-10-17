import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {

    public static final double ROOT_ULLAT = 37.892195547244356, ROOT_ULLON = -122.2998046875,
            ROOT_LRLAT = 37.82280243352756, ROOT_LRLON = -122.2119140625;
    public static final String IMGFORMAT = ".png";
    private Quadtree qt;
    /** imgRoot is the name of the directory containing the images.
     *  You may not actually need this for your class. */
    public Rasterer(String imgRoot) {
        Quadtree.Node root = new Quadtree.Node(0,ROOT_ULLAT,ROOT_ULLON,ROOT_LRLAT,ROOT_LRLON,imgRoot,8);
        qt = new Quadtree(root,8,imgRoot);
    }

    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     * <p>
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     * </p>
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified:
     * "render_grid"   -> String[][], the files to display
     * "raster_ul_lon" -> Number, the bounding upper left longitude of the rastered image <br>
     * "raster_ul_lat" -> Number, the bounding upper left latitude of the rastered image <br>
     * "raster_lr_lon" -> Number, the bounding lower right longitude of the rastered image <br>
     * "raster_lr_lat" -> Number, the bounding lower right latitude of the rastered image <br>
     * "depth"         -> Number, the 1-indexed quadtree depth of the nodes of the rastered image.
     *                    Can also be interpreted as the length of the numbers in the image
     *                    string. <br>
     * "query_success" -> Boolean, whether the query was able to successfully complete. Don't
     *                    forget to set this to true! <br>
     * see REQUIRED_RASTER_REQUEST_PARAMS
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        Double queryLrlon = params.get("lrlon");
        Double queryUllon = params.get("ullon");
        Double queryUllat = params.get("ullat");
        Double queryLrlat = params.get("lrlat");
        if(queryLrlon<queryUllon || queryLrlat >queryUllat){
            System.out.println("the value of longitude and latitude is wrong");
        }
        Double width = params.get("w");
        Double queryxDisPerPixel = (queryLrlon-queryUllon)/width;
        Map<String, Object> results = new HashMap<>();
        List<List<Quadtree.Node>> tempLst= new ArrayList<>();
        qt.getRaster(params,qt.root,queryxDisPerPixel,results,tempLst);
        String[][] filesName = convertNodesListToArray(tempLst);
        results.put("render_grid",filesName);
        return results;
    }

    private String[][] convertNodesListToArray(List<List<Quadtree.Node>> lst){
        String[][] result = new String[lst.size()][];
        for(int i = 0; i<lst.size();i++){
            List<Quadtree.Node> currRow= lst.get(i);
            result[i] = new String[currRow.size()];
            for(int j = 0; j<currRow.size();j++){
                result[i][j] = currRow.get(j).dir+IMGFORMAT;
            }
        }
        return result;
    }

}
