// Location: src/main/java/com/example/backend/service/GoogleOAuthService.java
package com.example.backend.form.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleOAuthService {
    
    @Value("${google.client-id}")
    private String googleClientId;
    
    private GoogleIdTokenVerifier verifier;
    
    private GoogleIdTokenVerifier getVerifier() {
        if (verifier == null) {
            verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
        }
        return verifier;
    }
    
    public GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdToken idToken = getVerifier().verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                
                // Print token details for debugging
                System.out.println("Google ID: " + payload.getSubject());
                System.out.println("Email: " + payload.getEmail());
                System.out.println("Email verified: " + payload.getEmailVerified());
                System.out.println("Name: " + payload.get("name"));
                System.out.println("Picture URL: " + payload.get("picture"));
                System.out.println("Locale: " + payload.get("locale"));
                System.out.println("Family name: " + payload.get("family_name"));
                System.out.println("Given name: " + payload.get("given_name"));
                
                return payload;
            } else {
                throw new RuntimeException("Invalid Google ID token");
            }
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to verify Google ID token", e);
        }
    }
    
    public String extractEmail(GoogleIdToken.Payload payload) {
        return payload.getEmail();
    }
    
    public String extractGoogleId(GoogleIdToken.Payload payload) {
        return payload.getSubject();
    }
    
    public String extractName(GoogleIdToken.Payload payload) {
        return (String) payload.get("name");
    }
    
    public String extractPictureUrl(GoogleIdToken.Payload payload) {
        return (String) payload.get("picture");
    }
    
    public String extractGivenName(GoogleIdToken.Payload payload) {
        return (String) payload.get("given_name");
    }
    
    public String extractFamilyName(GoogleIdToken.Payload payload) {
        return (String) payload.get("family_name");
    }
    
    public Boolean isEmailVerified(GoogleIdToken.Payload payload) {
        return payload.getEmailVerified();
    }
}