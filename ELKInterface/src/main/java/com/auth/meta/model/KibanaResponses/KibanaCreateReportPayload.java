package com.auth.meta.model.KibanaResponses;

import java.util.List;

public class KibanaCreateReportPayload {

	private String browserTimezone;
    private Layout layout;
    private String objectType;
    private String title;
    private String version;
    private List<LocatorParam> locatorParams;
    private boolean isDeprecated;
    private String forceNow;

    // Constructor
    public KibanaCreateReportPayload(String browserTimezone, Layout layout, String objectType, String title, String version, List<LocatorParam> locatorParams, boolean isDeprecated, String forceNow) {
        this.browserTimezone = browserTimezone;
        this.layout = layout;
        this.objectType = objectType;
        this.title = title;
        this.version = version;
        this.locatorParams = locatorParams;
        this.isDeprecated = isDeprecated;
        this.forceNow = forceNow;
    }

	public String getBrowserTimezone() {
		return browserTimezone;
	}

	public void setBrowserTimezone(String browserTimezone) {
		this.browserTimezone = browserTimezone;
	}

	public Layout getLayout() {
		return layout;
	}

	public void setLayout(Layout layout) {
		this.layout = layout;
	}

	public String getObjectType() {
		return objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<LocatorParam> getLocatorParams() {
		return locatorParams;
	}

	public void setLocatorParams(List<LocatorParam> locatorParams) {
		this.locatorParams = locatorParams;
	}

	public boolean isDeprecated() {
		return isDeprecated;
	}

	public void setDeprecated(boolean isDeprecated) {
		this.isDeprecated = isDeprecated;
	}

	public String getForceNow() {
		return forceNow;
	}

	public void setForceNow(String forceNow) {
		this.forceNow = forceNow;
	}
}
