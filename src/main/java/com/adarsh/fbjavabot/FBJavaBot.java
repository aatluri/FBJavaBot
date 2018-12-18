package com.adarsh.fbjavabot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FBJavaBot extends SpringBootServletInitializer
{

	// This method would be called from cmd line and start embedded Tomcat
	public static void main(String[] args)
	{
		SpringApplication.run(FBJavaBot.class, args);
	}
	
	//This method configures the application when initialized by external Tomcat container. 
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
	{
		return application.sources(FBJavaBot.class);
	}
}
