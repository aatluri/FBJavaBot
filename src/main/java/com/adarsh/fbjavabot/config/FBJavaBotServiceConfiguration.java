
package com.adarsh.fbjavabot.config;


import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.AsyncConfigurerSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableAsync
public class FBJavaBotServiceConfiguration extends AsyncConfigurerSupport
{
	private String fbTimeout = "10000";
	private String acsTimeout = "10000";

	@Value("${facebook.verifytoken}")
	private String verifyToken;
	
	@Value("${facebook.pagetoken}")
	private String pageToken;
	
	@Value("${facebook.appsecret}")
	private String appSecret;

	@Value("${facebook.api.url}")
	private String fbApiUrl;

	@Value("${facebook.api.message}")
	private String fbMessageUrlExtension;

	@Value("${facebook.api.threadsettings}")
	private String fbThreadSettingsUrlExtension;

	@Autowired
	private Environment environment;

	@Bean
	public RestTemplate fbRestTemplate()
	{
		RestTemplate fbRestReplyTemplate = new RestTemplate(getHttpClientRequestFactory(fbTimeout));
		fbRestReplyTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
		return fbRestReplyTemplate;
	}


	@Override
	public Executor getAsyncExecutor()
	{
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(50);
		executor.setMaxPoolSize(500);
		executor.setQueueCapacity(2); // number of tasks to queue against core pool before adding more threads (up to max pool size).
		executor.setThreadNamePrefix("FbPostback-");
		executor.initialize();
		return new MdcAwareExecutor(executor);
	}

	@Override
	public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler()
	{
		// TODO may need to customize this to handle uncaught exceptions from async tasks
		return new SimpleAsyncUncaughtExceptionHandler();
	}

	private ClientHttpRequestFactory getHttpClientRequestFactory(String timeout)
	{
		HttpClient httpClient = HttpClientBuilder.create()
		        .setMaxConnPerRoute(150)
		        .setMaxConnTotal(150)
		        .build();
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

		factory.setReadTimeout(Integer.parseInt(timeout));
		factory.setConnectTimeout(Integer.parseInt(timeout));
		return factory;
	}

	@Bean
	public String profileLink()
	{
		return fbApiUrl + "/SENDER_ID?access_token=PAGE_TOKEN";
	}

	@Bean
	public String fbMessageUrl()
	{
		return fbApiUrl + fbMessageUrlExtension + "?access_token=PAGE_TOKEN";
	}


	@Bean
	public String fbThreadSettingsUrl()
	{
		return fbApiUrl + fbThreadSettingsUrlExtension + "?access_token=PAGE_TOKEN";
	}

	@Bean
	public String verifyToken()
	{
		return verifyToken;
	}
	
	@Bean
	public String pageToken()
	{
		return pageToken;
	}
	
	@Bean
	public String appSecret()
	{
		return appSecret;
	}
	
	@Bean
	public RestTemplate acsRestTemplate(RestTemplateBuilder builder)
	{
		RestTemplate restTemplate =  builder.build();
		restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
		restTemplate.setRequestFactory(getHttpClientRequestFactory(acsTimeout));
		return restTemplate;
	}
}
