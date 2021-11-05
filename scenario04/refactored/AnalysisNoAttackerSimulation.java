package smartgrid.simcontrol.test.mocks;
import output.ScenarioResult;
import topo.SmartGridTopology;
public class AnalysisNoAttackerSimulation extends CommonsNoAttackerSimulation {
    /**
     * {@inheritDoc }
     * <p>
     *
     * An attacker who doesn't attack
     */
    @Override
    protected ScenarioResult run(final SmartGridTopology smartGridTopo, final ScenarioResult impactAnalysisOutput) {
        return impactAnalysisOutput;
    }
}