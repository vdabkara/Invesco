package com.invesco.datamigration.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.invesco.datamigration.utils.ParseXMLDoc;
import com.invesco.datamigration.utils.Utilities;
import com.invesco.datamigration.vo.DocumentDetails;

public class GenerateVersionImpactedDocsImpl {

	private static Logger logger = Logger.getLogger(GenerateVersionImpactedDocsImpl.class);

	public static void main(String[] args) {
		// initialie Loggers
		File jarPath = new File(GenerateVersionImpactedDocsImpl.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String propertiesPath = jarPath.getParentFile().getAbsolutePath();
		PropertyConfigurator.configure(propertiesPath+"/"+"log.properties");
		try
		{
			List<DocumentDetails> list = new ArrayList<DocumentDetails>();
			DocumentDetails details = null;
			String localeValue=null;
			String statusValue=null;
			String versionInHTMLFile=null;
			File sourceDir = new File("Z:\\egain_working\\INVESCO\\07-30-2022\\Content");
			if(null!=sourceDir && sourceDir.isDirectory() && null!=sourceDir.listFiles())
			{
				// get all channelDirs
				File[] channelDirs = sourceDir.listFiles();
				File channelDir=null;
				for(int a=0;a<channelDirs.length;a++)
				{
					channelDir = (File)channelDirs[a];
					if(channelDir.isDirectory() && null!=channelDir.listFiles() && channelDir.listFiles().length>0 && channelDir.getName().equals("STEP_BY_STEPS"))
					{
						// get all DocumentDirs
						File documentDir = null;
						for(int b=0;b<channelDir.listFiles().length;b++)
						{
							documentDir = (File)channelDir.listFiles()[b];
							if(documentDir.isDirectory() && null!=documentDir.listFiles() && documentDir.listFiles().length>0)
							{
								// get all VersionDirs
								File versionDir= null;
								for(int c=0;c<documentDir.listFiles().length;c++)
								{
									versionDir = (File)documentDir.listFiles()[c];
									if(versionDir.isDirectory() && null!=versionDir.listFiles() && versionDir.listFiles().length>0)
									{
										// get all html & other attachment files
										File htmlFile = null;
										for(int d=0;d<versionDir.listFiles().length;d++)
										{
											htmlFile = (File)versionDir.listFiles()[d];
											if(null!=htmlFile && htmlFile.exists() && htmlFile.isFile() && htmlFile.getName().equals("Article_"+documentDir.getName()+"_"+versionDir.getName()+".html"))
											{
												// HTML FILE FOUND - READ THE HTML FILE AND FINE THE VERSION FOR EACH LOCALE NODE
												try
												{
													Document document = ParseXMLDoc.parseFile(htmlFile.getAbsolutePath());
//													String xmlContent = Utilities.getStringFromXML(htmlFile);
//													xmlContent = xmlContent.replace("<META>", "<META></META>");
//													DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//													InputSource is = new InputSource();
//													is.setCharacterStream(new StringReader(xmlContent));
//
//													Document document = db.parse(is);
//													
//													is = null;
//													db=  null;
//													xmlContent = null;
													if(null!=document)
													{
														NodeList nodesList = document.getElementsByTagName("html");
														if(null!=nodesList && nodesList.getLength()>0)
														{
															Node htmlRootNode = (Node)nodesList.item(0);
															if(null!=htmlRootNode)
															{
																nodesList = htmlRootNode.getChildNodes();
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
																				/*
																				 * NOW GET VERSION VALUE
																				 */
																				for(int t=0;t<langChildNodesList.getLength();t++)
																				{
																					langChildNode = (Node)langChildNodesList.item(t);
																					// PROCEED FOR IDENTIFYING LOCALE AND ITS VERSION IN HTML FILE
																					if(null!=langChildNode.getNodeName() && langChildNode.getNodeName().trim().toLowerCase().equals("language".toLowerCase()))
																					{
																						localeValue=Utilities.readNodeValue(langChildNode);
																					}
																					else if(null!=langChildNode.getNodeName() && langChildNode.getNodeName().trim().toLowerCase().equals("status".toLowerCase()))
																					{
																						statusValue = Utilities.readNodeValue(langChildNode);
																					}
																					else if(null!=langChildNode.getNodeName() && langChildNode.getNodeName().trim().toLowerCase().equals("versionDataList".toLowerCase()))
																					{
																						NodeList versionChildNodesList = langChildNode.getChildNodes();
																						if(null!=versionChildNodesList && versionChildNodesList.getLength()>0)
																						{
																							Node versionChildNode = null;
																							for(int f=0;f<versionChildNodesList.getLength();f++)
																							{
																								versionChildNode = (Node)versionChildNodesList.item(f);
																								if(null!=versionChildNode.getNodeName() && versionChildNode.getNodeName().trim().toLowerCase().equals("content".toLowerCase()))
																								{
																									NodeList contentNodesList = versionChildNode.getChildNodes();
																									if(null!=contentNodesList && contentNodesList.getLength()>0)
																									{
																										Node contentNode = null;
																										for(int u=0;u<contentNodesList.getLength();u++)
																										{
																											contentNode = (Node)contentNodesList.item(u);
																											if(null!=contentNode.getNodeName() && contentNode.getNodeName().trim().toLowerCase().equals("VERSION".toLowerCase()))
																											{
																												// get version from Content Node
																												versionInHTMLFile = Utilities.readNodeValue(contentNode);
																											}
																											contentNode = null;
																										}
																									}
																									contentNodesList = null;
																								}
																								versionChildNode = null;
																							}
																						}
																						versionChildNodesList = null;
																					}
																					langChildNode = null;
																				}
																			}
																			langChildNodesList = null;
																			
																			/*
																			 * START ADDING INFO TO DOCUMENTS LIST
																			 */
																			details = new DocumentDetails();
																			details.setChannelRefKey(channelDir.getName());
																			details.setDocumentId(documentDir.getName());
																			details.setMajorVersion(versionDir.getName().replace("version", "")+".0");
																			details.setLocale(localeValue);
																			details.setDocumentStatus(statusValue);
																			details.setVersionInXMLContent(versionInHTMLFile);
																			/*
																			 * ADD TO LIST
																			 */
																			list.add(details);
																			details = null;
																			localeValue = null;
																			statusValue = null;
																		}
																		childNode = null;
																	}
																}
																nodesList = null;
															}
															htmlRootNode = null;
														}
														nodesList = null;
													}
													document = null;
												}
												catch(Exception e)
												{
													Utilities.printStackTraceToLogs(GenerateVersionImpactedDocsImpl.class.getName(), "main()", e);
												}
											}
											htmlFile = null;
										}
									}
									versionDir = null;
								}
							}
							documentDir = null;
						}
					}
					channelDir= null;
				}
			}
			sourceDir = null;
			
			/*
			 * IF LIST IS NOT NULL PROCEED FOR GENERATING REPORT
			 */
			if(null!=list && list.size()>0)
			{
				String path="C:/Users/dabkav/Documents/WD/Reports";
				String fName = "/VERSION_IMPACTED_DOCS_REPORT_STEP.xlsx";

				File myFile = new File(path + fName);     
				// Create the workbook instance for XLSX file, KEEP 100 ROWS IN MEMMORY AND RET ON DISK
				SXSSFWorkbook myWorkBook = new SXSSFWorkbook(100);

				// Create a new sheet
				Sheet mySheet = myWorkBook.createSheet("Details");
				
				Font font = myWorkBook.createFont();
				font.setFontHeightInPoints((short)10);
				font.setBoldweight(Font.BOLDWEIGHT_BOLD);
				CellStyle headerStyle = myWorkBook.createCellStyle();
				headerStyle.setFont(font);
				headerStyle.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
				headerStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
				headerStyle.setFillPattern((short)FillPatternType.SOLID_FOREGROUND.ordinal());
				 // and solid fill pattern produces solid grey cell fill
				headerStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
				headerStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
				headerStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
				headerStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
				/*
				 * Add Header Row
				 */
				Row headerRow = mySheet.createRow(0);
				Cell headerCell = null;

				String headers="CHANNEL_REFKEY,DOCUMENT_ID,LOCALE,DOCUMENT_STATUS,VERSION_IN_FOLDER,VERSION_IN_HTML,MATCHES"; 
				String[] tokens=headers.split(",");
				if(null!=tokens && tokens.length>0)
				{
					for(int c=0;c<tokens.length;c++)
					{
						headerCell = headerRow.createCell(c);
						headerCell.setCellStyle(headerStyle);
						headerCell.setCellValue(tokens[c]);
						headerCell  = null;
					}
				}
				tokens = null;
				headerCell=null;
				headerRow=null;
				headers=null;
				headerStyle = null;
				font = null;

				int rowCount = 0;

				/*
				 * GENERATE MULTIPLE ROWS SUCH THAT
				 */
				 String dataRow="";
				Row row=null;
				Cell dataCell=null;
				details = null;
				Font dFont = myWorkBook.createFont();
				dFont.setFontHeightInPoints((short)10);
				
				CellStyle dataStyle = myWorkBook.createCellStyle();
				dataStyle.setFont(dFont);
				dataStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
				dataStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
				dataStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
				dataStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
				
				for(int b=0;b<list.size();b++)
				{
					details = (DocumentDetails)list.get(b);
					dataRow=details.getChannelRefKey()+"<TOK_SEPARATOR>"+details.getDocumentId()+"<TOK_SEPARATOR>"+details.getLocale()+"<TOK_SEPARATOR>";
					dataRow+=details.getDocumentStatus()+"<TOK_SEPARATOR>"+details.getMajorVersion()+"<TOK_SEPARATOR>"+details.getVersionInXMLContent()+"<TOK_SEPARATOR>";
					if(null!=details.getMajorVersion() && null!=details.getVersionInXMLContent() && 
						!"".equals(details.getMajorVersion()) && !"".equals(details.getVersionInXMLContent()) && 
						details.getMajorVersion().trim().toLowerCase().equals(details.getVersionInXMLContent().trim().toLowerCase()))
					{
						// MATCHED = YES
						dataRow+="YES";
					}
					else
					{
						// MATCHED = NO
						dataRow+="NO";
					}
					// increment rowCount by 1
					rowCount++;
					// Create a new Row
					row = mySheet.createRow(rowCount);
					tokens = dataRow.split("<TOK_SEPARATOR>");
					if(null!=tokens && tokens.length>0)
					{
						for(int e=0;e<tokens.length;e++)
						{
							dataCell = row.createCell(e);
							dataCell.setCellStyle(dataStyle);
							dataCell.setCellValue("");
							if(null!=tokens[e] && !"".equals(tokens[e]) && !"null".equals(tokens[e].trim().toLowerCase()))
							{
								dataCell.setCellValue(tokens[e].trim());
							}
							dataCell =null;
						}
					}
					tokens = null;
					row=null;
					dataRow = null;
					dataCell = null;
					details = null;
				}
				dataStyle = null;
				dFont = null;
				
				headerRow = null;

				FileOutputStream os = new FileOutputStream(myFile);
				myWorkBook.write(os);
				logger.info("Writing on VERSION IMPACTED DOCUMENTS REPORT XLSX NAMED AS >"+fName+" file Finished ...");
				os.flush();
				os.close();

				// set mySheet to null
				mySheet = null;
				// set myWorkBook to null
				myWorkBook = null;
				// set path to null
				path = null;
				// set myFile to null
				myFile = null;
				fName = null;
			}
			list = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(GenerateVersionImpactedDocsImpl.class.getName(), "main()", e);
		}
	}

}
