
package com.adarsh.fbjavabot.facebook;

import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Hex;
import org.jboss.logging.MDC;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.adarsh.fbjavabot.facebook.model.Entry;
import com.adarsh.fbjavabot.facebook.model.Event;
import com.adarsh.fbjavabot.facebook.model.FBRequestMsg;
import com.adarsh.fbjavabot.facebook.model.Payload;
import com.adarsh.fbjavabot.facebook.model.Postback;
import com.adarsh.fbjavabot.facebook.util.EventType;
import com.adarsh.fbjavabot.facebook.util.ExecutionStatus;
import com.adarsh.fbjavabot.facebook.util.LoggingHelper;
import com.adarsh.fbjavabot.facebook.util.MdcConstants;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;


@Controller
@RequestMapping("/fbwebhook")
public class WebhookController
{

	private static final Logger log = LoggerFactory.getLogger(WebhookController.class);


	private String verifyToken;
	private String appSecret;
	private ObjectWriter writer;
	private ObjectMapper mapper;
	private String fbThreadSettingsUrl;
	private FacebookReplyService fbReplyService;
	private ConversationDispatcher dispatcher;
	private String pageToken;

	
	
	@Autowired
	public WebhookController(String verifyToken, String appSecret, String fbThreadSettingsUrl, FacebookReplyService fbReplyService, ConversationDispatcher dispatcher, String pageToken  )
	{
		this.verifyToken = verifyToken;
		this.appSecret = appSecret;
		this.fbThreadSettingsUrl = fbThreadSettingsUrl.replace("PAGE_TOKEN", pageToken);
		this.dispatcher = dispatcher;
		this.fbReplyService = fbReplyService;
		this.pageToken = pageToken;

		mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		writer = mapper.writer();
	}

