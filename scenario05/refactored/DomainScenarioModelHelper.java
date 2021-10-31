package smartgrid.helper;
import graph.LogicalCommunication;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import output.ScenarioResult;
import topo.NetworkNode;
import topo.SmartGridTopology;
import topo.impl.TopoPackageImpl;
import var;
public class DomainScenarioModelHelper extends ParadigmScenarioModelHelper {
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

    /**
     * Select random root node
     */
    protected static String selectRandomRoot(boolean ignoreLogicalConnections, ScenarioResult scenario) {
        final Random random = new Random();
        if (ignoreLogicalConnections) {
            // filter for clusters with elements other than networknode
            final var clusterList = scenario.getClusters().parallelStream().filter(( e) -> (!e.getHasEntities().isEmpty()) && (!e.getHasEntities().stream().map(( nodes) -> nodes.getOwner()).allMatch(( nodes) -> nodes instanceof NetworkNode))).collect(Collectors.toList());
            final var selectedCluster = clusterList.get(random.nextInt(clusterList.size()));
            if (selectedCluster == null) {
                throw new IllegalStateException("Cluster can't be null");
            }
            final var listEntities = selectedCluster.getHasEntities().stream().filter(( e) -> !(e.getOwner() instanceof NetworkNode)).collect(Collectors.toList());
            return listEntities.get(random.nextInt(listEntities.size())).getOwner().getName();
        } else {
            final var clusterList = scenario.getClusters().parallelStream().filter(( e) -> !e.getHasEntities().isEmpty()).collect(Collectors.toList());
            final var selectedCluster = clusterList.get(random.nextInt(clusterList.size()));
            if (selectedCluster == null) {
                throw new IllegalStateException("Cluster can't be null");
            }
            final var listEntities = selectedCluster.getHasEntities();
            return listEntities.get(random.nextInt(listEntities.size())).getOwner().getName();
        }
    }
}