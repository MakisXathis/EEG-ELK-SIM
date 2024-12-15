package com.auth.meta.model.KibanaResponses;

public class Layout {

	private Dimensions dimensions;
    private String id;

    public Layout() {
    }
    
    public Layout(Dimensions dimensions, String id) {
        this.dimensions = dimensions;
        this.id = id;
    }

	public Dimensions getDimensions() {
		return dimensions;
	}

	public void setDimensions(Dimensions dimensions) {
		this.dimensions = dimensions;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
}
