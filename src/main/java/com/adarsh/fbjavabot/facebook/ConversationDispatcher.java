
package com.adarsh.fbjavabot.facebook;

import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.adarsh.fbjavabot.facebook.model.Event;
import com.adarsh.fbjavabot.facebook.service.FacebookReplyService;
import com.adarsh.fbjavabot.facebook.util.ConversationType;
import com.adarsh.fbjavabot.facebook.util.EventType;
import com.adarsh.fbjavabot.facebook.util.LoggingHelper;
import com.adarsh.fbjavabot.facebook.util.MdcConstants;

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
		else if (event.getMessage().getQuickReply() != null) 
		{
            event.setType(EventType.QUICK_REPLY);
        }
		else if(event.getMessage() != null)
		{ 
			replyService.sendTypingOnIndicator(event.getSender());
			event.setType(EventType.MESSAGE);
			String prompt = "Hi there, How are you doing?";
			replyService.reply(event,prompt);
		}
		else if (event.getPostback() != null) 
		{
            event.setType(EventType.POSTBACK);
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

}
