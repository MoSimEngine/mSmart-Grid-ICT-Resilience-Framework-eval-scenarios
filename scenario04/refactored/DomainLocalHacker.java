package smartgrid.attackersimulation;
import output.ScenarioResult;
import topo.SmartGridTopology;
public class DomainLocalHacker extends CommonsLocalHacker {
    /**
     * Default constructor is needed by the OSGi framework to be able to use the extension point
     */
    public DomainLocalHacker() {
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