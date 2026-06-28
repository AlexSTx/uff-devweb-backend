package com.carlosribeiro.apirestful.messaging;

import com.carlosribeiro.apirestful.auth.service.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Intercepta o handshake do WebSocket STOMP para extrair o JWT enviado como
 * query string (?token=...). Os navegadores não permitem definir cabeçalhos
 * na abertura do WebSocket, então o token viaja na URL.
 *
 * Em caso de sucesso validamos o token e expomos um {@link Principal} (com
 * o id do usuário) no mapa de atributos da sessão STOMP. Sem token válido a
 * conexão é aceita mesmo assim (toy app), mas fica sem principal — quando
 * quisermos endurecer é só negar em {@link #after handshake} ou em um
 * ChannelInterceptor.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);

    private final JwtService jwtService;

    public JwtHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                  ServerHttpResponse response,
                                  WebSocketHandler wsHandler,
                                  Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpServletRequest req = servletRequest.getServletRequest();
            String token = req.getParameter("token");
            if (token != null && jwtService.validateToken(token)) {
                Long usuarioId = jwtService.getUserIdFromToken(token);
                var role = jwtService.getRoleFromToken(token);
                var auth = new UsernamePasswordAuthenticationToken(
                    usuarioId, null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                // Authentication implementa Principal; o Spring STOMP expõe
                // esse atributo como o principal da sessão, permitindo enviar
                // mensagens destinadas a um usuário específico.
                attributes.put("principal", auth);
                attributes.put("usuarioId", usuarioId);
                log.debug("Handshake WS autorizado para usuarioId={}", usuarioId);
            } else {
                log.debug("Handshake WS sem token válido — conexão anônima");
            }
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // nada a fazer
    }
}