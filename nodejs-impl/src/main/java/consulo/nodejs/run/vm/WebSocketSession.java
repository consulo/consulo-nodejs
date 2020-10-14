package consulo.nodejs.run.vm;

import javax.websocket.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.Principal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2020-06-17
 */
public class WebSocketSession implements Session
{
	private final WebSocketBackend.WebSocketClientImpl webSocketClient;
	private final Object o;

	private Set<MessageHandler> messageHandlers = new LinkedHashSet<>();

	public WebSocketSession(WebSocketBackend.WebSocketClientImpl webSocketClient, Object o)
	{
		this.webSocketClient = webSocketClient;
		this.o = o;

		webSocketClient.setHandlers(messageHandlers);
	}

	@Override
	public WebSocketContainer getContainer()
	{
		return null;
	}

	@Override
	public void addMessageHandler(MessageHandler messageHandler) throws IllegalStateException
	{
		messageHandlers.add(messageHandler);
	}

	@Override
	public <T> void addMessageHandler(Class<T> aClass, MessageHandler.Whole<T> whole)
	{
		messageHandlers.add(whole);
	}

	@Override
	public <T> void addMessageHandler(Class<T> aClass, MessageHandler.Partial<T> partial)
	{
		messageHandlers.add(partial);
	}

	@Override
	public Set<MessageHandler> getMessageHandlers()
	{
		return messageHandlers;
	}

	@Override
	public void removeMessageHandler(MessageHandler messageHandler)
	{
		messageHandlers.remove(messageHandler);
	}

	@Override
	public String getProtocolVersion()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getNegotiatedSubprotocol()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Extension> getNegotiatedExtensions()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isSecure()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isOpen()
	{
		return webSocketClient.isOpen();
	}

	@Override
	public long getMaxIdleTimeout()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMaxIdleTimeout(long l)
	{
		throw new UnsupportedOperationException();

	}

	@Override
	public void setMaxBinaryMessageBufferSize(int i)
	{
		throw new UnsupportedOperationException();

	}

	@Override
	public int getMaxBinaryMessageBufferSize()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setMaxTextMessageBufferSize(int i)
	{
		throw new UnsupportedOperationException();

	}

	@Override
	public int getMaxTextMessageBufferSize()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public RemoteEndpoint.Async getAsyncRemote()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public RemoteEndpoint.Basic getBasicRemote()
	{
		return new RemoteEndpoint.Basic()
		{
			@Override
			public void sendText(String s) throws IOException
			{
				webSocketClient.send(s);
			}

			@Override
			public void sendBinary(ByteBuffer byteBuffer) throws IOException
			{
				throw new UnsupportedOperationException();

			}

			@Override
			public void sendText(String s, boolean b) throws IOException
			{
				webSocketClient.send(s);
			}

			@Override
			public void sendBinary(ByteBuffer byteBuffer, boolean b) throws IOException
			{
				throw new UnsupportedOperationException();

			}

			@Override
			public OutputStream getSendStream() throws IOException
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public Writer getSendWriter() throws IOException
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public void sendObject(Object o) throws IOException, EncodeException
			{
				throw new UnsupportedOperationException();

			}

			@Override
			public void setBatchingAllowed(boolean b) throws IOException
			{
				throw new UnsupportedOperationException();

			}

			@Override
			public boolean getBatchingAllowed()
			{
				throw new UnsupportedOperationException();
			}

			@Override
			public void flushBatch() throws IOException
			{
				throw new UnsupportedOperationException();

			}

			@Override
			public void sendPing(ByteBuffer byteBuffer) throws IOException, IllegalArgumentException
			{
				throw new UnsupportedOperationException();

			}

			@Override
			public void sendPong(ByteBuffer byteBuffer) throws IOException, IllegalArgumentException
			{
				throw new UnsupportedOperationException();

			}
		};
	}

	@Override
	public String getId()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void close() throws IOException
	{
		webSocketClient.close();
	}

	@Override
	public void close(CloseReason closeReason) throws IOException
	{
		close();
	}

	@Override
	public URI getRequestURI()
	{
		return webSocketClient.getURI();
	}

	@Override
	public Map<String, List<String>> getRequestParameterMap()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getQueryString()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, String> getPathParameters()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<String, Object> getUserProperties()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Principal getUserPrincipal()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<Session> getOpenSessions()
	{
		throw new UnsupportedOperationException();
	}
}
