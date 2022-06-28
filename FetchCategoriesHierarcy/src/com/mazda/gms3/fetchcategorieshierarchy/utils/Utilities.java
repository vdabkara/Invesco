package com.mazda.gms3.fetchcategorieshierarchy.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.apache.axis.utils.XMLUtils;
import org.jsoup.Jsoup;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.apache.log4j.Logger;
import org.apache.poi.ss.format.CellNumberFormatter;
import org.apache.poi.ss.usermodel.Cell;



public class Utilities {

	static Logger logger = Logger.getLogger(Utilities.class);
	
	public static void printStackTraceToLogs(String className, String methodName, Exception e)
	{
		try
		{
			Writer writer = new StringWriter();
			PrintWriter print = new PrintWriter(writer);
			e.printStackTrace(print);
			
			logger.info(className+"::"+methodName+":: Error :: > " + e.getMessage());
			logger.info(className+"::"+methodName+":: Error :: > " + writer.toString());
//			String errorCode = e.getMessage();
//			String errorMessage = writer.toString();
			
			print = null;
			writer= null;
		}
		catch(Exception f)
		{
			f.printStackTrace();
		}
	}	
	
	
	public static String readNodeValue(Node node)
	{
		String nodeValue="";
		Element valueElement= (Element)node;
		String text = XMLUtils.ElementToString(valueElement);
		if(null!=text && !"".equals(text))
		{
			if(text.contains("<![CDATA["))
			{
				// GET CDATA VALUE OF THE ELEMENT
				nodeValue = Utilities.getCharacterDataFromElement(valueElement);
			}
			else
			{
				nodeValue = valueElement.getTextContent();
			}
		}
		text= null;
		valueElement = null;
		return nodeValue;
	}
	
	/**
	 * Function will help to get the CDATA value of an Element in the XML Document
	 * @param e
	 * @return
	 */
	private static String getCharacterDataFromElement(Element e) {
		Node child = e.getFirstChild();
		if (child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
		}
		return "";
	}

	public static String getStringFromXML(File f) throws IOException 
	{
		StringBuffer xmlData = new StringBuffer();
		try
		{
			BufferedReader input = new BufferedReader(new FileReader(f));
			try 
			{
				String line = null;
				while ((line = input.readLine()) != null) 
				{
					xmlData.append(line);
					xmlData.append(System.getProperty("line.separator"));
				}
			}
			finally 
			{
				input.close();
			}
		} 
		catch (IOException ex) 
		{
			ex.printStackTrace();
			logger.error("getStringFromXML :: Cannot Read the Input FileL :: "	+ f.getName());
			throw ex;
		}
		String articleXML = xmlData.toString();
		return articleXML;
	}
	
	/**
	 * Function will get the HTML Content from File
	 * @param f
	 * @return
	 * @throws IOException
	 */
	public static String getStringFromHTML(File f) throws IOException {
		String htmlContent = "";
		try {
			org.jsoup.nodes.Document doc = Jsoup.parse(f, "UTF-8");
			if (null != doc) {
				htmlContent = doc.toString();
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("getStringFromHTML :: Cannot Read the Input File :: "
					+ f.getName());
			throw e;
		}
		return htmlContent;
	}

	/**
	 * Function will identify the Cell Value Type
	 * and accordingly will read their values and will return
	 * @param cell
	 * @return
	 */
	public static Object readCellValue(Cell cell)
	{
		Object cellValue = null;
		try {
			if (null != cell) {
				/*
				 * check for Cell Type and format accordingly
				 */
				if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
					cellValue = cell.getNumericCellValue();
					
					/*
					 * Check here, if callValue is 0; THEN return "" as object
					 * else, check for decimal value in cell
					 * if decimal value is .0 or .00, then remove it
					 * if more than 0, then pass the value as it is
					 */

					String val = String.valueOf(cellValue);
					if(null!=val && !"".equals(val))
					{
						/*
						 *  check if val has any value in point to decimal and that value is 0 then remove the decimal Value
						 */
						if(val.lastIndexOf(".")!=-1)
						{
							String decimal = val.substring(val.lastIndexOf(".")+1,val.length());
							if(null!=decimal && !"".equals(decimal))
							{
								Double dec = new Double(decimal).doubleValue();
								if(dec==0)
								{
									// remove decimal from the val and value after decimal
									val = val.substring(0,val.lastIndexOf("."));
								}
								else
								{
									// GET THE NUMERIC VALUE IN THE FORMAT ########################## AND PASS IT
									CellNumberFormatter cn = new CellNumberFormatter("################################");
									val = cn.format(cell.getNumericCellValue());
									cn = null;
								}
								// set dec to null
								dec = null;
							}
							// set decimal to null
							decimal = null;
						}
					}
					
					// set cellValue as val
					cellValue = val;
					// set val to null
					val  = null;
				}
				else if(cell.getCellType() == Cell.CELL_TYPE_FORMULA)
				{
					cellValue = cell.getCellFormula();
					if(null!=cellValue)
					{
						// append a = with this formula
						cellValue = "="+cellValue;
					}
				}
				else if (cell.getCellType() == Cell.CELL_TYPE_STRING) {
					cellValue = cell.getStringCellValue();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			// set cellValue to null
			cellValue = null;
		}
		return cellValue;
	}

}
