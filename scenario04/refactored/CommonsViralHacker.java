package smartgrid.attackersimulation;
import InitializationMapKeys.HACKING_SPEED_KEY;
import InitializationMapKeys.HACKING_STYLE_KEY;
import InitializationMapKeys.IGNORE_LOC_CON_KEY;
import InitializationMapKeys.ROOT_NODE_ID_KEY;
import couplingToICT.initializer.InitializationMapKeys;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import output.On;
import output.ScenarioResult;
import smartgrid.attackersimulation.strategies.FullyMeshedStrategy;
import smartgrid.helper.HashMapHelper;
import smartgrid.simcontrol.test.baselib.Constants;
import smartgrid.simcontrol.test.baselib.HackingType;
import smartgrid.simcontrol.test.baselib.coupling.IAttackerSimulation;
public class CommonsViralHacker implements IAttackerSimulation {
    protected static final Logger LOG = Logger.getLogger(ViralHacker.class);

    // state variables
    protected boolean initDone;

    // config variables
    protected int hackingSpeed;

    protected HackingType hackingStyle;

    protected boolean ignoreLogicalConnections;

    protected boolean firstRun = true;

    protected String rootNode;

    /**
     * For ExtensionPoints .. use this together with the init() Method
     */
    public CommonsViralHacker() {
        this.initDone = false;
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
        return false;
    }

    /**
     *
     *
     * @param scenario
     * 		
     */
    protected void fullyMeshedHacking(final ScenarioResult scenario) {
        final var hackedNodes = this.getHackedNodes(scenario);
        final var strategy = new FullyMeshedStrategy(this.hackingSpeed);
        for (final var rootNodeItem : hackedNodes) {
            strategy.hackNextNode(rootNodeItem);
        }
    }

    /**
     * helper method to get the hacked nodes before every timeStep
     *
     * @param scenario
     * 		
     * @return list of hacked nodes
     */
    protected List<On> getHackedNodes(final ScenarioResult scenario) {
        return scenario.getClusters().stream().flatMap(( e) -> e.getHasEntities().stream()).filter(( e) -> e.isIsHacked()).collect(Collectors.toList());
    }

    @Override
    protected String getName() {
        return "Viral Hacker";
    }

    @Deprecated
    protected void initForTest(String hackingStyle, String hackingSpeed) {
        this.hackingSpeed = Integer.parseInt(hackingSpeed);
        this.ignoreLogicalConnections = false;
        this.hackingStyle = HackingType.valueOf(hackingStyle);
        this.rootNode = Constants.DEFAULT_ROOT_NODE_ID;
        CommonsViralHacker.LOG.info("Hacking speed is: " + this.hackingSpeed);
        CommonsViralHacker.LOG.info("Hacking style is: " + this.hackingStyle);
        CommonsViralHacker.LOG.debug("Init For Testing done");
        this.initDone = true;
    }

    /**
     * {@inheritDoc }
     * <p>
     *
     * Remark Root NodeIDs {@link smartgrid.simcontrol.baselib.Constants} have to be List of String!
     */
    @Override
    protected void init(final Map<InitializationMapKeys, String> initMap) {
        this.hackingSpeed = Integer.parseInt(HashMapHelper.getAttribute(initMap, HACKING_SPEED_KEY, Constants.DEFAULT_HACKING_SPEED));
        this.ignoreLogicalConnections = Boolean.valueOf(HashMapHelper.getAttribute(initMap, IGNORE_LOC_CON_KEY, Constants.FALSE));
        this.rootNode = HashMapHelper.getAttribute(initMap, ROOT_NODE_ID_KEY, Constants.DEFAULT_ROOT_NODE_ID);
        this.hackingStyle = HackingType.valueOf(HashMapHelper.getAttribute(initMap, HACKING_STYLE_KEY, Constants.DEFAULT_HACKING_STYLE));
        CommonsViralHacker.LOG.info("Hacking speed is: " + this.hackingSpeed);
        CommonsViralHacker.LOG.info("Hacking style is: " + this.hackingStyle);
        CommonsViralHacker.LOG.debug("Init done");
        this.initDone = true;
    }
}