package smartgrid.newsimcontrol.rcp.commands;
import couplingToICT.SimcontrolException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.apache.log4j.Logger;
import smartgrid.newsimcontrol.controller.AnalysisLocalController;
public abstract class CommonsControllerCommand {
    protected AnalysisLocalController controller;

    protected static final Logger LOG = Logger.getLogger(ControllerCommand.class);

    public enum ObjectType {

        topo,
        input;}

    public CommonsControllerCommand(AnalysisLocalController controller) {
        this.controller = controller;
    }

    protected void execute(String[] args) throws SimcontrolException, InterruptedException {
        if (checkArguments(args)) {
            doCommand(args);
        }
    }

    protected abstract boolean allow();

    protected abstract boolean checkArguments(String[] args);

    protected abstract void doCommand(String[] args) throws SimcontrolException, InterruptedException;

    protected void writeObjectToFile(Object serObj, String filepath) {
        try {
            FileOutputStream fileOut = new FileOutputStream(filepath);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(serObj);
            objectOut.close();
        } catch (IOException ex) {
            CommonsControllerCommand.LOG.error("The output can't be saved to the filepath: " + filepath);
            ex.printStackTrace();
        }
    }
}