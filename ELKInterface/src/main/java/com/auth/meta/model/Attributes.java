package com.auth.meta.model;

import com.auth.meta.model.KibanaResponses.KibanaSavedObjectMeta;

public class Attributes {

	private String description;
	private KibanaSavedObjectMeta kibanaSavedObjectMeta;
	private String optionsJSON;
	private String panelsJSON;
	private Boolean timeRestore;
	private String title;
	private int version;
	
	public Attributes() {
	}
	
	public Attributes(String description, KibanaSavedObjectMeta kibanaSavedObjectMeta, String optionsJSON, String panelsJSON, Boolean timeRestore, String title, int version) {
		this.description = description;
		this.kibanaSavedObjectMeta = kibanaSavedObjectMeta;
		this.optionsJSON = optionsJSON;
		this.panelsJSON = panelsJSON;
		this.timeRestore = timeRestore;
		this.title = title;
		this.version = version;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public KibanaSavedObjectMeta getKibanaSavedObjectMeta() {
		return kibanaSavedObjectMeta;
	}

	public void setKibanaSavedObjectMeta(KibanaSavedObjectMeta kibanaSavedObjectMeta) {
		this.kibanaSavedObjectMeta = kibanaSavedObjectMeta;
	}

	public String getOptionsJSON() {
		return optionsJSON;
	}

	public void setOptionsJSON(String optionsJSON) {
		this.optionsJSON = optionsJSON;
	}

	public Boolean getTimeRestore() {
		return timeRestore;
	}

	public void setTimeRestore(Boolean timeRestore) {
		this.timeRestore = timeRestore;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getPanelsJSON() {
		return panelsJSON;
	}

	public void setPanelsJSON(String panelsJSON) {
		this.panelsJSON = panelsJSON;
	}
	
	public String toString() {
		return "\"description\":"+description+
				",\"kibanaSavedObjectMeta\":{\"searchSourceJSON\":"+kibanaSavedObjectMeta.getSearchSourceJSON()+
				"}, \"optionsJSON\":"+optionsJSON+
				", \"panelsJSON\":"+panelsJSON+
				",\"timeRestore\":"+timeRestore+
				",\"title\":"+title+
				", \"version\":"+version;
	}
	
}
