package smartgrid.newsimcontrol.rcp.commands;
import couplingToICT.PowerAssigned;
import couplingToICT.PowerSpecContainer;
import couplingToICT.SimcontrolException;
import couplingToICT.SmartComponentStateContainer;
import couplingToICT.initializer.InitializationMapKeys;
import input.ScenarioState;
import java.util.Map;
import smartgrid.newsimcontrol.controller.DomainLocalController;
import smartgrid.newsimcontrol.rcp.helper.DomainEObjectsHelper;
import topo.SmartGridTopology;
public class CommonsGetModifiedPowerspecsCommand extends DomainControllerCommand {
    public CommonsGetModifiedPowerspecsCommand(DomainLocalController controller) {
        super(controller);
    }

    @Override
    protected boolean allow() {
        return true;
    }

    @Override
    protected boolean checkArguments(String[] args) {
        if (args.length != 7) {
            CommonsControllerCommand.LOG.error("The correct number of arguments isn't correct." + "For further info see the readme file");
            return false;
        }
        Object obj1 = readObjectFromFile(args[0]);
        Object obj2 = readObjectFromFile(args[3]);
        Object obj3 = readObjectFromFile(args[4]);
        return ((obj1 instanceof Map<?, ?>) && (obj2 instanceof PowerSpecContainer)) && (obj3 instanceof PowerAssigned);
    }

    @Override
    protected void doCommand(String[] args) throws SimcontrolException, InterruptedException {
        CommonsControllerCommand.LOG.info("Initializing the local controller");
        @SuppressWarnings("unchecked")
        Map<InitializationMapKeys, String> initMap = ((Map<InitializationMapKeys, String>) (readObjectFromFile(args[0])));
        controller.initConfiguration(initMap);
        CommonsControllerCommand.LOG.info("Initializing the topology");
        SmartGridTopology topo = DomainEObjectsHelper.loadTopology(args[1]);
        ScenarioState state = DomainEObjectsHelper.loadInput(args[2]);
        controller.setTopo(topo);
        controller.setInitalState(state);
        controller.setImpactInput(state);
        CommonsControllerCommand.LOG.info("Running the simulations and saving the modified powerspecContainer");
        PowerSpecContainer powerSpecs = ((PowerSpecContainer) (readObjectFromFile(args[3])));
        PowerAssigned sMPowerAssigned = ((PowerAssigned) (readObjectFromFile(args[4])));
        PowerSpecContainer powerSpecsModified;
        try {
            powerSpecsModified = controller.getModifiedPowerSpec(powerSpecs, sMPowerAssigned);
            writeObjectToFile(powerSpecsModified, args[5]);
            DomainEObjectsHelper.saveToFileSystem(topo, args[1]);
            DomainEObjectsHelper.saveToFileSystem(state, args[2]);
            CommonsControllerCommand.LOG.info("Saving the dysfunctional smartcomponents");
            SmartComponentStateContainer scsc;
            scsc = controller.getDysfunctSmartComponents();
            writeObjectToFile(scsc, args[6]);
        } catch (SimcontrolException | InterruptedException e1) {
            e1.printStackTrace();
        }
    }
}