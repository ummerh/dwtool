/**
 * 
 */
package demo.lucene.catalog.search;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Enumeration;

import javax.servlet.http.HttpSession;

/**
 * 
 */
public class HttpSessionDataLogger {
    @SuppressWarnings("unchecked")
    public static void logSession(HttpSession session) {
	try {
	    File dir = new File(System.getProperty("java.io.tmpdir") + File.separator + session.getId());
	    dir.mkdirs();
	    Enumeration<String> attributeNames = session.getAttributeNames();
	    while (attributeNames.hasMoreElements()) {
		String name = attributeNames.nextElement();
		Object data = session.getAttribute(name);
		if (Serializable.class.isAssignableFrom(data.getClass())) {
		    BufferedOutputStream bfos = new BufferedOutputStream(new FileOutputStream(new File(dir, name + ".obj")));
		    ObjectOutputStream oos = new ObjectOutputStream(bfos);
		    try {
			oos.writeObject(data);
		    } finally {
			bfos.flush();
			oos.flush();
			oos.close();
		    }
		} else {
		    System.out.println("Failed to serialize " + name + ", should implement serializable " + data.getClass());
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
