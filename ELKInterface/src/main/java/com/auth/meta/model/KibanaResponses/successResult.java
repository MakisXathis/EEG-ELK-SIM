package com.auth.meta.model.KibanaResponses;

public class successResult {

	private String type;
	private String id;
	private successResultMeta meta;
	private boolean managed;
	private String destinationId;
	
	public successResult(){	
	}
	
	public successResult(String type, String id, successResultMeta meta, boolean managed, String destinationId) {
		this.type = type;
		this.id = id;
		this.meta = meta;
		this.managed = managed;
		this.destinationId = destinationId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public successResultMeta getMeta() {
		return meta;
	}

	public void setMeta(successResultMeta meta) {
		this.meta = meta;
	}

	public boolean isManaged() {
		return managed;
	}

	public void setManaged(boolean managed) {
		this.managed = managed;
	}

	public String getDestinationId() {
		return destinationId;
	}

	public void setDestinationId(String destinationId) {
		this.destinationId = destinationId;
	}
	
}
