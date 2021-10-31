package smartgrid.simcontrol.test.mocks;

import java.util.Map;


import couplingToICT.initializer.InitializationMapKeys;
import smartgrid.simcontrol.test.baselib.coupling.IImpactAnalysis;
import input.ScenarioState;
import output.Cluster;
import output.Online;
import output.ScenarioResult;
import output.OutputFactory;
import output.impl.OutputPackageImpl;
import topo.ControlCenter;
import topo.SmartGridTopology;
import topo.SmartMeter;

/**
 * This Class mocks the Imapct Analysis
 *
 * It mocks the impact analysis in that way that regardless of the input states every output state
 * is marked as 'Online' and all states are packed in one single cluster
 *
 * @author Christian, Michael
 */
public class ImpactAnalysisMock implements IImpactAnalysis {

    @Override
    public ScenarioResult run(final SmartGridTopology smartGridTopo, final ScenarioState impactAnalysisInput) {
        OutputPackageImpl.init();
        final OutputFactory factory = OutputFactory.eINSTANCE;
        final ScenarioResult result = factory.createScenarioResult();

        result.setScenario(smartGridTopo);

        int smartmetercount = 0;
        int controlcentercount = 0;

        final Cluster cl = factory.createCluster();
        for (final input.EntityState entity : impactAnalysisInput.getEntityStates()) {
            final Online on = factory.createOnline();
            on.setOwner(entity.getOwner());
            on.setBelongsToCluster(cl);

            result.getStates().add(on);

            // Count smartMeters and controlCenters
            if (entity.getOwner() instanceof SmartMeter) {
                smartmetercount++;
            } else if (entity.getOwner() instanceof ControlCenter) {
                controlcentercount++;
            }
        }

        cl.setSmartMeterCount(smartmetercount);
        cl.setControlCenterCount(controlcentercount);

        result.getClusters().add(cl);

        return result;
    }
    
    @Override
    public void init(final Map<InitializationMapKeys, String> initMap) {
    }

    @Override
    public String getName() {
        return "Mock";
    }
}
