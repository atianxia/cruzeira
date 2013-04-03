/*
 * This file is part of cruzeira and it's licensed under the project terms.
 */
package org.cruzeira.netty;

import org.cruzeira.server.QueueExecutor;
import org.cruzeira.server.ServerManager;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncServer extends ServletServer {

	private Logger logger = LoggerFactory.getLogger(getClass());

	public AsyncServer(ServerManager serverManager) {
		super(serverManager);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
		logger.info("Async message received");

		Object[] servlets = data.get(event.getChannel());
		StringBuilder buf = new StringBuilder();

		try {
			Runnable runnable = (Runnable) QueueExecutor.futures.get(servlets[1]);
			logger.info("Request, response, runnable: {}, {}, {}", servlets[0], servlets[1], runnable);
			QueueExecutor.futures.remove(servlets[1]);
			runnable.run();
		} catch (Exception e) {
			e.printStackTrace();
		}

//		logger.info("Running servlet service for async request");
		servlets = doServlet(ctx, event, buf, servlets[0], servlets[1]);

		HttpRequest request = (HttpRequest) event.getMessage();
		if (servlets == null) {
		} else if (request.isChunked()) {
			// readingChunks = true;
		} else {
			writeResponse(event, request, buf, servlets[0], servlets[1]);
		}
	}

	@Override
	protected boolean isCheckAsync() {
		return false;
	}

}
