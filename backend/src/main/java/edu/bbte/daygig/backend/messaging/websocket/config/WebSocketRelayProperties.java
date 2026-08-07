package edu.bbte.daygig.backend.messaging.websocket.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.websocket.relay")
public record WebSocketRelayProperties(
        boolean enabled,
        String host,
        int port,
        String username,
        String password,
        String virtualHost
) {
}
