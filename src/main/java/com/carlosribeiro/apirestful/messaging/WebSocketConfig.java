package com.carlosribeiro.apirestful.messaging;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Habilita o broker STOMP sobre WebSocket. Os clientes conectam em /ws e
 * assinam tópicos sob /topic/..., e o backend publica neles via
 * SimpMessagingTemplate.
 *
 * Sem SockJS: usamos WebSocket puro (ws://), que basta para os navegadores
 * modernos e para a lib @stomp/stompjs no frontend. O handshake é autenticado
 * pelo JwtHandshakeInterceptor via query string ?token=<jwt>.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    public WebSocketConfig(JwtHandshakeInterceptor jwtHandshakeInterceptor) {
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Tópicos simples em memória. Suficiente para broadcast do
        // EstoqueEsgotadoEvent para todos os clients conectados.
        registry.enableSimpleBroker("/topic");
        // Mensagens de cliente -> backend (ex.: /app/...) — não usadas por
        // enquanto, mas mantidas para futuros comandos dos clientes.
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            // Por default o Spring só aceita handshake same-origin. Como o
            // frontend roda em outra origem (dev: localhost:5173/8081,
            // nginx em prod: localhost:8081), precisamos liberar essas.
            // setAllowedOriginPatterns("*") aceita qualquer host (com
            // curingas em scheme/port) — suficiente para o toy app.
            .setAllowedOriginPatterns("*")
            .addInterceptors(jwtHandshakeInterceptor);
        // Sem .withSockJS(): vamos de WS puro para compatibilidade com
        // @stomp/stompjs (brokerURL ws://...) no frontend.
    }
}