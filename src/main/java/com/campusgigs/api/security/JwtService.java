package com.campusgigs.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Responsavel por emitir e validar os tokens JWT usados como mecanismo de
 * autenticacao stateless da API.
 *
 * Decisao de implementacao: o token carrega apenas o e-mail do usuario
 * (subject), alem de emissao/expiracao. Nenhum dado sensivel (senha/hash) e
 * incluido no token.
 *
 * IMPORTANTE sobre autorizacao: a role usada para autorizar cada requisicao
 * NAO vem de dentro do token. O JwtAuthenticationFilter extrai o e-mail do
 * token e usa o UserDetailsService para carregar o usuario (e a role atual
 * dele) do banco a cada requisicao - ver JwtAuthenticationFilter. Isso e
 * proposital: se a role de alguem mudar, o efeito e imediato, sem precisar
 * esperar o token expirar ou reemitir um novo.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${jwt.secret}") String secret,
                       @Value("${jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserDetails userDetails) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    /** Retorna o e-mail (subject) do token, ou null se o token for invalido/expirado. */
    public String extractEmail(String token) {
        Claims claims = parseClaims(token);
        return claims == null ? null : claims.getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = parseClaims(token);
        if (claims == null) {
            return false;
        }
        boolean sameUser = userDetails.getUsername().equals(claims.getSubject());
        boolean notExpired = claims.getExpiration() != null && claims.getExpiration().after(new Date());
        return sameUser && notExpired;
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            // Assinatura invalida, token malformado ou expirado: tratado como
            // "sem autenticacao", nunca propagado com detalhe interno ao cliente.
            return null;
        }
    }
}
