package com.auth.meta.config;

import java.io.FileReader;
import java.io.IOException;

import com.auth.meta.model.Server;
import com.google.gson.Gson;

public class ConfigurationUtils {

	public static Configuration configFileReader(String path) {
		Gson gson = new Gson();
		
		Configuration config = null;

        try (FileReader reader = new FileReader(path)) {
            // Parse JSON file to Configuration object
            config = gson.fromJson(reader, Configuration.class);

        } catch (IOException e) {
            System.out.println("Error reading configuration file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error parsing configuration file: " + e.getMessage());
        }
		return config;
	}
	
	public static Boolean checkConfiguration(Configuration config) {
		
		if (config == null) {
			System.out.println("Configuration was not loaded properly. Please check configuration file...");
			return false;
		}
		
		if (config.getServer().getIp().isBlank()) {
			System.out.println("Server IP is empty. Please check configuration file...");
			return false;
		}
		
		if (config.getServer().getSsl_enabled() == null) {
			System.out.println("SSL is not setup for commucation with the server is empty. Please check configuration file...");
			return false;
		}
		
		if (config.getServer().getCredentials().getUsername().isBlank()) {
			System.out.println("Username for server authentication is empty. Please check configuration file...");
			return false;
		}
		
		if (config.getServer().getCredentials().getPassword().isBlank()) {
			System.out.println("Password for server authentication is empty. Please check configuration file...");
			return false;
		}
		
		
		return true;
	}
}
