package smartgrid.simcontrol.test.mocks;
import couplingToICT.initializer.InitializationMapKeys;
import java.util.Map;
import smartgrid.simcontrol.test.baselib.coupling.IAttackerSimulation;
public class CommonsNoAttackerSimulation implements IAttackerSimulation {
    @Override
    protected void init(final Map<InitializationMapKeys, String> initMap) {
        // Nothing to do here
    }

    @Override
    protected String getName() {
        return "No Attack Simulation";
    }

    @Override
    protected boolean enableHackingSpeed() {
        return false;
    }

    @Override
    protected boolean enableRootNode() {
        return false;
    }

    @Override
    protected boolean enableLogicalConnections() {
        return false;
    }
}