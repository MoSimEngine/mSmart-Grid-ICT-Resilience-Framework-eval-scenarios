package smartgrid.newsimcontrol.rcp.commands;
import java.net.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import Resource.Factory.Registry.INSTANCE;
import jdk.internal.loader.Resource;
import smartgrid.newsimcontrol.controller.AnalysisLocalController;
import topo.SmartGridTopology;
import topo.TopoPackage;
public abstract class DomainControllerCommand extends CommonsControllerCommand {
    public DomainControllerCommand(AnalysisLocalController controller) {
        super(controller);
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