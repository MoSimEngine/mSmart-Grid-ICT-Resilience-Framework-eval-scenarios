package smartgrid.newsimcontrol.rcp.helper;
import java.io.IOException;
import java.util.Collections;
import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
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
}