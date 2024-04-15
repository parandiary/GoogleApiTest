package com.enterprise1.rap.runner;

import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.enterprise1.rap.TestGoogleApiApplication;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.ServiceOptions;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Component
@Order(1)
public class ApplicationOptionsRunner implements ApplicationRunner  {

	private final Logger log = LoggerFactory.getLogger(ApplicationOptionsRunner.class);
	
	@Autowired
	@Qualifier("webClientGoogle")
    private WebClient webClient;
	
	private String projectId = "";
	private String accessToken = "";

	@Override
	public void run(ApplicationArguments args) throws Exception {

		log.info(">>>>>>>>>>>>>>> ApplicationOptionsCheck ");
		log.info("Application Argument Check");
		log.info("args.getOptionNames : {}",args.getOptionNames());
		log.info("args.getNonOptionArgs : {}",args.getNonOptionArgs());

		//String userDirectory = new File("").getAbsolutePath();
		//log.debug("USER Directory {}", userDirectory);
		//System.out.println("USER Directory : " + userDirectory);


		// Application HOME Path는 system properties > system env > getAbsolutePath 순으로 정의
		if(System.getProperty("GOOGLE_APPLICATION_CREDENTIALS") != null && !"".equals(System.getProperty("GOOGLE_APPLICATION_CREDENTIALS"))) {
			TestGoogleApiApplication.GOOGLE_APPLICATION_CREDENTIALS = System.getProperty("GOOGLE_APPLICATION_CREDENTIALS");
		}
		if("".equals(TestGoogleApiApplication.GOOGLE_APPLICATION_CREDENTIALS) && !"".equals(System.getenv("GOOGLE_APPLICATION_CREDENTIALS")) ) {
			TestGoogleApiApplication.GOOGLE_APPLICATION_CREDENTIALS = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
		}
		log.info(">>> ApplicationOptionsRunner TestGoogleApiApplication.GOOGLE_APPLICATION_CREDENTIALS {}",TestGoogleApiApplication.GOOGLE_APPLICATION_CREDENTIALS);
		
		
		// get google access token
		// Get Google access token logic goes here
		
		projectId = ServiceOptions.getDefaultProjectId();
		log.info(">>> ApplicationOptionsRunner google projectId {}",projectId);
		
		String CLOUD_SCOPE = "https://www.googleapis.com/auth/cloud-platform";
		String CHAT_SCOPE = "https://www.googleapis.com/auth/chat";
		String CHAT_BOT_SCOPE = "https://www.googleapis.com/auth/chat.bot";
		
		String OPEN_ID = "openid";
		String GOOGLE_CLOUD_SCOPE = "https://www.googleapis.com/auth/cloud-platform";
		String GOOGLE_CLOUD_SCOPE2 = "https://www.googleapis.com/auth/userinfo.email";
		String GOOGLE_CLOUD_SCOPE3 = "https://www.googleapis.com/auth/sqlservice.login";
		
		
		
//		List<String> scopes = Lists.newArrayList(
//		    CLOUD_SCOPE,
//		    CHAT_SCOPE,
//		    CHAT_BOT_SCOPE);
		List<String> scopes = Lists.newArrayList(
				GOOGLE_CLOUD_SCOPE2,
				GOOGLE_CLOUD_SCOPE,
				GOOGLE_CLOUD_SCOPE3
				);
		
		
		
		GoogleCredentials credential = GoogleCredentials
				.fromStream(new FileInputStream(TestGoogleApiApplication.GOOGLE_APPLICATION_CREDENTIALS));
				//.fromStream(new FileInputStream("/GOOGLE/apigee-bi-444d40cc0e99.json"));
		
		//check credential is null
		if(credential == null) {
			log.info(">>> ApplicationOptionsRunner google credential is null");
		}
		log.info(">>> ApplicationOptionsRunner google credential {}",credential);
		credential = credential.createScoped(scopes);
		
		//AccessToken accessToken = credential.getAccessToken();
		//log.info(">>> ApplicationOptionsRunner google accessToken {}",accessToken.getTokenValue());
		
		
		// Access token을 바로 사용할 수 없음.
		//AccessToken accessToken = credential.getAccessToken();
		//log.info(">>> ApplicationOptionsRunner google accessToken {}",accessToken.getTokenValue());
		
		
		// refrashTokn을 받아서 활용
		AccessToken refrashToken = credential.refreshAccessToken();
		log.info(">>> ApplicationOptionsRunner google refrashToken {}",refrashToken.getTokenValue());
		accessToken = refrashToken.getTokenValue();
		
		
		
		// Product 목록 조회
		webClient.get().uri(
					"https://apigee.googleapis.com/v1/organizations/{projectId}/apiproducts"
					,projectId
				)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.retrieve()
				.bodyToMono(String.class)
				.subscribe(response -> {
					log.info(">>> ApplicationOptionsRunner google response {}",response);
				});
		
		webClient.get().uri("/v1/organizations/"+projectId+"/apis")
		.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
		.retrieve()
		.bodyToMono(String.class)
		.subscribe(response -> {
			log.info(">>> apis {}",response);
			
			// json object 변환
			JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
			JsonArray jsonArray = jsonObject.getAsJsonArray("proxies");
			
			jsonArray.forEach(api -> {
				apiDetail(api.getAsJsonObject().get("name").getAsString());
			});
		});
		
		
		// APP 목록 조회
		webClient.get().uri(
				"https://apigee.googleapis.com/v1/organizations/{projectId}/apps"
				,projectId
				)
		.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
		.retrieve()
		.bodyToMono(String.class)
		.subscribe(response -> {
			log.info(">>> apps {}",response);
			
			// json object 변환
			// Convert the response to a JSON object
			JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
			JsonArray jsonArray = jsonObject.getAsJsonArray("app");
			
			jsonArray.forEach(app -> {
				// app detail
				appDetail(app.getAsJsonObject().get("appId").getAsString());
			});
			
		});
		
		
		
		
		
		// test apigee management api
		String apiUrl = "https://apigee.googleapis.com/v1/organizations/"+projectId+"/apiproducts";
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "Bearer " + accessToken);

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            Scanner scanner = new Scanner(connection.getInputStream());
            while (scanner.hasNextLine()) {
                System.out.println(scanner.nextLine());
            }
            scanner.close();
        } else {
            System.err.println("Failed to fetch data from APIGEE Management API. Response code: " + responseCode);
        }

	}
	
	
	
	// api detail 정보 조회
	private void apiDetail(String apiName) {
		webClient.get().uri(
				"/v1/organizations/{projectId}/apis/{apiName}"
				,projectId
				,apiName
				)
		.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
		.retrieve()
		.bodyToMono(String.class)
		.subscribe(response -> {
			log.info("===========  api name {} =========",apiName);
			log.info(">>> apiDetail {}",response);
		});
	}
	
	// app detail 정보 조회
	private void appDetail(String appName) {
		webClient.get().uri(
			"/v1/organizations/{projectId}/apps/{appName}"
			,projectId
			,appName
		)
		.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
		.retrieve()
		.bodyToMono(String.class)
		.subscribe(response -> {
			log.info("===========  app name {} =========",appName);
			log.info(">>> appDetail {}",response);
			
			// app detail 정보 json object convert
			JsonObject jsonObject = JsonParser.parseString(response).getAsJsonObject();
			if(jsonObject == null) return;
			
			// credentials json array object
			JsonArray jsonArray = jsonObject.getAsJsonArray("credentials");
			if(jsonArray == null) return;
			jsonArray.forEach(credentials -> {
				log.info(">>> credentials {}",credentials);
				
				// apiProducts json array object
				JsonArray apiProducts = credentials.getAsJsonObject().getAsJsonArray("apiProducts");
				
				// null check
				if(apiProducts == null) return;
				apiProducts.forEach(apiProduct -> {
					// convert to json object
					//JsonObject apiProductJson = JsonParser.parseString(apiProduct.toString()).getAsJsonObject();
					//log.info(">>> apiProduct {}, status {}",apiProductJson.get("apiproduct").getAsString(),apiProductJson.get("status").getAsString());
					
					JsonObject apiProductJson = apiProduct.getAsJsonObject();
					log.info(">>> apiproduct {} status {}",apiProductJson.get("apiproduct").getAsString(),apiProductJson.get("status").getAsString());
					
				
				});
			
			});
		});
	}
	
	
	// product의 api 목록 조회
	private void productApis(String productId) {
		
	}
	
	

}



