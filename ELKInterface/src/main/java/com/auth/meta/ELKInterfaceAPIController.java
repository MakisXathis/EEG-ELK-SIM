package com.auth.meta;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.auth.meta.config.Configuration;
import com.auth.meta.model.KibanaResponses.DashboardTemplate;
import com.auth.meta.model.KibanaResponses.KibanaCreateDashboardResponse;
import com.auth.meta.model.KibanaResponses.KibanaCreateReportResponse;
import com.auth.meta.model.KibanaResponses.KibanaDashboardsList;
import com.google.gson.*;
import com.google.gson.JsonSyntaxException;

@RestController
public class ELKInterfaceAPIController {

	private final Configuration config;
    private final CloseableHttpClient httpClient;
    private final String os;
	
    public ELKInterfaceAPIController(Configuration configuration, CloseableHttpClient httpClient, String os) {
        this.config = configuration;
        this.httpClient = httpClient;
		this.os = os;
    }
    
	@GetMapping(
			  value = "/get-dashboard"
			)
	public @ResponseBody ResponseEntity<?> getDashboard(@RequestParam(value = "start_date") String start_date, @RequestParam(value = "end_date") String end_date, @RequestParam(value = "electrodes") String electrodes, @RequestParam(value = "description") String description, @RequestParam(value = "title") String title) {
		
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		
		LocalDate startDate = null;
        LocalDate endDate = null;
        Long startDateDaysDifference = null;
        Long endDateDaysDifference = null;
        List<Integer> electrodesList = new ArrayList<>();
        String[] electrodesStrings = null;
        DashboardTemplate template = null;
		
        //Check validity of start_date
		try {
            startDate = LocalDate.parse(start_date, dateFormatter);
            startDateDaysDifference = calculateDaysDifference(startDate);
            if (startDateDaysDifference < 0) {
            	return ResponseEntity
                        .badRequest()
                        .body("Start date cannot be in the future. Please input a valid start date.");
            }
        } catch (DateTimeParseException e) {
        	return ResponseEntity
                    .badRequest()
                    .body("Invalid date format. Please use dd-MM-yyyy.");
        }
		
		//Check validity of end_date
		try {
            endDate = LocalDate.parse(end_date, dateFormatter);
            if (endDate.isBefore(startDate)) {
            	return ResponseEntity
                        .badRequest()
                        .body("End date cannot be before start date. Please try again.");
            }
            endDateDaysDifference = calculateDaysDifference(endDate);
            if (endDateDaysDifference < 0) {
            	return ResponseEntity
                        .badRequest()
                        .body("End date cannot be in the future. Please input a valid end date.");
            }
        } catch (DateTimeParseException e) {
        	return ResponseEntity
                    .badRequest()
                    .body("Invalid date format. Please use dd-MM-yyyy.");
        }
		
		//Check if electrodes are valid
		electrodesStrings = electrodes.split(",");
        
        if (checkDuplicateElectrodes(electrodesStrings))
        	return ResponseEntity
                    .badRequest()
                    .body("Invalid input. Each Electrode can be entered only once.");

        for (String electrodeString : electrodesStrings) {
            try {
                int number = Integer.parseInt(electrodeString.trim());
                if (number < 1 || number > 16) {
                	return ResponseEntity
                            .badRequest()
                            .body("Invalid number: \" + number + \". Please ensure all Electrodes are between 1 and 16.");
                }
                electrodesList.add(number);
            } catch (NumberFormatException e) {
            	return ResponseEntity
                        .badRequest()
                        .body("Invalid input: " + electrodeString + ". Please enter only integers.");
            }
        }
        
        System.out.println("Start date days difference: "+startDateDaysDifference);
        System.out.println("End date days difference: "+endDateDaysDifference);
        System.out.println("Description: "+description);
        System.out.println("Title: "+title);
        System.out.println("Electrodes: "+electrodesList.toString());
        
        try {
        	if(os.contains("win"))
        		template = readTemplate("dashboards\\dashboard_"+electrodesStrings.length+".ndjson");
        	else
        		template = readTemplate("dashboards/dashboard_"+electrodesStrings.length+".ndjson");
		}catch (Exception e) {
			return ResponseEntity
                    .badRequest()
                    .body(e.toString());
		}
        
        template = updateTemplate(template, startDateDaysDifference, endDateDaysDifference, electrodesList, description, title);
        
        System.out.println(template.toString());
        
        String newFilePath = "";
        if (os.contains("win"))
        	newFilePath = "dashboards\\new_dashboard_"+electrodesList.size()+".ndjson";
        else
        	newFilePath = "dashboards/new_dashboard_"+electrodesList.size()+".ndjson";
        try {
			writeTemplateToNDJSON(template, newFilePath);
		} catch (IOException e) {
			return ResponseEntity
                   .badRequest()
	               .body("An error occurred while writing NDJSON to file: " + e.getMessage());
		}
        
        //Create the new Kibana dashboard
        String updatedTemplatePath = "";
        if (os.contains("win"))
        	updatedTemplatePath = System.getProperty("user.dir")+"\\"+newFilePath;
        else
        	updatedTemplatePath = System.getProperty("user.dir")+"/"+newFilePath;
        
        String dashboardID = createKibanaDashboard(httpClient, config.getServer().getIp(), config.getServer().getPort(), config.getServer().getSsl_enabled(), config.getServer().getCredentials().getUsername(), config.getServer().getCredentials().getPassword(),updatedTemplatePath);
       	if (dashboardID != "") {
        	System.out.println("Created the new Kibana dashboard!");
        }else {
        	
        }
        
        /*
        String kibanaDashboards = getKibanaDashboards(httpClient, config.getServer().getIp(), config.getServer().getPort(), config.getServer().getSsl_enabled(), config.getServer().getCredentials().getUsername(), config.getServer().getCredentials().getPassword());
                
        dashboardID = getDashboardID(description, kibanaDashboards);
        if(dashboardID.isEmpty())
        	return ResponseEntity
                    .badRequest()
                    .body("No dashboard ID was extracted from the Kibana Dashboard list");
        else
        	System.out.println("Dashboard ID found in Kibana: "+dashboardID);
        */
        String reportPath = createkibanaPNGReport(httpClient, config.getServer().getIp(), config.getServer().getPort(), config.getServer().getSsl_enabled(), config.getServer().getCredentials().getUsername(), config.getServer().getCredentials().getPassword(), dashboardID, startDateDaysDifference, endDateDaysDifference, description);
        if(reportPath.isEmpty()) {
        	return ResponseEntity
                    .badRequest()
                    .body("No Report API download endpoint was retrieved from the request to create Kibana PNG report. Please check application logs error.");
        }else
        	System.out.println("Reportpath returned from Kibana: "+reportPath);
        
        try {
        	System.out.println("Waiting for report creation...");
			Thread.sleep(20000); //wait until the report is complete
		} catch (InterruptedException e) {
			return ResponseEntity
                    .badRequest()
                    .body("System was interrupted server is shutting down...");
		}
		
        byte[] imageData = getKibanaReport(httpClient, config.getServer().getIp(), config.getServer().getPort(), config.getServer().getSsl_enabled(), config.getServer().getCredentials().getUsername(), config.getServer().getCredentials().getPassword(), reportPath);
        return ResponseEntity
                .ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(imageData);
	}
	
