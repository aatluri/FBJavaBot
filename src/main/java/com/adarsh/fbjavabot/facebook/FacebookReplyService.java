
package com.adarsh.fbjavabot.facebook;

import com.adarsh.fbjavabot.facebook.model.Event;
import com.adarsh.fbjavabot.facebook.model.Message;
import com.adarsh.fbjavabot.facebook.model.Response;
import com.adarsh.fbjavabot.facebook.model.User;
import com.adarsh.fbjavabot.facebook.util.ExecutionStatus;
import com.adarsh.fbjavabot.facebook.util.MdcConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


/**
 * This class is where the application sends to the reply to the facebook page post processing the event.
 * 
 * 
 * @author aatluri
 */

@Component
public class FacebookReplyService
{

	private static final Logger log = LoggerFactory.getLogger(FacebookReplyService.class);

	private RestTemplate fbRestTemplate;

	private String fbMessageUrl;
	
	private String pageToken;
	

	@Autowired
	public FacebookReplyService(RestTemplate fbRestTemplate, String fbMessageUrl, String pageToken)
	{
		this.fbMessageUrl = fbMessageUrl.replace("PAGE_TOKEN", pageToken);
		this.fbRestTemplate = fbRestTemplate;
		this.pageToken = pageToken;
	}

	/**
	 * Sends the reply to the facebook page
	 * 
	 */
	public ResponseEntity<String> sendReply(String url,Event event)
	{
		try
		{
			sendTypingOffIndicator(event.getRecipient());
			log.info("event being sent: " + event.toString());
			MDC.put("jsonReply", event.toString());
			return fbRestTemplate.postForEntity(fbMessageUrl, event, String.class);
		}
		catch (HttpClientErrorException e) 
		{
	        log.error("Send message error: Response body: {} \nException: ", e.getResponseBodyAsString(), e);
	        MDC.put(MdcConstants.IS_SUCCESS, ExecutionStatus.FAILED.name());
	        return new ResponseEntity<>(e.getResponseBodyAsString(), e.getStatusCode());
	    }
		catch(Exception e)
		{
			MDC.put(MdcConstants.IS_SUCCESS, ExecutionStatus.FAILED.name());
			log.error(e + ": stacktrace: " + ExceptionUtils.getStackTrace(e));
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	/**
	 * Constructs the event to sent back to facebook when we need to send back just text
	 * 
	 */
    public ResponseEntity<String> reply(Event event, String text) {
        Event response = new Event()
                .setMessagingType("RESPONSE")
                .setRecipient(event.getSender())
                .setMessage(new Message().setText(text));
        return sendReply(fbMessageUrl,response);
    }
    
    /**
	 * Constructs the event to sent back to facebook when we need to send back more complex events like buttons, attachments etc..
	 * 
	 */

    public ResponseEntity<String> reply(Event event, Message message) {
        Event response = new Event()
                .setMessagingType("RESPONSE")
                .setRecipient(event.getSender())
                .setMessage(message);
        return sendReply(fbMessageUrl,response);
    }

    /**
	 * Used to send the typing on indicator which displays a graphic to the user that indicates that the bot is replying shortly.
	 * 
	 */
  	public void sendTypingOnIndicator(User recipient) 
  	{
  		fbRestTemplate.postForEntity(fbMessageUrl, new Event().setRecipient(recipient).setSenderAction("typing_on"), Response.class);
    }

  	/**
	 * Used to send the typing off indicator which removes the typing graphic to the user that indicates that the bot is replying shortly.
	 * 
	 */
    public void sendTypingOffIndicator(User recipient) 
    {
    	fbRestTemplate.postForEntity(fbMessageUrl,new Event().setRecipient(recipient).setSenderAction("typing_off"), Response.class);
    }
	
    public String getFbMessageUrl()
    {
    	return fbMessageUrl;
    }

	
    public void setFbMessageUrl(String fbMessageUrl)
    {
    	this.fbMessageUrl = fbMessageUrl;
    }


}