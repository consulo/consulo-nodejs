package consulo.nodejs.run.vm;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.Set;

/**
 * @author VISTALL
 * @since 2020-06-17
 */
public class WebSocketBackend implements WebSocketContainer
{
	public static class WebSocketClientImpl extends WebSocketClient
	{

		private final Object o;
		private Set<MessageHandler> messageHandlers;

		public WebSocketClientImpl(URI serverUri, Object o)
		{
			super(serverUri);
			this.o = o;
		}

		@Override
		public void onOpen(ServerHandshake serverHandshake)
		{

		}

		@Override
		public void onMessage(String s)
		{
			for(MessageHandler messageHandler : messageHandlers)
			{
				if(messageHandler instanceof MessageHandler.Whole)
				{
					((MessageHandler.Whole) messageHandler).onMessage(s);
				}
			}
		}

		@Override
		public void onClose(int i, String s, boolean b)
		{
			throw new UnsupportedOperationException();

		}

		@Override
		public void onError(Exception e)
		{
			e.printStackTrace();
		}


		public void setHandlers(Set<MessageHandler> messageHandlers)
		{

			this.messageHandlers = messageHandlers;
		}
	}

	@Override
	public long getDefaultAsyncSendTimeout()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setAsyncSendTimeout(long l)
	{
		throw new UnsupportedOperationException();

	}

	@Override
	public Session connectToServer(Object o, URI uri) throws DeploymentException, IOException
	{
		WebSocketClientImpl webSocketClient = new WebSocketClientImpl(uri, o);
		WebSocketSession session = new WebSocketSession(webSocketClient, o);
		webSocketClient.connect();
		return session;
	}

	@Override
	public Session connectToServer(Class<?> aClass, URI uri) throws DeploymentException, IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Session connectToServer(Endpoint endpoint, ClientEndpointConfig clientEndpointConfig, URI uri) throws DeploymentException, IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Session connectToServer(Class<? extends Endpoint> aClass, ClientEndpointConfig clientEndpointConfig, URI uri) throws DeploymentException, IOException
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public long getDefaultMaxSessionIdleTimeout()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDefaultMaxSessionIdleTimeout(long l)
	{
		throw new UnsupportedOperationException();

	}

	@Override
	public int getDefaultMaxBinaryMessageBufferSize()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDefaultMaxBinaryMessageBufferSize(int i)
	{
		throw new UnsupportedOperationException();

	}

	@Override
	public int getDefaultMaxTextMessageBufferSize()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void setDefaultMaxTextMessageBufferSize(int i)
	{
		throw new UnsupportedOperationException();

	}

	@Override
	public Set<Extension> getInstalledExtensions()
	{
		throw new UnsupportedOperationException();
	}
}
