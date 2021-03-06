package com.invesco.datamigration.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.invesco.datamigration.utils.DBConnectionHelper;
import com.invesco.datamigration.utils.Utilities;
import com.invesco.datamigration.vo.AttachmentDetails;
import com.invesco.datamigration.vo.CategoryDetails;
import com.invesco.datamigration.vo.DocumentDetails;
import com.invesco.datamigration.vo.InlineImageDetails;
import com.invesco.datamigration.vo.InlineInnerlinkDetails;
import com.invesco.datamigration.vo.ViewDetails;

public class WriteTransactionDAO extends DBConnectionHelper{
	
	public void saveDocumentDetails(DocumentDetails details)
	{
		PreparedStatement pstmt=null;
		ResultSet rs = null;
		Connection conn = null;
		try
		{
			conn = getDestinationConnection();
			conn.setAutoCommit(false);
			
			/*
			 * DELETE COMPLETE DOCUMENT DETAILS FROM ALL TABLES
			 */
			String sql = null;
			String[] tables="DOCUMENT_DETAILS,ATTACHMENT_DETAILS,INLINEIMAGES_DETAILS,INNERLINK_DETAILS,CATEGORY_DETAILS,VIEW_DETAILS".split(",");
			for(int a=0;a<tables.length;a++)
			{
				if(null!=details.getMajorVersion() && !"".equals(details.getMajorVersion()) && null!=details.getMinorVersion() && !"".equals(details.getMinorVersion()))
				{
					sql="DELETE FROM "+tables[a]+" WHERE DOCUMENT_ID=? AND LOCALE=? AND MAJOR_VERSION=? AND MINOR_VERSION=?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, details.getDocumentId());
					pstmt.setString(2, details.getLocale());
					pstmt.setString(3, details.getMajorVersion());
					pstmt.setString(4, details.getMinorVersion());
					pstmt.executeUpdate();
					pstmt.close();pstmt=null;
					sql=null;
				}
				else
				{
					sql="DELETE FROM "+tables[a]+" WHERE DOCUMENT_ID=? AND LOCALE=?";
					pstmt=conn.prepareStatement(sql);
					pstmt.setString(1, details.getDocumentId());
					pstmt.setString(2, details.getLocale());
					pstmt.executeUpdate();
					pstmt.close();pstmt=null;
					sql=null;
				}
			}
			
			if(null!=details.getProcessingStatus() && details.getProcessingStatus().equals("SUCCESS"))
			{
				if(null!=details.getAttachmentsList() && details.getAttachmentsList().size()>0 &&  
						(null==details.getAllAttachmentsMoved()) || (null!=details.getAllAttachmentsMoved() && "N".equals(details.getAllAttachmentsMoved())))
				{
					// set SUCCESS WITH ERROR
					details.setProcessingStatus("SUCCESS_WITH_ERRORS");
					// set ERROR MESSAGE
					details.setErrorMessage("FAILED TO MOVE SOME ATTACHMENTS. PLEASE REFER TO ATTACHMENT REPORTS.");
				}
				if( null!=details.getInlineImagesList() && details.getInlineImagesList().size()>0 && 
						(null==details.getAllInlineImagesMoved()) || (null!=details.getAllInlineImagesMoved() && "N".equals(details.getAllInlineImagesMoved())))
				{
					// set SUCCESS WITH ERROR
					details.setProcessingStatus("SUCCESS_WITH_ERRORS");
					// set ERROR MESSAGE
					if(null!=details.getErrorMessage() && !"".equals(details.getErrorMessage()))
					{
						details.setErrorMessage(details.getErrorMessage()+"\n"+"FAILED TO MOVE SOME INLINE IMAGES. PLEASE REFER TO INLINE IMAGES REPORTS.");
					}
					else
					{
						details.setErrorMessage("FAILED TO MOVE SOME INLINE IMAGES. PLEASE REFER TO INLINE IMAGES REPORTS.");
					}
				}
			}
			/*
			 * NOW PROCEED FOR SAVING DATA
			 */
			sql = "INSERT INTO DOCUMENT_DETAILS (CHANNEL_NAME,CHANNEL_REF_KEY,DOCUMENT_ID, LOCALE,MAJOR_VERSION,MINOR_VERSION,"
					+ "DATE_ADDED,DISPLAY_REVIEW_DATE,DISPLAY_END_DATE,PUBLISH_DATE,DOCUMENT_STATUS,PROCESSING_STATUS,ERROR_MESSAGE,"
					+ "XML_FILE_SOURCE_PATH,XML_FILE_DEST_NAME,XML_FILE_DEST_PATH,REC_CREATION_TMSTP,ALL_ATTACHMENTS_MOVED,"
					+ "ALL_IMAGES_MOVED,BASE_LOCALE,IS_TRANSLATION,TITLE,OWNER_NAME) "
					+ " VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			pstmt=conn.prepareStatement(sql);
			pstmt.setString(1, details.getChannel());
			pstmt.setString(2, details.getChannelRefKey());
			pstmt.setString(3, details.getDocumentId());
			pstmt.setString(4, details.getLocale());
			pstmt.setString(5, details.getMajorVersion());
			pstmt.setString(6, details.getMinorVersion());
			pstmt.setTimestamp(7, details.getDateAdded());
			pstmt.setTimestamp(8, details.getDisplayReviewDate());
			pstmt.setTimestamp(9, details.getDisplayEndDate());
			pstmt.setTimestamp(10, details.getPublishDate());
			pstmt.setString(11, details.getDocumentStatus());
			pstmt.setString(12, details.getProcessingStatus());
			pstmt.setString(13, details.getErrorMessage());
			pstmt.setString(14, details.getXmlFileSourcePath());
			pstmt.setString(15, details.getXmlFileDestinationName());
			pstmt.setString(16, details.getXmlFileDestinationPath());
			pstmt.setTimestamp(17, new Timestamp(new Date().getTime()));
			pstmt.setString(18, details.getAllAttachmentsMoved());
			pstmt.setString(19, details.getAllInlineImagesMoved());
			pstmt.setString(20, details.getBaseLocale());
			pstmt.setString(21, details.getIsTranslation());
			pstmt.setString(22, details.getTitle());
			pstmt.setString(23, details.getOwnerName());
			pstmt.executeUpdate();
			pstmt.close();pstmt=null;
			sql = null;
			
			// ATTACHMENTS 
			if(null!=details.getAttachmentsList() && details.getAttachmentsList().size()>0)
			{
				AttachmentDetails aDetails = null;
				sql = "INSERT INTO ATTACHMENT_DETAILS (CHANNEL_NAME,CHANNEL_REF_KEY,DOCUMENT_ID,LOCALE,MAJOR_VERSION,MINOR_VERSION,SOURCE_FILE_NAME,SOURCE_FILE_PATH,"
						+ "DEST_FILE_NAME,DEST_FILE_PATH,"
						+ "PROCESSING_STATUS,ERROR_MESSAGE,REC_CREATION_TMSTP,BASE_LOCALE,IS_TRANSLATION) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				pstmt = conn.prepareStatement(sql);
				for(int a=0;a<details.getAttachmentsList().size();a++)
				{
					aDetails = (AttachmentDetails)details.getAttachmentsList().get(a);
					
					pstmt.setString(1, details.getChannel());
					pstmt.setString(2, details.getChannelRefKey());
					pstmt.setString(3, details.getDocumentId());
					pstmt.setString(4, details.getLocale());
					pstmt.setString(5, details.getMajorVersion());
					pstmt.setString(6, details.getMinorVersion());
					pstmt.setString(7, aDetails.getAttachmentSourceName());
					pstmt.setString(8, aDetails.getAttachmentSourcePath());
					pstmt.setString(9, aDetails.getAttachmentDestName());
					pstmt.setString(10, aDetails.getAttachmentDestPath());
					pstmt.setString(11, aDetails.getProcessingStatus());
					pstmt.setString(12, aDetails.getErrorMessage());
					pstmt.setTimestamp(13, new Timestamp(new Date().getTime()));
					pstmt.setString(14, details.getBaseLocale());
					pstmt.setString(15, details.getIsTranslation());
					
					pstmt.addBatch();
					aDetails = null;
				}
				pstmt.executeBatch();
				pstmt.close();pstmt=null;
				sql = null;
			}
			
			// CATEGORY
			if(null!=details.getCategoryList() && details.getCategoryList().size()>0)
			{
				CategoryDetails catDetails = null;
				sql = "INSERT INTO CATEGORY_DETAILS (CHANNEL_NAME,CHANNEL_REF_KEY,DOCUMENT_ID,LOCALE,MAJOR_VERSION,MINOR_VERSION,NAME,REFKEY,"
						+ "GUID,OBJECTID,REC_CREATION_TMSTP,BASE_LOCALE,IS_TRANSLATION) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
				pstmt = conn.prepareStatement(sql);
				for(int a=0;a<details.getCategoryList().size();a++)
				{
					catDetails = (CategoryDetails)details.getCategoryList().get(a);
					
					pstmt.setString(1, details.getChannel());
					pstmt.setString(2, details.getChannelRefKey());
					pstmt.setString(3, details.getDocumentId());
					pstmt.setString(4, details.getLocale());
					pstmt.setString(5, details.getMajorVersion());
					pstmt.setString(6, details.getMinorVersion());
					pstmt.setString(7, catDetails.getName());
					pstmt.setString(8, catDetails.getRefKey());
					pstmt.setString(9, catDetails.getGuid());
					pstmt.setString(10, catDetails.getObjectId());
					pstmt.setTimestamp(11, new Timestamp(new Date().getTime()));
					pstmt.setString(12, details.getBaseLocale());
					pstmt.setString(13, details.getIsTranslation());
					
					pstmt.addBatch();
					catDetails = null;
				}
				pstmt.executeBatch();
				pstmt.close();pstmt=null;
				sql = null;
			}
			
			// VIEWS
			if(null!=details.getViewList() && details.getViewList().size()>0)
			{
				ViewDetails vDetails = null;
				sql = "INSERT INTO VIEW_DETAILS (CHANNEL_NAME,CHANNEL_REF_KEY,DOCUMENT_ID,LOCALE,MAJOR_VERSION,MINOR_VERSION,NAME,REFKEY,"
						+ "GUID,OBJECTID,REC_CREATION_TMSTP,BASE_LOCALE,IS_TRANSLATION) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
				pstmt = conn.prepareStatement(sql);
				for(int a=0;a<details.getViewList().size();a++)
				{
					vDetails = (ViewDetails)details.getViewList().get(a);

					pstmt.setString(1, details.getChannel());
					pstmt.setString(2, details.getChannelRefKey());
					pstmt.setString(3, details.getDocumentId());
					pstmt.setString(4, details.getLocale());
					pstmt.setString(5, details.getMajorVersion());
					pstmt.setString(6, details.getMinorVersion());
					pstmt.setString(7, vDetails.getName());
					pstmt.setString(8, vDetails.getRefKey());
					pstmt.setString(9, vDetails.getGuid());
					pstmt.setString(10, vDetails.getObjectId());
					pstmt.setTimestamp(11, new Timestamp(new Date().getTime()));
					pstmt.setString(12, details.getBaseLocale());
					pstmt.setString(13, details.getIsTranslation());

					pstmt.addBatch();
					vDetails = null;
				}
				pstmt.executeBatch();
				pstmt.close();pstmt=null;
				sql = null;
			}
			
			// INLINE IMAGES
			if(null!=details.getInlineImagesList() && details.getInlineImagesList().size()>0)
			{
				InlineImageDetails imgDetails = null;
				sql = "INSERT INTO INLINEIMAGES_DETAILS (CHANNEL_NAME,CHANNEL_REF_KEY,DOCUMENT_ID,LOCALE,MAJOR_VERSION,MINOR_VERSION,SOURCE_FILE_NAME,SOURCE_FILE_PATH,"
						+ "DEST_FILE_NAME,DEST_FILE_PATH,PROCESSING_STATUS,ERROR_MESSAGE,REC_CREATION_TMSTP,SOURCE_TAG,BASE_LOCALE,IS_TRANSLATION) "
						+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				pstmt = conn.prepareStatement(sql);
				for(int a=0;a<details.getInlineImagesList().size();a++)
				{
					imgDetails = (InlineImageDetails)details.getInlineImagesList().get(a);
					
					pstmt.setString(1, details.getChannel());
					pstmt.setString(2, details.getChannelRefKey());
					pstmt.setString(3, details.getDocumentId());
					pstmt.setString(4, details.getLocale());
					pstmt.setString(5, details.getMajorVersion());
					pstmt.setString(6, details.getMinorVersion());
					pstmt.setString(7, imgDetails.getImageSourceName());
					pstmt.setString(8, imgDetails.getImageSourcePath());
					pstmt.setString(9, imgDetails.getImageDestName());
					pstmt.setString(10, imgDetails.getImageDestPath());
					pstmt.setString(11, imgDetails.getProcessingStatus());
					pstmt.setString(12, imgDetails.getErrorMessage());
					pstmt.setTimestamp(13, new Timestamp(new Date().getTime()));
					if(null!=imgDetails.getImageSourceTag() && imgDetails.getImageSourceTag().length()>4000)
					{
						pstmt.setString(14, imgDetails.getImageSourceTag().substring(0, (4000-1)));
					}
					else
					{
						pstmt.setString(14, imgDetails.getImageSourceTag());
					}
					pstmt.setString(15, details.getBaseLocale());
					pstmt.setString(16, details.getIsTranslation());
					
					pstmt.addBatch();
					imgDetails = null;
				}
				pstmt.executeBatch();
				pstmt.close();pstmt=null;
				sql = null;
			}
			
			// INLINE INNER LINKS
			if(null!=details.getInlineInnerLinksList() && details.getInlineInnerLinksList().size()>0)
			{
				InlineInnerlinkDetails linkDetails = null;
				sql = "INSERT INTO INNERLINK_DETAILS (CHANNEL_NAME,CHANNEL_REF_KEY,DOCUMENT_ID,LOCALE,MAJOR_VERSION,"
						+ "MINOR_VERSION,INNERLINK_PATH,SOURCE_TAG,"
						+ "PROCESSING_STATUS,ERROR_MESSAGE,REC_CREATION_TMSTP,SOURCE_TAG_LENGTH,BASE_LOCALE,IS_TRANSLATION) "
						+ "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
				pstmt = conn.prepareStatement(sql);
				String sourceTag=null;
				for(int a=0;a<details.getInlineInnerLinksList().size();a++)
				{
					linkDetails = (InlineInnerlinkDetails)details.getInlineInnerLinksList().get(a);
					sourceTag = linkDetails.getInnerLinkSourceTag();
					if(null!=sourceTag)
					{
						if(sourceTag.length()>8000)
						{
							sourceTag = sourceTag.substring(0,(8000-1));
						}
					}
					
					pstmt.setString(1, details.getChannel());
					pstmt.setString(2, details.getChannelRefKey());
					pstmt.setString(3, details.getDocumentId());
					pstmt.setString(4, details.getLocale());
					pstmt.setString(5, details.getMajorVersion());
					pstmt.setString(6, details.getMinorVersion());
					if(null!=linkDetails.getInnerLinkSourceUrl() && linkDetails.getInnerLinkSourceUrl().length()>2000)
					{
						pstmt.setString(7, linkDetails.getInnerLinkSourceUrl().substring(0, (2000-1)));
					}
					else
					{
						pstmt.setString(7, linkDetails.getInnerLinkSourceUrl());
					}
					if(null!=linkDetails.getInnerLinkSourceTag() && linkDetails.getInnerLinkSourceTag().length()>8000)
					{
						pstmt.setString(8, linkDetails.getInnerLinkSourceTag().substring(0,(8000-1)));
					}
					else
					{
						pstmt.setString(8, linkDetails.getInnerLinkSourceTag());
					}
					pstmt.setString(9, linkDetails.getProcessingStatus());
					pstmt.setString(10, linkDetails.getErrorMessage());
					pstmt.setTimestamp(11, new Timestamp(new Date().getTime()));
					pstmt.setString(12, linkDetails.getInnerLinkSourceTagLength());
					pstmt.setString(13, details.getBaseLocale());
					pstmt.setString(14, details.getIsTranslation());
					
					pstmt.addBatch();
					linkDetails = null;
				}
				pstmt.executeBatch();
				pstmt.close();pstmt=null;
				sql = null;
			}
			
			// commit transaction
			conn.commit();
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(WriteTransactionDAO.class.getName(), "saveDocumentDetails()", e);
			if(null!=conn)
			{
				try
				{
					conn.rollback();
				}
				catch(SQLException e1)
				{
					Utilities.printStackTraceToLogs(WriteTransactionDAO.class.getName(), "saveDocumentDetails()", e1);
				}
			}
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
				Utilities.printStackTraceToLogs(WriteTransactionDAO.class.getName(), "saveDocumentDetails()", e);
			}
		}
	}
	
	
	public List<InlineInnerlinkDetails> getInnerlinkDetails(int offset, int limit)
	{
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement pstmt=null;
		List<InlineInnerlinkDetails> list = null;
		try
		{
			conn = getDestinationConnection();
			String sql = "select A.*,B.DOCUMENT_STATUS from INNERLINK_DETAILS A,DOCUMENT_DETAILS B WHERE "
					+ " A.DOCUMENT_ID=B.DOCUMENT_ID AND A.LOCALE = B.LOCALE AND "
					+ " A.MAJOR_VERSION=B.MAJOR_VERSION AND A.MINOR_VERSION=B.MINOR_VERSION"
					+ " order by A.ID ASC OFFSET "+offset+" ROWS FETCH NEXT "+limit+" ROWS ONLY";
			System.out.println(sql);
			pstmt = conn.prepareStatement(sql);
			rs=pstmt.executeQuery();
			InlineInnerlinkDetails details = null;
			while(rs.next())
			{
				details = new InlineInnerlinkDetails();
				details.setDocumentDetails(new DocumentDetails());
				details.getDocumentDetails().setChannel(rs.getString("CHANNEL_NAME"));
				details.getDocumentDetails().setChannelRefKey(rs.getString("CHANNEL_REF_KEY"));
				details.getDocumentDetails().setDocumentId(rs.getString("DOCUMENT_ID"));
				details.getDocumentDetails().setLocale(rs.getString("LOCALE"));
				details.getDocumentDetails().setBaseLocale(rs.getString("BASE_LOCALE"));
				details.getDocumentDetails().setMajorVersion(rs.getString("MAJOR_VERSION"));
				details.getDocumentDetails().setMinorVersion(rs.getString("MINOR_VERSION"));
				details.getDocumentDetails().setDocumentStatus(rs.getString("DOCUMENT_STATUS"));
				details.getDocumentDetails().setIsTranslation(rs.getString("IS_TRANSLATION"));
				
				details.setInnerLinkSourceUrl(rs.getString("INNERLINK_PATH"));
				details.setInnerLinkSourceTagLength(rs.getString("SOURCE_TAG_LENGTH"));
				details.setInnerLinkSourceTag(rs.getString("SOURCE_TAG"));
				if(null==list || list.size()<=0)
				{
					list = new ArrayList<InlineInnerlinkDetails>();
				}
				list.add(details);
				details = null;
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(WriteTransactionDAO.class.getName(), "getInnerlinkDetails()", e);
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
				Utilities.printStackTraceToLogs(WriteTransactionDAO.class.getName(), "getInnerlinkDetails()", e);
			}
		}
		return list;
	}

	public List<InlineImageDetails> getInlineImagesForReport(int offset, int limit)
	{
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement pstmt=null;
		List<InlineImageDetails> list = null;
		try
		{
			conn = getDestinationConnection();
			String sql = "select A.CHANNEL_REF_KEY,A.DOCUMENT_ID,A.LOCALE,A.BASE_LOCALE,A.IS_TRANSLATION,B.DOCUMENT_STATUS,A.MAJOR_VERSION,A.MINOR_VERSION,"
					+ "A.SOURCE_FILE_NAME,A.SOURCE_FILE_PATH,A.DEST_FILE_NAME,A.DEST_FILE_PATH,A.PROCESSING_STATUS,A.ERROR_MESSAGE,A.SOURCE_TAG "
					+ "from INLINEIMAGES_DETAILS A, DOCUMENT_DETAILS B WHERE A.DOCUMENT_ID=B.DOCUMENT_ID AND A.LOCALE = B.LOCALE "
					+ "AND A.MAJOR_VERSION=B.MAJOR_VERSION AND A.MINOR_VERSION=B.MINOR_VERSION "
					+ "order by A.ID ASC OFFSET "+offset+" ROWS FETCH NEXT "+limit+" ROWS ONLY";
			System.out.println(sql);
			pstmt = conn.prepareStatement(sql);
			rs=pstmt.executeQuery();
			InlineImageDetails details = null;
			while(rs.next())
			{
				details = new InlineImageDetails();
				details.setDocumentDetails(new DocumentDetails());
				details.getDocumentDetails().setChannelRefKey(rs.getString("CHANNEL_REF_KEY"));
				details.getDocumentDetails().setDocumentId(rs.getString("DOCUMENT_ID"));
				details.getDocumentDetails().setLocale(rs.getString("LOCALE"));
				details.getDocumentDetails().setBaseLocale(rs.getString("BASE_LOCALE"));
				details.getDocumentDetails().setMajorVersion(rs.getString("MAJOR_VERSION"));
				details.getDocumentDetails().setMinorVersion(rs.getString("MINOR_VERSION"));
				details.getDocumentDetails().setDocumentStatus(rs.getString("DOCUMENT_STATUS"));
				details.getDocumentDetails().setIsTranslation(rs.getString("IS_TRANSLATION"));
				
				details.setImageSourceName(rs.getString("SOURCE_FILE_NAME"));
				details.setImageSourcePath(rs.getString("SOURCE_FILE_PATH"));
				details.setImageDestName(rs.getString("DEST_FILE_NAME"));
				details.setImageDestPath(rs.getString("DEST_FILE_PATH"));
				details.setProcessingStatus(rs.getString("PROCESSING_STATUS"));
				details.setErrorMessage(rs.getString("ERROR_MESSAGE"));
				details.setImageSourceTag(rs.getString("SOURCE_TAG"));
				if(null==list || list.size()<=0)
				{
					list = new ArrayList<InlineImageDetails>();
				}
				list.add(details);
				details = null;
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(WriteTransactionDAO.class.getName(), "getInlineImagesForReport()", e);
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
				Utilities.printStackTraceToLogs(WriteTransactionDAO.class.getName(), "getInlineImagesForReport()", e);
			}
		}
		return list;
	}

	
	public List<InlineImageDetails> getInlineImagesDetails()
	{
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement pstmt=null;
		List<InlineImageDetails> list = null;
		try
		{
			conn = getDestinationConnection();
			String sql = "select * from INLINEIMAGES_DETAILS";
			pstmt = conn.prepareStatement(sql);
			rs=pstmt.executeQuery();
			InlineImageDetails details = null;
			while(rs.next())
			{
				details = new InlineImageDetails();
				details.setDocumentDetails(new DocumentDetails());
				details.getDocumentDetails().setChannel(rs.getString("CHANNEL_NAME"));
				details.getDocumentDetails().setChannelRefKey(rs.getString("CHANNEL_REF_KEY"));
				details.getDocumentDetails().setDocumentId(rs.getString("DOCUMENT_ID"));
				details.getDocumentDetails().setLocale(rs.getString("LOCALE"));
				details.getDocumentDetails().setMajorVersion(rs.getString("MAJOR_VERSION"));
				details.getDocumentDetails().setMinorVersion(rs.getString("MINOR_VERSION"));
				
				details.setImageSourceName(rs.getString("SOURCE_FILE_NAME"));
				details.setImageSourcePath(rs.getString("SOURCE_FILE_PATH"));
				details.setImageDestName(rs.getString("DEST_FILE_NAME"));
				details.setImageDestPath(rs.getString("DEST_FILE_PATH"));
				details.setProcessingStatus(rs.getString("PROCESSING_STATUS"));
				details.setErrorMessage(rs.getString("ERROR_MESSAGE"));
				
				if(null==list || list.size()<=0)
				{
					list = new ArrayList<InlineImageDetails>();
				}
				list.add(details);
				details = null;
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(WriteTransactionDAO.class.getName(), "getInlineImagesDetails()", e);
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
				Utilities.printStackTraceToLogs(WriteTransactionDAO.class.getName(), "getInlineImagesDetails()", e);
			}
		}
		return list;
	}

	public List<AttachmentDetails> getAttachmentDetails()
	{
		Connection conn = null;
		ResultSet rs = null;
		PreparedStatement pstmt=null;
		List<AttachmentDetails> list = null;
		try
		{
			conn = getDestinationConnection();
			String sql = "select * from ATTACHMENT_DETAILS";
			pstmt = conn.prepareStatement(sql);
			rs=pstmt.executeQuery();
			AttachmentDetails details = null;
			while(rs.next())
			{
				details = new AttachmentDetails();
				details.setDocumentDetails(new DocumentDetails());
				details.getDocumentDetails().setChannel(rs.getString("CHANNEL_NAME"));
				details.getDocumentDetails().setChannelRefKey(rs.getString("CHANNEL_REF_KEY"));
				details.getDocumentDetails().setDocumentId(rs.getString("DOCUMENT_ID"));
				details.getDocumentDetails().setLocale(rs.getString("LOCALE"));
				details.getDocumentDetails().setMajorVersion(rs.getString("MAJOR_VERSION"));
				details.getDocumentDetails().setMinorVersion(rs.getString("MINOR_VERSION"));
				
				details.setAttachmentSourceName(rs.getString("SOURCE_FILE_NAME"));
				details.setAttachmentSourcePath(rs.getString("SOURCE_FILE_PATH"));
				details.setAttachmentDestName(rs.getString("DEST_FILE_NAME"));
				details.setAttachmentDestPath(rs.getString("DEST_FILE_PATH"));
				details.setProcessingStatus(rs.getString("PROCESSING_STATUS"));
				details.setErrorMessage(rs.getString("ERROR_MESSAGE"));
				
				if(null==list || list.size()<=0)
				{
					list = new ArrayList<AttachmentDetails>();
				}
				list.add(details);
				details = null;
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(WriteTransactionDAO.class.getName(), "getAttachmentDetails()", e);
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
				Utilities.printStackTraceToLogs(WriteTransactionDAO.class.getName(), "getAttachmentDetails()", e);
			}
		}
		return list;
	}

	
}
