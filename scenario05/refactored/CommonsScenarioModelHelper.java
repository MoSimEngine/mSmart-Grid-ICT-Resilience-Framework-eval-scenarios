package smartgrid.helper;
import java.util.LinkedList;
import java.util.Map;
import org.apache.log4j.Logger;
public class CommonsScenarioModelHelper {
    protected static Logger LOG = Logger.getLogger(ScenarioModelHelper.class);

    /**
     * Add another Value (Neighbor) to keys (Node) list
     *
     * @param IDLinks
     * 		Reference to the List Nodes and their Neighbors
     * @param key
     * 		the Node
     * @param value
     * 		the Neighbor from "Key" Node
     */
    protected static void addNeighbors(final Map<String, LinkedList<String>> IDLinks, final String key, final String value) {
        // Search "Key" --> Neighbor List "Values"
        LinkedList<String> myNeighbor = IDLinks.get(key);
        // Key already in Map
        if (myNeighbor != null) {
            myNeighbor.add(value);
        } else // Key not in Map (myNeighbor List == null)
        {
            // Construct New Linked List
            myNeighbor = new LinkedList<>();
            // Add fresh Neighbor "Value"
            myNeighbor.add(value);
            // Add Key (Node) and his first Neighbor in Map
            IDLinks.put(key, myNeighbor);
        }
    }
}