package com.invesco.datamigration.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.invesco.datamigration.utils.DBConnectionHelper;
import com.invesco.datamigration.utils.Utilities;
import com.invesco.datamigration.vo.ChannelDetails;
import com.invesco.datamigration.vo.DocumentDetails;

public class ReadTransactionDAO extends DBConnectionHelper{
	
	private Logger logger = Logger.getLogger(ReadTransactionDAO.class);
	
	/*
	 * FETCH CHANNELS DATA ALONG WITH APPLICABLE LOCALES
	 */
	public List<ChannelDetails> getChannelDetails()
	{
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement pstmt=null;
		List<ChannelDetails> channelsList = null;
		try
		{
			conn = getDestinationConnection();
			String sql = "select * from im_channel_locale_mapping";
			pstmt = conn.prepareStatement(sql);
			rs=pstmt.executeQuery();
			ChannelDetails details = null;
			while(rs.next())
			{
				details = new ChannelDetails();
				details.setChannelAbbr(rs.getString("CHANNEL_ABBREVIATION"));
				details.setChannelName(rs.getString("CHANNEL_NAME"));
				details.setLocale(rs.getString("LOCALE").trim());
				details.setChannelRefKey(rs.getString("CHANNEL_REFKEY"));
				if(null==channelsList || channelsList.size()<=0)
				{
					channelsList = new ArrayList<ChannelDetails>();
				}
				channelsList.add(details);
				details = null;
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(ReadTransactionDAO.class.getName(), "getChannelDetails()", e);
		}
		finally
		{
			try
			{
				if(null!=conn)
					conn.close();conn=null;
				if(null!=rs)
					rs.close();rs=null;
				if(null!=pstmt)
					pstmt.close();pstmt=null;
			}
			catch(SQLException e)
			{
				Utilities.printStackTraceToLogs(ReadTransactionDAO.class.getName(), "getChannelDetails()", e);
			}
		}
		return channelsList;
	}

	public List<DocumentDetails> getDocumentsList(String channelAbbr, String locale, String criteriaDate)
	{
		List<DocumentDetails> documentsList = null;Connection conn = null;
		ResultSet rs = null;
		PreparedStatement pstmt=null;
		try
		{
			if(null!=channelAbbr && !"".equals(channelAbbr) && null!=locale && !"".equals(locale) && null!=criteriaDate && !"".equals(criteriaDate))
			{
				Date checkReviewDate = null;
				SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
				if(locale.trim().toLowerCase().equals("en_us"))
				{
					String d = "12-31-9999";
					checkReviewDate = sdf.parse(d);
					d = null;
				}
				
				conn = getSourceConnection();
				String sql = "";
				sql = "select documentid,localeid,majorversion,minorversion,dateadded,displayenddate,displayreviewdate,publishdate,published,basedlocaleid,indexmasteridentifiers,ownername "
						+ "from infomanager.contenttext where minorversion =0 and majorversion >= 1 and documentid like '"+channelAbbr.trim()+"%' AND "
						+ "localeid='"+locale+"' AND dateadded >= to_date('"+criteriaDate+"','YYYY-MM-DD') ";
				logger.info("getDocumentsList :: Sql :: >"+ sql);
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				DocumentDetails details = null;
				DocumentDetails existingDetails=null;
				while(rs.next())
				{
					details = new DocumentDetails();
					details.setDocumentId(rs.getString("documentid"));
					details.setLocale(rs.getString("localeid"));
					details.setBaseLocale(rs.getString("basedlocaleid"));
					/*
					 * IDENTIFY TRANSLATION FLAG
					 */
					if(null!=details.getBaseLocale() && !"".equals(details.getBaseLocale()))
					{
						if(details.getBaseLocale().trim().toLowerCase().equals(details.getLocale().trim().toLowerCase()))
						{
							// both are same - Master Identifier
							details.setIsTranslation("NO");
						}
						else
						{
							// Translation
							details.setIsTranslation("YES");
						}
					}
					else
					{
						// baseLocale is null - Master Identifier
						details.setIsTranslation("NO");
						// set baseLocale as documentLocale
						details.setBaseLocale(details.getLocale());
					}
					details.setMajorVersion(String.valueOf(rs.getInt("majorversion")));
					details.setMinorVersion(String.valueOf(rs.getInt("minorversion")));
					details.setDateAdded(rs.getTimestamp("dateadded"));
					details.setDisplayEndDate(rs.getTimestamp("displayenddate"));
					details.setDisplayReviewDate(rs.getTimestamp("displayreviewdate"));
					details.setPublishDate(rs.getTimestamp("publishdate"));
					details.setDocumentStatus(rs.getString("published"));
					
					details.setTitle(rs.getString("indexmasteridentifiers"));
					details.setOwnerName(rs.getString("ownername"));
					
					if(null!=details.getDocumentStatus() && "Y".equals(details.getDocumentStatus()))
					{
						// APPLY EXPIRED LOGIC IN LIVE STATUS AS WELL
						if(null!=details.getDisplayEndDate())
						{
							if(details.getDisplayEndDate().getTime()<= new Date().getTime())
							{
								// DOCUMENT IS EXPIRED
								details.setDocumentStatus("EXPIRED");
							}
							else
							{
								// DOCUMENT IS LIVE
								details.setDocumentStatus("LIVE");
							}
						}
						else
						{
							// DOCUMENT IS LIVE - SINCE EITHER OR BOTH DISPLAY END DATE / PUBLISH DATE ARE NULL
							details.setDocumentStatus("LIVE");
						}
					}
					else
					{
						if(null!=details.getDisplayEndDate())
						{
							if(details.getDisplayEndDate().getTime()<= new Date().getTime())
							{
								// DOCUMENT IS EXPIRED
								details.setDocumentStatus("EXPIRED");
							}
							else
							{
								// DOCUMENT IS UNPUBLISHED
								details.setDocumentStatus("UNPUBLISHED");
							}
						}
						else
						{
							// DOCUMENT IS UNPUBLISHED - SINCE EITHER OR BOTH DISPLAY END DATE / PUBLISH DATE ARE NULL
							details.setDocumentStatus("UNPUBLISHED");
						}
					}
					
					/*
					 * IF LOCALE IS US
					 * 	CHECK IF DOCUMENT STATUS IS LIVE OR EXPIRED
					 */
					boolean proceedFurther = true;
					if(details.getLocale().trim().toLowerCase().equals("en_us"))
					{
						if(details.getDocumentStatus().equals("LIVE") || details.getDocumentStatus().equals("EXPIRED"))
						{
							// set proceedFurther to FALSE
							proceedFurther = false;
							/*
							 * CHECK FOR REVIEW DATE IS NOT EQUALS TO 12-31-9999
							 */
							if(null!=checkReviewDate && null!=details.getDisplayReviewDate())
							{
								String formattedDate = sdf.format(details.getDisplayReviewDate());
								Date c = sdf.parse(formattedDate);
								if(checkReviewDate.getTime()!=c.getTime())
								{
									proceedFurther = true;
								}
								c=null;
								formattedDate = null;
							}
						}
					}
					
					if(proceedFurther==true)
					{
						/*
						 * NOW PREPARED DOCUMENTS LIST VERSION WISE
						 */
						boolean add = true;
						if(null!=documentsList && documentsList.size()>0)
						{
							existingDetails = null;
							for(int a=0;a<documentsList.size();a++)
							{
								existingDetails = (DocumentDetails)documentsList.get(a);
								if(existingDetails.getDocumentId().equals(details.getDocumentId()))
								{
									// DOCUMENT ALREADY ADDED - ANOTEHR VERSION HAS BEEN FOUND
									if(null==existingDetails.getVersionList() || existingDetails.getVersionList().size()<=0)
									{
										existingDetails.setVersionList(new ArrayList<DocumentDetails>());
									}
									// add this index to document Version List
									existingDetails.getVersionList().add(details);
									// do not add document to document list
									add = false;
									break;
								}
							}
						}

						if(add==true)
						{
							// add the current index TO document Version List as well
							details.setVersionList(new ArrayList<DocumentDetails>());
							details.getVersionList().add(details);

							// now add to documentsList
							if(null==documentsList || documentsList.size()<=0)
							{
								documentsList = new ArrayList<DocumentDetails>();
							}
							documentsList.add(details);
						}
					}
					details = null;
				}
				sql = null;
				
				if(null!=documentsList && documentsList.size()>0)
				{
					logger.info("getDocumentsList :: Total Unique Documents Found for "+channelAbbr+" / "+ locale+" are :: >"+ documentsList.size());
				}
				else
				{
					logger.info("getDocumentsList :: No Unique Documents Found for "+channelAbbr+" / "+ locale+".");
				}
				checkReviewDate = null;
				sdf = null;
			}
			else
			{
				logger.info("getDocumentsList :: Required Paramters > Channel Abbreviation / Locale / Criteria Date are null. No Documents can be fetched.");
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(ReadTransactionDAO.class.getName(), "getDocumentsList()", e);
		}
		finally
		{
			try
			{
				if(null!=conn)
					conn.close();conn=null;
				if(null!=rs)
					rs.close();rs=null;
				if(null!=pstmt)
					pstmt.close();pstmt=null;
			}
			catch(SQLException e)
			{
				Utilities.printStackTraceToLogs(ReadTransactionDAO.class.getName(), "getDocumentsList()", e);
			}
			
			channelAbbr = null;
			locale = null;
			criteriaDate = null;
		}
		return documentsList;
	}

	public List<DocumentDetails> getENUSOneYearDocumentsList(String channelAbbr, String locale, String criteriaDate)
	{
		List<DocumentDetails> documentsList = null;Connection conn = null;
		ResultSet rs = null;
		PreparedStatement pstmt=null;
		try
		{
			if(null!=channelAbbr && !"".equals(channelAbbr) && null!=locale && !"".equals(locale) && null!=criteriaDate && !"".equals(criteriaDate))
			{
				Date checkReviewDate = null;
				SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
				if(locale.trim().toLowerCase().equals("en_us"))
				{
					String d = "12-31-9999";
					checkReviewDate = sdf.parse(d);
					d = null;
				}
				
				conn = getSourceConnection();
				String sql = "";
				sql = "select documentid,localeid,majorversion,minorversion,dateadded,displayenddate,displayreviewdate,publishdate,published,basedlocaleid,indexmasteridentifiers,ownername  "
						+ "from infomanager.contenttext where minorversion =0 and majorversion >= 1 and documentid like '"+channelAbbr.trim()+"%' AND "
						+ "localeid='"+locale+"' AND dateadded >= to_date('"+criteriaDate+"','YYYY-MM-DD') ";
				logger.info("getENUSOneYearDocumentsList :: Sql :: >"+ sql);
				pstmt = conn.prepareStatement(sql);
				rs = pstmt.executeQuery();
				DocumentDetails details = null;
				DocumentDetails existingDetails=null;
				while(rs.next())
				{
					details = new DocumentDetails();
					details.setDocumentId(rs.getString("documentid"));
					details.setLocale(rs.getString("localeid"));
					details.setBaseLocale(rs.getString("basedlocaleid"));
					/*
					 * IDENTIFY TRANSLATION FLAG
					 */
					if(null!=details.getBaseLocale() && !"".equals(details.getBaseLocale()))
					{
						if(details.getBaseLocale().trim().toLowerCase().equals(details.getLocale().trim().toLowerCase()))
						{
							// both are same - Master Identifier
							details.setIsTranslation("NO");
						}
						else
						{
							// Translation
							details.setIsTranslation("YES");
						}
					}
					else
					{
						// baseLocale is null - Master Identifier
						details.setIsTranslation("NO");
						// set baseLocale as documentLocale
						details.setBaseLocale(details.getLocale());
					}
					details.setMajorVersion(String.valueOf(rs.getInt("majorversion")));
					details.setMinorVersion(String.valueOf(rs.getInt("minorversion")));
					details.setDateAdded(rs.getTimestamp("dateadded"));
					details.setDisplayEndDate(rs.getTimestamp("displayenddate"));
					details.setDisplayReviewDate(rs.getTimestamp("displayreviewdate"));
					details.setPublishDate(rs.getTimestamp("publishdate"));
					details.setDocumentStatus(rs.getString("published"));
					
					details.setTitle(rs.getString("indexmasteridentifiers"));
					details.setOwnerName(rs.getString("ownername"));
					
					if(null!=details.getDocumentStatus() && "Y".equals(details.getDocumentStatus()))
					{
						// APPLY EXPIRED LOGIC IN LIVE STATUS AS WELL
						if(null!=details.getDisplayEndDate())
						{
							if(details.getDisplayEndDate().getTime()<= new Date().getTime())
							{
								// DOCUMENT IS EXPIRED
								details.setDocumentStatus("EXPIRED");
							}
							else
							{
								// DOCUMENT IS LIVE
								details.setDocumentStatus("LIVE");
							}
						}
						else
						{
							// DOCUMENT IS LIVE - SINCE EITHER OR BOTH DISPLAY END DATE / PUBLISH DATE ARE NULL
							details.setDocumentStatus("LIVE");
						}
					}
					else
					{
						if(null!=details.getDisplayEndDate())
						{
							if(details.getDisplayEndDate().getTime()<= new Date().getTime())
							{
								// DOCUMENT IS EXPIRED
								details.setDocumentStatus("EXPIRED");
							}
							else
							{
								// DOCUMENT IS UNPUBLISHED
								details.setDocumentStatus("UNPUBLISHED");
							}
						}
						else
						{
							// DOCUMENT IS UNPUBLISHED - SINCE EITHER OR BOTH DISPLAY END DATE / PUBLISH DATE ARE NULL
							details.setDocumentStatus("UNPUBLISHED");
						}
					}
					
					/*
					 * IF LOCALE IS US
					 * 	CHECK IF DOCUMENT STATUS IS LIVE OR EXPIRED
					 */
					boolean proceedFurther = false;
					if(details.getLocale().trim().toLowerCase().equals("en_us"))
					{
						if(details.getDocumentStatus().equals("LIVE") || details.getDocumentStatus().equals("EXPIRED"))
						{
							/*
							 * CHECK FOR REVIEW DATE EQUALS TO 12-31-9999
							 */
							if(null!=checkReviewDate && null!=details.getDisplayReviewDate())
							{
								String formattedDate = sdf.format(details.getDisplayReviewDate());
								Date c = sdf.parse(formattedDate);
								if(checkReviewDate.getTime()==c.getTime())
								{
									proceedFurther = true;
								}
								c=null;
								formattedDate = null;
							}
						}
					}
					
					if(proceedFurther==true)
					{
						/*
						 * NOW PREPARED DOCUMENTS LIST VERSION WISE
						 */
						boolean add = true;
						if(null!=documentsList && documentsList.size()>0)
						{
							existingDetails = null;
							for(int a=0;a<documentsList.size();a++)
							{
								existingDetails = (DocumentDetails)documentsList.get(a);
								if(existingDetails.getDocumentId().equals(details.getDocumentId()))
								{
									// DOCUMENT ALREADY ADDED - ANOTEHR VERSION HAS BEEN FOUND
									if(null==existingDetails.getVersionList() || existingDetails.getVersionList().size()<=0)
									{
										existingDetails.setVersionList(new ArrayList<DocumentDetails>());
									}
									// add this index to document Version List
									existingDetails.getVersionList().add(details);
									// do not add document to document list
									add = false;
									break;
								}
							}
						}

						if(add==true)
						{
							// add the current index TO document Version List as well
							details.setVersionList(new ArrayList<DocumentDetails>());
							details.getVersionList().add(details);

							// now add to documentsList
							if(null==documentsList || documentsList.size()<=0)
							{
								documentsList = new ArrayList<DocumentDetails>();
							}
							documentsList.add(details);
						}
					}
					details = null;
				}
				sql = null;
				
				if(null!=documentsList && documentsList.size()>0)
				{
					logger.info("getENUSOneYearDocumentsList :: Total Unique Documents Found for "+channelAbbr+" / "+ locale+" are :: >"+ documentsList.size());
				}
				else
				{
					logger.info("getENUSOneYearDocumentsList :: No Unique Documents Found for "+channelAbbr+" / "+ locale+".");
				}
				checkReviewDate = null;
				sdf = null;
			}
			else
			{
				logger.info("getENUSOneYearDocumentsList :: Required Paramters > Channel Abbreviation / Locale / Criteria Date are null. No Documents can be fetched.");
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(ReadTransactionDAO.class.getName(), "getENUSOneYearDocumentsList()", e);
		}
		finally
		{
			try
			{
				if(null!=conn)
					conn.close();conn=null;
				if(null!=rs)
					rs.close();rs=null;
				if(null!=pstmt)
					pstmt.close();pstmt=null;
			}
			catch(SQLException e)
			{
				Utilities.printStackTraceToLogs(ReadTransactionDAO.class.getName(), "getENUSOneYearDocumentsList()", e);
			}
			
			channelAbbr = null;
			locale = null;
			criteriaDate = null;
		}
		return documentsList;
	}

}
