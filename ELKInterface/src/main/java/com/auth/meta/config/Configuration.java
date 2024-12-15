package com.auth.meta.config;

import com.auth.meta.model.Server;

public class Configuration {

	private Server server;
	
	public Configuration() {
	}
	
	public Configuration(Server server) {
		this.server = server;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}
}
