package com.auth.meta.model.KibanaResponses;

public class Job {

	private String id;
    private String index;
    private String jobtype;
    private String created_at;
    private String created_by;
    private Meta meta;
    private String status;
    private int attempts;
    private String migration_version;
    private KibanaCreateReportPayload payload;
    private Object output; // Assuming it's an empty object for now

    // Constructor
    public Job(String id, String index, String jobtype, String created_at, String created_by, Meta meta, String status, int attempts, String migration_version, KibanaCreateReportPayload payload, Object output) {
        this.id = id;
        this.index = index;
        this.jobtype = jobtype;
        this.created_at = created_at;
        this.created_by = created_by;
        this.meta = meta;
        this.status = status;
        this.attempts = attempts;
        this.migration_version = migration_version;
        this.payload = payload;
        this.output = output;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getJobtype() {
		return jobtype;
	}

	public void setJobtype(String jobtype) {
		this.jobtype = jobtype;
	}

	public String getCreated_at() {
		return created_at;
	}

	public void setCreated_at(String created_at) {
		this.created_at = created_at;
	}

	public String getCreated_by() {
		return created_by;
	}

	public void setCreated_by(String created_by) {
		this.created_by = created_by;
	}

	public Meta getMeta() {
		return meta;
	}

	public void setMeta(Meta meta) {
		this.meta = meta;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getAttempts() {
		return attempts;
	}

	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}

	public String getMigration_version() {
		return migration_version;
	}

	public void setMigration_version(String migration_version) {
		this.migration_version = migration_version;
	}

	public KibanaCreateReportPayload getPayload() {
		return payload;
	}

	public void setPayload(KibanaCreateReportPayload payload) {
		this.payload = payload;
	}

	public Object getOutput() {
		return output;
	}

	public void setOutput(Object output) {
		this.output = output;
	}
}
