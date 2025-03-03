package com.auth.meta.model.KibanaResponses;

public class KibanaCreateDashboardResponse {

	private int successCount;
	private boolean success;
	private String[] warnings;
	private successResult[] successResults;
	
	public KibanaCreateDashboardResponse() {
	}
	
	public KibanaCreateDashboardResponse(int successCount, boolean success, String[] warnings, successResult[] successResults) {
		this.successCount = successCount;
		this.success = success;
		this.warnings = warnings;
		this.successResults = successResults;
	}

	public int getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(int successCount) {
		this.successCount = successCount;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String[] getWarnings() {
		return warnings;
	}

	public void setWarnings(String[] warnings) {
		this.warnings = warnings;
	}

	public successResult[] getSuccessResults() {
		return successResults;
	}

	public void setSuccessResults(successResult[] successResults) {
		this.successResults = successResults;
	}
	
}
