package smartgrid.attackersimulation.strategies;
import graph.CommunicatingEntity;
import graph.LogicalCommunication;
import graph.NetworkEntity;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import output.Cluster;
import output.On;
public abstract class AnalysisAttackStrategies extends CommonsAttackStrategies {
    public AnalysisAttackStrategies(final boolean ignoreLogicalConnections, final int hackingSpeed) {
        this.ignoreLogicalConnections = ignoreLogicalConnections;
        this.hackingSpeed = hackingSpeed;
    }

    protected Set<On> getConnected(final Cluster cluster, final On node) {
        final var rootNode = node.getOwner();
        Set<NetworkEntity> nextNetworkEntities;
        if (this.ignoreLogicalConnections) {
            nextNetworkEntities = rootNode.getLinkedBy().stream().flatMap(( e) -> e.getLinks().stream()).distinct().collect(Collectors.toSet());
        } else if (rootNode instanceof CommunicatingEntity) {
            nextNetworkEntities = new HashSet<>();
            for (LogicalCommunication logConn : ((CommunicatingEntity) (rootNode)).getCommunicatesBy()) {
                for (CommunicatingEntity networkEntity : logConn.getLinks()) {
                    nextNetworkEntities.add(networkEntity);
                }
            }
        } else {
            nextNetworkEntities = new HashSet<>();
        }
        nextNetworkEntities.remove(rootNode);// remove rootNode

        Set<On> conenctedNodes = new HashSet<>();
        for (On onEntity : cluster.getHasEntities()) {
            if (nextNetworkEntities.contains(onEntity.getOwner())) {
                conenctedNodes.add(onEntity);
            }
        }
        return conenctedNodes;
    }

    protected abstract void hackNextNode(On rootNodeState);
}