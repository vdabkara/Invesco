/**
 * 
 */
package com.mazda.gms3.fetchcategorieshierarchy.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;


/**
 * @author darora
 * 
 */
public class ApplicationPropertiesUtil {
	static Logger log = Logger.getLogger(ApplicationPropertiesUtil.class);

	public static String getProperty(String key) {
		String value = null;
		FileInputStream fis = null;
		try {
			Properties mainProperties = new Properties();
			File jarPath = new File(ApplicationPropertiesUtil.class
					.getProtectionDomain().getCodeSource().getLocation()
					.getPath());

			String propertiesPath = jarPath.getParentFile().getAbsolutePath();
			// log.info("in getProperty method propertiesPath :" +
			// propertiesPath);
			// String propertiesPath = jarPath.getAbsolutePath();
			// if (null != propertiesPath && !"".equals(propertiesPath)) {
			// propertiesPath = propertiesPath.replace("\\", "/");
			// }
			fis = new FileInputStream(propertiesPath
					+ "/application.properties");
			mainProperties.load(fis);
			value = mainProperties.getProperty(key);

		} catch (IOException ioe) {
			log.error(ioe.getMessage());
		} catch (Exception e) {
			log.error(e.getMessage());
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					log.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}
		return value;
	}
}
