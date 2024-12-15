package com.auth.meta.model.KibanaResponses;

public class KibanaCreateReportResponse {
	private String path;
	private Job job;
	
	public KibanaCreateReportResponse() {
	}
	
	public KibanaCreateReportResponse(String path, Job job) {
		this.path = path;
		this.job = job;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}
}
