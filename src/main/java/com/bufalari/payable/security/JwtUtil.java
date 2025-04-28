// Path: employee-and-costs-service/src/main/java/com/bufalari/employee/config/JwtUtil.java
package com.bufalari.payable.security;

import io.jsonwebtoken.*; // Import SignatureException etc.
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException; // Specific import
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey; // Correct import for SecretKey
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Utility for handling JWT tokens, using a configured secret key.
 * Utilitário para manipulação de tokens JWT, usando uma chave secreta configurada.
 */
@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    // Inject the secret key from application properties
    // Injeta a chave secreta das propriedades da aplicação
    @Value("${security.jwt.token.secret-key}")// Make sure this property exists in application.yml/properties
    private String configuredSecretKey;

    private SecretKey secretKey; // Use SecretKey type

    /**
     * Initializes the SecretKey after properties injection.
     * Inicializa a SecretKey após a injeção das propriedades.
     */
    @PostConstruct
    public void init() {
        if (configuredSecretKey == null || configuredSecretKey.isBlank()) {
            log.error("JWT secret key is not configured properly in application properties (jwt.secret).");
            throw new IllegalStateException("JWT secret key must be configured.");
        }
        try {
            // Assume the secret key is a plain string suitable for HS256
            // Assume que a chave secreta é uma string plana adequada para HS256
            this.secretKey = Keys.hmacShaKeyFor(configuredSecretKey.getBytes(StandardCharsets.UTF_8));
            log.info("JWT Secret Key initialized successfully.");
        } catch (Exception e) {
            log.error("Error initializing JWT Secret Key from configured value.", e);
            throw new RuntimeException("Failed to initialize JWT Secret Key", e);
        }
    }

    /**
     * Extracts the username (subject) from the token.
     * Extrai o nome de usuário (subject) do token.
     * @param token The JWT token. / O token JWT.
     * @return The username. / O nome de usuário.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts the expiration date from the token.
     * Extrai a data de expiração do token.
     * @param token The JWT token. / O token JWT.
     * @return The expiration date. / A data de expiração.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extracts a specific claim from the token using a claims resolver function.
     * Extrai uma claim específica do token usando uma função resolvedora de claims.
     * @param token The JWT token. / O token JWT.
     * @param claimsResolver Function to extract the claim. / Função para extrair a claim.
     * @param <T> The type of the claim. / O tipo da claim.
     * @return The extracted claim. / A claim extraída.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the token and extracts all claims. Handles potential exceptions.
     * Faz o parse do token e extrai todas as claims. Trata exceções potenciais.
     * @param token The JWT token. / O token JWT.
     * @return The Claims object. / O objeto Claims.
     * @throws JwtException if the token is invalid or cannot be parsed. / Se o token for inválido ou não puder ser parseado.
     */
    private Claims extractAllClaims(String token) throws JwtException {
         try {
            return Jwts.parserBuilder()
                    .setSigningKey(this.secretKey) // Use the initialized key
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
         } catch (ExpiredJwtException e) {
             log.warn("JWT token is expired: {}", e.getMessage());
             throw e; // Re-throw specific exceptions if needed by callers
         } catch (UnsupportedJwtException e) {
             log.warn("JWT token is unsupported: {}", e.getMessage());
             throw e;
         } catch (MalformedJwtException e) {
             log.warn("JWT token is malformed: {}", e.getMessage());
             throw e;
         } catch (SignatureException e) {
             log.warn("JWT signature validation failed: {}", e.getMessage());
             throw e;
         } catch (IllegalArgumentException e) {
             log.warn("JWT token argument validation failed: {}", e.getMessage());
             throw e;
         }
    }

    /**
     * Checks if the token is expired.
     * Verifica se o token está expirado.
     * @param token The JWT token. / O token JWT.
     * @return true if the token is expired, false otherwise. / true se o token expirou, false caso contrário.
     */
    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true; // If parsing throws ExpiredJwtException, it's expired
        } catch (JwtException e) {
            log.warn("Could not determine expiration due to other JWT exception: {}", e.getMessage());
            return true; // Treat other parsing errors as potentially unsafe/expired
        }
    }

    /**
     * Validates the token against UserDetails (username match and expiration).
     * Valida o token em relação ao UserDetails (correspondência de nome de usuário e expiração).
     * @param token The JWT token. / O token JWT.
     * @param userDetails The UserDetails object. / O objeto UserDetails.
     * @return true if the token is valid for the user, false otherwise. / true se o token for válido para o usuário, false caso contrário.
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException e) {
            // Logged in extractUsername or isTokenExpired
            return false;
        }
    }

    // --- Token Generation (Optional - Keep only if this service generates tokens) ---
    // --- Geração de Token (Opcional - Mantenha apenas se este serviço gerar tokens) ---

    /**
     * Generates a new token for the user (Example - Adapt claims as needed).
     * Gera um novo token para o usuário (Exemplo - Adapte as claims conforme necessário).
     * NOTE: Typically, only the authentication service should generate tokens.
     * NOTA: Tipicamente, apenas o serviço de autenticação deve gerar tokens.
     * @param userDetails User details. / Detalhes do usuário.
     * @return A JWT token string. / Uma string de token JWT.
     */
    /*
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Add relevant claims if needed / Adicione claims relevantes se necessário
        // claims.put("customClaim", "value");
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // Use expiration from config if available, otherwise default
                // Use a expiração da config se disponível, senão use um padrão
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Example: 10 hours
                .signWith(this.secretKey, SignatureAlgorithm.HS256) // Use the initialized key
                .compact();
    }
    */
}