package smartgrid.impactanalysis;
import graph.LogicalCommunication;
import graph.NetworkEntity;
import graph.PhysicalConnection;
import input.EntityState;
import input.PowerState;
import input.ScenarioState;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import output.Cluster;
import output.EntityState;
import output.NoUplink;
import output.Online;
import output.OutputFactory;
import output.ScenarioResult;
import output.impl.OutputPackageImpl;
import smartgrid.helper.FileSystemHelper;
import smartgrid.model.helper.input.LoadInputModelConformityHelper;
import topo.ControlCenter;
import topo.SmartGridTopology;
import topo.SmartMeter;
public class DomainGraphAnalyzer extends ParadigmGraphAnalyzer {
    /**
     * For ExtensionPoints .. use this together with the init() Method
     */
    public DomainGraphAnalyzer() {
    }

    /**
     * The Constructor of the Class. For use with the old Impact Analysy only Version
     * {@link smartgrid.impactanalysis.ui}
     *
     * @deprecated It's now integrated in the SimControl Approach
     * @param outputPath
     * 		In case of using {@code public void analyze(...)} Method it is the Path to the
     * 		Outputfile
     */
    @Deprecated
    public DomainGraphAnalyzer(final String outputPath) {
        // Attention cloned from init() to be downward compatible
        this.internalMaxID = 0;
        this.powerStates = new HashMap<>();
        this.entityStates = new HashMap<>();
        this.controlCenters = new LinkedList<>();
        this.internalToExternalID = new HashMap<>();
        this.externalToInternalID = new HashMap<>();
        this.internalToCluster = new HashMap<>();
        this.logicalNodes = new LinkedList<>();
        this.controlCenterConnectivity = new HashMap<>();
        this.outputPath = outputPath;
        this.initDone = true;
        // Do it always with logical Connection
        this.ignoreLogicalConnections = false;
    }

    @Override
    protected ScenarioResult run(final SmartGridTopology smartGridTopo, final ScenarioState impactAnalysisInput) {
        assert this.initDone : "Init wasn't run! Run init() first !";
        CommonsGraphAnalyzer.LOG.debug("Start impact analysis");
        this.clearAll();
        if (!LoadInputModelConformityHelper.checkInputModelConformity(impactAnalysisInput, smartGridTopo)) {
            CommonsGraphAnalyzer.LOG.error("Input model is not conform to the current topo model");
        }
        // Szenario einlesen
        this.readStates(smartGridTopo, impactAnalysisInput);
        // now slowly build adjacent matrix
        this.adjacentMatrix = new double[this.internalMaxID][this.internalMaxID];
        this.logicalAdjacentMatrix = new double[this.internalMaxID][this.internalMaxID];
        for (int i = 0; i < this.internalMaxID; i++) {
            for (int j = 0; j < this.internalMaxID; j++) {
                this.adjacentMatrix[i][j] = 0;
                this.logicalAdjacentMatrix[i][j] = 0;
            }
        }
        // Es werden insgesamt 2 mal Cluster gebildet. Einmal �ber die
        // physikalischen Verbindungen, dann �ber die logischen
        this.readPhysicalConnections(smartGridTopo, impactAnalysisInput);
        // Run only if Not Ignore LogicalConnection ^= I want logical
        // Connections
        if (!this.ignoreLogicalConnections) {
            this.readLogicalConnections(smartGridTopo, impactAnalysisInput);
        }
        // Generates Result
        final ScenarioResult result = this.genOutputResult();
        result.setScenario(smartGridTopo);
        // result.setInput(impactAnalysisInput);
        CommonsGraphAnalyzer.LOG.debug("Working Directory = " + System.getProperty("user.dir"));
        return result;
    }

    /**
     * This Method does the Impact Analysis.
     *
     * @param scenario
     * 		the input scenario
     * @param state
     * 		the input state
     */
    protected void analyze(final SmartGridTopology scenario, final ScenarioState state) {
        // Generates Result
        final ScenarioResult result = this.run(scenario, state);
        if (result != null) {
            // Saves to File System
            FileSystemHelper.saveToFileSystem(result, this.outputPath);
        }
    }

