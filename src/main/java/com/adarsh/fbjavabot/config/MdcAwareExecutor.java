
package com.adarsh.fbjavabot.config;

import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.MDC;
import org.springframework.core.task.TaskExecutor;

/**
 * A SLF4J MDC-compatible {@link ThreadPoolExecutor}.
 * <p>
 * MDC is used to store diagnostic information in per-thread variables, to facilitate logging. However, although MDC data is passed to thread children, this doesn't work when threads are reused in a
 * thread pool.
 * </p>
 * 
 * @author aatluri
 */
public class MdcAwareExecutor implements Executor, TaskExecutor
{
	private final Executor executor;

	public MdcAwareExecutor(Executor executor)
	{
		this.executor = executor;
	}

	@Override
	public void execute(Runnable command)
	{
		Runnable ctxAwareCommand = decorateContextAware(command);
		executor.execute(ctxAwareCommand);
	}

	private Runnable decorateContextAware(Runnable command)
	{
		final Map<String, String> originalContextCopy = MDC.getCopyOfContextMap();
		Runnable ctxAwareCommand = () -> {
			// copy the current context
			final Map<String, String> localContextCopy = MDC.getCopyOfContextMap();

			MDC.clear();
			if(originalContextCopy != null)
			{

				MDC.setContextMap(originalContextCopy);
			}

			// execute the command
			command.run();

			MDC.clear();
			if(localContextCopy != null)
			{
				// reset the context
				MDC.setContextMap(localContextCopy);
			}
		};

		return ctxAwareCommand;
	}
}