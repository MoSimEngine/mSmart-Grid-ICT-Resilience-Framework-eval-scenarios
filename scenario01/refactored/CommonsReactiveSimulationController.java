package smartgrid.newsimcontrol.controller;
import InitializationMapKeys.ATTACKER_SIMULATION_KEY;
import InitializationMapKeys.IMPACT_ANALYSIS_SIMULATION_KEY;
import InitializationMapKeys.POWER_MODIFY_KEY;
import InitializationMapKeys.TIME_PROGRESSOR_SIMULATION_KEY;
import InitializationMapKeys.TOPO_GENERATION_STYLE;
import PowerSpecsModificationTypes.DOUBLE_MODIFIER;
import PowerSpecsModificationTypes.MAX_MODIFIER;
import PowerSpecsModificationTypes.NO_CHANGE_MODIFIER;
import PowerSpecsModificationTypes.ZERO_MODIFIER;
import couplingToICT.PowerSpecContainer;
import couplingToICT.SmartComponentStateContainer;
import couplingToICT.initializer.InitializationMapKeys;
import couplingToICT.initializer.PowerSpecsModificationTypes;
import couplingToICT.initializer.TopoGenerationStyle;
import input.ScenarioState;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.CoreException;
import output.ScenarioResult;
import smartgrid.attackersimulation.psm.DoublePSM;
import smartgrid.attackersimulation.psm.MaxPSM;
import smartgrid.attackersimulation.psm.PowerSpecsModifier;
import smartgrid.attackersimulation.psm.ZeroPSM;
import smartgrid.helper.HashMapHelper;
import smartgrid.helper.SimulationExtensionPointHelper;
import smartgrid.log4j.LoggingInitializer;
import smartgrid.simcontrol.test.baselib.coupling.IAttackerSimulation;
import smartgrid.simcontrol.test.baselib.coupling.IImpactAnalysis;
import smartgrid.simcontrol.test.baselib.coupling.ITimeProgressor;
public class CommonsReactiveSimulationController {
    protected static final Logger LOG = Logger.getLogger(ReactiveSimulationController.class);

    protected static boolean isOutage(double supply) {
        return supply == 0.0;
    }

    protected IAttackerSimulation attackerSimulation;

    protected SmartComponentStateContainer dysfunctionalcomponents;

    protected FileAppender fileAppender;

    protected IImpactAnalysis impactAnalsis;

    protected ScenarioState impactInput;

    protected ScenarioResult impactResult;

    protected ScenarioState initialState;

    protected PowerSpecsModificationTypes powerDemandModificationType;

    protected TopoGenerationStyle topoGenerationStyle;

    protected ITimeProgressor timeProgressor;

    // Simulation State
    protected int timeStep;

    protected String workingDirPath;

    public CommonsReactiveSimulationController() {
        this.timeStep = 0;
    }

    protected String determineWorkingDirPath(String initialPath) {
        initialPath = removeTrailingSeparator(initialPath);
        String currentPath = initialPath;
        int runningNumber = 0;
        while (new File(currentPath).exists()) {
            CommonsReactiveSimulationController.LOG.debug("Exists already: " + currentPath);
            currentPath = initialPath + runningNumber;
            runningNumber++;
        } 
        CommonsReactiveSimulationController.LOG.info("Working dir is: " + currentPath);
        return currentPath;
    }

    protected SmartComponentStateContainer getDysfunctionalcomponents() {
        return this.dysfunctionalcomponents;
    }

    protected void init(String outputPath) {
        LoggingInitializer.initialize();
        CommonsReactiveSimulationController.LOG.debug("init reactive launch config");
        this.workingDirPath = determineWorkingDirPath((outputPath + File.separator) + "Analyse");
        CommonsReactiveSimulationController.LOG.info("Output: " + outputPath);
        // add fileappender for local logs
        final Logger rootLogger = Logger.getRootLogger();
        try {
            Layout layout = ((Appender) (rootLogger.getAllAppenders().nextElement())).getLayout();
            this.fileAppender = new FileAppender(layout, (this.workingDirPath + File.separator) + "log.log");
            rootLogger.addAppender(this.fileAppender);
        } catch (final IOException e) {
            throw new RuntimeException("Error creating local log appender in the working directory. Most likely there are problems with access rights.");
        }
    }

