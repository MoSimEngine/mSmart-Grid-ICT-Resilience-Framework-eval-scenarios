package smartgrid.newsimcontrol.rcp.helper;
import EMFProfileApplicationPackage.eINSTANCE;
import Resource.Factory.Registry.INSTANCE;
import XMLResource.OPTION_DEFER_ATTACHMENT;
import XMLResource.OPTION_DEFER_IDREF_RESOLUTION;
import XMLResource.OPTION_USE_DEPRECATED_METHODS;
import XMLResource.OPTION_USE_PARSER_POOL;
import XMLResource.OPTION_USE_XML_NAME_TO_FEATURE_MAP;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLParserPoolImpl;
import topo.SmartGridTopology;
import topo.TopoPackage;
public class DomainEObjectsHelper extends CommonsEObjectsHelper {
    private DomainEObjectsHelper() {
    }

    /**
     *
     *
     * @param path
     * 		path of the ScenarioTopology to be used
     * @return The read ScenarioTopology file
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected static SmartGridTopology loadTopology(final String path) {
        final ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put("http://www.modelversioning.org/emfprofile/application/1.1", eINSTANCE);
        resourceSet.getPackageRegistry().put("http://sdq.ipd.uka.de/smartgridtopo/1.1", TopoPackage.eINSTANCE);
        INSTANCE.getExtensionToFactoryMap().put("smartgridtopo", new XMIResourceFactoryImpl());
        Map loadOptions = resourceSet.getLoadOptions();
        loadOptions.put(OPTION_DEFER_ATTACHMENT, Boolean.TRUE);
        loadOptions.put(OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
        loadOptions.put(OPTION_USE_DEPRECATED_METHODS, Boolean.TRUE);
        loadOptions.put(OPTION_USE_PARSER_POOL, new XMLParserPoolImpl());
        loadOptions.put(OPTION_USE_XML_NAME_TO_FEATURE_MAP, new HashMap());
        final Resource resource = resourceSet.getResource(URI.createFileURI(path), true);
        return ((SmartGridTopology) (resource.getContents().get(0)));
    }
}