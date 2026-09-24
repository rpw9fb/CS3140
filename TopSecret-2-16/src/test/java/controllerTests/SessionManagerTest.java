package controllerTests;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import controller.SessionManager;
import controller.WebController;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SessionManagerTest {
    @Test
    void checkSession_validSession() {
        HttpExchange exchange = mock(HttpExchange.class);
        Headers headers = mock(Headers.class);
        ConcurrentHashMap<String, String> sessions = new ConcurrentHashMap<>();
        sessions.put("1", "user");

        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(headers.getFirst("Cookie")).thenReturn("SESSIONID=1");

        SessionManager sm = new SessionManager();
        boolean result = sm.checkSession(exchange, sessions);

        assertTrue(result);
    }


    @Test
    void checkSession_invalidSession() {
        HttpExchange exchange = mock(HttpExchange.class);
        Headers headers = mock(Headers.class);
        ConcurrentHashMap<String, String> sessions = new ConcurrentHashMap<>();

        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(headers.getFirst("Cookie")).thenReturn("SESSIONID=1");

        SessionManager sm = new SessionManager();
        boolean result = sm.checkSession(exchange, sessions);

        assertFalse(result);
    }


    @Test
    void isSessionAdmin_adminSession() {
        HttpExchange exchange = mock(HttpExchange.class);
        Headers headers = mock(Headers.class);
        ConcurrentHashMap<String, Boolean> sessions = new ConcurrentHashMap<>();
        sessions.put("1", true);

        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(headers.getFirst("Cookie")).thenReturn("SESSIONID=1");

        SessionManager sm = new SessionManager();
        boolean result = sm.isSessionAdmin(exchange, sessions);

        assertTrue(result);
    }


    @Test
    void isSessionAdmin_nonAdminSession() {
        HttpExchange exchange = mock(HttpExchange.class);
        Headers headers = mock(Headers.class);
        ConcurrentHashMap<String, Boolean> sessions = new ConcurrentHashMap<>();
        sessions.put("1", false);

        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(headers.getFirst("Cookie")).thenReturn("SESSIONID=1");

        SessionManager sm = new SessionManager();
        boolean result = sm.isSessionAdmin(exchange, sessions);

        assertFalse(result);
    }


    @Test
    void isSessionAdmin_invalidSession() {
        HttpExchange exchange = mock(HttpExchange.class);
        Headers headers = mock(Headers.class);
        ConcurrentHashMap<String, Boolean> sessions = new ConcurrentHashMap<>();

        when(exchange.getRequestHeaders()).thenReturn(headers);
        when(headers.getFirst("Cookie")).thenReturn("SESSIONID=1");

        SessionManager sm = new SessionManager();
        boolean result = sm.isSessionAdmin(exchange, sessions);

        assertFalse(result);
    }
}
