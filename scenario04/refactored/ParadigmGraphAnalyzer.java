package smartgrid.impactanalysis;
import graph.PowerGridNode;
import input.EntityState;
import input.PowerState;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import output.Cluster;
public class ParadigmGraphAnalyzer extends CommonsGraphAnalyzer {
    /**
     * For ExtensionPoints .. use this together with the init() Method
     */
    public ParadigmGraphAnalyzer() {
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
    public ParadigmGraphAnalyzer(final String outputPath) {
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

    protected boolean externalNodeIsWorking(final String id) {
        return this.externalNodeHasPower(id) && (!this.externalNodeIsDestroyed(id));
    }

    protected boolean externalNodeHasPower(final String id) {
        boolean connected = false;
        try {
            for (final PowerGridNode pgn : this.entityStates.get(id).getOwner().getConnectedTo()) {
                if (!this.powerStates.get(pgn.getId()).isPowerOutage()) {
                    connected = true;
                }
            }
        } catch (final NullPointerException e) {
            CommonsGraphAnalyzer.LOG.error("Your input model may be not conform to the current topo model but hasn't set its Scenario attribute to a valid value");
        }
        return connected;
    }
}