package smartgrid.newsimcontrol.controller;
import PowerSpecsModificationTypes.DOUBLE_MODIFIER;
import PowerSpecsModificationTypes.MAX_MODIFIER;
import PowerSpecsModificationTypes.NO_CHANGE_MODIFIER;
import PowerSpecsModificationTypes.ZERO_MODIFIER;
import couplingToICT.ICTElement;
import couplingToICT.PowerAssigned;
import couplingToICT.PowerSpecContainer;
import couplingToICT.SmartComponentStateContainer;
import couplingToICT.SmartGridTopoContainer;
import input.ScenarioState;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import output.EntityState;
import output.Offline;
import output.On;
import output.ScenarioResult;
import smartgrid.attackersimulation.psm.DoublePSM;
import smartgrid.attackersimulation.psm.MaxPSM;
import smartgrid.attackersimulation.psm.PowerSpecsModifier;
import smartgrid.attackersimulation.psm.ZeroPSM;
import smartgrid.helper.FileSystemHelper;
import smartgrid.helper.ScenarioModelHelper;
import smartgrid.model.topo.generator.DefaultInputGenerator;
import smartgrid.model.topo.generator.ITopoGenerator;
import smartgrid.model.topo.generator.ring.RingTopoGenerator;
import smartgrid.model.topo.generator.star.StarTopoGenerator;
import smartgrid.model.topo.generator.starstar.StarStarTopoGenerator;
import smartgrid.model.topo.generator.trivial.TrivialTopoGenerator;
import smartgrid.newsimcontrol.ReportGenerator;
import topo.ControlCenter;
import topo.GenericController;
import topo.InterCom;
import topo.NetworkNode;
import topo.SmartGridTopology;
import topo.SmartMeter;
public class DomainReactiveSimulationController extends ParadigmReactiveSimulationController {
    protected SmartGridTopology topo;

    public DomainReactiveSimulationController() {
        this.timeStep = 0;
    }

    protected SmartComponentStateContainer generateSCSC(ScenarioResult impactResult) {
        var smartMeterStates = new ArrayList<String>();
        var iedStates = new ArrayList<String>();
        for (EntityState state : impactResult.getStates()) {
            if ((state.getOwner() instanceof SmartMeter) && (state instanceof Offline)) {
                smartMeterStates.add(String.valueOf(state.getOwner().getId()));
            }
        }
        return new SmartComponentStateContainer(smartMeterStates, iedStates);
    }

    protected Set<String> getHackedSmartMeters() {
        var hackedSmartMeters = new HashSet<String>();
        if (this.impactResult != null) {
            for (EntityState state : this.impactResult.getStates()) {
                if (((state.getOwner() instanceof SmartMeter) && (state instanceof On)) && ((On) (state)).isIsHacked()) {
                    hackedSmartMeters.add(String.valueOf(state.getOwner().getId()));
                }
            }
        }
        return hackedSmartMeters;
    }

    protected void initModelsFromFiles(String topoPath, String inputStatePath) {
        // load models
        this.initialState = ScenarioModelHelper.loadInput(inputStatePath);
        this.impactInput = this.initialState;
        this.topo = ScenarioModelHelper.loadScenario(topoPath);
        CommonsReactiveSimulationController.LOG.info("Scenario input state: " + inputStatePath);
        CommonsReactiveSimulationController.LOG.info("Topology: " + topoPath);
    }

