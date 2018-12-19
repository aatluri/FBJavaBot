
package com.adarsh.fbjavabot.facebook;

import java.util.concurrent.ThreadPoolExecutor;

import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.adarsh.fbjavabot.facebook.model.Attachment;
import com.adarsh.fbjavabot.facebook.model.Button;
import com.adarsh.fbjavabot.facebook.model.Element;
import com.adarsh.fbjavabot.facebook.model.Event;
import com.adarsh.fbjavabot.facebook.model.Message;
import com.adarsh.fbjavabot.facebook.model.Payload;
import com.adarsh.fbjavabot.facebook.util.ConversationType;
import com.adarsh.fbjavabot.facebook.util.EventType;
import com.adarsh.fbjavabot.facebook.util.LoggingHelper;
import com.adarsh.fbjavabot.facebook.util.MdcConstants;



/**
 * This class is used to actually process the event sent by facebook and reply to the page
 * It contains the dispatchMessageingEvent method which executes asynchronously and processes event based on what type of event it is
 * 
 * @author aatluri
 */

@Component
public class ConversationDispatcher
{

	private static final Logger log = LoggerFactory.getLogger(ConversationDispatcher.class);
	
	private FacebookReplyService replyService;

	@Autowired
	public ConversationDispatcher(FacebookReplyService replyService)
	{
		this.replyService=replyService;
	}

	@Async
	public void dispatchMessagingEvent(Event event, String pageId)
	{
		String senderId = event.getSender().getId();
		LoggingHelper.MDCSetup("handleFbPost", senderId, ConversationType.FACEBOOK.name(), pageId);

		if(event.getMessage() != null && event.getMessage().isEcho()!=null && event.getMessage().isEcho())
		{ 
			event.setType(EventType.MESSAGE_ECHO);
			MDC.put(MdcConstants.FACEBOOK_MSG_TYPE, EventType.MESSAGE_ECHO.name());
		}
		else if (event.getMessage() != null && event.getMessage().getQuickReply() != null) 
		{
			replyService.sendTypingOnIndicator(event.getSender());
            event.setType(EventType.QUICK_REPLY);
            MDC.put(MdcConstants.FACEBOOK_MSG_TYPE, EventType.QUICK_REPLY);
            if(event.getMessage().getQuickReply().getPayload().contains("Button"))
            {
            	displayButtons(event);
            }
            else
            {
            	displayList(event);
            }
        }
		else if(event.getMessage() != null)
		{ 
			replyService.sendTypingOnIndicator(event.getSender());
			event.setType(EventType.MESSAGE);
			MDC.put(MdcConstants.FACEBOOK_MSG_TYPE, EventType.MESSAGE);
			if(event.getMessage().getText().matches("^(?i)(hi|hello|hey)$"))
			{
				displayQuickRelpyButtons(event);
			}
			else
			{
				replyService.reply(event, "I didnt quite understand you. If you would like to restart the experience type hi or hello or hey. If you are done, then it was a pleasure talking to you");
			}

		}
		else if (event.getPostback() != null) 
		{
            event.setType(EventType.POSTBACK);
            MDC.put(MdcConstants.FACEBOOK_MSG_TYPE, EventType.POSTBACK);
        } 
		else if(event.getDelivery() != null)
		{ 
			event.setType(EventType.MESSAGE_DELIVERED);
			MDC.put(MdcConstants.FACEBOOK_MSG_TYPE, EventType.MESSAGE_DELIVERED);
		}
		else if(event.getRead() != null)
		{ 
			event.setType(EventType.MESSAGE_READ);
			MDC.put(MdcConstants.FACEBOOK_MSG_TYPE, EventType.MESSAGE_READ);
		}
		
		else if(event.getPostback() != null)
		{ 
			 event.setType(EventType.POSTBACK);
			 MDC.put(MdcConstants.FACEBOOK_MSG_TYPE, EventType.POSTBACK);
		}
		else if (event.getOptin() != null) 
		{
            event.setType(EventType.OPT_IN);
            MDC.put(MdcConstants.FACEBOOK_MSG_TYPE, EventType.OPT_IN);
        } 
		else if (event.getReferral() != null) 
        {
            event.setType(EventType.REFERRAL);
            MDC.put(MdcConstants.FACEBOOK_MSG_TYPE, EventType.REFERRAL);
        } 
		else if (event.getAccountLinking() != null) 
        {
            event.setType(EventType.ACCOUNT_LINKING);
            MDC.put(MdcConstants.FACEBOOK_MSG_TYPE, EventType.ACCOUNT_LINKING);
        }
		else
		{
			MDC.put(MdcConstants.FACEBOOK_MSG_TYPE, "unknown");
			log.error("Facebook Event Type Not Suported");
		}
	}
	
	private void displayList(Event event) 
	{
		Element[] elements = new Element[]{
                new Element().setTitle("AnimateScroll").setSubtitle("A jQuery Plugin for Animating Scroll.")
                        .setImageUrl("https://plugins.compzets.com/images/as-logo.png")
                        .setDefaultAction(new Button().setType("web_url").setMessengerExtensions(true)
                        .setUrl("https://www.facebook.com")),
                new Element().setTitle("Windows on Top").setSubtitle("Keeps a specific Window on Top of all others.")
                        .setImageUrl("https://plugins.compzets.com/images/compzets-logo.png")
                        .setDefaultAction(new Button().setType("web_url").setMessengerExtensions(true)
                        .setUrl("https://www.facebook.com"))
        };
    	replyService.reply(event, new Message().setAttachment(new Attachment().setType("template").setPayload(new Payload()
                .setTemplateType("list").setElements(elements))));
	}
	
	private void displayButtons(Event event) 
	{
		Button[] buttons = new Button[]{
                new Button().setType("web_url").setUrl("https://github.com/aatluri/FBJavaBot").setTitle("FBJavaBot Docs"),
                new Button().setType("web_url").setUrl("https://goo.gl/uKrJWX").setTitle("Button Template")
        };
        replyService.reply(event, new Message().setAttachment(new Attachment().setType("template").setPayload(new Payload()
                .setTemplateType("button").setText("These are 2 link buttons.").setButtons(buttons))));
	}
	
	private void displayQuickRelpyButtons(Event event)
	{
		String prompt = "Hi there, How are you doing?. The two buttons you see below are quick reply buttons. There are also normal buttons and lists. Click on the button below depending on what you want to see";
		Button[] quickReplies = new Button[]{
                new Button().setContentType("text").setTitle("NormalButtons").setPayload("NormalButtons"),
                new Button().setContentType("text").setTitle("Lists").setPayload("Lists")
        };
		replyService.reply(event, new Message().setText(prompt).setQuickReplies(quickReplies));
		
	}

}
