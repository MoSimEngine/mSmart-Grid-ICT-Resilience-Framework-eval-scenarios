package smartgrid.newsimcontrol.rcp.commands;
import EMFProfileApplicationPackage.eINSTANCE;
import Resource.Factory.Registry.INSTANCE;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import smartgrid.newsimcontrol.controller.DomainLocalController;
import topo.SmartGridTopology;
import topo.TopoPackage;
public abstract class DomainControllerCommand extends CommonsControllerCommand {
    public DomainControllerCommand(DomainLocalController controller) {
        this.controller = controller;
    }

    protected Object readObjectFromFile(String filepath) {
        return readObjectFromFile(filepath, null);
    }

    protected Object readObjectFromFile(String filepath, ControllerCommand.ObjectType objectType) {
        if (objectType != null) {
            return readTopoOrInput(filepath, objectType);
        }
        try {
            FileInputStream fileIn = new FileInputStream(filepath);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            Object obj = objectIn.readObject();
            objectIn.close();
            return obj;
        } catch (IOException | ClassNotFoundException ex) {
            CommonsControllerCommand.LOG.error("The input can't be read from the filepath: " + filepath);
            return null;
        }
    }

    protected Object readTopoOrInput(String filepath, ControllerCommand.ObjectType objectType) {
        switch (objectType) {
            case input :
                return ControllerCommand.loadInput(filepath);
            case topo :
                return ControllerCommand.loadTopology(filepath);
            default :
                return null;
        }
    }

    /**
     *
     *
     * @param path
     * 		path of the ScenarioTopology to be used
     * @return The read ScenarioTopology file
     */
    protected static SmartGridTopology loadTopology(final String path) {
        final ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put("http://www.modelversioning.org/emfprofile/application/1.1", eINSTANCE);
        resourceSet.getPackageRegistry().put("http://sdq.ipd.uka.de/smartgridtopo/1.1", TopoPackage.eINSTANCE);
        INSTANCE.getExtensionToFactoryMap().put("smartgridtopo", new XMIResourceFactoryImpl());
        final Resource resource = resourceSet.getResource(URI.createFileURI(path), true);
        return ((SmartGridTopology) (resource.getContents().get(0)));
    }
}