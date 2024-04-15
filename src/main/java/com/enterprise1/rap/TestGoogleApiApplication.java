package com.enterprise1.rap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TestGoogleApiApplication {
	
	public static String GOOGLE_APPLICATION_CREDENTIALS = "";

	public static void main(String[] args) {
		
		
		
		// Application HOME Path는 system properties > system env > getAbsolutePath 순으로 정의
		if(System.getProperty("GOOGLE_APPLICATION_CREDENTIALS") != null && !"".equals(System.getProperty("GOOGLE_APPLICATION_CREDENTIALS"))) {
			TestGoogleApiApplication.GOOGLE_APPLICATION_CREDENTIALS = System.getProperty("GOOGLE_APPLICATION_CREDENTIALS");
		}
		//log.info("Set GOOGLE_APPLICATION_CREDENTIALS {}",System.getProperty("GOOGLE_APPLICATION_CREDENTIALS"));
		System.out.println(">>> main > Set GOOGLE_APPLICATION_CREDENTIALS : " + GOOGLE_APPLICATION_CREDENTIALS);
		System.out.println(">>> main > get env GOOGLE_APPLICATION_CREDENTIALS : " + System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));
		
		
		
		SpringApplication.run(TestGoogleApiApplication.class, args);
	}

}
