package com.invesco.datamigration.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.List;

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
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.invesco.datamigration.dao.ReadTransactionDAO;
import com.invesco.datamigration.utils.ApplicationProperties;
import com.invesco.datamigration.utils.CustomUtils;
import com.invesco.datamigration.utils.ParseXMLDoc;
import com.invesco.datamigration.utils.Utilities;
import com.invesco.datamigration.vo.DocumentDetails;

public class AddDisplayReviewDateImpl {

	private static Logger logger = Logger.getLogger(AddDisplayReviewDateImpl.class);
	
	public static void main(String[] args) {
		// initialie Loggers
		File jarPath = new File(AddDisplayReviewDateImpl.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String propertiesPath = jarPath.getParentFile().getAbsolutePath();
		PropertyConfigurator.configure(propertiesPath+"/"+"log.properties");
		try
		{
			/*
			 * FETCH UNIQUE DOCUMENTS LIST WITH VERSION & CHANNEL
			 */
			ReadTransactionDAO rDao = new ReadTransactionDAO();
			List<DocumentDetails> documentsList = rDao.getDocumentsListForAddingReviewDate();
			if(null!=documentsList && documentsList.size()>0)
			{
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String extractLocation = ApplicationProperties.getProperty("invesco.content.extract.location");
				String path=null;
				File xmlFile = null;
				DocumentDetails details = null;
				String localeNodeXMLValue=null;
				for(int a=0;a<documentsList.size();a++)
				{
					details = (DocumentDetails)documentsList.get(a);
					try
					{
						/*
						 * prepare document path
						 * extractLocation/CHANNEL/DOCUMENT_ID/version<MAJORVERSION>/Article_<DOC_ID>_version<MAJORVERSION>.html
						 */
						path = extractLocation+details.getChannelRefKey()+"/"+details.getDocumentId()+"/version"+details.getMajorVersion()+"/";
						path+="Article_"+details.getDocumentId()+"_version"+details.getMajorVersion()+".html";
						// SET IN DOCUMENT DETAILS
						details.setXmlFileSourcePath(path);
						logger.info("main :: Proceed for reading HTML File at Path :: >"+ path);
						xmlFile = new File(path);
						if(xmlFile.exists() && xmlFile.isFile())
						{
							Document document = ParseXMLDoc.parseFile(xmlFile.getAbsolutePath());
							if(null!=document)
							{
								/*
								 * REMOVE ALL DISPLAYREVIEWDATE NODES FROM DOCUMENT
								 */
								NodeList nodesList = document.getElementsByTagName("CONTENT");
								if(null!=nodesList && nodesList.getLength()>0)
								{
									Node contentLocaleNode = null;
									Node contentNode = null;
									NodeList childNodesList = null;
									for(int r=0;r<nodesList.getLength();r++)
									{
										contentNode = (Node)nodesList.item(r);
										if(null!=contentNode && null!=contentNode.getNodeName())
										{
											childNodesList = contentNode.getChildNodes();
											if(null!=childNodesList && childNodesList.getLength()>0)
											{
												Node childNode = null;
												for(int t=0;t<childNodesList.getLength();t++)
												{
													childNode = (Node)childNodesList.item(t);
													if(null!=childNode && null!=childNode.getNodeName() && childNode.getNodeName().equals("LOCALECODE"))
													{
														String locale = Utilities.readNodeValue(childNode);
														if(null!=locale && !"".equals(locale) && locale.trim().toLowerCase().equals(details.getLocale().trim().toLowerCase()))
														{
															// LOCALE NODE FOUND
															contentLocaleNode = contentNode;
															break;
														}
														locale = null;
													}
													childNode = null;
												}
											}
											childNodesList = null;
										}
										contentNode = null;
									}
									
									if(null!=contentLocaleNode && null!=contentLocaleNode.getChildNodes() && contentLocaleNode.getChildNodes().getLength()>0)
									{
										contentNode = null;
										childNodesList = null;
										for(int r=0;r<contentLocaleNode.getChildNodes().getLength();r++)
										{
											contentNode = (Node)contentLocaleNode.getChildNodes().item(r);
											if(null!=contentNode && null!=contentNode.getNodeName() && contentNode.getNodeName().equals("DISPLAYREVIEWDATE"))
											{
												// remove this Node
												contentLocaleNode.removeChild(contentNode);
												break;
											}
											contentNode = null;
										}
									}
									
//									Node contentNode = null;
//									NodeList childNodesList = null;
//									for(int r=0;r<nodesList.getLength();r++)
//									{
//										contentNode = (Node)nodesList.item(r);
//										if(null!=contentNode && null!=contentNode.getNodeName())
//										{
//											childNodesList = contentNode.getChildNodes();
//											if(null!=childNodesList && childNodesList.getLength()>0)
//											{
//												Node childNode = null;
//												for(int t=0;t<childNodesList.getLength();t++)
//												{
//													childNode = (Node)childNodesList.item(t);
//													if(null!=childNode && null!=childNode.getNodeName() && childNode.getNodeName().equals("DISPLAYREVIEWDATE"))
//													{
//														// remove this Node
//														contentNode.removeChild(childNode);
//														break;
//													}
//													childNode = null;
//												}
//											}
//											childNodesList = null;
//										}
//										contentNode = null;
//									}
								}
								nodesList = null;
								/*
								 * CHECK IF LANGUAGE NODE FOR SAME LOCALE EXISTS
								 * 	REMOVE EXISTING NODE AND APPEND NEW NODE
								 * 	IF DOES NOT EXIST THEN APPEND NEW NODE
								 */
								Element newEle = document.createElement("DISPLAYREVIEWDATE");
								if(null!=details.getDisplayReviewDate())
								{
									newEle.setTextContent(sdf.format(details.getDisplayReviewDate())+" Etc/GMT");
								}
								else
								{
									newEle.setTextContent("");
								}
								
								localeNodeXMLValue = null;
								nodesList = document.getElementsByTagName("CONTENT");
								if(null!=nodesList && nodesList.getLength()>0)
								{
									Node contentNode = null;
									NodeList childNodesList = null;
									for(int r=0;r<nodesList.getLength();r++)
									{
										contentNode = (Node)nodesList.item(r);
										if(null!=contentNode && null!=contentNode.getNodeName())
										{
											childNodesList = contentNode.getChildNodes();
											if(null!=childNodesList && childNodesList.getLength()>0)
											{
												Node childNode = null;
												for(int t=0;t<childNodesList.getLength();t++)
												{
													childNode = (Node)childNodesList.item(t);
													if(null!=childNode && null!=childNode.getNodeName() && childNode.getNodeName().equals("LOCALECODE"))
													{
														localeNodeXMLValue = Utilities.readNodeValue(childNode);
														if(null!=localeNodeXMLValue && !"".equals(localeNodeXMLValue) && 
																localeNodeXMLValue.trim().toLowerCase().equals(details.getLocale().trim().toLowerCase()))
														{
//															System.out.println("------------------- ADDING DATE FOR LOCALE :: >"+ localeNodeXMLValue+" >>"+ details.getDisplayReviewDate());
															// NODE FOUND FOR APPENDING THE DATE
															contentNode.appendChild(newEle);
															break;
														}
													}
													childNode = null;
												}
											}
											childNodesList = null;
										}
										contentNode = null;
									}
									nodesList = null;
									newEle = null;
									
									// NOW CONVERT DOCUMENT TO STRING
									String xmlDoc = Utilities.transformString(document);
									if(null!=xmlDoc && !"".equals(xmlDoc))
									{
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
											logger.info("main :: XML File Written Successfully for "+details.getDocumentId()+"  at path :: >"+path);
										}
										catch(Exception e)
										{
											Utilities.printStackTraceToLogs(CustomUtils.class.getName(), "writeXMLFile()", e);
											// set ERROR MESSAGE
											details.setErrorMessage("FAILED TO WRITE XML FILE AT DESTINATION LOCATION AT PATH >"+path+". :: EXCEPTION :: >"+ e.getMessage());
											details.setProcessingStatus("FAILURE");
										}
									}
									else
									{
										// set errorMessage
										details.setErrorMessage("FAILED TO CONVERT EXISTING XML DOCUMENT TO STRING FOR APPENDING MASTER IDENTIFIER NODE.");
										details.setProcessingStatus("FAILURE");
									}
									xmlDoc = null;
								}
								else
								{
									details.setErrorMessage("FAILED TO READ PARENT <HTML> NODE FROM EXISTING XML FILE AT DESTINATION LOCATION AT PATH >"+ path);
									details.setProcessingStatus("FAILURE");
								}
							}
							else
							{
								details.setErrorMessage("FAILED TO READ EXISTING XML FILE AT DESTINATION LOCATION AT PATH >"+path);
								details.setProcessingStatus("FAILURE");
							}
							document = null;
						}
						else
						{
							details.setErrorMessage("XML FILE DOES NOT EXIST AT SOURCE LOCATION :: >"+ path);
							details.setProcessingStatus("FAILURE");
						}
						xmlFile = null;
						path = null;
					}
					catch(Exception e)
					{
						Utilities.printStackTraceToLogs(AddDisplayReviewDateImpl.class.getName(), "main()", e);
						
						Writer writer = new StringWriter();
						PrintWriter print = new PrintWriter(writer);
						e.printStackTrace(print);
						
						details.setErrorMessage(writer.toString());
						details.setProcessingStatus("FAILURE");
						writer.close();writer = null;
						print.close();print = null;
					}
					details = null;
				}
			}
			
			
			
			/*
			 * GENERTAE A TRANSACTION REPORT
			 */
			if(null!=documentsList && documentsList.size()>0)
			{
				printReport(documentsList);
			}
			documentsList = null;
			rDao = null;
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(AddDisplayReviewDateImpl.class.getName(), "main()", e);
		}
	}
	
	private static void printReport(List<DocumentDetails> list)
	{
		try
		{
			String path="C:/Users/dabkav/Documents/WD/Reports";
			String fName = "/ADD_DISPLAY_REVIEW_DATE_REPORT.xlsx";

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

			String headers="CHANNEL_REFKEY,DOCUMENT_ID,LOCALE,MAJOR_VERSION,SOURCE_FILE_PATH,PROCESSING_STATUS,ERROR_MESSAGE"; 
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
			DocumentDetails details = null;
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
				dataRow=details.getChannelRefKey()+"<TOK_SEPARATOR>"+details.getDocumentId()+"<TOK_SEPARATOR>";
				dataRow+=details.getLocale()+"<TOK_SEPARATOR>"+details.getMajorVersion()+"<TOK_SEPARATOR>"+details.getXmlFileSourcePath()+"<TOK_SEPARATOR>";
				dataRow+=details.getProcessingStatus()+"<TOK_SEPARATOR>"+details.getErrorMessage();
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
			logger.info("Writing on ADD MASTER IDENTIFIERS REPORT XLSX NAMED AS >"+fName+" file Finished ...");
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
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(AddDisplayReviewDateImpl.class.getName(), "printReport()", e);
		}
	}

}
