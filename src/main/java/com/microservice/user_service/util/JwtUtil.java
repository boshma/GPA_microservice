package com.microservice.user_service.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:secretsecretsecretsecret}")
    private String secretKey;

    @Value("${jwt.expiration:86400000}")
    private long expirationTime;

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
                    .expirationTime(new Date(System.currentTimeMillis() + this.expirationTime))
                    .build();

            // Create the JWS header with HMAC-SHA256 algorithm
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);

            // Create the signed JWT
            SignedJWT signedJWT = new SignedJWT(header, claims);

            // Sign the JWT with the secret key
            JWSSigner signer = new MACSigner(this.secretKey.getBytes());
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
            JWSVerifier verifier = new MACVerifier(this.secretKey.getBytes());
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

    public String generateTokenWithCustomExpiration(String userId, long expirationOffset) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(userId)
                    .issueTime(new Date())
                    .expirationTime(new Date(System.currentTimeMillis() + expirationOffset))
                    .build();
    
            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            SignedJWT signedJWT = new SignedJWT(header, claims);
            JWSSigner signer = new MACSigner(this.secretKey.getBytes());
            signedJWT.sign(signer);
    
            return signedJWT.serialize();
    
        } catch (JOSEException e) {
            throw new RuntimeException("Error generating JWT token", e);
        }
    }

    
}