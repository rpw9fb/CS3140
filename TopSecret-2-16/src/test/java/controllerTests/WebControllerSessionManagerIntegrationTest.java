package controllerTests;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import controller.WebController;
import models.Password;
import org.junit.jupiter.api.Test;
import view.HTMLView;
import java.util.concurrent.ConcurrentHashMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class WebControllerSessionManagerIntegrationTest {
    @Test
    void rootContextResponse_validSession() {
        HttpExchange exchange = mock(HttpExchange.class);
        Headers headers = mock(Headers.class);
        Password password = mock(Password.class);
        HTMLView view = mock(HTMLView.class);
        ConcurrentHashMap<String, String> sessions = new ConcurrentHashMap<>();
        sessions.put("1", "user");

        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(headers.getFirst("Cookie")).thenReturn("SESSIONID=1");
        when(view.mainMenu(true)).thenReturn("main menu");
        when(view.mainMenu(false)).thenReturn("main menu");

        WebController wc = new WebController(password);
        wc.view = view;
        wc.sessions = sessions;
        String result = wc.rootContextResponse(exchange);

        assertEquals("main menu", result);
    }


    @Test
    void rootContextResponse_invalidSession() {
        HttpExchange exchange = mock(HttpExchange.class);
        Headers headers = mock(Headers.class);
        Password password = mock(Password.class);
        HTMLView view = mock(HTMLView.class);
        ConcurrentHashMap<String, String> sessions = new ConcurrentHashMap<>();

        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(headers.getFirst("Cookie")).thenReturn("SESSIONID=1");
        when(view.loginPage(null)).thenReturn("login");

        WebController wc = new WebController(password);
        wc.view = view;
        wc.sessions = sessions;
        String result = wc.rootContextResponse(exchange);

        assertEquals("login", result);
    }
}
