package consulo.nodejs.impl.run;

import com.github.kklisura.cdt.services.WebSocketService;
import com.github.kklisura.cdt.services.exceptions.WebSocketServiceException;
import consulo.logging.Logger;
import consulo.util.collection.SmartList;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2026-02-15
 */
public class NodeJSWebSocketConnection implements WebSocketService {
    private static final Logger LOG = Logger.getInstance(NodeJSWebSocketConnection.class);

    private WebSocket myWs;

    private List<Consumer<String>> myConsumers = new SmartList<>();

    public NodeJSWebSocketConnection(String wsUrl) {
        HttpClient client = HttpClient.newHttpClient();

        myWs = client.newWebSocketBuilder()
            .buildAsync(URI.create(wsUrl), new WebSocket.Listener() {
                private final StringBuilder myBuff = new StringBuilder();

                @Override
                public void onOpen(WebSocket webSocket) {
                    webSocket.request(1);
                }

                @Override
                public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                    myBuff.append(data);

                    if (last) {
                        String msg = myBuff.toString();
                        
                        myBuff.setLength(0);

                        for (Consumer<String> consumer : myConsumers) {
                            consumer.accept(msg);
                        }
                    }

                    webSocket.request(1);
                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
                    webSocket.request(1);
                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
                    myWs = null;
                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public void onError(WebSocket webSocket, Throwable error) {
                    LOG.warn(error);
                }
            }).join();
    }

    @Override
    public void connect(URI uri) throws WebSocketServiceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(String message) throws WebSocketServiceException {
        if (myWs != null) {
            myWs.sendText(message, true);
        }
    }

    @Override
    public void addMessageHandler(Consumer<String> consumer) throws WebSocketServiceException {
        myConsumers.add(consumer);
    }

    @Override
    public void close() {
        myWs.abort();
        myWs = null;
    }

    @Override
    public boolean closed() {
        return myWs == null;
    }
}
