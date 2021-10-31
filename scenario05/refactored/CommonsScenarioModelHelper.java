package smartgrid.helper;
import input.ScenarioState;
import input.impl.InputPackageImpl;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import output.EntityState;
import output.On;
public class CommonsScenarioModelHelper {
    protected static Logger LOG = Logger.getLogger(ScenarioModelHelper.class);

    protected static ScenarioState loadInput(final String path) {
        try {
            InputPackageImpl.init();
        } catch (final Exception e) {
            CommonsScenarioModelHelper.LOG.warn("Exception thrown in SmartgridinputPackageImpl.init", e);
        }
        final ResourceSet resSet = new ResourceSetImpl();
        final Resource resource = resSet.getResource(URI.createFileURI(path), true);
        final EObject r = resource.getContents().get(0);
        CommonsScenarioModelHelper.LOG.debug("Class: " + r.getClass());
        return ((ScenarioState) (resource.getContents().get(0)));
    }

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

    protected static List<On> getHackedNodes(final List<EntityState> states) {
        final List<On> on = new ArrayList<>();
        for (final EntityState state : states) {
            if ((state instanceof On) && ((On) (state)).isIsHacked()) {
                on.add(((On) (state)));
            }
        }
        return on;
    }
}