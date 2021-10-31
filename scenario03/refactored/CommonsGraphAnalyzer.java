package smartgrid.impactanalysis;
import InitializationMapKeys.IGNORE_LOC_CON_KEY;
import couplingToICT.initializer.InitializationMapKeys;
import input.EntityState;
import input.PowerState;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import output.Cluster;
import smartgrid.helper.HashMapHelper;
import smartgrid.simcontrol.test.baselib.Constants;
import smartgrid.simcontrol.test.baselib.coupling.IImpactAnalysis;
public class CommonsGraphAnalyzer implements IImpactAnalysis {
    protected static final Logger LOG = Logger.getLogger(GraphAnalyzer.class);

    protected Map<String, PowerState> powerStates;

    protected Map<String, EntityState> entityStates;

    protected Map<String, Integer> externalToInternalID;

    protected Map<Integer, String> internalToExternalID;

    protected Map<Integer, Cluster> internalToCluster;

    protected Map<String, Double[]> controlCenterConnectivity;

    protected String outputPath;

    protected boolean initDone = false;

    protected boolean ignoreLogicalConnections;

    protected List<List<Integer>> physicalClusters;

    protected List<List<Integer>> logicalClusters;

    // Die Adjazenzmatrizen waren noch vom vorherigen Ansatz
    protected double[][] adjacentMatrix;

    protected double[][] logicalAdjacentMatrix;

    protected List<String> logicalNodes;

    protected List<String> controlCenters;

    protected int internalMaxID;

    /**
     * For ExtensionPoints .. use this together with the init() Method
     */
    public CommonsGraphAnalyzer() {
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
    public CommonsGraphAnalyzer(final String outputPath) {
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

    /**
     * for Test purposes
     *
     * @param ignoreLogicalConnections
     * 		whether to ignore logical connections or not
     */
    @Deprecated
    protected void initForTesting(final boolean ignoreLogicalConnections) {
        this.internalMaxID = 0;
        this.powerStates = new HashMap<>();
        this.entityStates = new HashMap<>();
        this.controlCenters = new LinkedList<>();
        this.internalToExternalID = new HashMap<>();
        this.externalToInternalID = new HashMap<>();
        this.internalToCluster = new HashMap<>();
        this.logicalNodes = new LinkedList<>();
        this.controlCenterConnectivity = new HashMap<>();
        this.ignoreLogicalConnections = ignoreLogicalConnections;
        CommonsGraphAnalyzer.LOG.info("Ignoring logical connections: " + ignoreLogicalConnections);
        CommonsGraphAnalyzer.LOG.debug("Init done");
        this.initDone = true;
    }

    /**
     *
     *
     * @return int
     */
    protected int getInternID() {
        final int result = this.internalMaxID;
        this.internalMaxID++;
        return result;
    }

    protected boolean externalNodeIsDestroyed(final String id) {
        return this.entityStates.get(id).isIsDestroyed();
    }

    protected boolean externalNodeIsHacked(final String id) {
        return this.entityStates.get(id).isIsHacked();
    }

    protected boolean areInSameCluster(final int n, final int m, final List<List<Integer>> clusterList) {
        boolean result = false;
        boolean notFound = true;
        for (final List<Integer> l : clusterList) {
            if (notFound && l.contains(n)) {
                if (l.contains(m)) {
                    result = true;
                }
                notFound = false;
            }
        }
        return result;
    }

    protected void clearAll() {
        this.entityStates.clear();
        this.powerStates.clear();
        this.controlCenters.clear();
        this.internalToCluster.clear();
        this.externalToInternalID.clear();
        this.internalToExternalID.clear();
        this.controlCenterConnectivity.clear();
        this.logicalNodes.clear();
        this.internalMaxID = 0;
    }

    @Override
    protected String getName() {
        return "Graph Analyzer Impact Analysis";
    }

    @Override
    protected void init(final Map<InitializationMapKeys, String> initMap) {
        this.internalMaxID = 0;
        this.powerStates = new HashMap<>();
        this.entityStates = new HashMap<>();
        this.controlCenters = new LinkedList<>();
        this.internalToExternalID = new HashMap<>();
        this.externalToInternalID = new HashMap<>();
        this.internalToCluster = new HashMap<>();
        this.logicalNodes = new LinkedList<>();
        this.controlCenterConnectivity = new HashMap<>();
        final String ignoreLogicalConnectionsString = HashMapHelper.getAttribute(initMap, IGNORE_LOC_CON_KEY, Constants.FAIL);
        if (ignoreLogicalConnectionsString.equals(Constants.FAIL)) {
            // Checks whether DEFAULT_IGNORE_LOC_CON_KEY is true and assigns it
            this.ignoreLogicalConnections = Constants.TRUE.equals(Constants.DEFAULT_IGNORE_LOC_CON);
        } else {
            // checks whether ignoreLogicalConnectionsString is true and assigns it
            this.ignoreLogicalConnections = Constants.TRUE.equals(ignoreLogicalConnectionsString);
        }
        CommonsGraphAnalyzer.LOG.info("Ignoring logical connections: " + this.ignoreLogicalConnections);
        CommonsGraphAnalyzer.LOG.debug("Init done");
        this.initDone = true;
    }
}