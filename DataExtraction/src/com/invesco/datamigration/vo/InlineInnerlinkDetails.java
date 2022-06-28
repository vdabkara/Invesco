package com.invesco.datamigration.vo;

public class InlineInnerlinkDetails {

	
	private DocumentDetails documentDetails = null;
	private String innerLinkSourceUrl = null;
	private String innerLinkSourceTag=null;
	
	private String innerLinkDestUrl=null;
	private String innerLinkDestTag= null;
	
	private String processingStatus=null;
	private String errorMessage=null;
	
	private String innerLinkSourceTagLength=null;
	
	
	
	public String getInnerLinkSourceTagLength() {
		return innerLinkSourceTagLength;
	}
	public void setInnerLinkSourceTagLength(String innerLinkSourceTagLength) {
		this.innerLinkSourceTagLength = innerLinkSourceTagLength;
	}
	public DocumentDetails getDocumentDetails() {
		return documentDetails;
	}
	public void setDocumentDetails(DocumentDetails documentDetails) {
		this.documentDetails = documentDetails;
	}
	public String getInnerLinkSourceUrl() {
		return innerLinkSourceUrl;
	}
	public void setInnerLinkSourceUrl(String innerLinkSourceUrl) {
		this.innerLinkSourceUrl = innerLinkSourceUrl;
	}
	public String getInnerLinkSourceTag() {
		return innerLinkSourceTag;
	}
	public void setInnerLinkSourceTag(String innerLinkSourceTag) {
		this.innerLinkSourceTag = innerLinkSourceTag;
	}
	public String getInnerLinkDestUrl() {
		return innerLinkDestUrl;
	}
	public void setInnerLinkDestUrl(String innerLinkDestUrl) {
		this.innerLinkDestUrl = innerLinkDestUrl;
	}
	public String getInnerLinkDestTag() {
		return innerLinkDestTag;
	}
	public void setInnerLinkDestTag(String innerLinkDestTag) {
		this.innerLinkDestTag = innerLinkDestTag;
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
}
