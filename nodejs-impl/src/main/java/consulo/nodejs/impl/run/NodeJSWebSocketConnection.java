package consulo.nodejs.impl.run;

import com.github.kklisura.cdt.services.WebSocketService;
import com.github.kklisura.cdt.services.exceptions.WebSocketServiceException;
import consulo.application.Application;
import consulo.http.ws.WebSocketConnectionBuilderFactory;
import consulo.http.ws.WebSocketSession;
import consulo.platform.Platform;
import consulo.util.collection.SmartList;

import java.net.URI;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author VISTALL
 * @since 2026-02-15
 */
public class NodeJSWebSocketConnection implements WebSocketService {
    private WebSocketSession mySession;

    private List<Consumer<String>> myConsumers = new SmartList<>();

    public NodeJSWebSocketConnection(String wsUrl) throws Exception {
        WebSocketConnectionBuilderFactory factory = Application.get().getInstance(WebSocketConnectionBuilderFactory.class);

        mySession = factory.newBuilder(Platform.current())
            .onText((webSocketSession, msg) -> {
                for (Consumer<String> consumer : myConsumers) {
                    consumer.accept(msg);
                }
            })
            .connect(wsUrl);
    }

    @Override
    public void connect(URI uri) throws WebSocketServiceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void send(String message) throws WebSocketServiceException {
        if (mySession != null) {
            mySession.send(message);
        }
    }

    @Override
    public void addMessageHandler(Consumer<String> consumer) throws WebSocketServiceException {
        myConsumers.add(consumer);
    }

    @Override
    public void close() {
        if (mySession != null) {
            mySession.close();
            mySession = null;
        }
    }

    @Override
    public boolean closed() {
        return mySession == null;
    }
}
