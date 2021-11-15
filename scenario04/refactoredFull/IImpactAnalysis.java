package smartgrid.simcontrol.test.baselib.coupling;
import input.ScenarioState;
import java.util.Map;
import output.ScenarioResult;
import topo.SmartGridTopology;
import couplingToICT.initializer.InitializationMapKeys;
public interface IImpactAnalysis extends ISimulationComponent {
    public ScenarioResult run(SmartGridTopology smartGridTopo, ScenarioState impactAnalysisInput);

    /**
     * To be used without a launch configuration
     *
     * @param config
     * 		behavior for the anaylsis as a Map
     * @return true if Init was successful
     */
    public void init(final Map<InitializationMapKeys, String> initMap);
}