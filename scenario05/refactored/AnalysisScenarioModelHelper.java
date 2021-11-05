package smartgrid.helper;
import input.ScenarioState;
import input.impl.InputPackageImpl;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import output.Cluster;
import output.EntityState;
import output.On;
import output.ScenarioResult;
import topo.NetworkNode;
import var;
public class AnalysisScenarioModelHelper extends DomainScenarioModelHelper {
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
     * Searches in all EntityStates that On State that has the given NetworkEntity ID
     *
     * @param entityID
     * 		The Id for that to search the On State
     * @param myScenarioResult
     * 		In that "Container" are the States to search for
     * @return the On State with given ID
     */
    protected static On findEntityOnStateFromID(final String entityID, final ScenarioResult myScenarioResult) {
        boolean foundNodeID = false;
        On foundEntity = null;
        // Alternative with foreach Cluster foreach On Entities ?? (Double
        // foreach Loop!)
        for (final EntityState currentNode : myScenarioResult.getStates()) {
            // Using only Entities that is On
            if (currentNode.getOwner().getName().equals(entityID) && (currentNode instanceof On)) {
                foundEntity = ((On) (currentNode));
                foundNodeID = true;
                break;
            }
        }
        // ID don't exists or found Entity has no On State
        if (!foundNodeID) {
            CommonsScenarioModelHelper.LOG.info("Provided ID not found or found Entity has no On State");
        }
        return foundEntity;
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

    /**
     * Returns the NetworkEntity Id from given On State
     *
     * @param eintityOnState
     * 		The State
     * @return Id of the Network Entity
     */
    protected static String getIDfromEntityOnState(final On eintityOnState) {
        return eintityOnState.getOwner().getName();
    }

    /**
     * Filters the hardwired Logical Connections of the Neighbors out that because of their State
     * (e.g. destroyed) don't functions
     *
     * @param clusterToHack
     * 		the Target Cluster in which are all Nodes located that have a (transitive)
     * 		Connection between them
     * @param neighborIDList
     * 		the hardwired Logical Connections List
     * @return Nodes that are in the intersection of the above Container --> These are my direct
    alive Neighbors
     */
    protected static LinkedList<On> getNeighborsFromCluster(final Cluster clusterToHack, final LinkedList<String> neighborIDList) {
        final LinkedList<On> neighborOnList = new LinkedList<>();
        // Check whether this Neighbor is in my Cluster
        for (final On clusterNode : clusterToHack.getHasEntities()) {
            // Are my Neighbors at my Cluster ? Otherwise they are gone
            // (Destroyed or something)
            if (neighborIDList.contains(ScenarioModelHelper.getIDfromEntityOnState(clusterNode))) {
                neighborOnList.add(clusterNode);
            }
        }// Check neighbor loop

        return neighborOnList;
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