	public static long calculateDaysDifference(LocalDate inputDate) {
        LocalDate currentDate = LocalDate.now();
        return ChronoUnit.DAYS.between(inputDate, currentDate);
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
	
	//Loads the dashboard template from the file
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
	
	//Updates the template based on the number of electrodes that were inserted
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
	
	//Created because its easier to upload the dashboard through the file than escape all the needed characters and using a String in the API. Also helpful for debugging purposes
	public static void writeTemplateToNDJSON(DashboardTemplate template, String filePath) throws IOException {
		try (FileWriter writer = new FileWriter(filePath)) {
	        Gson gson = new Gson();
	        gson.toJson(template, writer);
	    }
	}
	
	 //System.err.println("An error occurred while writing NDJSON to file: " + e.getMessage());
	 //Creates a Kibana dashboard based on the file provided and returns the DashboardID
	 public static String createKibanaDashboard(CloseableHttpClient client, String ip, int port, boolean isHttps, String username, String password, String filePath) {
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
	    	CloseableHttpResponse response = client.execute(postRequest);
			
			HttpEntity responseEntity = response.getEntity();
			String responseString = EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
			
			Gson gson = new Gson();
			KibanaCreateDashboardResponse kibanaCreateDashboardResponse = gson.fromJson(responseString, KibanaCreateDashboardResponse.class);
			if(kibanaCreateDashboardResponse.isSuccess()) {
				return kibanaCreateDashboardResponse.getSuccessResults()[0].getDestinationId();
			}else
				return "";
		} catch (IOException e) {
			System.out.println("There was an error while creating the Kibana Dashboard. Error: "+e);
			return "";
		}
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
	
	 //Receives a a Kibana report API download endpoint and downloads and creates the generated png to a local file
	 public static byte[] getKibanaReport(CloseableHttpClient client, String ip, int port, boolean isHttps, String username, String password, String reportPath) {
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
                return null;
            }
            	
            //Save response content to a byte array
            HttpEntity entity = response.getEntity();
            if (entity != null) {
           	 try (InputStream is = entity.getContent();
                        ByteArrayOutputStream imageByteArray = new ByteArrayOutputStream()) {

                       byte[] buffer = new byte[4096];
                       int len;
                       while ((len = is.read(buffer)) != -1) {
                       	imageByteArray.write(buffer, 0, len);
                       }
                       System.out.println("Report downloaded successfully.");
                       return imageByteArray.toByteArray();
                   }
            }else {
           	 return null;
            }
        } catch (IOException e) {
            System.out.println("There was an error while trying to get the Kibana report for path: "+reportPath+". Error: "+e);
            return null;
        }
	 }

	// Function that receives the path of a png image and presents it to the user
	// by opening a new window
	public static void showPNG(String filePath) {
	    try {
	        String[] windowTitle = filePath.split("\\\\");

	        // Load the image
	        File file = new File(filePath);
	        BufferedImage image = ImageIO.read(file);

	        // Create a JFrame to display the image
	        JFrame frame = new JFrame(windowTitle[windowTitle.length - 1]);
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
	        JOptionPane.showMessageDialog(null,
	                "Failed to load the image. Ensure the path is correct and the file is a valid PNG.",
	                "Error", JOptionPane.ERROR_MESSAGE);
	    }
	}
 

}
