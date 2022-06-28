package com.mazda.gms3.fetchcategorieshierarchy.execution;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.mazda.gms3.fetchcategorieshierarchy.impl.StartProcessingImpl;

public class ConversionRunner {

	static Logger logger = Logger.getLogger(ConversionRunner.class);

	public static void main(String[] args) {
		File jarPath = new File(ConversionRunner.class.getProtectionDomain()
				.getCodeSource().getLocation().getPath());
		String propertiesPath = jarPath.getParentFile().getAbsolutePath();
		PropertyConfigurator.configure(propertiesPath + "/log4j.properties");
		logger.info("ConversionRunner :: inside file processing..");

		try {
			String selectedLocale = ExecutableHelper.selectLocaleForOperation();
			if (null != selectedLocale && !"".equals(selectedLocale)) {
				/*
				 * Prompt User to enter the REF KEY FOR WHICH THE HIERARCHY HAS
				 * TO BE FETCHED
				 */
				StartProcessingImpl impl = new StartProcessingImpl();
				String[] tok = "TKM_UG,US_TRANSACTIONS,US_RETIREMENT_PLANS,US_RETIREMENT_RESOURCES,US_COMMUNICATIONS,US_REGISTRATION_TYPE,US_INTERNAL_FUNCTIONS,CAN_E_SERVICES,CAN_LEGAL_DOCUMENTS,CAN_PLAN_TYPES,CAN_PRODUCT,CAN_SYSTEMS,CAN_TAXATION,CAN_TRANSACTION,CAN_REGULATIONS,CAN_CAMPAIGNS_AND_INITIATIVES,CAN_FORMS,CAN_DEPARTMENT_RESOURCES,CAN_POLICIES_AND_PROCEDURES,US_HISTORICAL,CAN_ACCOUNTADVISORDEALER_INFORMATION,US_INVESCO_POLICIES,US_TAX,WORKFLOW_CONTROL,US_PRODUCT,US_STOP_CODES,GLOBAL,US_DEPARTMENTS,CAN_HISTORIC,CAN_TKM_NEWS,NEXEN".split(",");
				for(int a=0;a<tok.length;a++)
				{
//					String catRefKey = ExecutableHelper.enterCategoryRefKeyOpertion();
					String catRefKey = tok[a];
					if (null != catRefKey && !"".equals(catRefKey)) {
						logger.info("ConversionRunner :: Start Proceeding for fetching Data for {"
								+ catRefKey.trim().toUpperCase()
								+ "} for {"
								+ selectedLocale + "} Locales.");
						/*
						 * call function to fetch Category Data
						 */
						impl.startProcessing(selectedLocale,
								catRefKey);
					} else {
						logger.info("ConversionRunner :: Category Ref Key is Null. Exiting Tool.");
					}
				}
				
				if(null!=impl.catList && impl.catList.size()>0)
				{
					impl.generateExcel(impl.catList, selectedLocale, "");
				}
				
			} else {
				logger.info("ConversionRunner :: Selected Locale is Null. Exiting Tool.");
			}
		} catch (Exception e) {

		}
	}

}
