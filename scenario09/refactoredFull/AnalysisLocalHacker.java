package smartgrid.attackersimulation;
import output.On;
import output.ScenarioResult;
import smartgrid.helper.AnalysisScenarioModelHelper;
import smartgrid.simcontrol.test.baselib.Constants;
import topo.SmartGridTopology;
public class AnalysisLocalHacker extends CommonsLocalHacker {
    protected On rootNodeState;// Reference Changes between runs!


    protected ScenarioResult scenarioResult;

    /**
     * Default constructor is needed by the OSGi framework to be able to use the extension point
     */
    public AnalysisLocalHacker() {
    }

    /**
     * setting the Root node
     */
    protected void rootNodeUpdate() {
        if (this.rootNodeID.equalsIgnoreCase(Constants.NO_ROOT_NODE_ID)) {
            CommonsLocalHacker.LOG.info("No root node specified.");
            this.rootNodeID = AnalysisScenarioModelHelper.selectRandomRoot(this.ignoreLogicalConnections, this.scenarioResult);
        }
        this.rootNodeState = AnalysisScenarioModelHelper.findEntityOnStateFromID(this.rootNodeID, this.scenarioResult);// update

        // state
        this.rootNodeState.setIsHacked(true);
    }

    /**
     *
     *
     * @see smartgrid.simcontrol.interfaces.IAttackerSimulation#run(smartgridtopo .Scenario,
    smartgridoutput.ScenarioResult)
     */
    @Override
    protected ScenarioResult run(final SmartGridTopology smartGridTopo, final ScenarioResult scenarioResult) {
        if (!this.initDone) {
            throw new IllegalStateException("LocalHacker not initialization. Run init()");
        }
        this.scenarioResult = scenarioResult;
        this.rootNodeUpdate();
        if (this.rootNodeState != null) {
            this.hackingTypes.hackNextNode(this.rootNodeState);
        }
        this.scenarioResult.setScenario(smartGridTopo);
        CommonsLocalHacker.LOG.debug("Hacking done");
        return this.scenarioResult;
    }
}