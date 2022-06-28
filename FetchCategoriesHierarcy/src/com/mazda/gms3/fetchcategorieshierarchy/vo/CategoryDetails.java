package com.mazda.gms3.fetchcategorieshierarchy.vo;

import java.util.ArrayList;

public class CategoryDetails {

	
	private String categoryName=null;
	private String categoryRefKey=null;
	private ArrayList<CategoryDetails> childList = null;
	public String getCategoryName() {
		return categoryName;
	}
	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}
	public String getCategoryRefKey() {
		return categoryRefKey;
	}
	public void setCategoryRefKey(String categoryRefKey) {
		this.categoryRefKey = categoryRefKey;
	}
	public ArrayList<CategoryDetails> getChildList() {
		return childList;
	}
	public void setChildList(ArrayList<CategoryDetails> childList) {
		this.childList = childList;
	}
}
