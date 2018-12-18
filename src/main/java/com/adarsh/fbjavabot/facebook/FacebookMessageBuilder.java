package com.adarsh.fbjavabot.facebook;

import com.adarsh.fbjavabot.facebook.model.Attachment;
import com.adarsh.fbjavabot.facebook.model.Message;
import com.adarsh.fbjavabot.facebook.model.Payload;


public class FacebookMessageBuilder
{
	public static Message getFbMessage(String msg)
	{
		Message message = new Message();
		message.setText(msg);
		return message;
	}

	public static Message getMessageFromPayload(Payload payload)
	{
		Attachment attachment = new Attachment();
		attachment.setPayload(payload);
		attachment.setType("template");
		Message message = new Message();
		message.setAttachment(attachment);
		return message;
	}
}