package com.auth.meta.model;

public class Server {
	private String ip;
	private int port;
	private Boolean ssl_enabled;
	private int timeout;
	private Credentials credentials;
	
	public Server() {
	}
	
	public Server(String ip, int port, Boolean ssl_enabled, int timeout, Credentials credentials) {
		this.ip = ip;
		this.port = port;
		this.ssl_enabled = ssl_enabled;
		this.timeout = timeout;
		this.credentials = credentials;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Boolean getSsl_enabled() {
		return ssl_enabled;
	}

	public void setSsl_enabled(Boolean ssl_enabled) {
		this.ssl_enabled = ssl_enabled;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public Credentials getCredentials() {
		return credentials;
	}

	public void setCredentials(Credentials credentials) {
		this.credentials = credentials;
	}

}
