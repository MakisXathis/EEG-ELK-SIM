package com.auth.meta;

import java.net.Socket;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.auth.meta.config.Configuration;
import com.auth.meta.config.ConfigurationUtils;


@SpringBootApplication
public class ELKInterfaceClient {
	public static void main(String[] args) {
		SpringApplication.run(ELKInterfaceClient.class, args);
		
		Configuration config = null;
		if (args.length == 0) {
			config = ConfigurationUtils.configFileReader("config.json");
		}else {
			config = ConfigurationUtils.configFileReader(args[0]);
		}
		
		if (!ConfigurationUtils.checkConfiguration(config)) {
			System.out.println("There was an error while verifying the configuration. System is exiting...");
			System.exit(1);
		}
		
		//Set up HttpClient that trusts all certificates
		CloseableHttpClient client = null;
        try {
			client = createAllTrustingHTTPClient(config.getServer().getTimeout());
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			System.out.println("There was an error while creating the HTTP Client. Error: "+e);
		}
	}
	 
	 public static CloseableHttpClient createAllTrustingHTTPClient(int timeout) throws NoSuchAlgorithmException, KeyManagementException {
		 TrustManager MOCK_TRUST_MANAGER = new X509ExtendedTrustManager() {
		   @Override
		   public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		       return new java.security.cert.X509Certificate[0];
		   }
			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException { }
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket)
					throws CertificateException {}
			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket)
					throws CertificateException {}
			@Override
			public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
					throws CertificateException {}
			@Override
			public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine)
					throws CertificateException {}
		};

	    // Initialize the SSLContext to use the all-trusting TrustManager
	    SSLContext sslContext = SSLContext.getInstance("TLS");
	    sslContext.init(null, new TrustManager[]{MOCK_TRUST_MANAGER}, new java.security.SecureRandom());
	    
	    // Create an SSL socket factory with the custom SSLContext
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);

        // Create an HttpClient with the SSL socket factory and trust manager
        return HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .setConnectionTimeToLive(timeout, TimeUnit.SECONDS)
                .build();
	  	}
    
    @Bean
    public Configuration configuration(ApplicationArguments args) {
    	Configuration config;
        if (args.getSourceArgs().length == 0) {
            config = ConfigurationUtils.configFileReader("config.json");
        } else {
            config = ConfigurationUtils.configFileReader(args.getSourceArgs()[0]);
        }
        if (!ConfigurationUtils.checkConfiguration(config)) {
			System.out.println("There was an error while verifying the configuration. System is exiting...");
			System.exit(1);
		}
        return config;
    }
    
    @Bean
    public CloseableHttpClient httpClient(Configuration config) {
        try {
            return createAllTrustingHTTPClient(config.getServer().getTimeout());
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Error creating HTTP client", e);
        }
    }
    
    @Bean
    public String os() {
    	return System.getProperty("os.name").toLowerCase();
    }
}
