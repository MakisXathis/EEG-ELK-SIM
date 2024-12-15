package com.auth.meta.model.KibanaResponses;

public class LocatorParam {

	private String id;
    private LocatorParams params;

    public LocatorParam() {	
    }
    
    public LocatorParam(String id, LocatorParams params) {
        this.id = id;
        this.params = params;
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LocatorParams getParams() {
		return params;
	}

	public void setParams(LocatorParams params) {
		this.params = params;
	}
    
}
