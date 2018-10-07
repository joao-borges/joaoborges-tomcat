
package br.com.joaoborges;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardHost;
import org.apache.catalina.startup.ContextConfig;

/**
 * @author joaoborges
 */
public class SyspropConfigurableHost extends StandardHost {

	public SyspropConfigurableHost() {
		super();

		String contextConfigFile = System.getProperty("contextConfigFile");
		if (contextConfigFile == null) {
			throw new UnsupportedOperationException("missing conf file");
		}
		Properties props = new Properties();
		
		try (FileInputStream stream = new FileInputStream(new File(contextConfigFile))) {
			props.load(stream);
		} catch (IOException e) {
			throw new IllegalArgumentException("invalid file", e);
		}
		
		Map<String, ContextConfiguration> contexts = parseConfig(props);
		for (ContextConfiguration conf : contexts.values()) {
			StandardContext ctx = new StandardContext();
			ctx.setPath(conf.path);
			ctx.setDocBase(conf.docBase);
			ctx.addLifecycleListener(new ContextConfig());
			super.addChild(ctx);
		}
	}
	
	private Map<String, ContextConfiguration> parseConfig(Properties props) {
		Map<String, ContextConfiguration> map = new LinkedHashMap<>();
		for(Map.Entry<Object, Object> entry : props.entrySet()) {
			String key = entry.getKey().toString();
			String value = entry.getValue().toString();
			String[] keyBaseProp = key.split("\\.");
			if (map.get(keyBaseProp[0]) == null) {
				map.put(keyBaseProp[0], new ContextConfiguration());
			}
			try {
				Field f = ContextConfiguration.class.getDeclaredField(keyBaseProp[1]);
				f.set(map.get(keyBaseProp[0]), value);
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage(), e);
			}
		}
		return map;
	}

	private static class ContextConfiguration {
		String path;
		String docBase;
	}
}
