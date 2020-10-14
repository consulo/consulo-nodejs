package consulo.nodejs.run.vm;

import com.github.kklisura.cdt.services.WebSocketService;
import com.github.kklisura.cdt.services.exceptions.WebSocketServiceException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2020-06-17
 */
public class WebSocketImpl implements WebSocketService
{
	private Consumer<String> messageConsumer;

	private WebSocketClient webSocketClient;

	public WebSocketImpl(String wsUrl)
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
				System.out.println(serverHandshake);
			}

			@Override
			public void onMessage(String s)
			{
				messageConsumer.accept(s);
			}

			@Override
			public void onClose(int i, String s, boolean b)
			{
				System.out.println(s);
			}

			@Override
			public void onError(Exception e)
			{
				e.printStackTrace();
			}
		};
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
