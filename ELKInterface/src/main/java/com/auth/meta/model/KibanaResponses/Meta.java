package com.auth.meta.model.KibanaResponses;

public class Meta {

	private String objectType;
    private String layout;
    private boolean isDeprecated;

    public Meta() {
    }
    
    public Meta(String objectType, String layout, boolean isDeprecated) {
        this.objectType = objectType;
        this.layout = layout;
        this.isDeprecated = isDeprecated;
    }

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public String getLayout() {
		return layout;
	}

	public void setLayout(String layout) {
		this.layout = layout;
	}

	public boolean isDeprecated() {
		return isDeprecated;
	}

	public void setDeprecated(boolean isDeprecated) {
		this.isDeprecated = isDeprecated;
	}
}