    /**
     *
     *
     * @param scenario
     * 		
     * @param state
     * 		
     */
    protected void readStates(final SmartGridTopology scenario, final ScenarioState state) {
        CommonsGraphAnalyzer.LOG.debug("Start readStates");
        for (final EntityState s : state.getEntityStates()) {
            CommonsGraphAnalyzer.LOG.debug((((((("Class " + s.getOwner().getClass()) + " ID ") + s.getOwner().getId()) + " destroyed? ") + s.isIsDestroyed()) + " powersource ") + s.getOwner().getConnectedTo().toString());
            this.entityStates.put(s.getOwner().getName(), s);
            // if ((s.getOwner() instanceof ControlCenter) || (s.getOwner()
            // instanceof SmartMeter))
            // {
            this.logicalNodes.add(s.getOwner().getName());
            // }
            final int internalID = this.getInternID();
            this.externalToInternalID.put(s.getOwner().getName(), internalID);
            this.internalToExternalID.put(internalID, s.getOwner().getName());
            if (s.getOwner() instanceof ControlCenter) {
                this.controlCenters.add(s.getOwner().getName());
                CommonsGraphAnalyzer.LOG.debug("ControlCenter found: " + s.getOwner().getId());
            }
        }
        for (final PowerState p : state.getPowerStates()) {
            CommonsGraphAnalyzer.LOG.debug((((("Entity " + p.getOwner().getName()) + " ID ") + p.getOwner().getId()) + " powerOutage? ") + p.isPowerOutage());
            this.powerStates.put(p.getOwner().getName(), p);
        }
        CommonsGraphAnalyzer.LOG.debug("End readStates");
    }

    protected void readPhysicalConnections(final SmartGridTopology scenario, final ScenarioState state) {
        CommonsGraphAnalyzer.LOG.debug("Start readPhysicalConnections");
        final List<PhysicalConnection> pConns = scenario.getContainsPC();
        for (final PhysicalConnection p : pConns) {
            final NetworkEntity e1 = p.getLinks().get(0);
            final NetworkEntity e2 = p.getLinks().get(1);
            if (this.externalNodeIsWorking(String.valueOf(e1.getId())) && this.externalNodeIsWorking(String.valueOf(e2.getId()))) {
                final int internal1 = this.externalToInternalID.get(e1.getId());
                final int internal2 = this.externalToInternalID.get(e2.getId());
                this.adjacentMatrix[internal1][internal2] = 1;
                this.adjacentMatrix[internal2][internal1] = 1;
            }
        }
        // Building physical Cluster
        // LOG.debug(Matrix.toString(this.adjacentMatrix));
        CommonsGraphAnalyzer.LOG.debug("Validate clusteralgorithm");
        this.physicalClusters = Tarjan.getClusters(this.adjacentMatrix, this.internalToExternalID);
        for (final String controlID : this.controlCenters) {
            final int internalControlID = this.externalToInternalID.get(controlID);
            final Double[] connectionAvailable = new Double[this.internalMaxID + 1];
            for (int i = 0; i < connectionAvailable.length; i++) {
                connectionAvailable[i] = 0.0;
            }
            for (final List<Integer> l : this.physicalClusters) {
                if (l.contains(internalControlID)) {
                    for (final Integer n : l) {
                        connectionAvailable[n] = 1.0;
                    }
                }
            }
            this.controlCenterConnectivity.put(controlID, connectionAvailable);
        }
        CommonsGraphAnalyzer.LOG.debug("End readPhysicalConnections");
    }

    protected void readLogicalConnections(final SmartGridTopology scenario, final ScenarioState state) {
        CommonsGraphAnalyzer.LOG.debug("Start readLogicalConnections");
        // set logical adjacent
        final List<LogicalCommunication> lConns = scenario.getContainsLC();
        for (final LogicalCommunication l : lConns) {
            final String id1 = String.valueOf(l.getLinks().get(0).getId());
            final String id2 = String.valueOf(l.getLinks().get(1).getId());
            final int internal1 = this.externalToInternalID.get(id1);
            final int internal2 = this.externalToInternalID.get(id2);
            if (this.areInSameCluster(internal1, internal2, this.physicalClusters)) {
                this.logicalAdjacentMatrix[internal1][internal2] = 1;
                this.logicalAdjacentMatrix[internal2][internal1] = 1;
            }
        }
        // filthy Variant ! Better Solution?
        if (!this.ignoreLogicalConnections) {
            // find logical paths that work
            this.logicalClusters = Tarjan.getClusters(this.logicalAdjacentMatrix, this.internalToExternalID);
        } else {
            this.logicalClusters = this.physicalClusters;
        }
        // remove nodes that are not logical
        final List<List<Integer>> newClusters = new LinkedList<>();
        // Not every time "logical" Clusters see above
        for (final List<Integer> cluster : this.logicalClusters) {
            final List<Integer> newCluster = new LinkedList<>();
            for (final Integer i : cluster) {
                if (this.logicalNodes.contains(this.internalToExternalID.get(i))) {
                    newCluster.add(i);
                }
            }
            if (newCluster.size() > 0) {
                newClusters.add(newCluster);
            }
        }
        this.logicalClusters = newClusters;
        for (final String controlID : this.controlCenters) {
            final int internalControlID = this.externalToInternalID.get(controlID);
            final Double[] connectionAvailable = new Double[this.internalMaxID + 1];
            for (int i = 0; i < connectionAvailable.length; i++) {
                connectionAvailable[i] = 0.0;
            }
            for (final List<Integer> l : this.logicalClusters) {
                if (l.contains(internalControlID)) {
                    for (final Integer n : l) {
                        connectionAvailable[n] = 1.0;
                    }
                }
            }
            this.controlCenterConnectivity.put(controlID, connectionAvailable);
        }
        CommonsGraphAnalyzer.LOG.debug("End readLogicalConnections");
    }

