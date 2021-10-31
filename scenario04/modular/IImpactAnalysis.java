package smartgrid.simcontrol.test.baselib.coupling;

import java.util.Map;

import couplingToICT.initializer.InitializationMapKeys;
import input.ScenarioState;
import output.ScenarioResult;
import topo.SmartGridTopology;

public interface IImpactAnalysis extends ISimulationComponent {

    public ScenarioResult run(SmartGridTopology smartGridTopo, ScenarioState impactAnalysisInput);
    
    /**
     * To be used without a launch configuration
     * @param config
     *            behavior for the anaylsis as a Map
     * @return true if Init was successful
     */
    public void init(final Map<InitializationMapKeys, String> initMap);
}
