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
import graph.NetworkEntity;
import input.EntityState;
import input.PowerState;
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
public class AnalysisReactiveSimulationController extends DomainReactiveSimulationController {
    protected static String getProsumerIdOfInputPowerState(final PowerState inputPowerState) {
        String id = String.valueOf(inputPowerState.getOwner().getId());
        if (id.endsWith("_PGN")) {
            id = id.substring(0, id.length() - 4);
        }
        return id;
    }

    /**
     *
     *
     * @param impactInput
     * 		The outdated ImpactAnalysis input data that should be
     * 		updated
     * @param impactResult
     * 		The result from the last iteration. This also contains
     * 		the hacked states, which have to be transfered to
     * 		impactInput
     * @param powerSupply
     * 		The output of the PowerLoad analysis, which has to be
     * 		interpreted and transfered to impactInput
     */
    protected static void updateImactAnalysisInput(final ScenarioState impactInput, final ScenarioResult impactResult, Map<String, Map<String, Double>> powerSupply) {
        // transfer hacked state into next input
        for (final EntityState state : impactResult.getStates()) {
            final boolean hackedState = (state instanceof On) && ((On) (state)).isIsHacked();
            final NetworkEntity owner = state.getOwner();
            for (final EntityState inputEntityState : impactInput.getEntityStates()) {
                if (inputEntityState.getOwner().equals(owner)) {
                    inputEntityState.setIsHacked(hackedState);
                    break;
                }
            }
        }
        if (powerSupply == null) {
            CommonsReactiveSimulationController.LOG.error("Power Load Simulation returned null. Power supply remains unchanged.");
            return;
        }
        // transfer power supply state into next input
        for (final PowerState inputPowerState : impactInput.getPowerStates()) {
            String prosumerId = ReactiveSimulationController.getProsumerIdOfInputPowerState(inputPowerState);
            boolean foundSupply = false;
            // search this prosumer in the node map
            for (final Map<String, Double> subNodePowerSupply : powerSupply.values()) {
                Double supply = subNodePowerSupply.get(prosumerId);
                if (supply != null) {
                    inputPowerState.setPowerOutage(ReactiveSimulationController.isOutage(supply));
                    foundSupply = true;
                    break;
                }
            }
            if (!foundSupply) {
                CommonsReactiveSimulationController.LOG.error(("There is no power supply for CI " + prosumerId) + ". Power supply remains unchanged.");
            }
        }
    }

    protected ScenarioState impactInput;

    protected ScenarioResult impactResult;

    protected ScenarioState initialState;

    public AnalysisReactiveSimulationController() {
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