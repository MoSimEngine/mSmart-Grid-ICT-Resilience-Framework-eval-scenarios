package smartgrid.attackersimulation;
import InitializationMapKeys.HACKING_SPEED_KEY;
import InitializationMapKeys.HACKING_STYLE_KEY;
import InitializationMapKeys.IGNORE_LOC_CON_KEY;
import InitializationMapKeys.ROOT_NODE_ID_KEY;
import couplingToICT.initializer.InitializationMapKeys;
import java.util.Map;
import org.apache.log4j.Logger;
import output.On;
import output.ScenarioResult;
import smartgrid.attackersimulation.strategies.BFSStrategy;
import smartgrid.attackersimulation.strategies.DFSStrategy;
import smartgrid.attackersimulation.strategies.FullyMeshedStrategy;
import smartgrid.attackersimulation.strategies.SingleStepAttackStrategies;
import smartgrid.helper.HashMapHelper;
import smartgrid.helper.ScenarioModelHelper;
import smartgrid.simcontrol.test.baselib.Constants;
import smartgrid.simcontrol.test.baselib.HackingType;
import smartgrid.simcontrol.test.baselib.coupling.IAttackerSimulation;
import var;
import static smartgrid.simcontrol.test.baselib.HackingType.BFS_HACKING;
import static smartgrid.simcontrol.test.baselib.HackingType.DFS_HACKING;
import static smartgrid.simcontrol.test.baselib.HackingType.FULLY_MESHED_HACKING;
public class CommonsLocalHacker implements IAttackerSimulation {
    protected static final Logger LOG = Logger.getLogger(LocalHacker.class);

    protected SingleStepAttackStrategies hackingTypes;

    protected String rootNodeID;// IDs stay the same over the whole Analysis


    protected On rootNodeState;// Reference Changes between runs!


    protected int hackingSpeed;

    protected boolean ignoreLogicalConnections;

    protected boolean initDone = false;

    protected ScenarioResult scenarioResult;

    /**
     * Default constructor is needed by the OSGi framework to be able to use the extension point
     */
    public CommonsLocalHacker() {
    }

    @Override
    protected boolean enableHackingSpeed() {
        return true;
    }

    @Override
    protected boolean enableLogicalConnections() {
        return true;
    }

    @Override
    protected boolean enableRootNode() {
        return true;
    }

    @Override
    protected String getName() {
        return "Local Hacker";
    }

    /**
     * setting the Root node
     */
    protected void rootNodeUpdate() {
        if (this.rootNodeID.equalsIgnoreCase(Constants.NO_ROOT_NODE_ID)) {
            CommonsLocalHacker.LOG.info("No root node specified.");
            this.rootNodeID = ScenarioModelHelper.selectRandomRoot(this.ignoreLogicalConnections, this.scenarioResult);
        }
        this.rootNodeState = ScenarioModelHelper.findEntityOnStateFromID(this.rootNodeID, this.scenarioResult);// update

        // state
        this.rootNodeState.setIsHacked(true);
    }

    @Deprecated
    protected void initForTest(String hackingStyle, String hackingSpeed, String rootNode) {
        this.hackingSpeed = Integer.parseInt(hackingSpeed);
        this.ignoreLogicalConnections = false;
        final var hackingType = HackingType.valueOf(hackingStyle);
        switch (hackingType) {
            case BFS_HACKING :
                this.hackingTypes = new BFSStrategy(this.ignoreLogicalConnections, this.hackingSpeed);
                break;
            case DFS_HACKING :
                this.hackingTypes = new DFSStrategy(this.ignoreLogicalConnections, this.hackingSpeed);
                break;
            case FULLY_MESHED_HACKING :
                this.hackingTypes = new FullyMeshedStrategy(this.hackingSpeed);
                break;
            default :
                assert false;
                break;
        }
        this.rootNodeID = rootNode;
        CommonsLocalHacker.LOG.info("Hacking speed is: " + this.hackingSpeed);
        CommonsLocalHacker.LOG.debug("Init For Testing done");
        this.initDone = true;
    }

    @Override
    protected void init(final Map<InitializationMapKeys, String> initMap) {
        this.hackingSpeed = Integer.parseInt(HashMapHelper.getAttribute(initMap, HACKING_SPEED_KEY, Constants.DEFAULT_HACKING_SPEED));
        this.ignoreLogicalConnections = Boolean.valueOf(HashMapHelper.getAttribute(initMap, IGNORE_LOC_CON_KEY, Constants.FALSE));
        this.rootNodeID = HashMapHelper.getAttribute(initMap, ROOT_NODE_ID_KEY, Constants.DEFAULT_ROOT_NODE_ID);
        final var hackingType = HackingType.valueOf(HashMapHelper.getAttribute(initMap, HACKING_STYLE_KEY, Constants.DEFAULT_HACKING_STYLE));
        switch (hackingType) {
            case BFS_HACKING :
                this.hackingTypes = new BFSStrategy(this.ignoreLogicalConnections, this.hackingSpeed);
                break;
            case DFS_HACKING :
                this.hackingTypes = new DFSStrategy(this.ignoreLogicalConnections, this.hackingSpeed);
                break;
            case FULLY_MESHED_HACKING :
                this.hackingTypes = new FullyMeshedStrategy(this.hackingSpeed);
                this.ignoreLogicalConnections = false;
                break;
            default :
                assert false;
                break;
        }
        this.initDone = true;
    }
}