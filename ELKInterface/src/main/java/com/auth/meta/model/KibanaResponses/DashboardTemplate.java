package com.auth.meta.model.KibanaResponses;

import java.util.List;

import com.auth.meta.model.Attributes;

public class DashboardTemplate {

	private Attributes attributes;
	private String coreMigrationVersion;
	private String created_at;
	private String id;
	private Boolean managed;
	private List<References> references;
	private String type;
	private String typeMigrationVersion;
	private String updated_at;
	private String version;
	
	public DashboardTemplate() {
	}
	
	public DashboardTemplate(Attributes attributes, String coreMigrationVersion, String created_at, String id, Boolean managed, List<References> references, String type, String typeMigrationVersion, String updated_at, String version) {
		this.attributes = attributes;
		this.coreMigrationVersion = coreMigrationVersion;
		this.created_at = created_at;
		this.id = id;
		this.managed = managed;
		this.references = references;
		this.type = type;
		this.typeMigrationVersion = typeMigrationVersion;
		this.updated_at = updated_at;
		this.version  =version;
	}

	public Attributes getAttributes() {
		return attributes;
	}

	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	public String getCoreMigrationVersion() {
		return coreMigrationVersion;
	}

	public void setCoreMigrationVersion(String coreMigrationVersion) {
		this.coreMigrationVersion = coreMigrationVersion;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Boolean getManaged() {
		return managed;
	}

	public void setManaged(Boolean managed) {
		this.managed = managed;
	}

	public List<References> getReferences() {
		return references;
	}

	public void setReferences(List<References> references) {
		this.references = references;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTypeMigrationVersion() {
		return typeMigrationVersion;
	}

	public void setTypeMigrationVersion(String typeMigrationVersion) {
		this.typeMigrationVersion = typeMigrationVersion;
	}

	public String getUpdated_at() {
		return updated_at;
	}

	public void setUpdated_at(String updated_at) {
		this.updated_at = updated_at;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public String toString() {
		String references = "";
		for (References reference: this.references) {
			references = references+reference.toString();
		}
		return "attributes:"+attributes.toString()+
				"coreMigrationVersion: "+coreMigrationVersion+
				"created_at: "+created_at+
				"id: "+id+
				"managed: "+managed+
				"references: "+references+
				"type:"+type+
				"typeMigrationVersion: "+typeMigrationVersion+
				"updated_at: "+updated_at+
				"version: "+version;
	}
}
