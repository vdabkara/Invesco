package com.invesco.datamigration.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.invesco.datamigration.vo.AttachmentDetails;
import com.invesco.datamigration.vo.CategoryDetails;
import com.invesco.datamigration.vo.DocumentDetails;
import com.invesco.datamigration.vo.InlineImageDetails;
import com.invesco.datamigration.vo.InlineInnerlinkDetails;
import com.invesco.datamigration.vo.ViewDetails;

public class CustomUtils {

	private static Logger logger = Logger.getLogger(CustomUtils.class);

	public static DocumentDetails findXMLFileInLiveFolder(DocumentDetails details)
	{
		String iqXMLsDirectory=ApplicationProperties.getProperty("okassets.iqxml.location");
		try
		{
			String xmlPath = null;
			File xmlFileDir=null;
			File xmlFile=null;
			// add live folder
			iqXMLsDirectory+="live";
			// add channel folder
			iqXMLsDirectory+="/"+details.getChannelRefKey();
			// start searching for XML File
			File channelDir = new File(iqXMLsDirectory);
			File[] listFiles = channelDir.listFiles();
			boolean fileFound = false;
			String errorMessage=null;
			if(null!=listFiles && listFiles.length>0)
			{
				File childDir =null;
				File tempFile=null;
				for(int a=0;a<listFiles.length;a++)
				{
					childDir = listFiles[a];
					if(null!=childDir && childDir.isDirectory())
					{
						// LOOK FOR THE DOCUMENT FOLDER
						xmlPath = childDir.getAbsolutePath();
						xmlPath = xmlPath.replace("\\", "/");
						if(!xmlPath.endsWith("/"))
						{
							xmlPath+="/";
						}
						// add Document Id & Locale
						xmlPath+=details.getDocumentId()+"/"+details.getLocale();
						logger.info("findXMLFileInLiveFolder :: Proceed for Looking XML File at Path :: > "+ xmlPath);
						try
						{
							xmlFileDir = new File(xmlPath);
							if(xmlFileDir.exists() && xmlFileDir.isDirectory())
							{
								if(null!=xmlFileDir.listFiles() && xmlFileDir.listFiles().length>0)
								{
									for(int c=0;c<xmlFileDir.listFiles().length;c++)
									{
										tempFile = xmlFileDir.listFiles()[c];
										if(tempFile.exists() && tempFile.isFile())
										{
											if(null!=tempFile.getName() && tempFile.getName().toLowerCase().endsWith(".xml"))
											{
												// XML FILE Found
												xmlFile=tempFile;
												// set source File Path
												details.setXmlFileSourcePath(xmlFile.getAbsolutePath());
												/*
												 * READ CONTENT NODE FROM XML FILE 
												 */
												Document doc = ParseXMLDoc.parseFile(xmlFile.getAbsolutePath());
												if(null!=doc)
												{
													/*
													 * lookUp for Content Node
													 */
													NodeList nodeList =  doc.getElementsByTagName("CONTENT");
													if(null!=nodeList && nodeList.getLength()>0)
													{
														String xmlCon = Utilities.transformString(doc);
														if(null!=xmlCon && !"".equals(xmlCon))
														{
															/*
															 *  set Error Message to NULL - so that doc status does not go Failure, 
															 *  even when XML File is found in another directory 
															 */
															errorMessage=null;
															
															
															details.setXmlContent(xmlCon);
															// perform Attachments Operation as well
															details = performAttachmentsOperation(details, doc, xmlFile.getParentFile().getAbsolutePath());
															// perform Inline Images Operation as well
															details = performInlineImagesOperation(details);
															// perform Inline Innerlinks Opreation as well
															details = performInlineInnerLinksOperation(details);
															// perform categories operation as well
															details = performCategoriesOperation(details, doc);
															// perform views operations as well
															details = performViewsOperation(details, doc);

															/*
															 * IDENTIFY ALL FLAGS
															 * 	ALL ATTACHMENTS
															 * 	ALL INLINE IMAGES
															 */
															// attachments
															if(null!=details.getAttachmentsList() && details.getAttachmentsList().size()>0)
															{
																// set default value to Y
																details.setAllAttachmentsMoved("Y");
																AttachmentDetails aDetails = null;
																for(int t=0;t<details.getAttachmentsList().size();t++)
																{
																	aDetails = (AttachmentDetails)details.getAttachmentsList().get(t);
																	if((null==aDetails.getProcessingStatus() || "".equals(aDetails.getProcessingStatus())) || 
																			(null!=aDetails.getProcessingStatus() && !aDetails.getProcessingStatus().equalsIgnoreCase("SUCCESS")))
																	{
																		// set all ATTACHEMNTS MOVED TO N
																		details.setAllAttachmentsMoved("N");
																		break;
																	}
																	aDetails = null;
																}
															}
															// inline Images
															if(null!=details.getInlineImagesList() && details.getInlineImagesList().size()>0)
															{
																// set default value to Y
																details.setAllInlineImagesMoved("Y");
																InlineImageDetails imgDetails = null;
																for(int t=0;t<details.getInlineImagesList().size();t++)
																{
																	imgDetails = (InlineImageDetails)details.getInlineImagesList().get(t);
																	if((null==imgDetails.getProcessingStatus() || "".equals(imgDetails.getProcessingStatus())) || 
																			(null!=imgDetails.getProcessingStatus() && !imgDetails.getProcessingStatus().equalsIgnoreCase("SUCCESS")))
																	{
																		// set all INLINE IMAGES MOVED TO N
																		details.setAllInlineImagesMoved("N");
																		break;
																	}
																	imgDetails = null;
																}
															}

															/*
															 * PROCEED FOR WRITING XML FILE
															 */
															details = writeXMLFile(details);
														}
														else
														{
															logger.info("findXMLFileInLiveFolder :: FAILED TO TRANSFORM XML FILE DATA TO STRING AT PATH :: > "+ xmlFile.getAbsolutePath());
															// set erroMessage
															errorMessage="FAILED TO TRANSFORM XML FILE DATA TO STRING AT PATH >"+xmlFile.getAbsolutePath();
														}
														xmlCon = null;
													}
													else
													{
														logger.info("findXMLFileInLiveFolder :: NO CONTENT NODE FOUND IN THE XML FILE AT PATH :: > "+ xmlFile.getAbsolutePath());
														// set erroMessage
														errorMessage="NO CONTENT NODE FOUND IN THE XML FILE AT PATH >"+xmlFile.getAbsolutePath();
													}
													nodeList = null;
												}
												else
												{
													logger.info("findXMLFileInLiveFolder :: Failed to Parse XML File at Path :: >"+ xmlFile.getAbsolutePath());
													// set error Message
													errorMessage="FAILED TO PARSE XML FILE AT PATH >"+ xmlFile.getAbsolutePath();
												}
												doc = null;
												fileFound = true;
												xmlFile = null;
												break;
											}
										}
										tempFile = null;
									}
								}
							}
							else
							{
								logger.info("findXMLFileInLiveFolder :: Directory does not exist at Path :: >"+ xmlPath);
								// set errorMessage
								errorMessage="FAILED TO FIND XML FILE. DIRECTORY DOES NOT EXIST AT PATH :: >"+ xmlPath;
							}
							xmlFileDir  =null;
						}
						catch(Exception e)
						{
							logger.info("findXMLFileInLiveFolder :: Exception :: >"+ e.getMessage());
							// set errorMessage
							errorMessage="FAILED TO FIND XML FILE. DIRECTORY DOES NOT EXIST AT PATH :: >"+ xmlPath+". EXCEPTION OCCURED :: >"+ e.getMessage();
						}
						xmlFileDir = null;
					}
					if(fileFound==true)
					{
						// break the ID Folders Loop
						break;
					}
					childDir = null;
				}
			}
			else
			{
				logger.info("findXMLFileInLiveFolder :: NO SubDirectories found inside Channel Folder at path :: >"+ iqXMLsDirectory);
				// set errorMessage
				errorMessage = "FAILED TO FIND XML FILE. NO SUBDIRECTORIES FOUND INSIDE CHANNEL FOLDER AT PATH ::>"+ iqXMLsDirectory;
			}
			iqXMLsDirectory = null;

			if(fileFound==false)
			{
				if(null==errorMessage || "".equals(errorMessage))
				{
					// DIDNT WENT IN ANY CONDITION
					errorMessage="FAILED TO FIND XML FILE IN LIVE FOLDER.";
				}
			}

			// IF ERROR MESSAGE IS NOT NULL - THEN SET STATUS AS FAILURE
			if(null!=errorMessage && !"".equals(errorMessage))
			{
				details.setProcessingStatus("FAILURE");
				details.setErrorMessage(errorMessage);
			}
			errorMessage = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "findXMLFileInLiveFolder()", e);
		}
		return details;
	}

	public static DocumentDetails findXMLFileInStagingFolder(DocumentDetails details)
	{
		String iqXMLsDirectory=ApplicationProperties.getProperty("okassets.iqxml.location");
		try
		{
			String xmlPath = null;
			File xmlFileDir=null;
			File xmlFile=null;
			// add staging folder
			iqXMLsDirectory+="staging";
			// add channel folder
			iqXMLsDirectory+="/"+details.getChannelRefKey();
			// start searching for XML File
			File channelDir = new File(iqXMLsDirectory);
			File[] listFiles = channelDir.listFiles();
			boolean fileFound = false;
			String errorMessage=null;
			if(null!=listFiles && listFiles.length>0)
			{
				File childDir =null;
				File tempFile=null;
				for(int a=0;a<listFiles.length;a++)
				{
					childDir = listFiles[a];
					if(null!=childDir && childDir.isDirectory())
					{
						// LOOK FOR THE DOCUMENT FOLDER
						xmlPath = childDir.getAbsolutePath();
						xmlPath = xmlPath.replace("\\", "/");
						if(!xmlPath.endsWith("/"))
						{
							xmlPath+="/";
						}
						// add Document Id & Locale & Major.Minor version
						xmlPath+=details.getDocumentId()+"/"+details.getLocale()+"/"+details.getMajorVersion()+"."+details.getMinorVersion();
						logger.info("findXMLFileInStagingFolder :: Proceed for Looking XML File at Path :: > "+ xmlPath);
						try
						{
							xmlFileDir = new File(xmlPath);
							if(xmlFileDir.exists() && xmlFileDir.isDirectory())
							{
								if(null!=xmlFileDir.listFiles() && xmlFileDir.listFiles().length>0)
								{
									for(int c=0;c<xmlFileDir.listFiles().length;c++)
									{
										tempFile = xmlFileDir.listFiles()[c];
										if(tempFile.exists() && tempFile.isFile())
										{
											if(null!=tempFile.getName() && tempFile.getName().toLowerCase().endsWith(".xml"))
											{
												// XML FILE Found
												xmlFile=tempFile;
												// set source File Path
												details.setXmlFileSourcePath(xmlFile.getAbsolutePath());
												/*
												 * READ CONTENT NODE FROM XML FILE 
												 */
												Document doc = ParseXMLDoc.parseFile(xmlFile.getAbsolutePath());
												if(null!=doc)
												{
													/*
													 * lookUp for Content Node
													 */
													NodeList nodeList =  doc.getElementsByTagName("CONTENT");
													if(null!=nodeList && nodeList.getLength()>0)
													{
														String xmlCon = Utilities.transformString(doc);
														if(null!=xmlCon && !"".equals(xmlCon))
														{
															/*
															 *  set Error Message to NULL - so that doc status does not go Failure, 
															 *  even when XML File is found in another directory 
															 */
															errorMessage=null;
															details.setXmlContent(xmlCon);
															// perform Attachments Operation as well
															details = performAttachmentsOperation(details, doc, xmlFile.getParentFile().getAbsolutePath());
															// perform Inline Images Operation as well
															details = performInlineImagesOperation(details);
															// perform Inline Innerlinks Opreation as well
															details = performInlineInnerLinksOperation(details);
															// perform categories operation as well
															details = performCategoriesOperation(details, doc);
															// perform views operations as well
															details = performViewsOperation(details, doc);

															/*
															 * IDENTIFY ALL FLAGS
															 * 	ALL ATTACHMENTS
															 * 	ALL INLINE IMAGES
															 */
															// attachments
															if(null!=details.getAttachmentsList() && details.getAttachmentsList().size()>0)
															{
																// set default value to Y
																details.setAllAttachmentsMoved("Y");
																AttachmentDetails aDetails = null;
																for(int t=0;t<details.getAttachmentsList().size();t++)
																{
																	aDetails = (AttachmentDetails)details.getAttachmentsList().get(t);
																	if((null==aDetails.getProcessingStatus() || "".equals(aDetails.getProcessingStatus())) || 
																			(null!=aDetails.getProcessingStatus() && !aDetails.getProcessingStatus().equalsIgnoreCase("SUCCESS")))
																	{
																		// set all ATTACHEMNTS MOVED TO N
																		details.setAllAttachmentsMoved("N");
																		break;
																	}
																	aDetails = null;
																}
															}
															// inline Images
															if(null!=details.getInlineImagesList() && details.getInlineImagesList().size()>0)
															{
																// set default value to Y
																details.setAllInlineImagesMoved("Y");
																InlineImageDetails imgDetails = null;
																for(int t=0;t<details.getInlineImagesList().size();t++)
																{
																	imgDetails = (InlineImageDetails)details.getInlineImagesList().get(t);
																	if((null==imgDetails.getProcessingStatus() || "".equals(imgDetails.getProcessingStatus())) || 
																			(null!=imgDetails.getProcessingStatus() && !imgDetails.getProcessingStatus().equalsIgnoreCase("SUCCESS")))
																	{
																		// set all INLINE IMAGES MOVED TO N
																		details.setAllInlineImagesMoved("N");
																		break;
																	}
																	imgDetails = null;
																}
															}

															/*
															 * PROCEED FOR WRITING XML FILE
															 */
															details = writeXMLFile(details);
														}
														else
														{
															logger.info("findXMLFileInStagingFolder :: FAILED TO TRANSFORM XML FILE DATA TO STRING AT PATH :: > "+ xmlFile.getAbsolutePath());
															// set erroMessage
															errorMessage="FAILED TO TRANSFORM XML FILE DATA TO STRING AT PATH >"+xmlFile.getAbsolutePath();
														}
														xmlCon = null;
													}
													else
													{
														logger.info("findXMLFileInStagingFolder :: NO CONTENT NODE FOUND IN THE XML FILE AT PATH :: > "+ xmlFile.getAbsolutePath());
														// set erroMessage
														errorMessage="NO CONTENT NODE FOUND IN THE XML FILE AT PATH >"+xmlFile.getAbsolutePath();
													}
													nodeList = null;
												}
												else
												{
													logger.info("findXMLFileInStagingFolder :: Failed to Parse XML File at Path :: >"+ xmlFile.getAbsolutePath());
													// set error Message
													errorMessage="FAILED TO PARSE XML FILE AT PATH >"+ xmlFile.getAbsolutePath();
												}
												doc = null;
												fileFound = true;
												xmlFile = null;
												break;
											}
										}
										tempFile = null;
									}
								}
							}
							else
							{
								logger.info("findXMLFileInStagingFolder :: Directory does not exist at Path :: >"+ xmlPath);
								// set errorMessage
								errorMessage="FAILED TO FIND XML FILE. DIRECTORY DOES NOT EXIST AT PATH :: >"+ xmlPath;
							}
							xmlFileDir  =null;
						}
						catch(Exception e)
						{
							logger.info("findXMLFileInStagingFolder :: Exception :: >"+ e.getMessage());
							// set errorMessage
							errorMessage="FAILED TO FIND XML FILE. DIRECTORY DOES NOT EXIST AT PATH :: >"+ xmlPath+". EXCEPTION OCCURED :: >"+ e.getMessage();
						}
						xmlFileDir = null;
					}
					if(fileFound==true)
					{
						// break the ID Folders Loop
						break;
					}
					childDir = null;
				}
			}
			else
			{
				logger.info("findXMLFileInStagingFolder :: NO SubDirectories found inside Channel Folder at path :: >"+ iqXMLsDirectory);
				// set errorMessage
				errorMessage = "FAILED TO FIND XML FILE. NO SUBDIRECTORIES FOUND INSIDE CHANNEL FOLDER AT PATH ::>"+ iqXMLsDirectory;
			}
			iqXMLsDirectory = null;

			if(fileFound==false)
			{
				if(null==errorMessage || "".equals(errorMessage))
				{
					// DIDNT WENT IN ANY CONDITION
					errorMessage="FAILED TO FIND XML FILE IN STAGING FOLDER.";
				}
			}

			// IF ERROR MESSAGE IS NOT NULL - THEN SET STATUS AS FAILURE
			if(null!=errorMessage && !"".equals(errorMessage))
			{
				details.setProcessingStatus("FAILURE");
				details.setErrorMessage(errorMessage);
			}
			errorMessage = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "findXMLFileInStagingFolder()", e);
		}
		return details;
	}

	
	private static DocumentDetails performCategoriesOperation(DocumentDetails details, Document doc)
	{
		try
		{
			if(null!=doc)
			{
				// CATEGORIES NODES
				NodeList categoriesNodeList = doc.getElementsByTagName("CATEGORIES");
				if(null!=categoriesNodeList && categoriesNodeList.getLength()>0)
				{
					Node catNode=null;
					NodeList childNodesList  = null;
					Node childNode=null;
					for(int a=0;a<categoriesNodeList.getLength();a++)
					{
						catNode =(Node)categoriesNodeList.item(a);
						childNodesList = catNode.getChildNodes();
						if(null!=childNodesList && childNodesList.getLength()>0)
						{
							for(int b=0;b<childNodesList.getLength();b++)
							{
								childNode = (Node)childNodesList.item(b);
								if(null!=childNode.getNodeName() && childNode.getNodeName().equalsIgnoreCase("CATEGORY"))
								{
									NodeList subList = childNode.getChildNodes();
									if(null!=subList && subList.getLength()>0)
									{
										Node subNode=null;
										CategoryDetails dt = new CategoryDetails();
										for(int c=0;c<subList.getLength();c++)
										{
											subNode = (Node)subList.item(c);
											if(subNode.getNodeName().equals("NAME"))	
											{
												dt.setName(Utilities.readNodeValue(subNode));
											}
											else if(subNode.getNodeName().equals("REFERENCE_KEY"))	
											{
												dt.setRefKey(Utilities.readNodeValue(subNode));
											}
											else if(subNode.getNodeName().equals("GUID"))	
											{
												dt.setGuid(Utilities.readNodeValue(subNode));
											}
											else if(subNode.getNodeName().equals("OBJECTID"))	
											{
												dt.setObjectId(Utilities.readNodeValue(subNode));
											}
											subNode = null;
										}
										
										if(null!=dt && null!=dt.getRefKey() && !"".equals(dt.getRefKey()))
										{
											if(null==details.getCategoryList() || details.getCategoryList().size()<=0)
											{
												details.setCategoryList(new ArrayList<CategoryDetails>());
											}
											details.getCategoryList().add(dt);
										}
										dt  = null;
									}
									subList = null;
								}
								childNode = null;
							}
						}
						childNodesList=  null;
						catNode = null;
					}
				}				
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "performCategoriesOperation()", e);
		}
		return details;
	}
	
	private static DocumentDetails performViewsOperation(DocumentDetails details, Document doc)
	{
		try
		{
			if(null!=doc)
			{
				// VIEWS NODES
				NodeList viewsNodeList = doc.getElementsByTagName("VIEWS");
				if(null!=viewsNodeList && viewsNodeList.getLength()>0)
				{
					Node viewNode=null;
					NodeList childNodesList  = null;
					Node childNode=null;
					for(int a=0;a<viewsNodeList.getLength();a++)
					{
						viewNode =(Node)viewsNodeList.item(a);
						childNodesList = viewNode.getChildNodes();
						if(null!=childNodesList && childNodesList.getLength()>0)
						{
							for(int b=0;b<childNodesList.getLength();b++)
							{
								childNode = (Node)childNodesList.item(b);
								if(null!=childNode.getNodeName() && childNode.getNodeName().equalsIgnoreCase("VIEW"))
								{
									NodeList subList = childNode.getChildNodes();
									if(null!=subList && subList.getLength()>0)
									{
										Node subNode=null;
										ViewDetails dt = new ViewDetails();
										for(int c=0;c<subList.getLength();c++)
										{
											subNode = (Node)subList.item(c);
											if(subNode.getNodeName().equals("NAME"))	
											{
												dt.setName(Utilities.readNodeValue(subNode));
											}
											else if(subNode.getNodeName().equals("REFERENCE_KEY"))	
											{
												dt.setRefKey(Utilities.readNodeValue(subNode));
											}
											else if(subNode.getNodeName().equals("GUID"))	
											{
												dt.setGuid(Utilities.readNodeValue(subNode));
											}
											else if(subNode.getNodeName().equals("OBJECTID"))	
											{
												dt.setObjectId(Utilities.readNodeValue(subNode));
											}
											subNode = null;
										}
										
										if(null!=dt && null!=dt.getRefKey() && !"".equals(dt.getRefKey()))
										{
											if(null==details.getViewList() || details.getViewList().size()<=0)
											{
												details.setViewList(new ArrayList<ViewDetails>());
											}
											details.getViewList().add(dt);
										}
										dt  = null;
									}
									subList = null;
								}
								childNode = null;
							}
						}
						childNodesList=  null;
						viewNode = null;
					}
				}				
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "performViewsOperation()", e);
		}
		return details;
	}
	
	private static DocumentDetails performAttachmentsOperation(DocumentDetails details, Document doc, String parentFolderPath)
	{
		try
		{
			if(null!=doc)
			{
				parentFolderPath = parentFolderPath.replace("\\", "/");
				if(!parentFolderPath.endsWith("/"))
				{
					parentFolderPath+="/";
				}

				/*
				 * LOOK FOR ATTACHMENT NODES
				 * H_FILE_NODE/H_FILE
				 * 
				 * FILENODESEARCH/FILEATTACHSEARCH
				 * FILENODENONSEARCH/FILEATTACHNONSEARCH
				 * FILE
				 * FILE_NO_CRAWL
				 * 
				 * H_FILE_DETAILS_PAGE
				 * X_H_BODY_IMAGE
				 */

				// HIEARCHY NODES FILES
				String[] tok="H_FILE_NODE-H_FILE,FILENODESEARCH-FILEATTACHSEARCH,FILENODENONSEARCH-FILEATTACHNONSEARCH".split(",");
				if(null!=tok && tok.length>0)
				{
					for(int a=0;a<tok.length;a++)
					{
						details = startReadingAttachmentNodes(details, doc, tok[a], parentFolderPath);
					}
				}
				tok = null;

				NodeList nodesList = null;
				// SINGLE NODE
				tok = "FILE,FILE_NO_CRAWL,H_FILE_DETAILS_PAGE,X_H_BODY_IMAGE".split(",");
				if(null!=tok && tok.length>0)
				{
					for(int f=0;f<tok.length;f++)
					{
						nodesList = doc.getElementsByTagName(tok[f]);
						if(null!=nodesList && nodesList.getLength()>0)
						{
							Node node = null;
							for(int a=0;a<nodesList.getLength();a++)
							{
								node =(Node)nodesList.item(a);

								if(null!=node && null!=node.getNodeName())
								{
									details = addAttachmentFile(details, node, parentFolderPath);
								}
								node = null;
							}
						}
						nodesList = null;
					}
				}
				tok = null;
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "performAttachmentsOperation()", e);
		}
		return details;
	}

	private static DocumentDetails startReadingAttachmentNodes(DocumentDetails details, Document doc, String nodesHierarchy, String parentFolderPath)
	{
		String parentNodeName=nodesHierarchy.substring(0, nodesHierarchy.indexOf("-"));
		String childNodeName=nodesHierarchy.substring(nodesHierarchy.indexOf("-")+1,nodesHierarchy.length());
		NodeList nodesList = doc.getElementsByTagName(parentNodeName);
		if(null!=nodesList && nodesList.getLength()>0)
		{
			NodeList childNodesList=null;
			Node node = null;
			Node childNode=null;
			for(int a=0;a<nodesList.getLength();a++)
			{
				node =(Node)nodesList.item(a);
				if(null!=node)
				{
					childNodesList = node.getChildNodes();
					if(null!=childNodesList && childNodesList.getLength()>0)
					{
						for(int b=0;b<childNodesList.getLength();b++)
						{
							childNode= (Node)childNodesList.item(b);
							if(null!=childNode && null!=childNode.getNodeName() && childNode.getNodeName().equalsIgnoreCase(childNodeName))
							{
								details = addAttachmentFile(details, childNode, parentFolderPath);
							}
							childNode = null;
						}
					}
					childNodesList = null;
				}
				node = null;
			}
		}
		nodesList = null;
		parentNodeName = null;
		childNodeName = null;
		return details;
	}

	private static DocumentDetails addAttachmentFile(DocumentDetails details, Node node, String parentFolderPath)
	{

		AttachmentDetails attachmentDetails = new AttachmentDetails();
		attachmentDetails.setAttachmentSourceName(Utilities.readNodeValue(node));
		if(null!=attachmentDetails.getAttachmentSourceName() && !"".equals(attachmentDetails.getAttachmentSourceName()))
		{
			// set source Path
			attachmentDetails.setAttachmentSourcePath(parentFolderPath+attachmentDetails.getAttachmentSourceName());
			/*
			 * CHECK IF FILE EXISTS OR NOT
			 * IF YES - MOVE TO DESTINATION PATH
			 */
			attachmentDetails.setAttachmentDestName("Article_"+details.getDocumentId()+"_"+details.getLocale()+"_"+details.getMajorVersion()+"_"+attachmentDetails.getAttachmentSourceName());
			attachmentDetails.setAttachmentDestPath(ApplicationProperties.getProperty("invesco.content.extract.location")+details.getChannelRefKey()+"/"+details.getDocumentId()+"/version"+details.getMajorVersion()+"/"+attachmentDetails.getAttachmentDestName());
			// move File
			attachmentDetails = moveAttachmentFile(details, attachmentDetails);
			if(null!=attachmentDetails.getProcessingStatus() && attachmentDetails.getProcessingStatus().equals("SUCCESS"))
			{
				// replace Attachment Source Name With Destination Name in XML Content
				if(null!=details.getXmlContent() && !"".equals(details.getXmlContent()))
				{
					details.setXmlContent(details.getXmlContent().replace(attachmentDetails.getAttachmentSourceName(), attachmentDetails.getAttachmentDestName()));
				}
			}
			// add attachmentDetails to documentDetails
			if(null==details.getAttachmentsList() || details.getAttachmentsList().size()<=0)
			{
				details.setAttachmentsList(new ArrayList<AttachmentDetails>());
			}
			details.getAttachmentsList().add(attachmentDetails);
		}
		attachmentDetails = null;

		return details;
	}

	private static AttachmentDetails moveAttachmentFile(DocumentDetails details, AttachmentDetails attachmentDetails)
	{
		try
		{
			String destPath=ApplicationProperties.getProperty("invesco.content.extract.location");
			// check for channelFolder
			destPath=destPath+details.getChannelRefKey();
			File dir = new File(destPath);
			if(!dir.exists() && !dir.isDirectory())
			{
				dir.mkdir();
			}
			dir = null;
			// check for documentIdFolder
			destPath=destPath+"/"+details.getDocumentId();
			dir = new File(destPath);
			if(!dir.exists() && !dir.isDirectory())
			{
				dir.mkdir();
			}
			dir = null;
			// check for versionFolder
			destPath=destPath+"/version"+details.getMajorVersion();
			dir = new File(destPath);
			if(!dir.exists() && !dir.isDirectory())
			{
				dir.mkdir();
			}
			dir = null;

			/*
			 * NOW CHECK IF SOURCE FILE EXISTS OR NOT
			 */
			File sourceFile = new File(attachmentDetails.getAttachmentSourcePath());
			if(sourceFile.exists() && sourceFile.isFile())
			{
				File destFile = new File(attachmentDetails.getAttachmentDestPath());
				try
				{
					FileUtils.copyFile(sourceFile, destFile);
					// set Processing Status as Success
					attachmentDetails.setProcessingStatus("SUCCESS");
				}
				catch(Exception e)
				{
					Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "moveAttachmentFile()", e);
					// set Processing Status as Failure
					attachmentDetails.setProcessingStatus("FAILURE");
					attachmentDetails.setErrorMessage("FAILED TO MOVE ATTACHMENT FROM SOURCE TO DESTINATION LOCATION. Exception :: >"+ e.getMessage());
				}
				destFile = null;
			}
			else
			{
				// set Processing Status as Failure
				attachmentDetails.setProcessingStatus("FAILURE");
				attachmentDetails.setErrorMessage("ATTACHMENT DOES NOT EXIST AT SOURCE FILE LOCATION.");
			}
			sourceFile = null;
			destPath = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "moveAttachmentFile()", e);
			// set Processing Status as Failure
			attachmentDetails.setProcessingStatus("FAILURE");
			attachmentDetails.setErrorMessage("FAILED TO CREATE DIRECTORY STRUCTURE AT DESTINATION LOCATION. Exception :: >"+ e.getMessage());
		}
		return attachmentDetails;
	}

	private static DocumentDetails performInlineImagesOperation(DocumentDetails details)
	{
		try
		{
			org.jsoup.nodes.Document doc = Jsoup.parse(details.getXmlContent());
			Elements eleList = doc.getAllElements();
			if(null!=eleList && eleList.size()>0)
			{
				Element ele = null;
				org.jsoup.nodes.Document nDoc = null;
				Elements imagesList = null;
				Element imageEle = null;
				InlineImageDetails imageDetails = null;
				String srcValue = null;
				String sourceName=null;
				String tempSrcValue=null;
				for(int a=0;a<eleList.size();a++)
				{
					ele = (Element)eleList.get(a);
					nDoc = Jsoup.parse(ele.text());
					if(null!=nDoc)
					{
						imagesList = nDoc.select("img");
						if(null!=imagesList && imagesList.size()>0)
						{
							for(int r=0;r<imagesList.size();r++)
							{
								imageEle = (Element)imagesList.get(r);
								if(null!=imageEle && null!=imageEle.attr("src") && !imageEle.attr("src").toLowerCase().startsWith("data:image"))
								{
									srcValue=  imageEle.attr("src");
									srcValue = srcValue.replace("%20", " ");
									srcValue = srcValue.replace("%5B","[");
									srcValue = srcValue.replace("%5D","]");
									srcValue = srcValue.replace("%23", "#");
									srcValue = srcValue.replace("%26", "&");
									srcValue = srcValue.replace("%5F", "_");
									srcValue = srcValue.replace("%25", "%");
									srcValue = srcValue.replace("%2B", "+");
									srcValue = srcValue.replace("%24", "$");

									// decode URL for removing ASCII Chars
									srcValue = replacer(srcValue);
									if(srcValue.trim().toLowerCase().indexOf("library/")!=-1)
									{
										tempSrcValue = srcValue.substring(srcValue.trim().toLowerCase().indexOf("library/"),srcValue.length());
										if(null!=tempSrcValue)
										{
											if(!tempSrcValue.startsWith("/"))
											{
												tempSrcValue="/"+tempSrcValue;
											}
											if(tempSrcValue.lastIndexOf("/")!=-1)
											{
												sourceName = tempSrcValue.substring(tempSrcValue.lastIndexOf("/")+1, tempSrcValue.length());
											}
											else
											{
												sourceName = tempSrcValue;
											}
										}
										// add this Image
										imageDetails = new InlineImageDetails();
										imageDetails.setImageSourceName(sourceName);
										imageDetails.setImageSourcePath(tempSrcValue);
										imageDetails.setImageSourceTag(imageEle.outerHtml());
										if(null!=imageDetails.getImageSourceTag() && imageDetails.getImageSourceTag().length()>4000)
										{
											imageDetails.setImageSourceTag(imageDetails.getImageSourceTag().substring(0, (4000-1)));
										}
										

										// set destination name & path
										imageDetails.setImageDestName("Article_"+details.getDocumentId()+"_"+details.getLocale()+"_"+details.getMajorVersion()+"_"+imageDetails.getImageSourceName());
										imageDetails.setImageDestPath(ApplicationProperties.getProperty("invesco.content.extract.location")+details.getChannelRefKey()+"/"+details.getDocumentId()+"/version"+details.getMajorVersion()+"/"+imageDetails.getImageDestName());

										/*
										 * MOVE INLINE IMAGE FILE
										 */
										imageDetails = moveInlineImageFile(details, imageDetails);
										// reset source path of image to srcValue
										imageDetails.setImageSourcePath(srcValue);
										/*
										 * proceed for adding to list
										 */
										if(null!=imageDetails.getProcessingStatus() && imageDetails.getProcessingStatus().equals("SUCCESS"))
										{
											// replace Image Source Path With Destination Name in XML Content
											if(null!=details.getXmlContent() && !"".equals(details.getXmlContent()))
											{
												details.setXmlContent(details.getXmlContent().replace(imageEle.attr("src"), imageDetails.getImageDestName()));
											}
										}
										// add imageDetails to documentInlineImagesList
										if(null==details.getInlineImagesList() || details.getInlineImagesList().size()<=0)
										{
											details.setInlineImagesList(new ArrayList<InlineImageDetails>());
										}
										details.getInlineImagesList().add(imageDetails);
									}
									else
									{
										// pass the Image as AS_IS
										// add this Image
										imageDetails = new InlineImageDetails();
										imageDetails.setImageSourcePath(srcValue);
										imageDetails.setImageSourceTag(URLDecoder.decode(imageEle.outerHtml(), (StandardCharsets.UTF_8).toString()));
										if(null!=imageDetails.getImageSourceTag() && imageDetails.getImageSourceTag().length()>4000)
										{
											imageDetails.setImageSourceTag(imageDetails.getImageSourceTag().substring(0, (4000-1)));
										}
										// set PROCESSING STATUS AS AS_IS
										imageDetails.setProcessingStatus("AS_IS");

										// add imageDetails to documentInlineImagesList
										if(null==details.getInlineImagesList() || details.getInlineImagesList().size()<=0)
										{
											details.setInlineImagesList(new ArrayList<InlineImageDetails>());
										}
										details.getInlineImagesList().add(imageDetails);
									}
									imageDetails = null;
									srcValue = null;
									sourceName = null;
									tempSrcValue = null;
								}
								imageEle = null;
							}
						}
						imagesList=  null;
					}
					nDoc = null;
					ele = null;
				}
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "performInlineImagesOperation()", e);
		}
		return details;
	}

	private static DocumentDetails performInlineInnerLinksOperation(DocumentDetails details)
	{
		try
		{
			org.jsoup.nodes.Document doc = Jsoup.parse(details.getXmlContent());
			Elements eleList = doc.getAllElements();
			if(null!=eleList && eleList.size()>0)
			{
				Element ele = null;
				org.jsoup.nodes.Document nDoc = null;
				Elements innerLinksList = null;
				Element aEle = null;
				InlineInnerlinkDetails linkDetails = null;
				String hrefValue = null;
				for(int a=0;a<eleList.size();a++)
				{
					ele = (Element)eleList.get(a);
					nDoc = Jsoup.parse(ele.text());
					if(null!=nDoc)
					{
						innerLinksList = nDoc.select("a");
						if(null!=innerLinksList && innerLinksList.size()>0)
						{
							for(int r=0;r<innerLinksList.size();r++)
							{
								aEle = (Element)innerLinksList.get(r);
								if(null!=aEle && null!=aEle.attr("href"))
								{
									hrefValue=  aEle.attr("href");
									hrefValue = hrefValue.replace("%20", " ");
									hrefValue = hrefValue.replace("%5B","[");
									hrefValue = hrefValue.replace("%5D","]");
									hrefValue = hrefValue.replace("%23", "#");
									hrefValue = hrefValue.replace("%26", "&");
									hrefValue = hrefValue.replace("%5F", "_");
									hrefValue = hrefValue.replace("%25", "%");
									hrefValue = hrefValue.replace("%2B", "+");
									hrefValue = hrefValue.replace("%24", "$");
									// DECODE HREF VALUE TO REMOVE ASCII CHARS
									hrefValue = replacer(hrefValue);
									// add this InnerLink
									linkDetails = new InlineInnerlinkDetails();
									linkDetails.setInnerLinkSourceUrl(hrefValue);
									linkDetails.setInnerLinkSourceTag(aEle.outerHtml());
									if(null!=linkDetails.getInnerLinkSourceTag())
									{
										linkDetails.setInnerLinkSourceTagLength(String.valueOf(linkDetails.getInnerLinkSourceTag().length()));
										
									}
									if(null!=linkDetails.getInnerLinkSourceTag() && linkDetails.getInnerLinkSourceTag().length()>8000)
									{
										linkDetails.setInnerLinkSourceTag(linkDetails.getInnerLinkSourceTag().substring(0, (8000-1)));
									}
									// add linkDetails to documentInline InnerLinksList
									if(null==details.getInlineInnerLinksList() || details.getInlineInnerLinksList().size()<=0)
									{
										details.setInlineInnerLinksList(new ArrayList<InlineInnerlinkDetails>());
									}
									details.getInlineInnerLinksList().add(linkDetails);
									linkDetails = null;
									hrefValue = null;
								}
								aEle = null;
							}
						}
						innerLinksList=  null;
					}
					nDoc = null;
					ele = null;
				}
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "performInlineInnerLinksOperation()", e);
		}
		return details;
	}

	private static DocumentDetails writeXMLFile(DocumentDetails details)
	{
		try
		{
			String destPath=ApplicationProperties.getProperty("invesco.content.extract.location");
			// check for channelFolder
			destPath=destPath+details.getChannelRefKey();
			File dir = new File(destPath);
			if(!dir.exists() && !dir.isDirectory())
			{
				dir.mkdir();
			}
			dir = null;
			// check for documentIdFolder
			destPath=destPath+"/"+details.getDocumentId();
			dir = new File(destPath);
			if(!dir.exists() && !dir.isDirectory())
			{
				dir.mkdir();
			}
			dir = null;
			// check for versionFolder
			destPath=destPath+"/version"+details.getMajorVersion();
			dir = new File(destPath);
			if(!dir.exists() && !dir.isDirectory())
			{
				dir.mkdir();
			}
			dir = null;

			// XML FILE NAME WILL be
			String xmlFileName="Article_"+details.getDocumentId()+"_version"+details.getMajorVersion()+".html";
			// SET XML DETINATION PATH & NAME
			details.setXmlFileDestinationName(xmlFileName);
			details.setXmlFileDestinationPath(destPath+"/"+xmlFileName);
			/*
			 * CHECK FILE IF ALREADY EXISTS OR NOT
			 */
			File xmlFile = new File(destPath+"/"+xmlFileName);
			if(xmlFile.exists() && xmlFile.isFile())
			{
				logger.info("writeXMLFile :: XML File Already Exists at Destionation Location at Path :: >"+ xmlFile.getAbsolutePath());
				logger.info("writeXMLFile :: Proceed for Appending "+details.getLocale()+" Node to the Existing HTML File.");
				String errorMessage=null;
				try
				{
					Document document = ParseXMLDoc.parseFile(xmlFile.getAbsolutePath());
					if(null!=document)
					{
						/*
						 * CHECK IF LANGUAGE NODE FOR SAME LOCALE EXISTS
						 * 	REMOVE EXISTING NODE AND APPEND NEW NODE
						 * 	IF DOES NOT EXIST THEN APPEND NEW NODE
						 */
						NodeList nodesList = document.getElementsByTagName("html");
						if(null!=nodesList && nodesList.getLength()>0)
						{
							Node htmlRootNode = (Node)nodesList.item(0);
							if(null!=htmlRootNode)
							{
								// SET default ERROR MESSAGE
								errorMessage="FAILED TO READ CHILD NODES DATA FROM EXISTING XML FILE AT DESTINATION LOCATION AT PATH >"+destPath;

								nodesList = htmlRootNode.getChildNodes();
								boolean localeNodeFound = false;
								Node langNodeToBeRemoved=null;
								if(null!=nodesList && nodesList.getLength()>0)
								{
									Node childNode=null;
									for(int r=0;r<nodesList.getLength();r++)
									{
										childNode =(Node)nodesList.item(r);
										if(null!=childNode.getNodeName() && childNode.getNodeName().toLowerCase().equals("langDataList".toLowerCase()))
										{
											NodeList langChildNodesList = childNode.getChildNodes();
											if(null!=langChildNodesList && langChildNodesList.getLength()>0)
											{
												Node langChildNode = null;
												for(int t=0;t<langChildNodesList.getLength();t++)
												{
													langChildNode = (Node)langChildNodesList.item(t);
													if(null!=langChildNode.getNodeName() && langChildNode.getNodeName().trim().toLowerCase().equals("language"))
													{
														String localeValue = Utilities.readNodeValue(langChildNode);
														if(null!=localeValue && localeValue.trim().toLowerCase().equals(details.getLocale().trim().toLowerCase()))
														{
															// locale node already exist
															localeNodeFound = true;
															// set parent node to be removed
															langNodeToBeRemoved = langChildNode.getParentNode();
															break;
														}
														localeValue = null;
													}
													langChildNode = null;
												}
											}
											langChildNodesList = null;
										}
										childNode = null;
									}
								}

								if(localeNodeFound==true)
								{
									logger.info("writeXMLFile :: "+details.getLocale()+" Node already Found in Existing XML. Proceed for deleting Node.");
									if(null!=langNodeToBeRemoved)
									{
										htmlRootNode.removeChild(langNodeToBeRemoved);
									}
									langNodeToBeRemoved = null;
								}
								// NOW CONVERT DOCUMENT TO STRING
								String xmlDoc = Utilities.transformString(document);
								if(null!=xmlDoc && !"".equals(xmlDoc))
								{
									// remove </html> tag
									xmlDoc = xmlDoc.replace("</html>", "");
									// now start creating locale node
									StringBuilder str = new StringBuilder();
									str.append("<langDataList>");
									str.append("<language>"+details.getLocale()+"</language>");
									str.append("<status>"+details.getDocumentStatus()+"</status>");
									str.append("<versionDataList>");
									if(null!=details.getXmlContent() && !"".equals(details.getXmlContent()))
									{
										str.append(details.getXmlContent().trim().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""));
									}
									str.append("</versionDataList>");
									str.append("</langDataList>");
									str.append("</html>");

									xmlDoc =xmlDoc.trim()+str.toString();

									// update existing file
									/*
									 * PROCEED FOR WRITING HTML FILE
									 */
									try
									{
										FileOutputStream fos = new FileOutputStream(xmlFile);
										fos.write(xmlDoc.toString().getBytes(Charset.forName("UTF-8")));
										fos.flush();
										fos.close();
										fos = null;
										// set PROCESSING STATUS AS SUCCESS
										details.setProcessingStatus("SUCCESS");
										// set error message to null
										errorMessage = null;
										logger.info("writeXMLFile :: XML File Written Successfully for "+details.getDocumentId()+" of "+details.getLocale()+" at path :: >"+destPath);
									}
									catch(Exception e)
									{
										Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "writeXMLFile()", e);
										// set ERROR MESSAGE
										errorMessage="FAILED TO WRITE XML FILE AT DESTINATION LOCATION AT PATH >"+destPath+". :: EXCEPTION :: >"+ e.getMessage();
									}
									str = null;
								}
								else
								{
									// set errorMessage
									errorMessage="FAILED TO CONVERT EXISTING XML DOCUMENT TO STRING FOR APPENDING LANGUAGE NODE FOR "+ details.getLocale();
								}
								xmlDoc = null;

							}
							htmlRootNode = null;
						}
						else
						{
							errorMessage="FAILED TO READ PARENT <HTML> NODE FROM EXISTING XML FILE AT DESTINATION LOCATION AT PATH >"+destPath;
							logger.info("writeXMLFile :: "+ errorMessage);
						}
						nodesList = null;
					}
					else
					{
						errorMessage="FAILED TO READ EXISTING XML FILE AT DESTINATION LOCATION AT PATH >"+destPath;
					}
					document = null;
				}
				catch(Exception e)
				{
					Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "writeXMLFile()", e);
					errorMessage="FAILED TO READ EXISTING XML FILE AT DESTINATION LOCATION AT PATH >"+destPath+". :: EXCEPTION :: >"+ e.getMessage();
					logger.info("writeXMLFile :: FAILED TO READ EXISTING XML FILE AT DESTINATION LOCATION AT PATH >"+ destPath+". :: EXCEPTION :: >"+ e.getMessage());

				}

				if(null!=errorMessage && !"".equals(errorMessage))
				{
					// set PROCESSING STATUS AS FAILURE
					details.setProcessingStatus("FAILURE");
					// set ERROR MESSAGE
					details.setErrorMessage(errorMessage);
				}
				errorMessage = null;
			}
			else
			{
				logger.info("writeXMLFile :: XML File Does not Exists at Destionation Location at Path :: >"+ destPath);
				logger.info("writeXMLFile :: Proceed for Writing "+details.getLocale()+" Node to the New HTML File.");

				StringBuilder str = new StringBuilder();
				str.append("<html>");
				str.append("<id>"+details.getDocumentId()+"</id>");
				str.append("<langDataList>");
				str.append("<language>"+details.getLocale()+"</language>");
				str.append("<status>"+details.getDocumentStatus()+"</status>");
				str.append("<versionDataList>");
				if(null!=details.getXmlContent() && !"".equals(details.getXmlContent()))
				{
					// replace <?xml version="1.0" encoding="UTF-8"?> from xmlContent
					str.append(details.getXmlContent().replace("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", ""));
				}
				str.append("</versionDataList>");
				str.append("</langDataList>");
				str.append("</html>");

				/*
				 * PROCEED FOR WRITING HTML FILE
				 */
				try
				{
					File neF = new File(destPath+"/"+xmlFileName);
					neF.createNewFile();
					FileOutputStream fos = new FileOutputStream(neF);
					fos.write(str.toString().getBytes(Charset.forName("UTF-8")));
					fos.flush();
					fos.close();
					fos = null;
					neF = null;
					// set PROCESSING STATUS AS SUCCESS
					details.setProcessingStatus("SUCCESS");
					logger.info("writeXMLFile :: XML File Written Successfully for "+details.getDocumentId()+" of "+details.getLocale()+" at path :: >"+destPath);
				}
				catch(Exception e)
				{
					Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "writeXMLFile()", e);
					// set PROCESSING STATUS AS FAILURE
					details.setProcessingStatus("FAILURE");
					// set ERROR MESSAGE
					details.setErrorMessage("FAILED TO WRITE XML FILE AT DESTINATION LOCATION AT PATH >"+destPath+". :: EXCEPTION :: >"+ e.getMessage());
				}
			}
			xmlFileName = null;
			xmlFile = null;
			destPath = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "writeXMLFile()", e);
			// set PROCESSING STATUS AS FAILURE
			details.setProcessingStatus("FAILURE");
			// set ERROR MESSAGE
			details.setErrorMessage("FAILED TO WRITE XML FILE AT DESTINATION LOCATION :: EXCEPTION :: >"+ e.getMessage());
		}
		return details;
	}

	private static InlineImageDetails moveInlineImageFile(DocumentDetails details, InlineImageDetails imgDetails)
	{
		try
		{
			String infoManagerLibraryPath=ApplicationProperties.getProperty("infomanager.library.path");
			String destPath=ApplicationProperties.getProperty("invesco.content.extract.location");
			// check for channelFolder
			destPath=destPath+details.getChannelRefKey();
			File dir = new File(destPath);
			if(!dir.exists() && !dir.isDirectory())
			{
				dir.mkdir();
			}
			dir = null;
			// check for documentIdFolder
			destPath=destPath+"/"+details.getDocumentId();
			dir = new File(destPath);
			if(!dir.exists() && !dir.isDirectory())
			{
				dir.mkdir();
			}
			dir = null;
			// check for versionFolder
			destPath=destPath+"/version"+details.getMajorVersion();
			dir = new File(destPath);
			if(!dir.exists() && !dir.isDirectory())
			{
				dir.mkdir();
			}
			dir = null;

			/*
			 * NOW CHECK IF SOURCE FILE EXISTS OR NOT
			 */
			File sourceFile = new File(infoManagerLibraryPath+imgDetails.getImageSourcePath());
			if(sourceFile.exists() && sourceFile.isFile())
			{
				File destFile = new File(imgDetails.getImageDestPath());
				try
				{
					FileUtils.copyFile(sourceFile, destFile);
					// set Processing Status as Success
					imgDetails.setProcessingStatus("SUCCESS");
				}
				catch(Exception e)
				{
					Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "moveInlineImageFile()", e);
					// set Processing Status as Failure
					imgDetails.setProcessingStatus("FAILURE");
					imgDetails.setErrorMessage("FAILED TO MOVE INLINE IMAGE FROM SOURCE TO DESTINATION LOCATION. Exception :: >"+ e.getMessage());
				}
				destFile = null;
			}
			else
			{
				// set Processing Status as Failure
				imgDetails.setProcessingStatus("FAILURE");
				imgDetails.setErrorMessage("INLINE IMAGE DOES NOT EXIST AT SOURCE FILE LOCATION.");
			}
			sourceFile = null;
			destPath = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "moveInlineImageFile()", e);
			// set Processing Status as Failure
			imgDetails.setProcessingStatus("FAILURE");
			imgDetails.setErrorMessage("FAILED TO CREATE DIRECTORY STRUCTURE AT DESTINATION LOCATION. Exception :: >"+ e.getMessage());
		}
		return imgDetails;
	}

	private static String replacer(String data) {

	    try {
	        StringBuffer tempBuffer = new StringBuffer();
	        int incrementor = 0;
	        int dataLength = data.length();
	        while (incrementor < dataLength) {
	            char charecterAt = data.charAt(incrementor);
	            if (charecterAt == '%') {
	                tempBuffer.append("<percentage>");
	            } else if (charecterAt == '+') {
	                tempBuffer.append("<plus>");
	            } else {
	                tempBuffer.append(charecterAt);
	            }
	            incrementor++;
	        }
	        data = tempBuffer.toString();
	        data = URLDecoder.decode(data, "utf-8");
	        data = data.replaceAll("<percentage>", "%");
	        data = data.replaceAll("<plus>", "+");
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	    return data;
	}

}
