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
				// since the data is to be extracted until 29th July 2022
				String endDate="2022-07-30";
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
						+ "localeid='"+locale+"' AND dateadded >= to_date('"+criteriaDate+"','YYYY-MM-DD') and dateadded < to_date('"+endDate+"','YYYY-MM-DD') ";
//						+ " AND documentid in ('BR1004','BR1006','BR1008','BR1012','BR1016','BR1021','BR1024','BR1025','BR1027','BR1028','BR1030','BR1041','BR1051','BR1065','BR1069','BR1086','BR1087','BR1090','BR1099','BR1102','BR1109','BR1112','BR1113','BR1121','BR1122','BR1125','BR1126','BR1127','BR1128','BR1129','BR1132','BR1133','BR1134','BR1138','BR1140','BR1141','BR1143','BR1144','BR1145','BR1147','BR1148','BR1149','BR1150','BR1157','BR1161','BR1166','BR1167','BR1170','BR1171','BR1179','BR1183','BR1184','BR1185','BR1186','BR1188','BR1191','BR1194','BR1195','BR1196','BR1197','BR1198','BR1199','BR120','BR1200','BR1201','BR1202','BR1203','BR1204','BR1205','BR1206','BR1207','BR1208','BR1209','BR1210','BR1211','BR1212','BR1213','BR1214','BR1215','BR1216','BR1217','BR1218','BR1219','BR1220','BR1221','BR1222','BR1223','BR1224','BR1225','BR1226','BR1227','BR1228','BR1229','BR1230','BR1231','BR1232','BR1233','BR1234','BR1235','BR1238','BR1239','BR1242','BR1243','BR1244','BR1245','BR1246','BR1247','BR1252','BR126','BR1266','BR1271','BR1273','BR1274','BR1275','BR1280','BR1289','BR1336','BR1345','BR1373','BR1399','BR1404','BR1409','BR1429','BR1448','BR1453','BR147','BR173','BR19','BR201','BR209','BR223','BR230','BR232','BR234','BR241','BR246','BR250','BR253','BR259','BR279','BR286','BR301','BR310','BR339','BR340','BR345','BR375','BR394','BR482','BR494','BR496','BR500','BR513','BR520','BR544','BR545','BR547','BR549','BR552','BR553','BR554','BR561','BR562','BR567','BR57','BR613','BR631','BR634','BR667','BR669','BR679','BR68','BR721','BR722','BR723','BR724','BR725','BR737','BR743','BR758','BR759','BR774','BR775','BR781','BR790','BR791','BR797','BR798','BR803','BR806','BR813','BR824','BR830','BR831','BR849','BR853','BR857','BR860','BR867','BR869','BR90','BR908','BR920','BR922','BR924','BR927','BR931','BR932','BR933','BR934','BR935','BR936','BR937','BR940','BR942','BR957','BR988','DR10','DR100','DR101','DR103','DR104','DR105','DR106','DR107','DR108','DR109','DR11','DR110','DR111','DR112','DR113','DR115','DR116','DR117','DR118','DR119','DR120','DR121','DR122','DR123','DR124','DR125','DR126','DR127','DR128','DR129','DR130','DR131','DR132','DR133','DR135','DR136','DR137','DR138','DR139','DR140','DR141','DR142','DR143','DR144','DR145','DR146','DR147','DR148','DR149','DR150','DR151','DR153','DR154','DR155','DR156','DR157','DR158','DR159','DR160','DR161','DR162','DR163','DR164','DR165','DR166','DR167','DR168','DR169','DR170','DR171','DR172','DR173','DR174','DR175','DR176','DR177','DR178','DR179','DR180','DR181','DR182','DR183','DR184','DR185','DR186','DR187','DR188','DR189','DR19','DR190','DR191','DR192','DR194','DR195','DR196','DR197','DR198','DR199','DR20','DR200','DR201','DR202','DR203','DR204','DR205','DR206','DR207','DR208','DR209','DR21','DR210','DR211','DR212','DR213','DR214','DR215','DR216','DR217','DR218','DR219','DR220','DR221','DR222','DR223','DR224','DR226','DR227','DR228','DR229','DR23','DR230','DR231','DR232','DR233','DR234','DR235','DR236','DR237','DR238','DR24','DR240','DR241','DR242','DR244','DR245','DR246','DR247','DR248','DR249','DR251','DR252','DR253','DR254','DR255','DR256','DR257','DR258','DR259','DR26','DR260','DR261','DR262','DR263','DR264','DR265','DR266','DR267','DR268','DR269','DR27','DR270','DR271','DR272','DR273','DR274','DR275','DR276','DR277','DR278','DR279','DR28','DR280','DR281','DR282','DR283','DR284','DR285','DR287','DR289','DR29','DR290','DR291','DR292','DR294','DR295','DR296','DR297','DR298','DR299','DR30','DR300','DR301','DR302','DR303','DR304','DR306','DR307','DR308','DR309','DR31','DR310','DR311','DR313','DR314','DR315','DR316','DR317','DR318','DR319','DR32','DR320','DR321','DR322','DR323','DR33','DR34','DR35','DR36','DR37','DR38','DR4','DR40','DR41','DR44','DR45','DR46','DR47','DR48','DR49','DR5','DR50','DR51','DR52','DR53','DR54','DR56','DR57','DR58','DR59','DR6','DR60','DR61','DR62','DR63','DR64','DR65','DR66','DR67','DR68','DR69','DR7','DR70','DR71','DR72','DR73','DR74','DR75','DR76','DR77','DR78','DR79','DR80','DR81','DR82','DR83','DR84','DR85','DR86','DR87','DR88','DR89','DR9','DR90','DR91','DR92','DR93','DR94','DR95','DR96','DR97','DR98','DR99','LIB10181','LIB10185','LIB10186','LIB10187','LIB10188','LIB10189','LIB10190','LIB10421','LIB10422','LIB10847','LIB10857','LIB10956','LIB10957','LIB11044','LIB9797','LIB9798','LIB9874','OVR112','RDE123','STEP1115','STEP1122','STEP1139','STEP1140','STEP1141','STEP1142','STEP1149','STEP1152','STEP1154','STEP1161','STEP1164','STEP1167','STEP1168','STEP1169','STEP1170','STEP1173','STEP1174','STEP1183','STEP1184','STEP1185','STEP1186','STEP1187','STEP1188','STEP1189','STEP1190','STEP1225','STEP1230','STEP1231','STEP1232','STEP1233','STEP1241','STEP1513','STEP1514','STEP24','STEP37','STEP39','STEP441','STEP492','STEP552','STEP577','STEP620','STEP751','STEP768','STEP770')";
//				sql = "select documentid,localeid,majorversion,minorversion,dateadded,displayenddate,displayreviewdate,publishdate,published,basedlocaleid,indexmasteridentifiers,ownername "
//						+ "from infomanager.contenttext where minorversion =0 and majorversion >= 1 and documentid like '"+channelAbbr.trim()+"%' AND "
//						+ "localeid='"+locale+"' AND displayreviewdate IS NULL AND dateadded >= to_date('"+criteriaDate+"','YYYY-MM-DD') and dateadded < to_date('"+endDate+"','YYYY-MM-DD') ";
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
					 * 	CHECK IF DOCUMENT STATUS IS EXPIRED
					 */
					boolean proceedFurther = true;
