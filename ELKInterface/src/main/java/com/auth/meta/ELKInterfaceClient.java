package com.auth.meta;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.auth.meta.config.Configuration;
import com.auth.meta.config.ConfigurationUtils;
import com.auth.meta.model.KibanaResponses.DashboardTemplate;
import com.auth.meta.model.KibanaResponses.KibanaCreateReportResponse;
import com.auth.meta.model.KibanaResponses.KibanaDashboardsList;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ELKInterfaceClient {
	public static void main(String[] args) {
		
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
		
		// Set up HttpClient that trusts all certificates
		CloseableHttpClient client = null;
        try {
			client = createAllTrustingHTTPClient(config.getServer().getTimeout());
		} catch (KeyManagementException | NoSuchAlgorithmException e) {
			System.out.println("There was an error while creating the HTTP Client. Error: "+e);
		}
		
		String currentDirectory = System.getProperty("user.dir");
		
		DashboardTemplate template = null;
		
		//Get the start and end dates
		Scanner scanner = new Scanner(System.in);
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        
        while (true) {
        	LocalDate startDate = null;
            LocalDate endDate = null;
            Long startDateDaysDifference = null;
            Long endDateDaysDifference = null;
            List<Integer> electrodes = new ArrayList<>();

            // Request and validate starting date
            while (startDate == null) {
                System.out.print("Enter the starting date (dd-MM-yyyy): ");
                String startDateInput = scanner.nextLine();
                try {
                    startDate = LocalDate.parse(startDateInput, dateFormatter);
                    startDateDaysDifference = calculateDaysDifference(startDate);
                    if (startDateDaysDifference < 0) {
                    	System.out.println("Start date cannot be in the future. Please input a valid start date.");
                    	startDate = null;
                    }
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format. Please use dd-MM-yyyy.");
                }
            }

            // Request and validate ending date
            while (endDate == null) {
                System.out.print("Enter the ending date (dd-MM-yyyy): ");
                String endDateInput = scanner.nextLine();
                try {
                    endDate = LocalDate.parse(endDateInput, dateFormatter);
                    if (endDate.isBefore(startDate)) {
                        System.out.println("End date cannot be before start date. Please try again.");
                        endDate = null;
                    }
                    endDateDaysDifference = calculateDaysDifference(endDate);
                    if (endDateDaysDifference < 0) {
                    	System.out.println("End date cannot be in the future. Please input a valid end date.");
                    	endDate = null;
                    }
                } catch (DateTimeParseException e) {
                    System.out.println("Invalid date format. Please use dd-MM-yyyy.");
                }
            }

            // Request and validate Electrodes between 1 and 16
            String[] electrodesStrings = null;
            while (electrodes.isEmpty()) {
                System.out.print("Enter the Electrodes between 1 and 16, separated by commas: ");
                String electrodesInput = scanner.nextLine();
                electrodesStrings = electrodesInput.split(",");
                boolean isValid = true;
                
                if (checkDuplicateElectrodes(electrodesStrings))
                	System.out.println("invalid input. Each Electrode can be entered only once.");

                for (String electrodeString : electrodesStrings) {
                    try {
                        int number = Integer.parseInt(electrodeString.trim());
                        if (number < 1 || number > 16) {
                            System.out.println("Invalid number: " + number + ". Please ensure all Electrodes are between 1 and 16.");
                            isValid = false;
                            break;
                        }
                        electrodes.add(number);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid input: " + electrodeString + ". Please enter only integers.");
                        isValid = false;
                        break;
                    }
                }

                if (!isValid) {
                	electrodes.clear(); // Clear the list and re-prompt if invalid
                }
            }
            
            try {
    			template = readTemplate("dashboards\\dashboard_"+electrodesStrings.length+".ndjson");
    		}catch (Exception e) {
    			System.exit(1);
    		}
            
            System.out.print("Enter the description of the dashboard: ");
            String dashboardDescription = scanner.nextLine();
            
            System.out.print("Enter the title of the dashboard: ");
            String dashboardTitle = scanner.nextLine();
            
            System.out.println("Enter the path to save the dashboard as png:");
            String pngPath = scanner.nextLine();

            // Display the inputs
            System.out.println("\nSummary:");
            System.out.println("Starting Date: " + startDate.format(dateFormatter));
            System.out.println("Ending Date: " + endDate.format(dateFormatter));
            System.out.println("Selected Numbers: " + electrodes);

            template = updateTemplate(template, startDateDaysDifference, endDateDaysDifference, electrodes, dashboardDescription, dashboardTitle);
            
            String newFilePath = "dashboards\\new_dashboard_"+electrodes.size()+".ndjson";
            writeTemplateToNDJSON(template, newFilePath);
            //writeTemplateToNDJSON(newFilePath,"new_"+newFilePath);
            
            
            if (createKibanaDashboard(client, config.getServer().getIp(), config.getServer().getPort(), config.getServer().getSsl_enabled(), config.getServer().getCredentials().getUsername(), config.getServer().getCredentials().getPassword(),currentDirectory+"\\"+newFilePath)) {
            	System.out.println("Done!");
            }
            
            
            String kibanaDashboards = getKibanaDashboards(client, config.getServer().getIp(), config.getServer().getPort(), config.getServer().getSsl_enabled(), config.getServer().getCredentials().getUsername(), config.getServer().getCredentials().getPassword());
            
            
            String dashboardID = getDashboardID(dashboardDescription, kibanaDashboards);
            if(dashboardID.isEmpty())
            	System.out.println("No dashboard ID was extracted from the Kibana Dashboard list");
            else
            	System.out.println("Dashboard ID found in Kibana: "+dashboardID);
            	
            String reportPath = createkibanaPNGReport(client, config.getServer().getIp(), config.getServer().getPort(), config.getServer().getSsl_enabled(), config.getServer().getCredentials().getUsername(), config.getServer().getCredentials().getPassword(), dashboardID, startDateDaysDifference, endDateDaysDifference, dashboardDescription);
            if(reportPath.isEmpty())
            	System.out.println("No Report API download endpoint was retrieved from the request to create Kibana PNG report. Please check above error.");
            else
            	System.out.println("Reportpath returned from Kibana: "+reportPath);
            
            try {
            	System.out.println("Waiting for report creation...");
				Thread.sleep(20000); //wait until the report is complete
			} catch (InterruptedException e) {
				System.out.println("System was interrupted. Exiting application...");
			}
            
            if(getKibanaReport(client, config.getServer().getIp(), config.getServer().getPort(), config.getServer().getSsl_enabled(), config.getServer().getCredentials().getUsername(), config.getServer().getCredentials().getPassword(), reportPath, pngPath+"\\"+dashboardTitle+".png")) {
            	
            	showPNG(pngPath+"\\"+dashboardTitle+".png");
            	
            	System.out.println("Starting over!");
            }
        }
	}
	
	 public static long calculateDaysDifference(LocalDate inputDate) {
	        LocalDate currentDate = LocalDate.now();
	        return ChronoUnit.DAYS.between(inputDate, currentDate);
	 }
	 
	 public static DashboardTemplate readTemplate(String filePath) throws IOException,JsonSyntaxException  {
		 Gson gson = new Gson();

	     try (FileReader reader = new FileReader(filePath)) {
	         // Parse the JSON file into the User class
	    	 DashboardTemplate template = gson.fromJson(reader, DashboardTemplate.class);
	    	 return template;
	     } catch (IOException e) {
	        System.err.println("Error reading the file: " + e.getMessage()+". Please check that the file exists and try again.");
	        throw e;
	     } catch (JsonSyntaxException e) {
	        System.err.println("Invalid JSON format: " + e.getMessage()+". Please check the setup of the file and try again");
	        throw e;
	     }
	 }
	 
	 public static DashboardTemplate updateTemplate(DashboardTemplate template, Long startDate, Long endDate, List<Integer> electrodes, String dashboardDescription, String dashboardTitle) {
		 template.getAttributes().setDescription(dashboardDescription);;
		 template.getAttributes().setTitle(dashboardTitle);
		 
		 template.getAttributes().getKibanaSavedObjectMeta().setSearchSourceJSON(template.getAttributes().getKibanaSavedObjectMeta().getSearchSourceJSON().replaceAll("now-29d", "now-"+startDate+"d"));
		 template.getAttributes().getKibanaSavedObjectMeta().setSearchSourceJSON(template.getAttributes().getKibanaSavedObjectMeta().getSearchSourceJSON().replaceAll("now-1d", "now-"+endDate+"d"));
		 
		 int i = 1;
		 for (Integer electrode: electrodes) {
			 template.getAttributes().setPanelsJSON(template.getAttributes().getPanelsJSON().replaceFirst("Median of Electrode "+i, "Median of Electrode "+electrode));
			 template.getAttributes().setPanelsJSON(template.getAttributes().getPanelsJSON().replaceFirst("sourceField\":\"Electrode "+i+"\"", "sourceField\":\"Electrode "+electrode+"\""));
			 template.getAttributes().setPanelsJSON(template.getAttributes().getPanelsJSON().replaceFirst("1 Electrode through time", dashboardTitle));
			 i++;
		 }
		 
		 template.getAttributes().setTitle(dashboardTitle);
		 return template;
	 }
	
	 public static void writeTemplateToNDJSON(DashboardTemplate template, String filePath) {
		 try (FileWriter writer = new FileWriter(filePath)) {
	            Gson gson = new Gson();
	            gson.toJson(template, writer);
	        } catch (IOException e) {
	            System.err.println("An error occurred while writing NDJSON to file: " + e.getMessage());
	            System.exit(1);
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
	 
	 public static boolean createKibanaDashboard(CloseableHttpClient client, String ip, int port, boolean isHttps, String username, String password, String filePath) {
		 String ssl = "";
		 
		 if(isHttps)
			 ssl = "https";
		 else
			 ssl="http";
		 
         // Create a POST request
         HttpPost postRequest = new HttpPost(ssl + "://" + ip + ":" + port + "/api/saved_objects/_import?createNewCopies=true");

         // Set up Basic Authentication header
         String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
         postRequest.setHeader("Authorization", authHeader);
         postRequest.setHeader("kbn-xsrf", "true");
         

         // Create the multipart entity
         File file = new File(filePath);
         FileBody fileBody = new FileBody(file); // The file to upload
         HttpEntity entity = MultipartEntityBuilder.create()
	                    .addPart("file", fileBody) // Adding the file to the request
	                    .build();

	     // Set the entity to the post request
	     postRequest.setEntity(entity);

	     try {
			client.execute(postRequest);
			
		    return true;
		} catch (IOException e) {
			System.out.println("There was an error while creating the Kibana Dashboard under file: "+filePath+". Error: "+e);
			return false;
		}
	 }
	 
	 //Check if duplicate electrodes were inserted in the request
	 //in order to avoid issues with the template setup
	 public static boolean checkDuplicateElectrodes(String[] electrodesStrings) {
	        Set<String> seen = new HashSet<>();

	        for (String electrode : electrodesStrings) {
	            if (!seen.add(electrode)) { // If add() returns false, it's a duplicate
	                return true;
	            }
	        }
	        return false;
	 }
	 
	 //Gets a list of all the KibanaDashboards (limited to 100)
	 public static String getKibanaDashboards(CloseableHttpClient client, String ip, int port, boolean isHttps, String username, String password) {
		 System.out.println("Getting list of Kibana Dashboards...");
		 String ssl = "";
		 
		 if(isHttps)
			 ssl = "https";
		 else
			 ssl="http";
		 
         // Create a GET request
         HttpGet getRequest = new HttpGet(ssl + "://" + ip + ":" + port + "/api/saved_objects/_find?type=dashboard&per_page=100");
         
         // Set up Basic Authentication header
         String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
         getRequest.setHeader("Authorization", authHeader);
         
         CloseableHttpResponse response;
		 try {
			response = client.execute(getRequest);
			
			// Get the response
		     HttpEntity responseEntity = response.getEntity();
		     String responseString = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
		     return responseString;
		} catch (IOException e) {
			System.out.println("There was an error while trying to retrieve the list of dashboards from Kibana. Error: "+e);
			return "";
		}
	 }
	 
	 //Receives as input a list of KibanaDashboards and a dashboardDescription and returns the dashboardID of the dashboard based on its description
	 public static String getDashboardID(String dashboardDescription, String kibanaDashboards) {
		System.out.println("Getting dashboard ID based on Dashboard description...");
		Gson gson = new Gson();
		KibanaDashboardsList dashboards = gson.fromJson(kibanaDashboards, KibanaDashboardsList.class);
		String dashboardID = "";
		
		for (DashboardTemplate dashboard: dashboards.getSaved_objects()) {
			if (dashboard.getAttributes().getDescription().equals(dashboardDescription)) {
				dashboardID = dashboard.getId();
				break;
			}
		}
		return dashboardID;
	 }
	 
	 //Receives a a Kibana report API download endpoint and downloads and creates the generated png to a local file
	 public static boolean getKibanaReport(CloseableHttpClient client, String ip, int port, boolean isHttps, String username, String password, String reportPath, String outputFilePath) {
		 System.out.println("Downloading Report from Kibana...");
		 
		 String ssl = "";
		 
		 if(isHttps)
			 ssl = "https";
		 else
			 ssl="http";
		 
         // Create a POST request
         HttpGet getRequest = new HttpGet(ssl + "://" + ip + ":" + port + reportPath);
         
         // Set up Basic Authentication header
         String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
         getRequest.setHeader("Authorization", authHeader);
         
         try (CloseableHttpResponse response = client.execute(getRequest)) {
             // Check if the response is successful (HTTP 200)
             int statusCode = response.getStatusLine().getStatusCode();
             if (statusCode != 200) {
                 System.err.println("Failed to get report. HTTP Status Code: " + statusCode);
                 return false;
             }
             
             // Save response content to a file
             HttpEntity entity = response.getEntity();
             if (entity != null) {
                 try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                     entity.writeTo(fos);
                 }
                 System.out.println("Report saved successfully to: " + outputFilePath);
                 return true;
             }
         } catch (IOException e) {
             System.out.println("There was an error while trying to get the Kibana report for path: "+reportPath+". Error: "+e);
             return false;
         }
		 return true;
	 }
	 
	 //Function that creates the Kibana report based on the created dashboard and returns the API download endpoint
    public static String createkibanaPNGReport(CloseableHttpClient client, String ip, int port, boolean isHttps, String username, String password, String dashboardID, Long startDate, Long endDate, String dashboardDescription) {
	    System.out.println("Creating Kibana PNG Report...");
		
		String reportPath="/api/reporting/generate/pngV2?jobParams=%28browserTimezone%3AEurope%2FAthens%2Clayout%3A%28dimensions%3A%28height%3A428%2Cwidth%3A2048%29%2Cid%3Apreserve_layout%29%2ClocatorParams%3A%28id%3ADASHBOARD_APP_LOCATOR%2Cparams%3A%28dashboardId%3A%27dcb64b8e-3238-4af0-975d-1adba952337a%27%2CpreserveSavedFilters%3A%21t%2CtimeRange%3A%28from%3Anow-30d%2Cto%3Anow-3d%29%2CuseHash%3A%21f%2CviewMode%3Aview%29%29%2CobjectType%3Adashboard%2Ctitle%3A%278%20Electrode%20through%20time%27%2Cversion%3A%278.12.2%27%29";
		
		reportPath = reportPath.replaceAll("dcb64b8e-3238-4af0-975d-1adba952337a", dashboardID);
		reportPath = reportPath.replaceAll("from%3Anow-30d", "from%3Anow-"+startDate+"d");
		reportPath = reportPath.replaceAll("to%3Anow-3d", "to%3Anow-"+endDate+"d");
		reportPath = reportPath.replaceAll("8%20Electrode%20through%20time", dashboardDescription.replaceAll(" ", "%20"));
		
		System.out.println("Kibana PNG Report path: "+reportPath);
		
		String ssl = "";
		
		if(isHttps)
		 ssl = "https";
		else
		 ssl="http";
		 
        // Create a POST request
		HttpPost postRequest = new HttpPost(ssl + "://" + ip + ":" + port + reportPath);
         
        // Set up Basic Authentication header
        String authHeader = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        postRequest.setHeader("Authorization", authHeader);
        postRequest.setHeader("kbn-xsrf", "true");
		 
        try (CloseableHttpResponse response = client.execute(postRequest)) {
            // Check if the response is successful (HTTP 200)
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                System.err.println("Failed to get report. HTTP Status Code: " + statusCode);
                return "";
            }
            
           String kibanaResponseString = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
           Gson gson = new Gson();
           KibanaCreateReportResponse kibanaResponse = gson.fromJson(kibanaResponseString, KibanaCreateReportResponse.class);
           
           return kibanaResponse.getPath();
           
        } catch (IOException e) {
         System.out.println("There was an error while trying to create the Kibana report for dashboard: "+dashboardID+". Error: "+e);
            return "";
        }
	}
    
    //Function that receives the path of a png image and presents it to the user opening a new window
    public static void showPNG(String filePath) {
        try {
        	String[] windowTitle = filePath.split("\\\\");
        	
            // Load the image
            File file = new File(filePath);
            BufferedImage image = ImageIO.read(file);

            // Create a JFrame to display the image
            JFrame frame = new JFrame(windowTitle[windowTitle.length-1]);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(image.getWidth(), image.getHeight());

            // Create a JPanel to hold the image
            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (image != null) {
                    	int panelWidth = getWidth();
                        int panelHeight = getHeight();

                        // Image dimensions
                        int imageWidth = image.getWidth();
                        int imageHeight = image.getHeight();

                        // Calculate aspect ratios
                        double panelAspect = (double) panelWidth / panelHeight;
                        double imageAspect = (double) imageWidth / imageHeight;

                        int drawWidth, drawHeight;
                        int drawX = 0, drawY = 0;

                        if (imageAspect > panelAspect) {
                            // Scale based on width
                            drawWidth = panelWidth;
                            drawHeight = (int) (panelWidth / imageAspect);
                            drawY = (panelHeight - drawHeight) / 2; // Center vertically
                        } else {
                            // Scale based on height
                            drawHeight = panelHeight;
                            drawWidth = (int) (panelHeight * imageAspect);
                            drawX = (panelWidth - drawWidth) / 2; // Center horizontally
                        }

                        // Draw scaled image
                        g.drawImage(image, drawX, drawY, drawWidth, drawHeight, null);
                    }
                }
            };

            // Set panel size based on the image dimensions
            panel.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));

            // Add the panel to the frame
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null); // Center the window
            frame.setVisible(true);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to load the image. Ensure the path is correct and the file is a valid PNG.",
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
