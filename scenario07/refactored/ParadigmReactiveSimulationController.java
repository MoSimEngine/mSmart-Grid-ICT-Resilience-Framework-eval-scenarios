package smartgrid.newsimcontrol.controller;
import graph.NetworkEntity;
import input.EntityState;
import input.PowerState;
import input.ScenarioState;
import java.util.Map;
import output.EntityState;
import output.On;
import output.ScenarioResult;
public class ParadigmReactiveSimulationController extends CommonsReactiveSimulationController {
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

    public ParadigmReactiveSimulationController() {
        this.timeStep = 0;
    }
}