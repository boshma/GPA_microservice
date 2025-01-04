package com.microservice.user_service.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

@Component
public class JwtUtil {

    private final String SECRET_KEY = "secretsecretsecretsecret"; // Use a secure key in production
    private final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 hours

    /**
     * Generates a JWT token for the given userId.
     * 
     * @param userId The user ID to include in the token.
     * @return The generated JWT token as a String.
     */
    public String generateToken(String userId) {
        try {
            // Create the JWT claims
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userId)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .build();

            // Create the JWS header with HMAC-SHA256 algorithm
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

            // Create the signed JWT
            SignedJWT signedJWT = new SignedJWT(header, claims);

            // Sign the JWT with the secret key
            JWSSigner signer = new MACSigner(SECRET_KEY.getBytes());
            signedJWT.sign(signer);

            // Serialize the token
            return signedJWT.serialize();

        } catch (JOSEException e) {
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    /**
     * Extracts the userId from the provided JWT token.
     * 
     * @param token The JWT token.
     * @return The userId (subject) from the token.
     */
    public String extractUserId(String token) {
        try {
            // Parse the token
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Return the userId (subject) from the claims
            return signedJWT.getJWTClaimsSet().getSubject();

        } catch (ParseException e) {
            throw new RuntimeException("Error parsing JWT token", e);
        }
    }

    /**
     * Validates the provided JWT token.
     * 
     * @param token The JWT token.
     * @return True if the token is valid, false otherwise.
     */
    public boolean validateToken(String token) {
        try {
            // Parse the token
            SignedJWT signedJWT = SignedJWT.parse(token);

            // Verify the signature
            JWSVerifier verifier = new MACVerifier(SECRET_KEY.getBytes());
            if (!signedJWT.verify(verifier)) {
                return false;
            }

            // Check token expiration
            Date expirationTime = signedJWT.getJWTClaimsSet().getExpirationTime();
            return expirationTime != null && expirationTime.after(new Date());

        } catch (ParseException | JOSEException e) {
            return false; // Return false if the token is invalid
        }
    }
}