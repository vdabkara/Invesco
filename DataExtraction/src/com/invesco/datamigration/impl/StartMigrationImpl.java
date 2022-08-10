package com.invesco.datamigration.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.invesco.datamigration.dao.CreateTableDAO;
import com.invesco.datamigration.dao.ReadTransactionDAO;
import com.invesco.datamigration.dao.WriteTransactionDAO;
import com.invesco.datamigration.utils.CustomUtils;
import com.invesco.datamigration.utils.Utilities;
import com.invesco.datamigration.vo.ChannelDetails;
import com.invesco.datamigration.vo.DocumentDetails;

public class StartMigrationImpl {

	private Logger logger = Logger.getLogger(StartMigrationImpl.class);
	
	private ReadTransactionDAO readTransactionDAO = null;
	
	private WriteTransactionDAO writeTransactionDAO = null;
	
	public static void main(String[] args) {
		// initialie Loggers
		File jarPath = new File(StartMigrationImpl.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String propertiesPath = jarPath.getParentFile().getAbsolutePath();
		PropertyConfigurator.configure(propertiesPath+"/"+"log.properties");
		try
		{
			boolean allGood = true;
			String[] tables = "DOCUMENT_DETAILS,ATTACHMENT_DETAILS,INLINEIMAGES_DETAILS,INNERLINK_DETAILS,VIEW_DETAILS,CATEGORY_DETAILS".split(",");
			for(int a=0;a<tables.length;a++)
			{
				allGood = CreateTableDAO.checkTableExists(tables[a], tables[a]);
				if(allGood==false)
				{
					break;
				}
			}
			
			tables = null;
			
			if(allGood==true)
			{
				// proceed
				StartMigrationImpl impl = new StartMigrationImpl();
				impl.startMigration();
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(StartMigrationImpl.class.getName(), "main()", e);
		}
	}
	
	private void startMigration()
	{
		try
		{
			readTransactionDAO = new ReadTransactionDAO();
			writeTransactionDAO = new WriteTransactionDAO();
			/*
			 * READ CHANNELS WITH LOCALES LIST AND START IDENTIFYING DOCUMENTS
			 */
			List<ChannelDetails> channelsList = readTransactionDAO.getChannelDetails();
			if(null!=channelsList && channelsList.size()>0)
			{
				/*
				 * IDENTIFY SEARCH CRITERIA DATE - WHICH WILL BE 3 YEARS LESS THEN CURRENT DATE
				 */
//				String criteriaDate = Utilities.getPreviousDate(-3);
				String criteriaDate = "2019-01-01";
				// get ONE YEAR BEHIND CRITERIA DATE FOR EN_US - UNPUBLISHED DOCUMENTS 
//				String oneYearCriteriaDate = Utilities.getPreviousDate(-1);
				String oneYearCriteriaDate= "2021-01-01";
				logger.info("startMigration:: Previous 3 Years Date Identified is :: >"+ criteriaDate);
				logger.info("startMigration:: Previous 1 Year Date Identified for EN_US LOCALE for LIVE / EXPIRED DOCS is :: >"+ oneYearCriteriaDate);
				ChannelDetails channelDetails = null;
				List<DocumentDetails> documentsList = null;
				List<DocumentDetails> threeYearsDocumentsList = null;
				List<DocumentDetails> enUsOneYearDocumentsList = null;
				List<DocumentDetails> liveDocumentsMinus3And7YearsList = null;
				DocumentDetails documentDetails=null;
				DocumentDetails versionDetails = null;
				for(int a=0;a<channelsList.size();a++)
				{
					channelDetails = (ChannelDetails)channelsList.get(a);
					logger.info("############ Start Processing ::> "+ channelDetails.getChannelName()+" For Locale :: >"+ channelDetails.getLocale());
					/*
					 * START FETCHING DOCUMENTS
					 * FETCH LIVE DOCUMENTS ONLY = MINUS 3 YEARS CRITERIA & MINUS 7 YEARS DOCS DATA FOR EN_CA LOCALE ONLY
					 */
					liveDocumentsMinus3And7YearsList = readTransactionDAO.getLiveDocumentsMinus3And7YearsList(channelDetails.getChannelAbbr(), channelDetails.getLocale(), criteriaDate);
					if(null!=liveDocumentsMinus3And7YearsList && liveDocumentsMinus3And7YearsList.size()>0)
					{
						// add to documentsList
						if(null==documentsList || documentsList.size()<=0)
						{
							documentsList = new ArrayList<DocumentDetails>();
						}
						documentsList.addAll(liveDocumentsMinus3And7YearsList);
					}
					liveDocumentsMinus3And7YearsList = null;
//					// FETCH ON 3 YEARS CRITERIA
					threeYearsDocumentsList = readTransactionDAO.getDocumentsList(channelDetails.getChannelAbbr(), channelDetails.getLocale(), criteriaDate);
					if(null!=threeYearsDocumentsList && threeYearsDocumentsList.size()>0)
					{
						// add to documentsList
						if(null==documentsList || documentsList.size()<=0)
						{
							documentsList = new ArrayList<DocumentDetails>();
						}
						documentsList.addAll(threeYearsDocumentsList);
					}
					threeYearsDocumentsList = null;
					if(channelDetails.getLocale().trim().toLowerCase().equals("en_us"))
					{
						// FETCH ON 1 YEAR CRITERIA
						enUsOneYearDocumentsList= readTransactionDAO.getENUSOneYearDocumentsList(channelDetails.getChannelAbbr(), channelDetails.getLocale(), oneYearCriteriaDate);
						if(null!=enUsOneYearDocumentsList && enUsOneYearDocumentsList.size()>0)
						{
							// add to documentsList
							if(null==documentsList || documentsList.size()<=0)
							{
								documentsList = new ArrayList<DocumentDetails>();
							}
							documentsList.addAll(enUsOneYearDocumentsList);
						}
						enUsOneYearDocumentsList = null;
					}
					if(null!=documentsList && documentsList.size()>0)
					{
						/*
						 * ITERATE DOCUMENTS LIST AND START PROCESSING EACH VERSION
						 */
						documentDetails = null;
						for(int b=0;b<documentsList.size();b++)
						{
							documentDetails = (DocumentDetails)documentsList.get(b);
							logger.info("------- STARTING "+documentDetails.getDocumentId()+" OF "+ documentDetails.getLocale()+" ("+ (b+1)+" / "+ documentsList.size()+").");
							// set ChannelName
							documentDetails.setChannel(channelDetails.getChannelName());
							// set channelRefKey
							documentDetails.setChannelRefKey(channelDetails.getChannelRefKey());
							if(null!=documentDetails.getVersionList() && documentDetails.getVersionList().size()>0)
							{
								logger.info("---- TOTAL VERSIONS FOUND ARE --------------------- >"+ documentDetails.getVersionList().size());
								versionDetails = null;
								for(int c=0;c<documentDetails.getVersionList().size();c++)
								{
									versionDetails = (DocumentDetails)documentDetails.getVersionList().get(c);
									// SET CHANNEL NAME
									versionDetails.setChannel(channelDetails.getChannelName());
									// SET CHANNEL REF KEY
									versionDetails.setChannelRefKey(channelDetails.getChannelRefKey());
									/*
									 * FOR EACH VERSION GET THE IQ XML
									 * IDENTIFY ATTACHMENTS
									 * IDENTIFY INNERLINKS
									 * IDENTIFY IMAGES
									 * 
									 * GENERATE XML FILE
									 */
									logger.info(" ------ PROCESSING VERSION > "+ versionDetails.getMajorVersion()+"."+versionDetails.getMinorVersion()+" ------");
									/*
									 *  prepare IQ XML Path
									 *  IF DOCUMENT IS PUBLISED E.G. LIVE 
									 *  	LOOK FOR THE VERSION IN LIVE FOLDER
									 *  ELSE
									 *  	LOOK FOR THE VERSION IN STAGING FOLDER
									 */
									if(versionDetails.getDocumentStatus().equals("LIVE"))
									{
										logger.info("---------------- PROCEED FOR CHECKING IN LIVE FOLDER ---------------");
										versionDetails = CustomUtils.findXMLFileInLiveFolder(versionDetails);
									}
									else
									{
										logger.info("---------------- PROCEED FOR CHECKING IN STAGING FOLDER ---------------");
										// staging folder = Unpublished / Expired
										versionDetails = CustomUtils.findXMLFileInStagingFolder(versionDetails);
									}
									/*
									 * save the transaction is database 
									 */
									writeTransactionDAO.saveDocumentDetails(versionDetails);
									versionDetails = null;
								}
							}
							else
							{
								/*
								 * SKIP PROCESSING OF SUCH DOCUMENTS
								 * AS NO VERSION FOUND 
								 */
								documentDetails.setProcessingStatus("SKIPPED");
								documentDetails.setErrorMessage("NO VERSIONS FOUND.PROCESSING FOR THE DOCUMENT WILL BE SKIPPED.");
								/*
								 * save the transaction is database 
								 */
								writeTransactionDAO.saveDocumentDetails(documentDetails);
							}
							logger.info("------- ENDING "+documentDetails.getDocumentId()+" OF "+ documentDetails.getLocale()+" ("+ (b+1)+" / "+ documentsList.size()+").");
							documentDetails = null;
						}
					}
					documentsList = null;
					logger.info("############ End Processing ::> "+ channelDetails.getChannelName()+" For Locale :: >"+ channelDetails.getLocale());
				}
				
				criteriaDate = null;
				oneYearCriteriaDate = null;
			}
			else
			{
				logger.info("startMigration :: No Channels Data Found for Extraction. Exiting.");
			}
			channelsList = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(StartMigrationImpl.class.getName(), "startMigration()", e);
		}
		finally
		{
			readTransactionDAO = null;
			writeTransactionDAO = null;
		}
	}

}
