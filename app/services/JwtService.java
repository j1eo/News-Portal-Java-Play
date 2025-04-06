package services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Claim;

import java.util.Date;

public class JwtService {
    private static final String SECRET_KEY = "tu_clave_secreta_32_caracteres_123456";
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);
    private static final int EXPIRATION_HOURS = 24;

    public String generarToken(String userId, String userRole) {
        try {
            return JWT.create()
                .withSubject(userId)
                .withClaim("role", userRole)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_HOURS * 3600 * 1000))
                .sign(ALGORITHM);
        } catch (JWTCreationException e) {
            throw new RuntimeException("Error al generar token JWT", e);
        }
    }

    public boolean validarToken(String token) {
        try {
            JWT.require(ALGORITHM).build().verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    public String obtenerSubject(String token) {
        try {
            return JWT.require(ALGORITHM).build().verify(token).getSubject();
        } catch (JWTVerificationException e) {
            return null;
        }
    }

    public String obtenerUserRole(String token) {
        try {
            return JWT.require(ALGORITHM).build().verify(token).getClaim("role").asString();
        } catch (JWTVerificationException e) {
            return null;
        }
    }
    public String obtenerClaim(String token, String claimName) {
        try {
            Claim claim = JWT.require(ALGORITHM).build().verify(token).getClaim(claimName);
            return claim != null ? claim.asString() : null;
        } catch (JWTVerificationException e) {
            return null;
        }
    }
}