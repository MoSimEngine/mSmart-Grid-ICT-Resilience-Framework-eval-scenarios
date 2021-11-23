package smartgrid.newsimcontrol.controller;
import InitializationMapKeys.INPUT_PATH_KEY;
import InitializationMapKeys.OUTPUT_PATH_KEY;
import InitializationMapKeys.TOPO_GENERATION_KEY;
import InitializationMapKeys.TOPO_PATH_KEY;
import couplingToICT.ICTElement;
import couplingToICT.ISimulationController;
import couplingToICT.PowerAssigned;
import couplingToICT.PowerSpecContainer;
import couplingToICT.SimcontrolException;
import couplingToICT.SmartComponentStateContainer;
import couplingToICT.SmartGridTopoContainer;
import couplingToICT.initializer.InitializationMapKeys;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
public class CommonsLocalController implements ISimulationController {
    protected static final Logger LOG = Logger.getLogger(LocalController.class);

    protected AnalysisReactiveSimulationController reactiveSimControl;

    // 1
    @Override
    protected PowerSpecContainer getModifiedPowerSpec(PowerSpecContainer powerSpecs, PowerAssigned sMPowerAssigned) throws SimcontrolException, InterruptedException {
        // TODO Check PowerSpecs wegen PowerdistrictId, smartID
        this.reactiveSimControl.run(sMPowerAssigned);
        return this.reactiveSimControl.modifyPowerSpecContainer(powerSpecs);
    }

    // 2
    @Override
    protected SmartComponentStateContainer getDysfunctSmartComponents() throws SimcontrolException, InterruptedException {
        return this.reactiveSimControl.getDysfunctionalcomponents();
    }

    @Override
    protected Collection<ICTElement> initTopo(SmartGridTopoContainer topologyContainer) throws SimcontrolException {
        if (topologyContainer == null) {
            CommonsLocalController.LOG.warn("Topo Container is null");
        } else {
            return this.reactiveSimControl.initTopo(topologyContainer);
        }
        return new LinkedList<>();
    }

    @Override
    protected void initConfiguration(Map<InitializationMapKeys, String> initMap) {
        this.reactiveSimControl = new AnalysisReactiveSimulationController();
        // Values in the map
        String outputPath = null;
        String topoPath = "";
        String inputStatePath = "";
        boolean generateTopo = false;
        // fill values in the working copy
        for (Entry<InitializationMapKeys, String> entry : initMap.entrySet()) {
            if (entry.getKey().equals(INPUT_PATH_KEY)) {
                inputStatePath = entry.getValue();
            } else if (entry.getKey().equals(TOPO_PATH_KEY)) {
                topoPath = entry.getValue();
            } else if (entry.getKey().equals(OUTPUT_PATH_KEY)) {
                outputPath = entry.getValue();
            } else if (entry.getKey().equals(TOPO_GENERATION_KEY)) {
                generateTopo = Boolean.valueOf(entry.getValue());
            }
        }
        if (outputPath == null) {
            outputPath = System.getProperty("java.io.tmpdir");
            outputPath += (File.separator + "smargrid") + System.currentTimeMillis();
        }
        this.reactiveSimControl.init(outputPath);
        if (!generateTopo) {
            this.reactiveSimControl.initModelsFromFiles(topoPath, inputStatePath);
        }
        try {
            this.reactiveSimControl.loadCustomUserAnalysis(initMap);
        } catch (CoreException e) {
            CommonsLocalController.LOG.error("Error while intializing the simulations");
        }
    }
}