//					boolean proceedFurther = false;
					if(details.getLocale().trim().toLowerCase().equals("en_us"))
					{
//						if(details.getDocumentStatus().equals("LIVE"))
//						{
//							proceedFurther=true;
//						}
						if(details.getDocumentStatus().equals("EXPIRED"))
						{
							// set proceedFurther to FALSE
							proceedFurther = false;
							/*
							 * CHECK FOR REVIEW DATE IS NOT EQUALS TO 12-31-9999
							 * AS WELL AS REVIEW DATE IS NULL
							 */
							if(null==details.getDisplayReviewDate())
							{
								proceedFurther=true;
							}
							else if(null!=checkReviewDate && null!=details.getDisplayReviewDate())
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
				// since the data is to be extracted until 29th July 2022
				String endDate="2022-07-30";
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
						+ "localeid='"+locale+"' AND dateadded >= to_date('"+criteriaDate+"','YYYY-MM-DD') and dateadded < to_date('"+endDate+"','YYYY-MM-DD') ";
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
					 * 	CHECK IF DOCUMENT STATUS = EXPIRED
					 */
					boolean proceedFurther = false;
					if(details.getLocale().trim().toLowerCase().equals("en_us"))
					{
						if(details.getDocumentStatus().equals("EXPIRED"))
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

	public List<DocumentDetails> getLiveDocumentsMinus3And7YearsList(String channelAbbr, String locale, String criteriaDate)
	{
		List<DocumentDetails> documentsList = null;Connection conn = null;
		ResultSet rs = null;
		PreparedStatement pstmt=null;
		try
		{
			if(null!=channelAbbr && !"".equals(channelAbbr) && null!=locale && !"".equals(locale) && null!=criteriaDate && !"".equals(criteriaDate))
			{
				conn = getSourceConnection();
				String sql = "";
				sql = "select documentid,localeid,majorversion,minorversion,dateadded,displayenddate,displayreviewdate,publishdate,published,basedlocaleid,indexmasteridentifiers,ownername "
						+ "from infomanager.contenttext where minorversion =0 and majorversion >= 1 and documentid like '"+channelAbbr.trim()+"%' AND published = 'Y' AND "
						+ "localeid='"+locale+"' AND dateadded < to_date('"+criteriaDate+"','YYYY-MM-DD') ";
				if(locale.trim().toLowerCase().equals("en_ca"))
				{
					// add 7 years documentsList
					sql = sql+ " AND documentid NOT in ('BR1004','BR1006','BR1008','BR1012','BR1016','BR1021','BR1024','BR1025','BR1027','BR1028','BR1030','BR1041','BR1051','BR1065','BR1069','BR1086','BR1087','BR1090','BR1099','BR1102','BR1109','BR1112','BR1113','BR1121','BR1122','BR1125','BR1126','BR1127','BR1128','BR1129','BR1132','BR1133','BR1134','BR1138','BR1140','BR1141','BR1143','BR1144','BR1145','BR1147','BR1148','BR1149','BR1150','BR1157','BR1161','BR1166','BR1167','BR1170','BR1171','BR1179','BR1183','BR1184','BR1185','BR1186','BR1188','BR1191','BR1194','BR1195','BR1196','BR1197','BR1198','BR1199','BR120','BR1200','BR1201','BR1202','BR1203','BR1204','BR1205','BR1206','BR1207','BR1208','BR1209','BR1210','BR1211','BR1212','BR1213','BR1214','BR1215','BR1216','BR1217','BR1218','BR1219','BR1220','BR1221','BR1222','BR1223','BR1224','BR1225','BR1226','BR1227','BR1228','BR1229','BR1230','BR1231','BR1232','BR1233','BR1234','BR1235','BR1238','BR1239','BR1242','BR1243','BR1244','BR1245','BR1246','BR1247','BR1252','BR126','BR1266','BR1271','BR1273','BR1274','BR1275','BR1280','BR1289','BR1336','BR1345','BR1373','BR1399','BR1404','BR1409','BR1429','BR1448','BR1453','BR147','BR173','BR19','BR201','BR209','BR223','BR230','BR232','BR234','BR241','BR246','BR250','BR253','BR259','BR279','BR286','BR301','BR310','BR339','BR340','BR345','BR375','BR394','BR482','BR494','BR496','BR500','BR513','BR520','BR544','BR545','BR547','BR549','BR552','BR553','BR554','BR561','BR562','BR567','BR57','BR613','BR631','BR634','BR667','BR669','BR679','BR68','BR721','BR722','BR723','BR724','BR725','BR737','BR743','BR758','BR759','BR774','BR775','BR781','BR790','BR791','BR797','BR798','BR803','BR806','BR813','BR824','BR830','BR831','BR849','BR853','BR857','BR860','BR867','BR869','BR90','BR908','BR920','BR922','BR924','BR927','BR931','BR932','BR933','BR934','BR935','BR936','BR937','BR940','BR942','BR957','BR988','DR10','DR100','DR101','DR103','DR104','DR105','DR106','DR107','DR108','DR109','DR11','DR110','DR111','DR112','DR113','DR115','DR116','DR117','DR118','DR119','DR120','DR121','DR122','DR123','DR124','DR125','DR126','DR127','DR128','DR129','DR130','DR131','DR132','DR133','DR135','DR136','DR137','DR138','DR139','DR140','DR141','DR142','DR143','DR144','DR145','DR146','DR147','DR148','DR149','DR150','DR151','DR153','DR154','DR155','DR156','DR157','DR158','DR159','DR160','DR161','DR162','DR163','DR164','DR165','DR166','DR167','DR168','DR169','DR170','DR171','DR172','DR173','DR174','DR175','DR176','DR177','DR178','DR179','DR180','DR181','DR182','DR183','DR184','DR185','DR186','DR187','DR188','DR189','DR19','DR190','DR191','DR192','DR194','DR195','DR196','DR197','DR198','DR199','DR20','DR200','DR201','DR202','DR203','DR204','DR205','DR206','DR207','DR208','DR209','DR21','DR210','DR211','DR212','DR213','DR214','DR215','DR216','DR217','DR218','DR219','DR220','DR221','DR222','DR223','DR224','DR226','DR227','DR228','DR229','DR23','DR230','DR231','DR232','DR233','DR234','DR235','DR236','DR237','DR238','DR24','DR240','DR241','DR242','DR244','DR245','DR246','DR247','DR248','DR249','DR251','DR252','DR253','DR254','DR255','DR256','DR257','DR258','DR259','DR26','DR260','DR261','DR262','DR263','DR264','DR265','DR266','DR267','DR268','DR269','DR27','DR270','DR271','DR272','DR273','DR274','DR275','DR276','DR277','DR278','DR279','DR28','DR280','DR281','DR282','DR283','DR284','DR285','DR287','DR289','DR29','DR290','DR291','DR292','DR294','DR295','DR296','DR297','DR298','DR299','DR30','DR300','DR301','DR302','DR303','DR304','DR306','DR307','DR308','DR309','DR31','DR310','DR311','DR313','DR314','DR315','DR316','DR317','DR318','DR319','DR32','DR320','DR321','DR322','DR323','DR33','DR34','DR35','DR36','DR37','DR38','DR4','DR40','DR41','DR44','DR45','DR46','DR47','DR48','DR49','DR5','DR50','DR51','DR52','DR53','DR54','DR56','DR57','DR58','DR59','DR6','DR60','DR61','DR62','DR63','DR64','DR65','DR66','DR67','DR68','DR69','DR7','DR70','DR71','DR72','DR73','DR74','DR75','DR76','DR77','DR78','DR79','DR80','DR81','DR82','DR83','DR84','DR85','DR86','DR87','DR88','DR89','DR9','DR90','DR91','DR92','DR93','DR94','DR95','DR96','DR97','DR98','DR99','LIB10181','LIB10185','LIB10186','LIB10187','LIB10188','LIB10189','LIB10190','LIB10421','LIB10422','LIB10847','LIB10857','LIB10956','LIB10957','LIB11044','LIB9797','LIB9798','LIB9874','OVR112','RDE123','STEP1115','STEP1122','STEP1139','STEP1140','STEP1141','STEP1142','STEP1149','STEP1152','STEP1154','STEP1161','STEP1164','STEP1167','STEP1168','STEP1169','STEP1170','STEP1173','STEP1174','STEP1183','STEP1184','STEP1185','STEP1186','STEP1187','STEP1188','STEP1189','STEP1190','STEP1225','STEP1230','STEP1231','STEP1232','STEP1233','STEP1241','STEP1513','STEP1514','STEP24','STEP37','STEP39','STEP441','STEP492','STEP552','STEP577','STEP620','STEP751','STEP768','STEP770')";
				}		
				logger.info("getLiveDocumentsMinus3And7YearsList :: Sql :: >"+ sql);
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
					 * 	CHECK IF DOCUMENT STATUS IS LIVE
					 */
					boolean proceedFurther = false;
					if(null!=details.getDocumentStatus() && details.getDocumentStatus().equals("LIVE"))
					{
						// set proceedFurther to TRUE
						proceedFurther = true;
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
					logger.info("getLiveDocumentsMinus3And7YearsList :: Total Unique Documents Found for "+channelAbbr+" / "+ locale+" are :: >"+ documentsList.size());
				}
				else
				{
					logger.info("getLiveDocumentsMinus3And7YearsList :: No Unique Documents Found for "+channelAbbr+" / "+ locale+".");
				}
			}
			else
			{
				logger.info("getLiveDocumentsMinus3And7YearsList :: Required Paramters > Channel Abbreviation / Locale / Criteria Date are null. No Documents can be fetched.");
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(ReadTransactionDAO.class.getName(), "getLiveDocumentsMinus3And7YearsList()", e);
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
				Utilities.printStackTraceToLogs(ReadTransactionDAO.class.getName(), "getLiveDocumentsMinus3And7YearsList()", e);
			}
			
			channelAbbr = null;
			locale = null;
			criteriaDate = null;
		}
		return documentsList;
	}

}
