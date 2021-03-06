package com.mazda.gms3.fetchcategorieshierarchy.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import com.inquira.im.ito.CategoryITO;
import com.inquira.im.ito.CategoryKeyITO;
import com.mazda.gms3.fetchcategorieshierarchy.utils.ApplicationPropertiesUtil;
import com.mazda.gms3.fetchcategorieshierarchy.utils.Utilities;
import com.mazda.gms3.fetchcategorieshierarchy.vo.CategoryDetails;

public class StartProcessingImpl {

	 Logger logger  = Logger.getLogger(StartProcessingImpl.class);
	
	 StringBuilder summaryBuilder = new StringBuilder();
	
	public ArrayList<Map<Object, Object>> catList = new ArrayList<Map<Object, Object>>();
	
	public  void startProcessing(String localeValue, String categoryRefKey)
	{
		try
		{
			if(null!=localeValue && !"".equals(localeValue) && null!=categoryRefKey && !"".equals(categoryRefKey))
			{
				/*
				 * check here, if Locale value is All - then fetch the data for the Category Ref Key and its Childs
				 * for all Locales
				 * else fetch it for the specified Locale
				 */
				if(localeValue.trim().toLowerCase().equals(ApplicationPropertiesUtil.getProperty("check.all").trim().toLowerCase()))
				{
					/*
					 * Fetch for all Locales
					 */
					// en_US
					executeFetchOperation(ApplicationPropertiesUtil.getProperty("en_us"), categoryRefKey);
					// en_CA
					executeFetchOperation(ApplicationPropertiesUtil.getProperty("en_ca"), categoryRefKey);
					// es_MX - NO EX MX 
//					executeFetchOperation(ApplicationPropertiesUtil.getProperty("es_mx"), categoryRefKey);
					// fr_CA
					executeFetchOperation(ApplicationPropertiesUtil.getProperty("fr_ca"), categoryRefKey);
				}
				else
				{
					/*
					 * Fetch only for the specified Locale
					 */
					executeFetchOperation(localeValue, categoryRefKey);
				}
				
				if(null!=summaryBuilder)
				{
					System.err.println(summaryBuilder.toString());
				}
			}
			else
			{
				logger.info("startProcessing :: Either Locale Values or Category Ref Key is null. Existing Tool.");
				System.err.println("Either Locale Values or Category Ref Key is null. Existing Tool.");
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(StartProcessingImpl.class.getName(), "startProcessing()", e);
		}
	}
	
	
	private  void executeFetchOperation(String locale, String catRefKey)
	{
		try
		{
			CategoryITO categoryIto = InfoManagerServiceImpl.getCategory(catRefKey, locale);
			if(null!=categoryIto && null!=categoryIto.getReferenceKey() && !"".equals(categoryIto.getReferenceKey()))
			{
				CategoryDetails categoryDetails = new CategoryDetails();
				categoryDetails.setCategoryName(categoryIto.getName());
				categoryDetails.setCategoryRefKey(categoryIto.getReferenceKey());
				categoryDetails.setObjectId(categoryIto.getObjectID());
				ArrayList<CategoryDetails> childList = fetchAllChildrensData(categoryDetails, locale, categoryIto);
				if(null!=childList && childList.size()>0)
				{
					categoryDetails.setChildList(childList);
				}
				childList = null;
				
				ArrayList<Map<Object, Object>> dataList = new ArrayList<Map<Object, Object>>();
				dataList  = arrangeCategoriesData(categoryDetails);
				
				if(null!=dataList && dataList.size()>0)
				{
					/*
					 * Proceed for Generating Excel Locale Wise
					 */
//					generateExcel(dataList, locale, catRefKey);
					catList.addAll(dataList);
				}
				else
				{
					logger.info("No Data Could be extracted for {"+catRefKey+"} under Locale :: > "+ locale);
					System.err.println("No Data Could be extracted for {"+catRefKey+"} under Locale :: > "+ locale);
					summaryBuilder.append("No Data Could be extracted for {"+catRefKey+"} under Locale :: > "+ locale);
					summaryBuilder.append("\n");
				}
				dataList = null;
			}
			else
			{
				logger.info("executeFetchOperation :: {"+catRefKey+"} Does not exists in InfoManager for Locale :: > " + locale);
//				System.err.println("{"+catRefKey+"} Does not exists in InfoManager for Locale :: > " + locale);
				summaryBuilder.append("{"+catRefKey+"} Does not exists in InfoManager for Locale :: > " + locale);
				summaryBuilder.append("\n");
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(StartProcessingImpl.class.getName(), "executeFetchOperation()", e);
		}
	}
	
	@SuppressWarnings("deprecation")
	private  ArrayList<CategoryDetails> fetchAllChildrensData(CategoryDetails parentCategoryDetails, String locale, CategoryITO parentCategoryIto)
	{
		ArrayList<CategoryDetails> list = new ArrayList<CategoryDetails>();
		try
		{
			List<CategoryKeyITO> childsList = parentCategoryIto.getChildren();
			if(null!=childsList && childsList.size()>0)
			{
				for(int i=0;i<childsList.size();i++)
				{
					CategoryKeyITO keyIto = (CategoryKeyITO)childsList.get(i);
					if(null!=keyIto && null!=keyIto.getReferenceKey() && !"".equals(keyIto.getReferenceKey()))
					{
						
						/*
						 * Look for the Childs of the Key and if Found then add to the Data, Iterate each of the Child
						 * and check if any of them has a child, keep on adding to the excel. 
						 */
						CategoryITO childCatIto = InfoManagerServiceImpl.getCategory(keyIto.getReferenceKey(), locale);
						if(null!=childCatIto && null!=childCatIto.getReferenceKey() && !"".equals(childCatIto.getReferenceKey()))
						{
							// SET IN NEW CHILD CATEGORY dETILS
							CategoryDetails childCatDetails = new CategoryDetails();
							childCatDetails.setCategoryName(childCatIto.getName());
							childCatDetails.setCategoryRefKey(childCatIto.getReferenceKey());
							childCatDetails.setObjectId(childCatIto.getObjectID());
							
							List<CategoryKeyITO> secondLevelChildsList = childCatIto.getChildren();
							if(null!=secondLevelChildsList && secondLevelChildsList.size()>0)
							{
								// CAL RECURSIVE FUNCTION.
								ArrayList<CategoryDetails> secondLevelList = fetchAllChildrensData(childCatDetails, locale, childCatIto);
								if(null!=secondLevelList && secondLevelList.size()>0)
								{
									childCatDetails.setChildList(secondLevelList);
								}
								secondLevelList = null;
							}
							secondLevelChildsList = null;
							// add childCatDetails to list
							list.add(childCatDetails);
							childCatDetails = null;
						}
						childCatIto = null;
					}
					keyIto = null;
				}
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(StartProcessingImpl.class.getName(), "fetchAllChildrensData()", e);
		}
		return list;
	}
	
	
	private  ArrayList<Map<Object, Object>> arrangeCategoriesData(CategoryDetails categoryDetails)
	{
		ArrayList<Map<Object, Object>> dataList = new ArrayList<Map<Object, Object>>();
		try
		{
			if(null!=categoryDetails.getChildList() && categoryDetails.getChildList().size()>0)
			{
				for(int i=0;i<categoryDetails.getChildList().size();i++)
				{
					CategoryDetails childLevel2Details = (CategoryDetails)categoryDetails.getChildList().get(i);
					if(null!=childLevel2Details.getChildList() && childLevel2Details.getChildList().size()>0)
					{
						for(int j=0;j<childLevel2Details.getChildList().size();j++)
						{
							CategoryDetails childLevel3Details = (CategoryDetails)childLevel2Details.getChildList().get(j);
							if(null!=childLevel3Details.getChildList() && !"".equals(childLevel3Details.getChildList()))
							{
								for(int k=0;k<childLevel3Details.getChildList().size();k++)
								{
									CategoryDetails childLevel4Details = (CategoryDetails)childLevel3Details.getChildList().get(k);
									if(null!=childLevel4Details.getChildList() && childLevel4Details.getChildList().size()>0)
									{
										for(int l=0;l<childLevel4Details.getChildList().size();l++)
										{
											CategoryDetails childLevel5Details = (CategoryDetails)childLevel4Details.getChildList().get(l);
											if(null!=childLevel5Details.getChildList() && childLevel5Details.getChildList().size()>0)
											{
												for(int m=0;m<childLevel5Details.getChildList().size();m++)
												{
													CategoryDetails childLevel6Details = (CategoryDetails)childLevel5Details.getChildList().get(m);
													if(null!=childLevel6Details.getChildList() && childLevel6Details.getChildList().size()>0)
													{
														for(int n=0;n<childLevel6Details.getChildList().size();n++)
														{
															CategoryDetails childLevel7Details = (CategoryDetails)childLevel6Details.getChildList().get(n);
															if(null!=childLevel7Details.getChildList() && childLevel7Details.getChildList().size()>0)
															{
																for(int o=0;o<childLevel7Details.getChildList().size();o++)
																{
																	CategoryDetails childLevel8Details = (CategoryDetails)childLevel7Details.getChildList().get(o);
																	Map<Object, Object> dataMap= new HashMap<Object , Object>();
																	if(null!=categoryDetails.getCategoryName() && !"".equals(categoryDetails.getCategoryName()))
																	{
																		dataMap.put("LEVEL1_NAME", categoryDetails.getCategoryName());
																	}
																	dataMap.put("LEVEL1_REFKEY", categoryDetails.getCategoryRefKey());
																	dataMap.put("LEVEL1_OBJECTID", categoryDetails.getObjectId());
																	
																	
																	// add Level 2
																	if(null!=childLevel2Details.getCategoryName() && !"".equals(childLevel2Details.getCategoryName()))
																	{
																		dataMap.put("LEVEL2_NAME", childLevel2Details.getCategoryName());
																	}
																	dataMap.put("LEVEL2_REFKEY", childLevel2Details.getCategoryRefKey());
																	dataMap.put("LEVEL2_OBJECTID", childLevel2Details.getObjectId());
																	
																	// add Level 3
																	if(null!=childLevel3Details.getCategoryName() && !"".equals(childLevel3Details.getCategoryName()))
																	{
																		dataMap.put("LEVEL3_NAME", childLevel3Details.getCategoryName());
																	}
																	dataMap.put("LEVEL3_REFKEY", childLevel3Details.getCategoryRefKey());
																	dataMap.put("LEVEL3_OBJECTID", childLevel3Details.getObjectId());
																	
																	// add Level 4
																	if(null!=childLevel4Details.getCategoryName() && !"".equals(childLevel4Details.getCategoryName()))
																	{
																		dataMap.put("LEVEL4_NAME", childLevel4Details.getCategoryName());
																	}
																	dataMap.put("LEVEL4_REFKEY", childLevel4Details.getCategoryRefKey());
																	dataMap.put("LEVEL4_OBJECTID", childLevel4Details.getObjectId());
																	
																	// add Level 5
																	if(null!=childLevel5Details.getCategoryName() && !"".equals(childLevel5Details.getCategoryName()))
																	{
																		dataMap.put("LEVEL5_NAME", childLevel5Details.getCategoryName());
																	}
																	dataMap.put("LEVEL5_REFKEY", childLevel5Details.getCategoryRefKey());
																	dataMap.put("LEVEL5_OBJECTID", childLevel5Details.getObjectId());
																	
																	// add Level 6
																	if(null!=childLevel6Details.getCategoryName() && !"".equals(childLevel6Details.getCategoryName()))
																	{
																		dataMap.put("LEVEL6_NAME", childLevel6Details.getCategoryName());
																	}
																	dataMap.put("LEVEL6_REFKEY", childLevel6Details.getCategoryRefKey());
																	dataMap.put("LEVEL6_OBJECTID", childLevel6Details.getObjectId());
																	
																	// add Level 7
																	if(null!=childLevel7Details.getCategoryName() && !"".equals(childLevel7Details.getCategoryName()))
																	{
																		dataMap.put("LEVEL7_NAME", childLevel7Details.getCategoryName());
																	}
																	dataMap.put("LEVEL7_REFKEY", childLevel7Details.getCategoryRefKey());
																	dataMap.put("LEVEL7_OBJECTID", childLevel7Details.getObjectId());
																	
																	// add Level 8
																	if(null!=childLevel8Details.getCategoryName() && !"".equals(childLevel8Details.getCategoryName()))
																	{
																		dataMap.put("LEVEL8_NAME", childLevel8Details.getCategoryName());
																	}
																	dataMap.put("LEVEL8_REFKEY", childLevel8Details.getCategoryRefKey());
																	dataMap.put("LEVEL8_OBJECTID", childLevel8Details.getObjectId());
																	dataMap.put("MAX_LEVEL", "8");
																	
																	
																	dataList.add(dataMap);
																	dataMap  = null;
																
																	childLevel8Details = null;
																}
															}
															else
															{
																Map<Object, Object> dataMap= new HashMap<Object , Object>();
																if(null!=categoryDetails.getCategoryName() && !"".equals(categoryDetails.getCategoryName()))
																{
																	dataMap.put("LEVEL1_NAME", categoryDetails.getCategoryName());
																}
																dataMap.put("LEVEL1_REFKEY", categoryDetails.getCategoryRefKey());
																dataMap.put("LEVEL1_OBJECTID", categoryDetails.getObjectId());
																
																
																// add Level 2
																if(null!=childLevel2Details.getCategoryName() && !"".equals(childLevel2Details.getCategoryName()))
																{
																	dataMap.put("LEVEL2_NAME", childLevel2Details.getCategoryName());
																}
																dataMap.put("LEVEL2_REFKEY", childLevel2Details.getCategoryRefKey());
																dataMap.put("LEVEL2_OBJECTID", childLevel2Details.getObjectId());
																
																// add Level 3
																if(null!=childLevel3Details.getCategoryName() && !"".equals(childLevel3Details.getCategoryName()))
																{
																	dataMap.put("LEVEL3_NAME", childLevel3Details.getCategoryName());
																}
																dataMap.put("LEVEL3_REFKEY", childLevel3Details.getCategoryRefKey());
																dataMap.put("LEVEL3_OBJECTID", childLevel3Details.getObjectId());
																
																// add Level 4
																if(null!=childLevel4Details.getCategoryName() && !"".equals(childLevel4Details.getCategoryName()))
																{
																	dataMap.put("LEVEL4_NAME", childLevel4Details.getCategoryName());
																}
																dataMap.put("LEVEL4_REFKEY", childLevel4Details.getCategoryRefKey());
																dataMap.put("LEVEL4_OBJECTID", childLevel4Details.getObjectId());
																
																// add Level 5
																if(null!=childLevel5Details.getCategoryName() && !"".equals(childLevel5Details.getCategoryName()))
																{
																	dataMap.put("LEVEL5_NAME", childLevel5Details.getCategoryName());
																}
																dataMap.put("LEVEL5_REFKEY", childLevel5Details.getCategoryRefKey());
																dataMap.put("LEVEL5_OBJECTID", childLevel5Details.getObjectId());
																
																// add Level 6
																if(null!=childLevel6Details.getCategoryName() && !"".equals(childLevel6Details.getCategoryName()))
																{
																	dataMap.put("LEVEL6_NAME", childLevel6Details.getCategoryName());
																}
																dataMap.put("LEVEL6_REFKEY", childLevel6Details.getCategoryRefKey());
																dataMap.put("LEVEL6_OBJECTID", childLevel6Details.getObjectId());
																
																// add Level 7
																if(null!=childLevel7Details.getCategoryName() && !"".equals(childLevel7Details.getCategoryName()))
																{
																	dataMap.put("LEVEL7_NAME", childLevel7Details.getCategoryName());
																}
																dataMap.put("LEVEL7_REFKEY", childLevel7Details.getCategoryRefKey());
																dataMap.put("LEVEL7_OBJECTID", childLevel7Details.getObjectId());
																dataMap.put("MAX_LEVEL", "7");
																
																dataList.add(dataMap);
																dataMap  = null;
															}
															childLevel7Details = null;
														}
													}
													else
													{
														Map<Object, Object> dataMap= new HashMap<Object , Object>();
														if(null!=categoryDetails.getCategoryName() && !"".equals(categoryDetails.getCategoryName()))
														{
															dataMap.put("LEVEL1_NAME", categoryDetails.getCategoryName());
														}
														dataMap.put("LEVEL1_REFKEY", categoryDetails.getCategoryRefKey());
														dataMap.put("LEVEL1_OBJECTID", categoryDetails.getObjectId());
														
														
														// add Level 2
														if(null!=childLevel2Details.getCategoryName() && !"".equals(childLevel2Details.getCategoryName()))
														{
															dataMap.put("LEVEL2_NAME", childLevel2Details.getCategoryName());
														}
														dataMap.put("LEVEL2_REFKEY", childLevel2Details.getCategoryRefKey());
														dataMap.put("LEVEL2_OBJECTID", childLevel2Details.getObjectId());
														
														// add Level 3
														if(null!=childLevel3Details.getCategoryName() && !"".equals(childLevel3Details.getCategoryName()))
														{
															dataMap.put("LEVEL3_NAME", childLevel3Details.getCategoryName());
														}
														dataMap.put("LEVEL3_REFKEY", childLevel3Details.getCategoryRefKey());
														dataMap.put("LEVEL3_OBJECTID", childLevel3Details.getObjectId());
														
														// add Level 4
														if(null!=childLevel4Details.getCategoryName() && !"".equals(childLevel4Details.getCategoryName()))
														{
															dataMap.put("LEVEL4_NAME", childLevel4Details.getCategoryName());
														}
														dataMap.put("LEVEL4_REFKEY", childLevel4Details.getCategoryRefKey());
														dataMap.put("LEVEL4_OBJECTID", childLevel4Details.getObjectId());
														
														// add Level 5
														if(null!=childLevel5Details.getCategoryName() && !"".equals(childLevel5Details.getCategoryName()))
														{
															dataMap.put("LEVEL5_NAME", childLevel5Details.getCategoryName());
														}
														dataMap.put("LEVEL5_REFKEY", childLevel5Details.getCategoryRefKey());
														dataMap.put("LEVEL5_OBJECTID", childLevel5Details.getObjectId());
														
														// add Level 6
														if(null!=childLevel6Details.getCategoryName() && !"".equals(childLevel6Details.getCategoryName()))
														{
															dataMap.put("LEVEL6_NAME", childLevel6Details.getCategoryName());
														}
														dataMap.put("LEVEL6_REFKEY", childLevel6Details.getCategoryRefKey());
														dataMap.put("LEVEL6_OBJECTID", childLevel6Details.getObjectId());
														dataMap.put("MAX_LEVEL", "6");
														
														dataList.add(dataMap);
														dataMap  = null;
													}
													childLevel6Details = null;	
												}
											}
											else
											{
												Map<Object, Object> dataMap= new HashMap<Object , Object>();
												if(null!=categoryDetails.getCategoryName() && !"".equals(categoryDetails.getCategoryName()))
												{
													dataMap.put("LEVEL1_NAME", categoryDetails.getCategoryName());
												}
												dataMap.put("LEVEL1_REFKEY", categoryDetails.getCategoryRefKey());
												dataMap.put("LEVEL1_OBJECTID", categoryDetails.getObjectId());
												
												
												// add Level 2
												if(null!=childLevel2Details.getCategoryName() && !"".equals(childLevel2Details.getCategoryName()))
												{
													dataMap.put("LEVEL2_NAME", childLevel2Details.getCategoryName());
												}
												dataMap.put("LEVEL2_REFKEY", childLevel2Details.getCategoryRefKey());
												dataMap.put("LEVEL2_OBJECTID", childLevel2Details.getObjectId());
												
												// add Level 3
												if(null!=childLevel3Details.getCategoryName() && !"".equals(childLevel3Details.getCategoryName()))
												{
													dataMap.put("LEVEL3_NAME", childLevel3Details.getCategoryName());
												}
												dataMap.put("LEVEL3_REFKEY", childLevel3Details.getCategoryRefKey());
												dataMap.put("LEVEL3_OBJECTID", childLevel3Details.getObjectId());
												
												// add Level 4
												if(null!=childLevel4Details.getCategoryName() && !"".equals(childLevel4Details.getCategoryName()))
												{
													dataMap.put("LEVEL4_NAME", childLevel4Details.getCategoryName());
												}
												dataMap.put("LEVEL4_REFKEY", childLevel4Details.getCategoryRefKey());
												dataMap.put("LEVEL4_OBJECTID", childLevel4Details.getObjectId());
												
												// add Level 5
												if(null!=childLevel5Details.getCategoryName() && !"".equals(childLevel5Details.getCategoryName()))
												{
													dataMap.put("LEVEL5_NAME", childLevel5Details.getCategoryName());
												}
												dataMap.put("LEVEL5_REFKEY", childLevel5Details.getCategoryRefKey());
												dataMap.put("LEVEL5_OBJECTID", childLevel5Details.getObjectId());
												dataMap.put("MAX_LEVEL", "5");
												
												dataList.add(dataMap);
												dataMap  = null;
											}
											childLevel5Details = null;
										}
									}
									else
									{
										Map<Object, Object> dataMap= new HashMap<Object , Object>();
										if(null!=categoryDetails.getCategoryName() && !"".equals(categoryDetails.getCategoryName()))
										{
											dataMap.put("LEVEL1_NAME", categoryDetails.getCategoryName());
										}
										dataMap.put("LEVEL1_REFKEY", categoryDetails.getCategoryRefKey());
										dataMap.put("LEVEL1_OBJECTID", categoryDetails.getObjectId());
										
										
										// add Level 2
										if(null!=childLevel2Details.getCategoryName() && !"".equals(childLevel2Details.getCategoryName()))
										{
											dataMap.put("LEVEL2_NAME", childLevel2Details.getCategoryName());
										}
										dataMap.put("LEVEL2_REFKEY", childLevel2Details.getCategoryRefKey());
										dataMap.put("LEVEL2_OBJECTID", childLevel2Details.getObjectId());
										
										// add Level 3
										if(null!=childLevel3Details.getCategoryName() && !"".equals(childLevel3Details.getCategoryName()))
										{
											dataMap.put("LEVEL3_NAME", childLevel3Details.getCategoryName());
										}
										dataMap.put("LEVEL3_REFKEY", childLevel3Details.getCategoryRefKey());
										dataMap.put("LEVEL3_OBJECTID", childLevel3Details.getObjectId());
										
										// add Level 4
										if(null!=childLevel4Details.getCategoryName() && !"".equals(childLevel4Details.getCategoryName()))
										{
											dataMap.put("LEVEL4_NAME", childLevel4Details.getCategoryName());
										}
										dataMap.put("LEVEL4_REFKEY", childLevel4Details.getCategoryRefKey());
										dataMap.put("LEVEL4_OBJECTID", childLevel4Details.getObjectId());
										dataMap.put("MAX_LEVEL", "4");
										
										
										dataList.add(dataMap);
										dataMap  = null;
									}
									childLevel4Details = null;
								}
							}
							else
							{
								Map<Object, Object> dataMap= new HashMap<Object , Object>();
								if(null!=categoryDetails.getCategoryName() && !"".equals(categoryDetails.getCategoryName()))
								{
									dataMap.put("LEVEL1_NAME", categoryDetails.getCategoryName());
								}
								dataMap.put("LEVEL1_REFKEY", categoryDetails.getCategoryRefKey());
								dataMap.put("LEVEL1_OBJECTID", categoryDetails.getObjectId());
								
								
								// add Level 2
								if(null!=childLevel2Details.getCategoryName() && !"".equals(childLevel2Details.getCategoryName()))
								{
									dataMap.put("LEVEL2_NAME", childLevel2Details.getCategoryName());
								}
								dataMap.put("LEVEL2_REFKEY", childLevel2Details.getCategoryRefKey());
								dataMap.put("LEVEL2_OBJECTID", childLevel2Details.getObjectId());
								
								// add Level 3
								if(null!=childLevel3Details.getCategoryName() && !"".equals(childLevel3Details.getCategoryName()))
								{
									dataMap.put("LEVEL3_NAME", childLevel3Details.getCategoryName());
								}
								dataMap.put("LEVEL3_REFKEY", childLevel3Details.getCategoryRefKey());
								dataMap.put("LEVEL3_OBJECTID", childLevel3Details.getObjectId());
								dataMap.put("MAX_LEVEL", "3");
								
								dataList.add(dataMap);
								dataMap  = null;
							}
							childLevel3Details = null;
						}
					}
					else
					{
						Map<Object, Object> dataMap= new HashMap<Object , Object>();
						if(null!=categoryDetails.getCategoryName() && !"".equals(categoryDetails.getCategoryName()))
						{
							dataMap.put("LEVEL1_NAME", categoryDetails.getCategoryName());
						}
						dataMap.put("LEVEL1_REFKEY", categoryDetails.getCategoryRefKey());
						dataMap.put("LEVEL1_OBJECTID", categoryDetails.getObjectId());
						
						
						// add Level 2
						if(null!=childLevel2Details.getCategoryName() && !"".equals(childLevel2Details.getCategoryName()))
						{
							dataMap.put("LEVEL2_NAME", childLevel2Details.getCategoryName());
						}
						dataMap.put("LEVEL2_REFKEY", childLevel2Details.getCategoryRefKey());
						dataMap.put("LEVEL2_OBJECTID", childLevel2Details.getObjectId());
						dataMap.put("MAX_LEVEL", "2");
						
						dataList.add(dataMap);
						dataMap  = null;
					}
					childLevel2Details = null;
				}
			}
			else
			{
				Map<Object, Object> dataMap= new HashMap<Object , Object>();
				if(null!=categoryDetails.getCategoryName() && !"".equals(categoryDetails.getCategoryName()))
				{
					dataMap.put("LEVEL1_NAME", categoryDetails.getCategoryName());
				}
				dataMap.put("LEVEL1_REFKEY", categoryDetails.getCategoryRefKey());
				dataMap.put("LEVEL1_OBJECTID", categoryDetails.getObjectId());
				dataMap.put("MAX_LEVEL", "1");
				
				dataList.add(dataMap);
				dataMap  = null;
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(StartProcessingImpl.class.getName(), "arrangeCategoriesData()", e);
		}
		return dataList;
	}
	
	public void generateExcel(ArrayList<Map<Object, Object>> dataList, String locale, String catRefKey)
	{
		try
		{
			if(null!=dataList && dataList.size()>0 && null!=locale && !"".equals(locale))
			{
				int maxLevel=0;
				/*
				 * 
				 * to do identify Max level from the List
				 */
				for(int a=0;a<dataList.size();a++)
				{
					Map<Object, Object> dataMap = (Map<Object, Object>)dataList.get(a);
					if(null!=dataMap.get("MAX_LEVEL") && !"".equals(dataMap.get("MAX_LEVEL")))
					{
						Integer definedLevel = new Integer(String.valueOf(dataMap.get("MAX_LEVEL"))).intValue();
						if(maxLevel < definedLevel)
						{
							maxLevel= definedLevel;
						}
						definedLevel = null;
					}
				}
				
				SimpleDateFormat sdf= new SimpleDateFormat("ddMMyyyy_HHmmss");
				String folderDate = sdf.format(new Date());
				
				String path =ApplicationPropertiesUtil.getProperty("REPORTS_PATH");
				/*
				 * Now check inside the reports directory does a directory
				 * exists for wslId
				 */
				File reportsDir = new File(path);
				if (!reportsDir.exists() || !reportsDir.isDirectory()) {
					// create new directory
					reportsDir.mkdir();
				}
				reportsDir = null;
				
				// add Suffix, Locale & CurrentTimestamp to the Name and append to Path
				String name="CAT_"+locale.trim().toUpperCase()+"_"+ folderDate+".xlsx";
				if(!path.endsWith("/"))
				{
					path = path+"/";
				}
				
				File myFile = new File(path + name);
				// Create the workbook instance for XLSX file, KEEP 100 ROWS IN MEMMORY AND RET ON DISK
				SXSSFWorkbook myWorkBook = new SXSSFWorkbook(100);
				// Create a new sheet
				Sheet mySheet = myWorkBook.createSheet("Category Details");
				
				Row headerRow = mySheet.createRow(0);
				headerRow = createHeaderRow(maxLevel, headerRow);
				int rowCount = 0;
				for(int i=0;i<dataList.size();i++)
				{
					Map<Object, Object> dataMap = (Map<Object, Object>)dataList.get(i);
					rowCount++;
					Row row = mySheet.createRow(rowCount);
					Cell cell0 = row.createCell(0);
					Cell cell1 = row.createCell(1);
					Cell cell2 = row.createCell(2);
					Cell cell3 = row.createCell(3);
					Cell cell4 = row.createCell(4);
					Cell cell5 = row.createCell(5);
					Cell cell6 = row.createCell(6);
					Cell cell7 = row.createCell(7);
					Cell cell8 = row.createCell(8);
					Cell cell9 = row.createCell(9);
					Cell cell10 = row.createCell(10);
					Cell cell11 = row.createCell(11);
					Cell cell12 = row.createCell(12);
					Cell cell13 = row.createCell(13);
					Cell cell14 = row.createCell(14);
					Cell cell15 = row.createCell(15);
					Cell cell16 = row.createCell(16);
					Cell cell17 = row.createCell(17);
					Cell cell18 = row.createCell(18);
					Cell cell19 = row.createCell(19);
					Cell cell20 = row.createCell(20);
					Cell cell21 = row.createCell(21);
					Cell cell22 = row.createCell(22);
					Cell cell23 = row.createCell(23);
					
					
					if(null!=dataMap.get("LEVEL1_NAME") && !"".equals(dataMap.get("LEVEL1_NAME")))
					{
						cell0.setCellValue(String.valueOf(dataMap.get("LEVEL1_NAME")));
					}
					if(null!=dataMap.get("LEVEL1_REFKEY") && !"".equals(dataMap.get("LEVEL1_REFKEY")))
					{
						cell1.setCellValue(String.valueOf(dataMap.get("LEVEL1_REFKEY")));
					}
					if(null!=dataMap.get("LEVEL1_OBJECTID") && !"".equals(dataMap.get("LEVEL1_OBJECTID")))
					{
						cell2.setCellValue(String.valueOf(dataMap.get("LEVEL1_OBJECTID")));
					}
					if(null!=dataMap.get("LEVEL2_NAME") && !"".equals(dataMap.get("LEVEL2_NAME")))
					{
						cell3.setCellValue(String.valueOf(dataMap.get("LEVEL2_NAME")));
					}
					if(null!=dataMap.get("LEVEL2_REFKEY") && !"".equals(dataMap.get("LEVEL2_REFKEY")))
					{
						cell4.setCellValue(String.valueOf(dataMap.get("LEVEL2_REFKEY")));
					}
					if(null!=dataMap.get("LEVEL2_OBJECTID") && !"".equals(dataMap.get("LEVEL2_OBJECTID")))
					{
						cell5.setCellValue(String.valueOf(dataMap.get("LEVEL2_OBJECTID")));
					}
					if(null!=dataMap.get("LEVEL3_NAME") && !"".equals(dataMap.get("LEVEL3_NAME")))
					{
						cell6.setCellValue(String.valueOf(dataMap.get("LEVEL3_NAME")));
					}
					if(null!=dataMap.get("LEVEL3_REFKEY") && !"".equals(dataMap.get("LEVEL3_REFKEY")))
					{
						cell7.setCellValue(String.valueOf(dataMap.get("LEVEL3_REFKEY")));
					}
					if(null!=dataMap.get("LEVEL3_OBJECTID") && !"".equals(dataMap.get("LEVEL3_OBJECTID")))
					{
						cell8.setCellValue(String.valueOf(dataMap.get("LEVEL3_OBJECTID")));
					}
					if(null!=dataMap.get("LEVEL4_NAME") && !"".equals(dataMap.get("LEVEL4_NAME")))
					{
						cell9.setCellValue(String.valueOf(dataMap.get("LEVEL4_NAME")));
					}
					if(null!=dataMap.get("LEVEL4_REFKEY") && !"".equals(dataMap.get("LEVEL4_REFKEY")))
					{
						cell10.setCellValue(String.valueOf(dataMap.get("LEVEL4_REFKEY")));
					}
					if(null!=dataMap.get("LEVEL4_OBJECTID") && !"".equals(dataMap.get("LEVEL4_OBJECTID")))
					{
						cell11.setCellValue(String.valueOf(dataMap.get("LEVEL4_OBJECTID")));
					}
					if(null!=dataMap.get("LEVEL5_NAME") && !"".equals(dataMap.get("LEVEL5_NAME")))
					{
						cell12.setCellValue(String.valueOf(dataMap.get("LEVEL5_NAME")));
					}
					if(null!=dataMap.get("LEVEL5_REFKEY") && !"".equals(dataMap.get("LEVEL5_REFKEY")))
					{
						cell13.setCellValue(String.valueOf(dataMap.get("LEVEL5_REFKEY")));
					}
					if(null!=dataMap.get("LEVEL5_OBJECTID") && !"".equals(dataMap.get("LEVEL5_OBJECTID")))
					{
						cell14.setCellValue(String.valueOf(dataMap.get("LEVEL5_OBJECTID")));
					}
					if(null!=dataMap.get("LEVEL6_NAME") && !"".equals(dataMap.get("LEVEL6_NAME")))
					{
						cell15.setCellValue(String.valueOf(dataMap.get("LEVEL6_NAME")));
					}
					if(null!=dataMap.get("LEVEL6_REFKEY") && !"".equals(dataMap.get("LEVEL6_REFKEY")))
					{
						cell16.setCellValue(String.valueOf(dataMap.get("LEVEL6_REFKEY")));
					}
					if(null!=dataMap.get("LEVEL6_OBJECTID") && !"".equals(dataMap.get("LEVEL6_OBJECTID")))
					{
						cell17.setCellValue(String.valueOf(dataMap.get("LEVEL6_OBJECTID")));
					}
					if(null!=dataMap.get("LEVEL7_NAME") && !"".equals(dataMap.get("LEVEL7_NAME")))
					{
						cell18.setCellValue(String.valueOf(dataMap.get("LEVEL7_NAME")));
					}
					if(null!=dataMap.get("LEVEL7_REFKEY") && !"".equals(dataMap.get("LEVEL7_REFKEY")))
					{
						cell19.setCellValue(String.valueOf(dataMap.get("LEVEL7_REFKEY")));
					}
					if(null!=dataMap.get("LEVEL7_OBJECTID") && !"".equals(dataMap.get("LEVEL7_OBJECTID")))
					{
						cell20.setCellValue(String.valueOf(dataMap.get("LEVEL7_OBJECTID")));
					}
					if(null!=dataMap.get("LEVEL8_NAME") && !"".equals(dataMap.get("LEVEL8_NAME")))
					{
						cell21.setCellValue(String.valueOf(dataMap.get("LEVEL8_NAME")));
					}
					if(null!=dataMap.get("LEVEL8_REFKEY") && !"".equals(dataMap.get("LEVEL8_REFKEY")))
					{
						cell22.setCellValue(String.valueOf(dataMap.get("LEVEL8_REFKEY")));
					}
					if(null!=dataMap.get("LEVEL8_OBJECTID") && !"".equals(dataMap.get("LEVEL8_OBJECTID")))
					{
						cell23.setCellValue(String.valueOf(dataMap.get("LEVEL8_OBJECTID")));
					}
					
					
					cell23=null;
					cell22=null;
					cell21=null;
					cell20=null;
					cell19=null;
					cell18=null;
					cell17=null;
					cell16=null;
					cell15=null;
					cell14=null;
					cell13=null;
					cell12=null;
					cell11=null;
					cell10=null;
					cell9=null;
					cell8=null;
					cell7=null;
					cell6=null;
					cell5=null;
					cell4=null;
					cell3=null;
					cell2=null;
					cell1=null;
					cell0=null;
					dataMap=  null;
					row = null;
				}
				
				/*
				 * Before Writing check for size if equals to or more than
				 * 10 MB then generate a file with a extension to it.
				 */

				FileOutputStream os = new FileOutputStream(myFile);
				myWorkBook.write(os);
				System.out.println("Writing on CATEGORY EXPRTED DATA XLSX file Finished ...");
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
				
				name= null;
				path=null;
				folderDate = null;
				sdf = null;
			}
			else
			{
				logger.info("generateExcel :: Either Category List or Locale is null. No Data could be exported in Excel.");
			}
		}
		catch(Exception e)
		{
			Utilities.printStackTraceToLogs(StartProcessingImpl.class.getName(), "generateExcel()", e);
		}
	}
	
	
	private  Row createHeaderRow(int maxLevel, Row headerRow)
	{
		if(maxLevel==1)
		{
			Cell cell0 = headerRow.createCell(0);
			cell0.setCellValue("LEVEL1_NAME");
			Cell cell1 = headerRow.createCell(1);
			cell1.setCellValue("LEVEL1_REFKEY");
			Cell cell2 = headerRow.createCell(2);
			cell2.setCellValue("LEVEL1_OBJECTID");
			
		}
		else if(maxLevel==2)
		{
			Cell cell0 = headerRow.createCell(0);
			cell0.setCellValue("LEVEL1_NAME");
			Cell cell1 = headerRow.createCell(1);
			cell1.setCellValue("LEVEL1_REFKEY");
			Cell cell2 = headerRow.createCell(2);
			cell2.setCellValue("LEVEL1_OBJECTID");
			Cell cell3 = headerRow.createCell(3);
			cell3.setCellValue("LEVEL2_NAME");
			Cell cell4 = headerRow.createCell(4);
			cell4.setCellValue("LEVEL2_REFKEY");
			Cell cell5 = headerRow.createCell(5);
			cell5.setCellValue("LEVEL2_OBJECTID");
		}
		else if(maxLevel==3)
		{
			Cell cell0 = headerRow.createCell(0);
			cell0.setCellValue("LEVEL1_NAME");
			Cell cell1 = headerRow.createCell(1);
			cell1.setCellValue("LEVEL1_REFKEY");
			Cell cell2 = headerRow.createCell(2);
			cell2.setCellValue("LEVEL1_OBJECTID");
			Cell cell3 = headerRow.createCell(3);
			cell3.setCellValue("LEVEL2_NAME");
			Cell cell4 = headerRow.createCell(4);
			cell4.setCellValue("LEVEL2_REFKEY");
			Cell cell5 = headerRow.createCell(5);
			cell5.setCellValue("LEVEL2_OBJECTID");
			Cell cell6 = headerRow.createCell(6);
			cell6.setCellValue("LEVEL3_NAME");
			Cell cell23 = headerRow.createCell(7);
			cell23.setCellValue("LEVEL3_REFKEY");
			Cell cell7 = headerRow.createCell(8);
			cell7.setCellValue("LEVEL2_OBJECTID");
		}
		else if(maxLevel==4)
		{
			Cell cell0 = headerRow.createCell(0);
			cell0.setCellValue("LEVEL1_NAME");
			Cell cell1 = headerRow.createCell(1);
			cell1.setCellValue("LEVEL1_REFKEY");
			Cell cell2 = headerRow.createCell(2);
			cell2.setCellValue("LEVEL1_OBJECTID");
			Cell cell3 = headerRow.createCell(3);
			cell3.setCellValue("LEVEL2_NAME");
			Cell cell4 = headerRow.createCell(4);
			cell4.setCellValue("LEVEL2_REFKEY");
			Cell cell5 = headerRow.createCell(5);
			cell5.setCellValue("LEVEL2_OBJECTID");
			Cell cell6 = headerRow.createCell(6);
			cell6.setCellValue("LEVEL3_NAME");
			Cell cell23 = headerRow.createCell(7);
			cell23.setCellValue("LEVEL3_REFKEY");
			Cell cell7 = headerRow.createCell(8);
			cell7.setCellValue("LEVEL3_OBJECTID");
			Cell cell8 = headerRow.createCell(9);
			cell8.setCellValue("LEVEL4_NAME");
			Cell cell9 = headerRow.createCell(10);
			cell9.setCellValue("LEVEL4_REFKEY");
			Cell cell10 = headerRow.createCell(11);
			cell10.setCellValue("LEVEL4_OBJECTID");
		}
		else if(maxLevel==5)
		{
			Cell cell0 = headerRow.createCell(0);
			cell0.setCellValue("LEVEL1_NAME");
			Cell cell1 = headerRow.createCell(1);
			cell1.setCellValue("LEVEL1_REFKEY");
			Cell cell2 = headerRow.createCell(2);
			cell2.setCellValue("LEVEL1_OBJECTID");
			Cell cell3 = headerRow.createCell(3);
			cell3.setCellValue("LEVEL2_NAME");
			Cell cell4 = headerRow.createCell(4);
			cell4.setCellValue("LEVEL2_REFKEY");
			Cell cell5 = headerRow.createCell(5);
			cell5.setCellValue("LEVEL2_OBJECTID");
			Cell cell6 = headerRow.createCell(6);
			cell6.setCellValue("LEVEL3_NAME");
			Cell cell23 = headerRow.createCell(7);
			cell23.setCellValue("LEVEL3_REFKEY");
			Cell cell7 = headerRow.createCell(8);
			cell7.setCellValue("LEVEL3_OBJECTID");
			Cell cell8 = headerRow.createCell(9);
			cell8.setCellValue("LEVEL4_NAME");
			Cell cell9 = headerRow.createCell(10);
			cell9.setCellValue("LEVEL4_REFKEY");
			Cell cell10 = headerRow.createCell(11);
			cell10.setCellValue("LEVEL4_OBJECTID");
			Cell cell11 = headerRow.createCell(12);
			cell11.setCellValue("LEVEL5_NAME");
			Cell cell12 = headerRow.createCell(13);
			cell12.setCellValue("LEVEL5_REFKEY");
			Cell cell13 = headerRow.createCell(14);
			cell13.setCellValue("LEVEL5_OBJECTID");
		}
		else if(maxLevel==6)
		{
			Cell cell0 = headerRow.createCell(0);
			cell0.setCellValue("LEVEL1_NAME");
			Cell cell1 = headerRow.createCell(1);
			cell1.setCellValue("LEVEL1_REFKEY");
			Cell cell2 = headerRow.createCell(2);
			cell2.setCellValue("LEVEL1_OBJECTID");
			Cell cell3 = headerRow.createCell(3);
			cell3.setCellValue("LEVEL2_NAME");
			Cell cell4 = headerRow.createCell(4);
			cell4.setCellValue("LEVEL2_REFKEY");
			Cell cell5 = headerRow.createCell(5);
			cell5.setCellValue("LEVEL2_OBJECTID");
			Cell cell6 = headerRow.createCell(6);
			cell6.setCellValue("LEVEL3_NAME");
			Cell cell23 = headerRow.createCell(7);
			cell23.setCellValue("LEVEL3_REFKEY");
			Cell cell7 = headerRow.createCell(8);
			cell7.setCellValue("LEVEL3_OBJECTID");
			Cell cell8 = headerRow.createCell(9);
			cell8.setCellValue("LEVEL4_NAME");
			Cell cell9 = headerRow.createCell(10);
			cell9.setCellValue("LEVEL4_REFKEY");
			Cell cell10 = headerRow.createCell(11);
			cell10.setCellValue("LEVEL4_OBJECTID");
			Cell cell11 = headerRow.createCell(12);
			cell11.setCellValue("LEVEL5_NAME");
			Cell cell12 = headerRow.createCell(13);
			cell12.setCellValue("LEVEL5_REFKEY");
			Cell cell13 = headerRow.createCell(14);
			cell13.setCellValue("LEVEL5_OBJECTID");
			Cell cell14 = headerRow.createCell(15);
			cell14.setCellValue("LEVEL6_NAME");
			Cell cell15 = headerRow.createCell(16);
			cell15.setCellValue("LEVEL6_REFKEY");
			Cell cell16 = headerRow.createCell(17);
			cell16.setCellValue("LEVEL6_OBJECTID");
		}
		else if(maxLevel==7)
		{
			Cell cell0 = headerRow.createCell(0);
			cell0.setCellValue("LEVEL1_NAME");
			Cell cell1 = headerRow.createCell(1);
			cell1.setCellValue("LEVEL1_REFKEY");
			Cell cell2 = headerRow.createCell(2);
			cell2.setCellValue("LEVEL1_OBJECTID");
			Cell cell3 = headerRow.createCell(3);
			cell3.setCellValue("LEVEL2_NAME");
			Cell cell4 = headerRow.createCell(4);
			cell4.setCellValue("LEVEL2_REFKEY");
			Cell cell5 = headerRow.createCell(5);
			cell5.setCellValue("LEVEL2_OBJECTID");
			Cell cell6 = headerRow.createCell(6);
			cell6.setCellValue("LEVEL3_NAME");
			Cell cell23 = headerRow.createCell(7);
			cell23.setCellValue("LEVEL3_REFKEY");
			Cell cell7 = headerRow.createCell(8);
			cell7.setCellValue("LEVEL3_OBJECTID");
			Cell cell8 = headerRow.createCell(9);
			cell8.setCellValue("LEVEL4_NAME");
			Cell cell9 = headerRow.createCell(10);
			cell9.setCellValue("LEVEL4_REFKEY");
			Cell cell10 = headerRow.createCell(11);
			cell10.setCellValue("LEVEL4_OBJECTID");
			Cell cell11 = headerRow.createCell(12);
			cell11.setCellValue("LEVEL5_NAME");
			Cell cell12 = headerRow.createCell(13);
			cell12.setCellValue("LEVEL5_REFKEY");
			Cell cell13 = headerRow.createCell(14);
			cell13.setCellValue("LEVEL5_OBJECTID");
			Cell cell14 = headerRow.createCell(15);
			cell14.setCellValue("LEVEL6_NAME");
			Cell cell15 = headerRow.createCell(16);
			cell15.setCellValue("LEVEL6_REFKEY");
			Cell cell16 = headerRow.createCell(17);
			cell16.setCellValue("LEVEL6_OBJECTID");
			Cell cell17 = headerRow.createCell(18);
			cell17.setCellValue("LEVEL7_NAME");
			Cell cell18 = headerRow.createCell(19);
			cell18.setCellValue("LEVEL7_REFKEY");
			Cell cell19 = headerRow.createCell(20);
			cell19.setCellValue("LEVEL7_OBJECTID");
		}
		else if(maxLevel==8)
		{
			Cell cell0 = headerRow.createCell(0);
			cell0.setCellValue("LEVEL1_NAME");
			Cell cell1 = headerRow.createCell(1);
			cell1.setCellValue("LEVEL1_REFKEY");
			Cell cell2 = headerRow.createCell(2);
			cell2.setCellValue("LEVEL1_OBJECTID");
			Cell cell3 = headerRow.createCell(3);
			cell3.setCellValue("LEVEL2_NAME");
			Cell cell4 = headerRow.createCell(4);
			cell4.setCellValue("LEVEL2_REFKEY");
			Cell cell5 = headerRow.createCell(5);
			cell5.setCellValue("LEVEL2_OBJECTID");
			Cell cell6 = headerRow.createCell(6);
			cell6.setCellValue("LEVEL3_NAME");
			Cell cell23 = headerRow.createCell(7);
			cell23.setCellValue("LEVEL3_REFKEY");
			Cell cell7 = headerRow.createCell(8);
			cell7.setCellValue("LEVEL3_OBJECTID");
			Cell cell8 = headerRow.createCell(9);
			cell8.setCellValue("LEVEL4_NAME");
			Cell cell9 = headerRow.createCell(10);
			cell9.setCellValue("LEVEL4_REFKEY");
			Cell cell10 = headerRow.createCell(11);
			cell10.setCellValue("LEVEL4_OBJECTID");
			Cell cell11 = headerRow.createCell(12);
			cell11.setCellValue("LEVEL5_NAME");
			Cell cell12 = headerRow.createCell(13);
			cell12.setCellValue("LEVEL5_REFKEY");
			Cell cell13 = headerRow.createCell(14);
			cell13.setCellValue("LEVEL5_OBJECTID");
			Cell cell14 = headerRow.createCell(15);
			cell14.setCellValue("LEVEL6_NAME");
			Cell cell15 = headerRow.createCell(16);
			cell15.setCellValue("LEVEL6_REFKEY");
			Cell cell16 = headerRow.createCell(17);
			cell16.setCellValue("LEVEL6_OBJECTID");
			Cell cell17 = headerRow.createCell(18);
			cell17.setCellValue("LEVEL7_NAME");
			Cell cell18 = headerRow.createCell(19);
			cell18.setCellValue("LEVEL7_REFKEY");
			Cell cell19 = headerRow.createCell(20);
			cell19.setCellValue("LEVEL7_OBJECTID");
			Cell cell20 = headerRow.createCell(21);
			cell20.setCellValue("LEVEL8_NAME");
			Cell cell21 = headerRow.createCell(22);
			cell21.setCellValue("LEVEL8_REFKEY");
			Cell cell22 = headerRow.createCell(23);
			cell22.setCellValue("LEVEL8_OBJECTID");
		}
		return headerRow;
	}
}
