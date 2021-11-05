package smartgrid.attackersimulation;
import java.util.List;
import java.util.stream.Collectors;
import output.On;
import output.ScenarioResult;
import smartgrid.attackersimulation.strategies.BFSStrategy;
import smartgrid.attackersimulation.strategies.FullyMeshedStrategy;
import smartgrid.helper.AnalysisScenarioModelHelper;
import smartgrid.simcontrol.test.baselib.Constants;
import topo.SmartGridTopology;
import static smartgrid.simcontrol.test.baselib.HackingType.FULLY_MESHED_HACKING;
import static smartgrid.simcontrol.test.baselib.HackingType.STANDARD_HACKING;
public class AnalysisViralHacker extends CommonsViralHacker {
    /**
     * For ExtensionPoints .. use this together with the init() Method
     */
    public AnalysisViralHacker() {
        this.initDone = false;
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

    /**
     * Run the attacker simulation.
     * If there is no root node defined, it will pick up any hacked node
     * if there no hacked nodes it will randomly hack a one and takes it as its root node
     */
    @Override
    protected ScenarioResult run(final SmartGridTopology topo, final ScenarioResult scenario) {
        if (!this.initDone) {
            throw new IllegalStateException("ViralHacker not initialization. Run init()");
        }
        if (this.firstRun) {
            if (this.rootNode.equals(Constants.DEFAULT_ROOT_NODE_ID) && (getHackedNodes(scenario).size() == 0)) {
                this.rootNode = AnalysisScenarioModelHelper.selectRandomRoot(this.ignoreLogicalConnections, scenario);
            } else if (this.rootNode.equals(Constants.DEFAULT_ROOT_NODE_ID) && (getHackedNodes(scenario).size() != 0)) {
                this.rootNode = String.valueOf(getHackedNodes(scenario).get(0).getOwner().getId());
            }
            final var rootState = AnalysisScenarioModelHelper.findEntityOnStateFromID(this.rootNode, scenario);
            rootState.setIsHacked(true);
            this.firstRun = !this.firstRun;
        }
        CommonsViralHacker.LOG.debug("Start Hacking with Viral Hacker");
        switch (this.hackingStyle) {
            case STANDARD_HACKING :
                this.standardHacking(topo, scenario);
                break;
            case FULLY_MESHED_HACKING :
                this.fullyMeshedHacking(scenario);
                break;
            default :
                throw new IllegalStateException(this.hackingStyle + " not applicable for ViralHacker");
        }
        return scenario;
    }

    /**
     * Standard hacking means that every node will search for the nearest node to it to hack
     *
     * @param topo
     * 		the topology
     * @param impactAnalysisOutput
     * 		the output of the impact analysis
     */
    protected void standardHacking(final SmartGridTopology topo, final ScenarioResult impactAnalysisOutput) {
        final var hackedNodes = this.getHackedNodes(impactAnalysisOutput);
        final var strategy = new BFSStrategy(this.ignoreLogicalConnections, this.hackingSpeed);
        for (final var rootNode : hackedNodes) {
            strategy.hackNextNode(rootNode);
        }
    }
}