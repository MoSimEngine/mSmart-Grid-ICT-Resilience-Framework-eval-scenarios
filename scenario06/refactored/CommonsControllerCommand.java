package smartgrid.newsimcontrol.rcp.commands;
import EMFProfileApplicationPackage.eINSTANCE;
import Resource.Factory.Registry.INSTANCE;
import couplingToICT.SimcontrolException;
import input.InputPackage;
import input.ScenarioState;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import smartgrid.newsimcontrol.controller.DomainLocalController;
public abstract class CommonsControllerCommand {
    protected DomainLocalController controller;

    protected static final Logger LOG = Logger.getLogger(ControllerCommand.class);

    public enum ObjectType {

        topo,
        input;}

    public CommonsControllerCommand(DomainLocalController controller) {
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

    /**
     *
     *
     * @param path
     * 		path of the ScenarioState to be used
     * @return The read ScenarioState file
     */
    protected static ScenarioState loadInput(final String path) {
        final ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put("http://www.modelversioning.org/emfprofile/application/1.1", eINSTANCE);
        resourceSet.getPackageRegistry().put("http://sdq.ipd.uka.de/smartgridinput/1.0", InputPackage.eINSTANCE);
        INSTANCE.getExtensionToFactoryMap().put("smartgridinput", new XMIResourceFactoryImpl());
        final Resource resource = resourceSet.getResource(URI.createFileURI(path), true);
        return ((ScenarioState) (resource.getContents().get(0)));
    }
}