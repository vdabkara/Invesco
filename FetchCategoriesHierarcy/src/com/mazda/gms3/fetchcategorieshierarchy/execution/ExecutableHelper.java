package com.mazda.gms3.fetchcategorieshierarchy.execution;

import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.mazda.gms3.fetchcategorieshierarchy.utils.ApplicationPropertiesUtil;
import com.mazda.gms3.fetchcategorieshierarchy.utils.Utilities;


public class ExecutableHelper {
	
	static Logger logger = Logger.getLogger(ExecutableHelper.class);
	private static Scanner scanner;

	
	public static String selectLocaleForOperation()
	{
		logger.info("ExecutableHelper :: selectLocaleForOperation() :: method starts");		
		String selectedLocale="";
		try
		{
			/*
			 * Read all the Channel Labels & Values
			 */
			StringTokenizer strLabel = new StringTokenizer(ApplicationPropertiesUtil.getProperty("locales_keys"),",");
			String optionsToPrintOnConsole="";
			StringBuilder str = new StringBuilder();
			while(strLabel.hasMoreTokens())
			{
				String token = strLabel.nextToken();
				String label = String.valueOf(token);
				str.append(label);
				str.append("\n");
				// set label to null
				label = null;
			}
			optionsToPrintOnConsole = str.toString();
			str=null;
			if(null!=optionsToPrintOnConsole && !"".equals(optionsToPrintOnConsole))
			{
				// remove extra space from the optionsToPrintOnConsole
				optionsToPrintOnConsole = optionsToPrintOnConsole.trim();
				
				scanner = new Scanner(System.in);
				System.out.println("## Please Select Locale to fetch the Category Data -  ");
				System.out.println(optionsToPrintOnConsole);
				System.out.println("Enter Locale Value to perform :" );

				selectedLocale=scanner.nextLine();
				
				if(null!=selectedLocale && !"".equals(selectedLocale))
				{
					
					if(ApplicationPropertiesUtil.getProperty("locales_keys").contains(selectedLocale))
					{
						// set scanner to null
						scanner= null;
						// return operationType
						return selectedLocale;
					}
					else
					{
						System.err.println("Please enter a Valid Value for the Locale :: ");
						selectedLocale=selectLocaleForOperation();
					}
				}
				else
				{
					//call the recursion type
					selectedLocale=selectLocaleForOperation();
				}
			}
			else
			{
				// return null
				selectedLocale = null;
			}
			// set strLabels to null
			strLabel=null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(ExecutableHelper.class.getName(), "selectLocaleForOperation()", e);
		}
		logger.info("ExecutableHelper :: selectLocaleForOperation() :: method ends");
		return selectedLocale;
	}

	public static String enterCategoryRefKeyOpertion()
	{
		logger.info("ExecutableHelper :: enterCategoryRefKeyOpertion() :: method starts");		
		String catRefKey="";
		try
		{
			scanner = new Scanner(System.in);
			System.out.println("## Enter Category Ref Key for Which the Complete Hierarchy has to be Fetched -  ");
			catRefKey=scanner.nextLine();
			
			if(null!=catRefKey && !"".equals(catRefKey))
			{
				// set scanner to null
				scanner= null;
				// return operationType
				return catRefKey;	
			}
			else
			{
				//call the recursion type
				catRefKey=enterCategoryRefKeyOpertion();
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(ExecutableHelper.class.getName(), "enterCategoryRefKeyOpertion()", e);
		}
		logger.info("ExecutableHelper :: enterCategoryRefKeyOpertion() :: method ends");
		return catRefKey;
	}

}
