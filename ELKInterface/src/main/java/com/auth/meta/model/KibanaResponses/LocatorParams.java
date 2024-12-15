package com.auth.meta.model.KibanaResponses;

public class LocatorParams {

	private String dashboardId;
    private boolean preserveSavedFilters;
    private TimeRange timeRange;
    private boolean useHash;
    private String viewMode;

    public LocatorParams() {
    }
    
    public LocatorParams(String dashboardId, boolean preserveSavedFilters, TimeRange timeRange, boolean useHash, String viewMode) {
        this.dashboardId = dashboardId;
        this.preserveSavedFilters = preserveSavedFilters;
        this.timeRange = timeRange;
        this.useHash = useHash;
        this.viewMode = viewMode;
    }

	public String getDashboardId() {
		return dashboardId;
	}

	public void setDashboardId(String dashboardId) {
		this.dashboardId = dashboardId;
	}

	public boolean isPreserveSavedFilters() {
		return preserveSavedFilters;
	}

	public void setPreserveSavedFilters(boolean preserveSavedFilters) {
		this.preserveSavedFilters = preserveSavedFilters;
	}

	public TimeRange getTimeRange() {
		return timeRange;
	}

	public void setTimeRange(TimeRange timeRange) {
		this.timeRange = timeRange;
	}

	public boolean isUseHash() {
		return useHash;
	}

	public void setUseHash(boolean useHash) {
		this.useHash = useHash;
	}

	public String getViewMode() {
		return viewMode;
	}

	public void setViewMode(String viewMode) {
		this.viewMode = viewMode;
	}
}
