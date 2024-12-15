package com.auth.meta.model.KibanaResponses;

import java.util.List;

public class KibanaDashboardsList {

	private int page;
	private int per_page;
	private int total;
	private List<DashboardTemplate> saved_objects;
	
	public KibanaDashboardsList() {
	}
	
	public KibanaDashboardsList(int page, int per_page, int total, List<DashboardTemplate> saved_objects) {
		this.page = page;
		this.per_page = per_page;
		this.total = total;
		this.saved_objects = saved_objects;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	public int getPer_page() {
		return per_page;
	}

	public void setPer_page(int per_page) {
		this.per_page = per_page;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public List<DashboardTemplate> getSaved_objects() {
		return saved_objects;
	}

	public void setSaved_objects(List<DashboardTemplate> saved_objects) {
		this.saved_objects = saved_objects;
	}
}
