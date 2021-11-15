package smartgrid.impactanalysis;
import input.EntityState;
import input.PowerState;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import output.Cluster;
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
     *
     *
     * @return int
     */
    protected int getInternID() {
        final int result = this.internalMaxID;
        this.internalMaxID++;
        return result;
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
}