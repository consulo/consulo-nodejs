package consulo.javascript.run.debug.inspect;

import com.github.kklisura.cdt.services.WebSocketService;
import com.github.kklisura.cdt.services.exceptions.WebSocketServiceException;
import consulo.logging.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2020-06-17
 */
class WebSocketServiceImplOverJavaWebSocket implements WebSocketService
{
	private static final Logger LOG = Logger.getInstance(WebSocketServiceImplOverJavaWebSocket.class);

	private Consumer<String> messageConsumer;

	private WebSocketClient webSocketClient;

	public WebSocketServiceImplOverJavaWebSocket(String wsUrl)
	{
		try
		{
			connect(new URI(wsUrl));
		}
		catch(WebSocketServiceException | URISyntaxException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void connect(URI uri) throws WebSocketServiceException
	{
		webSocketClient = new WebSocketClient(uri)
		{
			@Override
			public void onOpen(ServerHandshake serverHandshake)
			{
			}

			@Override
			public void onMessage(String s)
			{
				messageConsumer.accept(s);
			}

			@Override
			public void onClose(int i, String s, boolean b)
			{
			}

			@Override
			public void onError(Exception e)
			{
				LOG.warn(e);
			}
		};
		try
		{
			webSocketClient.connectBlocking();
		}
		catch(InterruptedException e)
		{
			throw new WebSocketServiceException(e.getMessage(), e);
		}
	}

	@Override
	public void send(String message) throws WebSocketServiceException
	{
		webSocketClient.send(message);
	}

	@Override
	public void addMessageHandler(Consumer<String> consumer) throws WebSocketServiceException
	{
		messageConsumer = consumer;
	}

	@Override
	public void close()
	{
		webSocketClient.close();
	}

	@Override
	public boolean closed()
	{
		return webSocketClient == null || webSocketClient.isClosed();
	}
}
