package consulo.nodejs.run.vm;

import org.chromium.sdk.internal.transport.Connection;
import org.chromium.sdk.internal.transport.Message;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author VISTALL
 * @since 2020-06-16
 */
public class WebSocketConnection extends WebSocketClient implements Connection
{
	private NetListener myNetListener;

	public WebSocketConnection(String url) throws URISyntaxException
	{
		super(new URI(url));
	}

	@Override
	public void setNetListener(NetListener netListener)
	{
		myNetListener = netListener;
	}

	@Override
	public void send(Message message)
	{
		String text = message.getContent();
		System.out.println("send " + text);
		send(text);
	}

	@Override
	public void runInDispatchThread(Runnable callback)
	{
		System.out.println("runInDispatchThread");
	}

	@Override
	public void start() throws IOException
	{
		connect();
	}

	@Override
	public void onOpen(ServerHandshake serverHandshake)
	{
		System.out.println("onOpen " + serverHandshake);

	}

	@Override
	public void onMessage(String text)
	{
		System.out.println("onMessage " + text);

	}

	@Override
	public void onClose(int i, String error, boolean b)
	{
		System.out.println(error);
	}

	@Override
	public void onError(Exception e)
	{
		e.printStackTrace();
	}

	@Override
	public boolean isConnected()
	{
		return false;
	}
}
