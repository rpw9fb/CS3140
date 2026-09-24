package controller;
import com.sun.net.httpserver.HttpExchange;
import java.util.concurrent.ConcurrentHashMap;


public class SessionManager {
    public boolean checkSession(HttpExchange exchange, ConcurrentHashMap<String, String> sessions) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                cookie = cookie.trim();
                if (cookie.startsWith("SESSIONID=")) {
                    String sessionId = cookie.substring("SESSIONID=".length());
                    if (sessions.containsKey(sessionId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isSessionAdmin(HttpExchange exchange, ConcurrentHashMap<String, Boolean> adminSessions ) {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                cookie = cookie.trim();
                if (cookie.startsWith("SESSIONID=")) {
                    String sessionId = cookie.substring("SESSIONID=".length());
                    return adminSessions.getOrDefault(sessionId, false);
                }
            }
        }
        return false;
    }
}
