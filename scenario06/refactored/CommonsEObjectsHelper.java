package smartgrid.newsimcontrol.rcp.helper;
import EMFProfileApplicationPackage.eINSTANCE;
import Resource.Factory.Registry.INSTANCE;
import XMLResource.OPTION_DEFER_ATTACHMENT;
import XMLResource.OPTION_DEFER_IDREF_RESOLUTION;
import XMLResource.OPTION_USE_DEPRECATED_METHODS;
import XMLResource.OPTION_USE_PARSER_POOL;
import XMLResource.OPTION_USE_XML_NAME_TO_FEATURE_MAP;
import input.InputPackage;
import input.ScenarioState;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLParserPoolImpl;
public class CommonsEObjectsHelper {
    private CommonsEObjectsHelper() {
    }

    protected static final Logger LOG = Logger.getLogger(EObjectsHelper.class);

    /**
     * This Method saves a Scenario Result on the File System at the given Path
     *
     * @param result
     * 		Scenario Result to be written on File System
     * @param path
     * 		Path there the File will be written to
     */
    protected static void saveToFileSystem(final EObject result, final String path) {
        if (result == null) {
            CommonsEObjectsHelper.LOG.error("Cannot persist a model with the content of \"null\". Something went wrong.");
            return;
        }
        final ResourceSet resSet = new ResourceSetImpl();
        final Resource resource = resSet.createResource(URI.createFileURI(path));
        resource.getContents().add(result);
        try {
            resource.save(Collections.emptyMap());
        } catch (final IOException e) {
            CommonsEObjectsHelper.LOG.error("Could not save to file.", e);
        }
    }

    /**
     *
     *
     * @param path
     * 		path of the ScenarioState to be used
     * @return The read ScenarioState file
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected static ScenarioState loadInput(final String path) {
        final ResourceSet resourceSet = new ResourceSetImpl();
        resourceSet.getPackageRegistry().put("http://www.modelversioning.org/emfprofile/application/1.1", eINSTANCE);
        resourceSet.getPackageRegistry().put("http://sdq.ipd.uka.de/smartgridinput/1.0", InputPackage.eINSTANCE);
        INSTANCE.getExtensionToFactoryMap().put("smartgridinput", new XMIResourceFactoryImpl());
        Map loadOptions = resourceSet.getLoadOptions();
        loadOptions.put(OPTION_DEFER_ATTACHMENT, Boolean.TRUE);
        loadOptions.put(OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
        loadOptions.put(OPTION_USE_DEPRECATED_METHODS, Boolean.TRUE);
        loadOptions.put(OPTION_USE_PARSER_POOL, new XMLParserPoolImpl());
        loadOptions.put(OPTION_USE_XML_NAME_TO_FEATURE_MAP, new HashMap());
        final Resource resource = resourceSet.getResource(URI.createFileURI(path), true);
        return ((ScenarioState) (resource.getContents().get(0)));
    }
}