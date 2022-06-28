package com.invesco.datamigration.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.invesco.datamigration.dao.WriteTransactionDAO;
import com.invesco.datamigration.utils.Utilities;
import com.invesco.datamigration.vo.InlineInnerlinkDetails;

public class PrintInnerlinkDetailsReportImpl {

	private static Logger logger = Logger.getLogger(PrintInnerlinkDetailsReportImpl.class);
	
	public static void main(String[] args) {
		// initialie Loggers
		File jarPath = new File(PrintInnerlinkDetailsReportImpl.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		String propertiesPath = jarPath.getParentFile().getAbsolutePath();
		PropertyConfigurator.configure(propertiesPath+"/"+"log.properties");
		try
		{
			generateInnerLinksReports();
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(PrintInnerlinkDetailsReportImpl.class.getName(), "main()", e);
		}
	}
	
	private static void generateInnerLinksReports()
	{
		try
		{
			WriteTransactionDAO dao = new WriteTransactionDAO();
			int limit = 250000;
			// set end value as 10 Crore
			int endValue = 50;
			int offset=0;
			for(int a=0;a<endValue;a++)
			{
				if(a==0)
				{
					offset = 0;
				}
				else
				{
					offset = offset+limit;
				}
				List<InlineInnerlinkDetails> list = dao.getInnerlinkDetails(offset, limit);
				if(null!=list && list.size()>0)
				{
					String path="C:/Users/dabkav/Documents/WD/Reports/24-04-2022";
					String fName = "/INNERLINKS_DETAILS_"+(a+1)+".xlsx";

					File myFile = new File(path + fName);     
					// Create the workbook instance for XLSX file, KEEP 100 ROWS IN MEMMORY AND RET ON DISK
					SXSSFWorkbook myWorkBook = new SXSSFWorkbook(100);

					// Create a new sheet
					Sheet mySheet = myWorkBook.createSheet("Details");
					/*
					 * Add Header Row
					 */
					Row headerRow = mySheet.createRow(0);
					Cell headerCell = null;

					String headers="CHANNEL_NAME,CHANNEL_REFKEY,DOCUMENT_ID,LOCALE,MAJOR_VERSION,MINOR_VERSION,INNERLINK_PATH,SOURCE_TAG_LENGTH,SOURCE_TAG"; 
					String[] tokens=headers.split(",");
					if(null!=tokens && tokens.length>0)
					{
						for(int c=0;c<tokens.length;c++)
						{
							headerCell = headerRow.createCell(c);
							headerCell.setCellValue(tokens[c]);
							headerCell  = null;
						}
					}
					tokens = null;
					headerCell=null;
					headerRow=null;
					headers=null;

					int rowCount = 0;

					/*
					 * GENERATE MULTIPLE ROWS SUCH THAT
					 */
					 String dataRow="";
					Row row=null;
					Cell dataCell=null;
					InlineInnerlinkDetails details = null;
					for(int b=0;b<list.size();b++)
					{
						details = (InlineInnerlinkDetails)list.get(b);
						dataRow=details.getDocumentDetails().getChannel()+"<TOK_SEPARATOR>"+details.getDocumentDetails().getChannelRefKey()+"<TOK_SEPARATOR>"+details.getDocumentDetails().getDocumentId()+"<TOK_SEPARATOR>";
						dataRow+=details.getDocumentDetails().getLocale()+"<TOK_SEPARATOR>"+details.getDocumentDetails().getMajorVersion()+"<TOK_SEPARATOR>"+details.getDocumentDetails().getMinorVersion()+"<TOK_SEPARATOR>";
						dataRow+=details.getInnerLinkSourceUrl()+"<TOK_SEPARATOR>"+details.getInnerLinkSourceTagLength()+"<TOK_SEPARATOR>"+details.getInnerLinkSourceTag();
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
					
					headerRow = null;

					FileOutputStream os = new FileOutputStream(myFile);
					myWorkBook.write(os);
					logger.info("Writing on INNERLINK REPORT XLSX NAMED AS >"+fName+" file Finished ...");
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
					
					continue;
				}
				else
				{
					// break the loop
					break;
				}
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(PrintInnerlinkDetailsReportImpl.class.getName(), "generateInnerLinksReports()", e);
		}
	}

}