    protected ScenarioResult genOutputResult() {
        OutputPackageImpl.init();
        final OutputFactory factory = OutputFactory.eINSTANCE;
        final ScenarioResult result = factory.createScenarioResult();
        // 
        if (this.ignoreLogicalConnections) {
            this.clusterCleaning(factory, result, this.physicalClusters);
        } else {
            this.clusterCleaning(factory, result, this.logicalClusters);
        }
        // Generate output for every node depending on connection status
        for (final String nodeID : this.logicalNodes) {
            CommonsGraphAnalyzer.LOG.debug("Generate output for node with id " + nodeID);
            EntityState state = null;
            final int internalNode = this.externalToInternalID.get(nodeID);
            final List<String> connectedCCs = new LinkedList<>();
            for (final String ccID : this.controlCenters) {
                if (this.controlCenterConnectivity.get(ccID)[internalNode] > 0) {
                    connectedCCs.add(ccID);
                }
            }// End Foreach ControlCenters

            if (this.externalNodeIsDestroyed(nodeID)) {
                state = factory.createDefect();
            } else if (!this.externalNodeHasPower(nodeID)) {
                state = factory.createNoPower();
            } else if (connectedCCs.size() == 0) {
                final NoUplink n = factory.createNoUplink();
                n.setBelongsToCluster(this.internalToCluster.get(internalNode));
                this.internalToCluster.get(internalNode).getHasEntities().add(n);
                // Passthrough IsHacked State from input into Output
                n.setIsHacked(this.externalNodeIsHacked(nodeID));
                state = n;
            } else {
                final Online s = factory.createOnline();
                s.setBelongsToCluster(this.internalToCluster.get(internalNode));
                this.internalToCluster.get(internalNode).getHasEntities().add(s);
                // s.setReachableControlCenters(connectedCCs.size()); - Thorsten
                // Passthrough IsHacked State from input into Output
                s.setIsHacked(this.externalNodeIsHacked(nodeID));
                state = s;
            }
            state.setOwner(this.entityStates.get(nodeID).getOwner());
            result.getStates().add(state);
        }// End Foreach LogicalNodes

        return result;
    }

    protected void clusterCleaning(final OutputFactory factory, final ScenarioResult result, final List<List<Integer>> clusterToClean) {
        for (final List<Integer> c : clusterToClean) {
            final Cluster cluster = factory.createCluster();
            // cluster.setSmartMeterCount(c.size());
            // long smCount = cluster.getHasEntities().stream().filter(s -> s.getOwner() instanceof
            // SmartMeter).count();
            int controlCentersInCluster = 0;
            int smCount = 0;
            for (final Integer i : c) {
                if (i != null) {
                    this.internalToCluster.put(i, cluster);
                    // check if its a controlCenter and increase
                    // controlCentersInCluster-Count
                    final String externalID = this.internalToExternalID.get(i);
                    if (this.externalNodeIsWorking(externalID)) {
                        if (this.controlCenters.contains(externalID)) {
                            controlCentersInCluster++;
                        } else if (this.entityStates.get(externalID).getOwner() instanceof SmartMeter) {
                            smCount++;
                        }
                    }
                }
            }
            cluster.setSmartMeterCount(smCount);
            cluster.setControlCenterCount(controlCentersInCluster);
            if ((smCount > 0) || (controlCentersInCluster > 0)) {
                result.getClusters().add(cluster);
            }
        }
    }
}