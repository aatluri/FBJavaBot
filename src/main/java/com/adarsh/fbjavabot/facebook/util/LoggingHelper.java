
package com.adarsh.fbjavabot.facebook.util;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class LoggingHelper
{

	private static final Logger log = LoggerFactory.getLogger(LoggingHelper.class);

	public static void MDCSetup(String operationName)
	{
		if (MDC.get(MdcConstants.GENERATED_REQUEST_ID) == null)
		{
			MDC.put(MdcConstants.GENERATED_REQUEST_ID, UUID.randomUUID().toString());
		}
		MDC.put(MdcConstants.OPERATION, operationName);
		MDC.put(MdcConstants.IS_SUCCESS, ExecutionStatus.INPROGRESS.name());
	}

	public static void MDCSetup(String operationName, String conversationId, String coversationType, String pageId)
	{
		LoggingHelper.MDCSetup(operationName);
		MDC.put(MdcConstants.CONVO_ID, conversationId);
		MDC.put(MdcConstants.CONVO_TYPE, coversationType);
		MDC.put(MdcConstants.FACEBOOK_PAGEID, pageId);
	}

}