	/**
     * Add the webhook endpoint
     *
     * @param callback
     * @return 200 OK response
     */
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity setupWebHook(
			@RequestParam(value = "hub.mode", defaultValue = "") String mode,
	        @RequestParam(value = "hub.verify_token", defaultValue = "") String verifyToken,
	        @RequestParam(value = "hub.challenge", defaultValue = "") String challenge)
	{
		MDC.clear();
		try
		{
			LoggingHelper.MDCSetup("setupWebhook");
			log.info("hub.verify_token = " + verifyToken);
			log.info("hub.challenge = " + challenge);

			if(EventType.SUBSCRIBE.name().equalsIgnoreCase(mode) && this.verifyToken.equals(verifyToken))
			{
				
				MDC.put(MdcConstants.IS_SUCCESS, ExecutionStatus.SUCCESS.name());
				log.info("Verification Success");
				return ResponseEntity.ok(challenge); 
			}
			else
			{
				MDC.put(MdcConstants.IS_SUCCESS, ExecutionStatus.FAILED.name());
				log.error("The verify token sent does not match the configured verify token. Please check the appropriate application.properties file");
				return new ResponseEntity<>(HttpStatus.FORBIDDEN);
			}
		}
		catch(Exception e)
		{
			MDC.put(MdcConstants.IS_SUCCESS, ExecutionStatus.FAILED.name());
			log.error(e.getMessage(), e);
			return new ResponseEntity<>(HttpStatus.FORBIDDEN);
		}
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public ResponseEntity handlePost(@RequestBody String request, HttpServletRequest httpReq)
	{
		MDC.clear();
		MDC.put(MdcConstants.GENERATED_REQUEST_ID, UUID.randomUUID().toString());
		log.info("RequestBody: " + request);
		
		if(!verifySignature(httpReq, this.appSecret, request))
		{
			log.error("We were not able to verify the signature from facebook ");
			return ResponseEntity.ok("EVENT_RECEIVED");
		}
		
		try
		{
			FBRequestMsg fbMsgRequest = mapper.readValue(request, FBRequestMsg.class);
			//log.info("Unmarshalled FbMsgRequest=" + writer.writeValueAsString(fbMsgRequest));
			for(Entry pageEntry : fbMsgRequest.getEntry())
			{
				String pageId = pageEntry.getId();
				MDC.put(MdcConstants.FACEBOOK_PAGEID, pageId);
				if(pageEntry.getMessaging() != null)
				{
					for(Event event : pageEntry.getMessaging())
					{
						// The dispatchMessagingEvent method spawns off a new thread and the processing is done on that thread.
						// We do this so that we sent a response back to facebook quickly so that it does not try to send the same message repeatedly.
						dispatcher.dispatchMessagingEvent(event, pageId);
					}
				}
			}
		}
		catch(Exception e)
		{
			MDC.put(MdcConstants.IS_SUCCESS, ExecutionStatus.FAILED.name());
			log.error(e.getMessage(), e);
		}
		return ResponseEntity.ok("EVENT_RECEIVED");
	}

	 /**
     * Call this endpoint with a json payload in the request body which consists of the below
     *  {
     *  	"greetingText":<actual greeting text>
     *  	"getStartedButtonPayload":<Text that shows on the button>
     *  }
     * <p>
     * See https://developers.facebook.com/docs/messenger-platform/discovery/welcome-screen for more.
     * @return response from facebook
     */
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/setPageGreeting", produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public HttpStatus setPageGreeting(
	        @RequestBody String requestBody)
	{
		MDC.clear();
		try
		{
			JSONObject jsonObj = new JSONObject(requestBody);
			String greetingText = jsonObj.getString("greetingText");
			String getStartedButtonPayload = jsonObj.getString("getStartedButtonPayload");
			String pageId = jsonObj.getString("pageId");

			LoggingHelper.MDCSetup("setPageGreeting", greetingText, getStartedButtonPayload, pageId);
			log.debug("setPageGreeting Call Received");

			if(greetingText.isEmpty() || getStartedButtonPayload.isEmpty() || pageId.isEmpty())
			{
				MDC.put(MdcConstants.IS_SUCCESS, ExecutionStatus.FAILED.name());
				log.error("The greetingText or getStartedButtonPayload or pageId passed was blank");
				return HttpStatus.BAD_REQUEST;
			}
			else
			{
				long start = System.currentTimeMillis();
				log.debug("Sending GetStartedButton Settings");
				Payload[] greetingPayload = new Payload[]{new Payload().setLocale("default").setText(greetingText)};
				Event greetingEvent = new Event().setGreeting(greetingPayload);
				Event getStartedButtonEvent = new Event().setGetStarted(new Postback().setPayload(getStartedButtonPayload));
			
				fbReplyService.sendReply(fbThreadSettingsUrl,greetingEvent);
				fbReplyService.sendReply(fbThreadSettingsUrl,getStartedButtonEvent);
				log.info("Done,ThreadSettingsDurationMs=" + String.valueOf(System.currentTimeMillis() - start));
				return HttpStatus.OK;
			}
		}
		catch(IllegalArgumentException iae)
		{
			MDC.put(MdcConstants.IS_SUCCESS, ExecutionStatus.FAILED.name());
			log.error(iae.getMessage(), iae);
			return HttpStatus.UNAUTHORIZED;
		}
		catch(Exception e)
		{
			MDC.put(MdcConstants.IS_SUCCESS, ExecutionStatus.FAILED.name());
			log.error(e.getMessage(), e);
			return HttpStatus.INTERNAL_SERVER_ERROR;
		}
	}

	/**
     * Verifies that the incoming request is actually from facebook
     * It compares the appSecret which can be obtained from facebook app dashboard to a hash of the requestbody
     * @return boolean
     */
	public static boolean verifySignature(HttpServletRequest httpReq, String appsecret, String requestBody)
	{
		try
		{					
			if(httpReq.getHeader("x-hub-signature").split("=")[1].equals(calculateHash(appsecret, requestBody)))
			{
				MDC.put(MdcConstants.IS_SUCCESS, ExecutionStatus.SUCCESS.name());
				return true;
			}
			else
			{
				MDC.put(MdcConstants.IS_SUCCESS, ExecutionStatus.FAILED.name());
				log.error("Verify Signature Failed. The request signature hash does not match the expected hash generated using the appsecret.");
				return false;
			}
		}
		catch(Exception e)
		{
			MDC.put(MdcConstants.IS_SUCCESS, ExecutionStatus.FAILED.name());
			log.error(e.getMessage(), e);
			return false;
		}
	}
	
	
	public static String calculateHash(String appsecret, String requestBody) throws Exception
	{
		SecretKeySpec key = new SecretKeySpec((appsecret).getBytes(), "HmacSHA1");
		Mac mac = Mac.getInstance("HmacSHA1");
		mac.init(key);
		byte[] bytes = mac.doFinal(requestBody.getBytes());
		byte[] hexBytes = new Hex().encode(bytes);
		
		return new String(hexBytes, "UTF-8");
	}
}