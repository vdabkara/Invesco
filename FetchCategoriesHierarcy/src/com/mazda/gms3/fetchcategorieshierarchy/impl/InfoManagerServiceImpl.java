package com.mazda.gms3.fetchcategorieshierarchy.impl;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.inquira.client.serviceclient.IQServiceClient;
import com.inquira.client.serviceclient.IQServiceClientManager;
import com.inquira.client.serviceclient.request.IQCategoryRequest;
import com.inquira.im.ito.CategoryITO;
import com.inquira.im.ito.impl.CategoryITOImpl;
import com.inquira.util.ewr.ErrorRecord;
import com.mazda.gms3.fetchcategorieshierarchy.utils.ApplicationPropertiesUtil;
import com.mazda.gms3.fetchcategorieshierarchy.utils.Utilities;

public class InfoManagerServiceImpl {

	static Logger logger = Logger.getLogger(InfoManagerServiceImpl.class);
	
	private static IQServiceClient getConnectionWithIM(IQServiceClient client) {
		try {
			client = IQServiceClientManager
					.connect(
							""
									+ ApplicationPropertiesUtil
									.getProperty("USERNAME") + "",
									""
											+ ApplicationPropertiesUtil
											.getProperty("PASSWORD") + "",
											""
													+ ApplicationPropertiesUtil
													.getProperty("DOMAIN") + "",
													""
															+ ApplicationPropertiesUtil
															.getProperty("REPOSITORY") + "",
															""
																	+ ApplicationPropertiesUtil
																	.getProperty("IM_CLIENT_LIBRARY_ENDPOINT")
																	+ "",
																	""
																			+ ApplicationPropertiesUtil
																			.getProperty("SEARCH_CLIENT_LIBRARY_ENDPOINT")
																			+ "", null, true);
		} catch (Exception e) {
			Utilities.printStackTraceToLogs(InfoManagerServiceImpl.class.getName(), "getConnectionWithIM()", e);
		}
		return client;
	}

	
	public static CategoryITO getCategory(String refKey, String locale) 
	{
		IQServiceClient client = null;
		CategoryITO catIto = new CategoryITOImpl();
		try 
		{
			client = getConnectionWithIM(client);
			if (null != client && null != client.getAuthenticationToken() 
					&& !"".equals(client.getAuthenticationToken())) 
			{
				IQCategoryRequest catRequest = client.getCategoryRequest();
				catIto = catRequest.getCategoryByReferenceKeyAndLocale(refKey, locale);
				catRequest.release();
				catRequest = null;
			}
			else 
			{
				logger.info("getCategory :: Authentication Token is null.");
			}
		} 
		catch (Exception e) 
		{
			if (null!=client) 
			{
				if (null!=client.getEWR() && client.getEWR().hasErrorsOrWarnings()) 
				{
					// EWR reported a problem, check to see if it is an error
					if (client.getEWR().hasErrors()) 
					{
						// output the errors
						List<ErrorRecord> errors = client.getEWR().getErrors();
						for (Iterator<ErrorRecord> iter = errors.iterator(); iter.hasNext();) {
							ErrorRecord rec = iter.next();
							logger.info("getCategory :: checkEWR :: Error Code :: >" + rec.getCode());
							logger.info("getCategory :: checkEWR :: Error Message :: >" + rec.getMessage());
						}
					}
				}
			}
			Utilities.printStackTraceToLogs(InfoManagerServiceImpl.class.getName(), "getCategory()", e);
		} 
		finally 
		{
			if(null!=client)
			{
				client.close();
			}
			client = null;
		}
		return catIto;
	}

}
