package smartgrid.simcontrol.test.mocks;
import couplingToICT.initializer.InitializationMapKeys;
import java.util.Map;
import smartgrid.simcontrol.test.baselib.coupling.IImpactAnalysis;
public class CommonsImpactAnalysisMock implements IImpactAnalysis {
    @Override
    protected void init(final Map<InitializationMapKeys, String> initMap) {
    }

    @Override
    protected String getName() {
        return "Mock";
    }
}