    /**
     * A method to generate a toplogy from a toplogy container
     * the style of generation is defined in the initialization map
     * if no style is defined, so the trivial one will be used.
     *
     * @param topoContainer
     * 		the container of the to be generated topology
     * @return 
     */
    protected List<ICTElement> initTopo(SmartGridTopoContainer topoContainer) {
        // generate and persist topo
        ITopoGenerator generator;
        if (this.topoGenerationStyle == null) {
            generator = new TrivialTopoGenerator();
        } else {
            switch (this.topoGenerationStyle) {
                case STERN_TOPO :
                    generator = new StarTopoGenerator();
                    break;
                case RING_TOPO :
                    generator = new RingTopoGenerator();
                    break;
                case STERN_STERN_TOPO :
                    generator = new StarStarTopoGenerator();
                    break;
                default :
                    generator = new TrivialTopoGenerator();
                    break;
            }
        }
        this.topo = generator.generateTopo(topoContainer);
        FileSystemHelper.saveToFileSystem(this.topo, (this.workingDirPath + File.separatorChar) + "generated.smartgridtopo");
        CommonsReactiveSimulationController.LOG.info("Topo is generated");
        // generate and persist input
        DefaultInputGenerator defaultInputGenerator = new DefaultInputGenerator();
        this.initialState = defaultInputGenerator.generateInput(this.topo);
        FileSystemHelper.saveToFileSystem(this.initialState, (this.workingDirPath + File.separatorChar) + "generated.smartgridinput");
        this.impactInput = this.initialState;
        CommonsReactiveSimulationController.LOG.info("Input is generated");
        return this.topo.getContainsNE().stream().filter(( e) -> (((e instanceof NetworkNode) || (e instanceof ControlCenter)) || (e instanceof InterCom)) || (e instanceof GenericController)).map(( e) -> new ICTElement(String.valueOf(e.getId()), e.eClass().toString())).collect(Collectors.toList());
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

    protected SmartComponentStateContainer run(PowerAssigned power) {
        Map<String, Map<String, Double>> powerSupply = null;
        CommonsReactiveSimulationController.LOG.info("Starting time step " + this.timeStep);
        this.topo.setId("testID");
        CommonsReactiveSimulationController.LOG.info(this.topo.getId());
        // Generates Path with default System separators
        final String timeStepPath = new File(((this.workingDirPath + File.separator) + "Zeitschritt ") + this.timeStep).getPath();
        // get power supply
        if (power != null) {
            LinkedHashMap<String, HashMap<String, Double>> powerAssignedMap = power.getPowerAssigned();
            powerSupply = new LinkedHashMap<>(powerAssignedMap);
        }
        CommonsReactiveSimulationController.LOG.info("Starting Impact Analysis");
        ScenarioResult currentimpactResult = this.impactAnalsis.run(this.topo, this.impactInput);
        // Save input to file
        final String inputFile = new File((timeStepPath + File.separator) + "PowerLoadResult.smartgridinput").getPath();
        FileSystemHelper.saveToFileSystem(this.impactInput, inputFile);
        CommonsReactiveSimulationController.LOG.info("Starting Attacker Simulation");
        currentimpactResult = this.attackerSimulation.run(this.topo, currentimpactResult);
        this.impactResult = currentimpactResult;
        // save attack result to file
        final String attackResultFile = new File((timeStepPath + File.separator) + "AttackerSimulationResult.smartgridoutput").getPath();
        FileSystemHelper.saveToFileSystem(currentimpactResult, attackResultFile);
        ReactiveSimulationController.updateImactAnalysisInput(this.impactInput, currentimpactResult, powerSupply);// update for next timestep

        CommonsReactiveSimulationController.LOG.info("Collecting dysfunctionalComponents");
        // get smartmeters
        this.dysfunctionalcomponents = generateSCSC(currentimpactResult);
        // Save Result
        final String resultFile = new File((timeStepPath + File.separator) + "ImpactResult.smartgridoutput").getPath();
        FileSystemHelper.saveToFileSystem(currentimpactResult, resultFile);
        // generate report
        final File resultReportPath = new File((timeStepPath + File.separator) + "ResultReport.csv");
        ReportGenerator.saveScenarioResult(resultReportPath, currentimpactResult);
        // modify the scenario between time steps
        this.timeProgressor.progress();
        this.timeStep++;
        CommonsReactiveSimulationController.LOG.info("Finished time step " + this.timeStep);
        return this.dysfunctionalcomponents;
    }

    protected SmartGridTopology getTopo() {
        return this.topo;
    }

    protected void setTopo(SmartGridTopology topo) {
        this.topo = topo;
    }
}