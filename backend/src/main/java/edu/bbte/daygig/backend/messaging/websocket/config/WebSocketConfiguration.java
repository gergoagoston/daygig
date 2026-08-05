package edu.bbte.daygig.backend.messaging.websocket.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.config.StompBrokerRelayRegistration;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@EnableConfigurationProperties(WebSocketRelayProperties.class)
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {

    private final String[] allowedOriginPatterns;
    private final WebSocketRelayProperties relayProperties;

    public WebSocketConfiguration(
            @Value("${app.websocket.allowed-origin-patterns:http://localhost:5173}")
            String[] allowedOriginPatterns,
            WebSocketRelayProperties relayProperties
    ) {
        this.allowedOriginPatterns = allowedOriginPatterns;
        this.relayProperties = relayProperties;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setUserDestinationPrefix("/user");
        registry.setApplicationDestinationPrefixes("/app");

        if (!relayProperties.enabled()) {
            registry.enableSimpleBroker("/queue", "/topic", "/exchange");
            return;
        }

        StompBrokerRelayRegistration relay = registry.enableStompBrokerRelay(
                "/queue",
                "/topic",
                "/exchange"
        );
        relay
                .setRelayHost(relayProperties.host())
                .setRelayPort(relayProperties.port())
                .setClientLogin(relayProperties.username())
                .setClientPasscode(relayProperties.password())
                .setSystemLogin(relayProperties.username())
                .setSystemPasscode(relayProperties.password())
                .setVirtualHost(relayProperties.virtualHost())
                .setUserDestinationBroadcast("/topic/unresolved-user-destination")
                .setUserRegistryBroadcast("/topic/simp-user-registry");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry
                .addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOriginPatterns);
    }
}
