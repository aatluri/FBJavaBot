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

    /**
     * Entry point of the application. Run this method to start the sample bots,
     * but don't forget to add the correct tokens in application.properties file as indicated in the readme file.
     *
     * @param args
     */
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
