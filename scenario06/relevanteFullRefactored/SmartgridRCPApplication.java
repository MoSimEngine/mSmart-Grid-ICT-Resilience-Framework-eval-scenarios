package smartgrid.newsimcontrol.rcp;
import IApplicationContext.APPLICATION_ARGS;
import java.util.Arrays;
import smartgrid.newsimcontrol.controller.AnalysisLocalController;
import smartgrid.newsimcontrol.rcp.commands.AnalysisControllerCommand;
import smartgrid.newsimcontrol.rcp.commands.CommonsGetModifiedPowerspecsCommand;
import smartgrid.newsimcontrol.rcp.commands.InitTopoCommand;
import smartgrid.newsimcontrol.rcp.commands.SimControlCommands;
import couplingToICT.SimcontrolException;
import org.apache.log4j.Logger;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import static smartgrid.newsimcontrol.rcp.commands.SimControlCommands.GET_MODIFIED_POWERSPECS;
import static smartgrid.newsimcontrol.rcp.commands.SimControlCommands.INIT_TOPO;
/**
 * This class controls all aspects of the application's execution
 */
public class SmartgridRCPApplication implements IApplication {
    protected static final Logger LOG = Logger.getLogger(SmartgridRCPApplication.class);

    String test = "";

    @Override
    public Object start(IApplicationContext context) throws Exception {
        LOG.info("RCP starting");
        String[] arguments = ((String[]) (context.getArguments().get(APPLICATION_ARGS)));
        if (arguments.length == 0) {
            LOG.error("The name of the method to be run should be given with its arguments.");
            return IApplication.EXIT_OK;
        } else {
            AnalysisControllerCommand cCommand = getCommand(arguments[0]);
            applyArguments(cCommand, arguments);
        }
        return IApplication.EXIT_OK;
    }

    @Override
    public void stop() {
        // same as parent
    }

    private AnalysisControllerCommand getCommand(String commandString) {
        SimControlCommands command = null;
        AnalysisControllerCommand cCommand = null;
        try {
            command = simControlCommandFromValue(commandString);
        } catch (IllegalArgumentException e) {
            LOG.error("The entered command can't be recognized. The program will end.");
            return null;
        }
        var controller = new AnalysisLocalController();// (LocalController) Activator.getInstance().getController();

        switch (command) {
            case INIT_TOPO :
                cCommand = new InitTopoCommand(controller);
                break;
            case GET_MODIFIED_POWERSPECS :
                cCommand = new CommonsGetModifiedPowerspecsCommand(controller);
                break;
            default :
                cCommand = null;
                LOG.error("The entered command can't be recognized. The program will end.");
        }
        return cCommand;
    }

    private void applyArguments(AnalysisControllerCommand cCommand, String[] arguments) throws SimcontrolException, InterruptedException {
        String[] arguments2 = Arrays.copyOfRange(arguments, 1, arguments.length);
        if (cCommand.allow())
            cCommand.execute(arguments2);

    }

    public static SimControlCommands simControlCommandFromValue(String text) {
        for (SimControlCommands comm : SimControlCommands.values()) {
            if (String.valueOf(comm).equals(text)) {
                return comm;
            }
        }
        throw new IllegalArgumentException();
    }

    /**
     * Test Method
     * would be removed
     *
     * @param commandArg
     * 		
     * @throws SimcontrolException
     * 		
     * @throws InterruptedException
     * 		
     */
    public void startTest(String commandArg) throws SimcontrolException, InterruptedException {
        LOG.info("RCP starting");
        String[] arguments = commandArg.split(" ");
        if (arguments.length == 0) {
            LOG.error("Please write the name of the method to be run.");
        } else {
            AnalysisControllerCommand cCommand = getCommand(arguments[0]);
            applyArguments(cCommand, arguments);
        }
    }
}