package consulo.nodejs.run.vm;

import org.chromium.sdk.DebugEventListener;
import org.chromium.sdk.internal.JsonUtil;
import org.chromium.sdk.internal.transport.Connection;
import org.chromium.sdk.internal.transport.Message;
import org.chromium.sdk.internal.v8native.*;
import org.chromium.sdk.internal.v8native.protocol.output.DebuggerMessage;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

/**
 * @author VISTALL
 * @since 2020-06-16
 */
public class WebSocketVmImpl extends JavascriptVmImpl
{
	private enum ConnectionState
	{
		INIT,
		CONNECTING,
		EXPECTING_HANDSHAKE,
		CONNECTED,
		DETACHED
	}

	private static class V8CommandOutputImpl implements V8CommandOutput
	{
		private final Connection outputConnection;

		V8CommandOutputImpl(Connection outputConnection)
		{
			this.outputConnection = outputConnection;
		}

		public void send(DebuggerMessage debuggerMessage, boolean immediate)
		{
			String jsonString = JsonUtil.streamAwareToJson(debuggerMessage);
			Message message = new Message(Collections.<String, String>emptyMap(), jsonString);

			outputConnection.send(message);
			// TODO(peter.rybin): support {@code immediate} in protocol
		}

		public void runInDispatchThread(Runnable callback)
		{
			outputConnection.runInDispatchThread(callback);
		}
	}

	private DebugEventListener debugEventListener = null;

	private final DebugSession debugSession;

	private static final V8ContextFilter CONTEXT_FILTER = contextHandle -> {
		// We do not check context in standalone V8 mode.
		return true;
	};

	private final DebugSessionManager sessionManager = new DebugSessionManager()
	{
		public DebugEventListener getDebugEventListener()
		{
			return debugEventListener;
		}

		public void onDebuggerDetached()
		{
			// Never called for standalone.
		}
	};

	private ConnectionState connectionState = ConnectionState.INIT;

	private WebSocketConnection connection;

	public WebSocketVmImpl(String url)
	{
		WebSocketConnection connection;
		try
		{
			connection = new WebSocketConnection(url);
		}
		catch(URISyntaxException e)
		{
			throw new RuntimeException(e);
		}

		this.connection = connection;

		V8CommandOutputImpl v8CommandOutput = new V8CommandOutputImpl(connection);
		this.debugSession = new DebugSession(sessionManager, CONTEXT_FILTER, v8CommandOutput, this);
	}

	@Override
	public DebugSession getDebugSession()
	{
		return null;
	}

	@Override
	public boolean detach()
	{
		return false;
	}

	@Override
	public boolean isAttached()
	{
		return false;
	}

	public void attach(DebugEventListener listener) throws IOException
	{
		debugEventListener = listener;

		connectionState = ConnectionState.CONNECTING;

		Connection.NetListener netListener = new Connection.NetListener()
		{
			public void connectionClosed()
			{
			}

			public void eosReceived()
			{
				debugSession.getV8CommandProcessor().processEos();
				onDebuggerDetachedImpl(null);
			}

			public void messageReceived(Message message)
			{
				JSONObject json;
				try
				{
					json = JsonUtil.jsonObjectFromJson(message.getContent());
				}
				catch(ParseException e)
				{
					//LOGGER.log(Level.SEVERE, "Invalid JSON received: {0}", message.getContent());
					return;
				}
				debugSession.getV8CommandProcessor().processIncomingJson(json);
			}
		};
		connection.setNetListener(netListener);

		connection.start();

		connectionState = ConnectionState.EXPECTING_HANDSHAKE;

		//		Handshaker.StandaloneV8.RemoteInfo remoteInfo;
		//		try
		//		{
		//			remoteInfo = handshaker.getRemoteInfo().get(WAIT_FOR_HANDSHAKE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
		//		}
		//		catch(InterruptedException e)
		//		{
		//			throw new RuntimeException(e);
		//		}
		//		catch(ExecutionException e)
		//		{
		//			throw newIOException("Failed to get version", e);
		//		}
		//		catch(TimeoutException e)
		//		{
		//			throw newIOException("Timed out waiting for handshake", e);
		//		}
		//
		//		String versionString = remoteInfo.getProtocolVersion();
		//		// TODO(peter.rybin): check version here
		//		if(versionString == null)
		//		{
		//			throw new UnsupportedVersionException(null, null);
		//		}

		//this.savedRemoteInfo = remoteInfo;

		//this.debugEventListener = listener;

		debugSession.startCommunication();

		connectionState = ConnectionState.CONNECTED;
	}

	private boolean onDebuggerDetachedImpl(Exception cause)
	{
		return false;
	}
}
