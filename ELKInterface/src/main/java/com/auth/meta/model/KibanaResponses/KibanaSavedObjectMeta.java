package com.auth.meta.model.KibanaResponses;

public class KibanaSavedObjectMeta {

	private String searchSourceJSON;
	
	public KibanaSavedObjectMeta() {
	}
	
	public KibanaSavedObjectMeta(String searchSourceJSON) {
		this.searchSourceJSON = searchSourceJSON;
	}

	public String getSearchSourceJSON() {
		return searchSourceJSON;
	}

	public void setSearchSourceJSON(String searchSourceJSON) {
		this.searchSourceJSON = searchSourceJSON;
	}
}
