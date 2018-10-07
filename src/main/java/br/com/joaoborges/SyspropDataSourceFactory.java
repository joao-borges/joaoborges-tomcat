package br.com.joaoborges;

import java.lang.reflect.Field;
import java.sql.Driver;
import java.util.Hashtable;
import java.util.Vector;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.apache.naming.ResourceRef;
import org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory;

import br.com.joaoborges.database.DatabaseInfoCache;
import br.com.joaoborges.database.DatabaseInfoCache.ConnectionInfo;

/**
 * @author joaoborges
 */
public class SyspropDataSourceFactory extends BasicDataSourceFactory {

	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment)
			throws Exception {
		ResourceRef ref = (ResourceRef) obj;
		StringRefAddr urlRef = (StringRefAddr) ref.get("url");
		StringRefAddr vqRef = (StringRefAddr) ref.get("validationQuery");
		String url = urlRef.getContent().toString();
		ConnectionInfo connectionInfo = DatabaseInfoCache.findConnectionInfo(url);
		if (connectionInfo != null) {
			Driver driver = connectionInfo.driver;
			StringRefAddr vq = null;
			if (driver.getClass().getName().equals(DatabaseInfoCache.ORACLE_DRIVER)) {
				vq = new StringRefAddr(vqRef.getType(), "select 1 from dual");				
			} else if (driver.getClass().getName().equals(DatabaseInfoCache.POSTGRESQL_DRIVER)) {
				vq = new StringRefAddr(vqRef.getType(), "select 1");				
			}
			
			Field field = Reference.class.getDeclaredField("addrs");
			field.setAccessible(true);
			Vector<RefAddr> addrs = (Vector<RefAddr>) field.get(ref);
			field.setAccessible(false);
			
			int idx = addrs.indexOf(vqRef);
			addrs.set(idx, vq);
			
			ref.remove(addrs.indexOf(ref.get("description")));
		}
				
		return super.getObjectInstance(obj, name, nameCtx, environment);
	}
	
}