    protected void loadCustomUserAnalysis(Map<InitializationMapKeys, String> initMap) throws CoreException {
        this.attackerSimulation = SimulationExtensionPointHelper.findExtension(initMap, SimulationExtensionPointHelper.getAttackerSimulationExtensions(), ATTACKER_SIMULATION_KEY, IAttackerSimulation.class);
        this.impactAnalsis = SimulationExtensionPointHelper.findExtension(initMap, SimulationExtensionPointHelper.getImpactAnalysisExtensions(), IMPACT_ANALYSIS_SIMULATION_KEY, IImpactAnalysis.class);
        this.timeProgressor = SimulationExtensionPointHelper.findExtension(initMap, SimulationExtensionPointHelper.getProgressorExtensions(), TIME_PROGRESSOR_SIMULATION_KEY, ITimeProgressor.class);
        this.impactAnalsis.init(initMap);
        this.attackerSimulation.init(initMap);
        this.timeProgressor.init(initMap);
        CommonsReactiveSimulationController.LOG.info("Using impact analysis: " + this.impactAnalsis.getName());
        CommonsReactiveSimulationController.LOG.info("Using attacker simulation: " + this.attackerSimulation.getName());
        CommonsReactiveSimulationController.LOG.info("Using time progressor: " + this.timeProgressor.getName());
        if (!HashMapHelper.getAttribute(initMap, POWER_MODIFY_KEY, "").equals("")) {
            String powerModificationString = HashMapHelper.getAttribute(initMap, POWER_MODIFY_KEY, "");
            PowerSpecsModificationTypes powerSpecsModificationType = PowerSpecsModificationTypes.valueOf(powerModificationString);
            this.powerDemandModificationType = powerSpecsModificationType;
        }
        if (!HashMapHelper.getAttribute(initMap, TOPO_GENERATION_STYLE, "").equals("")) {
            String topoGenerationString = HashMapHelper.getAttribute(initMap, TOPO_GENERATION_STYLE, "");
            TopoGenerationStyle currenttopoGenerationStyle = TopoGenerationStyle.valueOf(topoGenerationString);
            this.topoGenerationStyle = currenttopoGenerationStyle;
        }
    }

    protected PowerSpecContainer modifyPowerSpecContainer(PowerSpecContainer powerSpecContainer) {
        var hackedSmartMeters = getHackedSmartMeters();
        // modify the powerSpecs
        PowerSpecsModifier pDemandModifier = null;
        if (this.powerDemandModificationType.equals(MAX_MODIFIER)) {
            pDemandModifier = new MaxPSM();
        } else if (this.powerDemandModificationType.equals(ZERO_MODIFIER)) {
            pDemandModifier = new ZeroPSM();
        } else if (this.powerDemandModificationType.equals(DOUBLE_MODIFIER)) {
            pDemandModifier = new DoublePSM();
        } else if (this.powerDemandModificationType.equals(NO_CHANGE_MODIFIER)) {
            return powerSpecContainer;
        } else {
            throw new IllegalStateException("Unkown PowerDemandModification");
        }
        return pDemandModifier.modifyPowerSpecs(powerSpecContainer, hackedSmartMeters);
    }

    protected String removeTrailingSeparator(String initialPath) {
        if (initialPath.endsWith(File.pathSeparator)) {
            return initialPath.substring(0, initialPath.length() - 1);
        }
        return initialPath;
    }

    protected void shutDown() {
        // remove file appender of this run
        if (this.fileAppender != null) {
            Logger.getRootLogger().removeAppender(this.fileAppender);
            this.fileAppender.close();
        }
    }

    protected ScenarioState getInitialState() {
        return this.initialState;
    }

    protected void setInitialState(ScenarioState initialState) {
        this.initialState = initialState;
    }

    protected ScenarioState getImpactInput() {
        return this.impactInput;
    }

    protected void setImpactInput(ScenarioState impactInput) {
        this.impactInput = impactInput;
    }
}