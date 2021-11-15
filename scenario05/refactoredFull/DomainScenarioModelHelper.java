package smartgrid.helper;
import graph.LogicalCommunication;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import topo.SmartGridTopology;
import topo.impl.TopoPackageImpl;
public class DomainScenarioModelHelper extends CommonsScenarioModelHelper {
    protected static SmartGridTopology loadScenario(final String path) {
        TopoPackageImpl.init();
        final ResourceSet resSet = new ResourceSetImpl();
        final Resource resource = resSet.getResource(URI.createFileURI(path), true);
        final EObject r = resource.getContents().get(0);
        CommonsScenarioModelHelper.LOG.debug("Class: " + r.getClass());
        return ((SmartGridTopology) (resource.getContents().get(0)));
    }

    /**
     * Generates a Map the Keys are one certain NodeId and the LinkedList <Integer> Element are all
     * Nodes to which Key has a logical Connection in the Scenario (Topology) Model @see
     * {@link smartgridtopo.Scenario}
     *
     * Attention these are the hardwired Connections ! the current EntityState of the Network Entity
     * is respected !!
     *
     * @param mySmartGridTopo
     * 		The Topology of the Model from the Analysis
     * @return a Map with NodeID as Key and LinkedList<"NodeID"> of Keys logical Neighbor as Value
     */
    protected static Map<String, LinkedList<String>> genNeighborMapbyID(final SmartGridTopology mySmartGridTopo) {
        /* Maps one Node (by ID) to his List of neighbor Nodes (also by ID) */
        final Map<String, LinkedList<String>> idLinks = new HashMap<>();
        for (final LogicalCommunication myLCom : mySmartGridTopo.getContainsLC()) {
            // Links 2! NetworkEntities together
            // Node u <--Link--> Node v
            final String key = myLCom.getLinks().get(0).getName();
            final String value = myLCom.getLinks().get(1).getName();
            // Add Node u --Link--> Node v
            ScenarioModelHelper.addNeighbors(idLinks, key, value);
            // Get Neighbors from other side of the Link
            // Add Node u <--Link-- Node v
            ScenarioModelHelper.addNeighbors(idLinks, value, key);
        }
        return idLinks;
    }
}