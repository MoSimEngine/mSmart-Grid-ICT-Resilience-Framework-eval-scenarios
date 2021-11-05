package smartgrid.simcontrol.test.mocks;
import input.EntityState;
import input.ScenarioState;
import output.Cluster;
import output.Online;
import output.OutputFactory;
import output.ScenarioResult;
import output.impl.OutputPackageImpl;
import topo.ControlCenter;
import topo.SmartGridTopology;
import topo.SmartMeter;
public class AnalysisImpactAnalysisMock extends CommonsImpactAnalysisMock {
    @Override
    protected ScenarioResult run(final SmartGridTopology smartGridTopo, final ScenarioState impactAnalysisInput) {
        OutputPackageImpl.init();
        final OutputFactory factory = OutputFactory.eINSTANCE;
        final ScenarioResult result = factory.createScenarioResult();
        result.setScenario(smartGridTopo);
        int smartmetercount = 0;
        int controlcentercount = 0;
        final Cluster cl = factory.createCluster();
        for (final EntityState entity : impactAnalysisInput.getEntityStates()) {
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
}