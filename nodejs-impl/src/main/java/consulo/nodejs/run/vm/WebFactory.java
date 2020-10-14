package consulo.nodejs.run.vm;

import com.github.kklisura.cdt.services.factory.WebSocketContainerFactory;

import javax.websocket.WebSocketContainer;

/**
 * @author VISTALL
 * @since 2020-06-17
 */
public class WebFactory implements WebSocketContainerFactory
{
	@Override
	public WebSocketContainer getWebSocketContainer()
	{
		return new WebSocketBackend();
	}
}
