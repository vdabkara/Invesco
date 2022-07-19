package com.invesco.datamigration.vo;

import java.sql.Timestamp;
import java.util.List;

public class DocumentDetails {
	
	private String channel=null;
	private String channelRefKey=null;
	private String documentId=null;
	private String locale=null;
	private Timestamp dateAdded=null;
	private Timestamp displayReviewDate=null;
	private Timestamp displayEndDate=null;
	private Timestamp publishDate=null;
	
	private String documentStatus=null;
	private String baseLocale=null;
	private String isTranslation=null;
	private String title=null;
	private String ownerName=null;
	
	
	private String processingStatus=null;
	private String errorMessage=null;
	private String majorVersion=null;
	private String minorVersion=null;
	
	private List<DocumentDetails> versionList = null;
	private String xmlFileSourcePath=null;
	private String xmlFileDestinationPath=null;
	private String xmlFileDestinationName=null;
	
	private String allAttachmentsMoved=null;
	private String allInlineImagesMoved=null;
	
	private List<AttachmentDetails> attachmentsList=null;
	private List<InlineImageDetails> inlineImagesList = null;
	private List<InlineInnerlinkDetails> inlineInnerLinksList=null;
	
	private List<CategoryDetails> categoryList = null;
	private List<ViewDetails> viewList  =null;
	
	private String xmlContent=null;
	
	
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public String getBaseLocale() {
		return baseLocale;
	}

	public void setBaseLocale(String baseLocale) {
		this.baseLocale = baseLocale;
	}

	public String getIsTranslation() {
		return isTranslation;
	}

	public void setIsTranslation(String isTranslation) {
		this.isTranslation = isTranslation;
	}

	public List<CategoryDetails> getCategoryList() {
		return categoryList;
	}

	public void setCategoryList(List<CategoryDetails> categoryList) {
		this.categoryList = categoryList;
	}

	public List<ViewDetails> getViewList() {
		return viewList;
	}

	public void setViewList(List<ViewDetails> viewList) {
		this.viewList = viewList;
	}

	public List<InlineInnerlinkDetails> getInlineInnerLinksList() {
		return inlineInnerLinksList;
	}

	public void setInlineInnerLinksList(List<InlineInnerlinkDetails> inlineInnerLinksList) {
		this.inlineInnerLinksList = inlineInnerLinksList;
	}

	public String getAllInlineImagesMoved() {
		return allInlineImagesMoved;
	}

	public void setAllInlineImagesMoved(String allInlineImagesMoved) {
		this.allInlineImagesMoved = allInlineImagesMoved;
	}

	public List<InlineImageDetails> getInlineImagesList() {
		return inlineImagesList;
	}

	public void setInlineImagesList(List<InlineImageDetails> inlineImagesList) {
		this.inlineImagesList = inlineImagesList;
	}

	public String getAllAttachmentsMoved() {
		return allAttachmentsMoved;
	}

	public void setAllAttachmentsMoved(String allAttachmentsMoved) {
		this.allAttachmentsMoved = allAttachmentsMoved;
	}

	public List<AttachmentDetails> getAttachmentsList() {
		return attachmentsList;
	}

	public void setAttachmentsList(List<AttachmentDetails> attachmentsList) {
		this.attachmentsList = attachmentsList;
	}

	public String getXmlFileSourcePath() {
		return xmlFileSourcePath;
	}

	public void setXmlFileSourcePath(String xmlFileSourcePath) {
		this.xmlFileSourcePath = xmlFileSourcePath;
	}

	public String getXmlContent() {
		return xmlContent;
	}

	public void setXmlContent(String xmlContent) {
		this.xmlContent = xmlContent;
	}

	public String getXmlFileDestinationPath() {
		return xmlFileDestinationPath;
	}

	public void setXmlFileDestinationPath(String xmlFileDestinationPath) {
		this.xmlFileDestinationPath = xmlFileDestinationPath;
	}

	public String getXmlFileDestinationName() {
		return xmlFileDestinationName;
	}

	public void setXmlFileDestinationName(String xmlFileDestinationName) {
		this.xmlFileDestinationName = xmlFileDestinationName;
	}

	public String getChannelRefKey() {
		return channelRefKey;
	}
	
	public void setChannelRefKey(String channelRefKey) {
		this.channelRefKey = channelRefKey;
	}
	
	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public Timestamp getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(Timestamp dateAdded) {
		this.dateAdded = dateAdded;
	}

	public Timestamp getDisplayReviewDate() {
		return displayReviewDate;
	}

	public void setDisplayReviewDate(Timestamp displayReviewDate) {
		this.displayReviewDate = displayReviewDate;
	}

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public Timestamp getDisplayEndDate() {
		return displayEndDate;
	}

	public void setDisplayEndDate(Timestamp displayEndDate) {
		this.displayEndDate = displayEndDate;
	}

	public Timestamp getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(Timestamp publishDate) {
		this.publishDate = publishDate;
	}

	public String getDocumentStatus() {
		return documentStatus;
	}

	public void setDocumentStatus(String documentStatus) {
		this.documentStatus = documentStatus;
	}

	public String getProcessingStatus() {
		return processingStatus;
	}

	public void setProcessingStatus(String processingStatus) {
		this.processingStatus = processingStatus;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getMajorVersion() {
		return majorVersion;
	}

	public void setMajorVersion(String majorVersion) {
		this.majorVersion = majorVersion;
	}

	public String getMinorVersion() {
		return minorVersion;
	}

	public void setMinorVersion(String minorVersion) {
		this.minorVersion = minorVersion;
	}

	
	public List<DocumentDetails> getVersionList() {
		return versionList;
	}

	public void setVersionList(List<DocumentDetails> versionList) {
		this.versionList = versionList;
	}
}