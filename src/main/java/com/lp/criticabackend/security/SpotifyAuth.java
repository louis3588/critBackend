package com.lp.criticabackend.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lp.criticabackend.AppLogger;
import com.lp.criticabackend.util.WebUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Component
public class SpotifyAuth {

    private static final AppLogger log = AppLogger.getLogger(SpotifyAuth.class);
    private final WebClient webClient;
    private static final HttpClient httpClient = WebUtil.httpClient(15);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${spotify.client-id}")
    private String clientId;
    @Value("${spotify.client-secret}")
    private String clientSecret;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private String cachedToken = null;
    private Instant tokenExpiresAt = Instant.MIN;

    public SpotifyAuth() {
        this.webClient = WebClient
                .builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    private boolean isTokenValid(){
        return cachedToken != null
                && Instant.now().isBefore(tokenExpiresAt.minusSeconds(60));
    }

    private void fetchNewToken(){
        try{
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "client_credentials");
            String url = "https://accounts.spotify.com/api/token";

            String res = webClient
                    .post()
                    .uri(url)
                    .headers(h -> h.setBasicAuth(clientId, clientSecret))
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(BodyInserters.fromFormData(params))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode json = objectMapper.readTree(res);
            cachedToken = json.get("access_token").asText();
            int expiresIn = json.get("expires_in").asInt(3600);
            tokenExpiresAt = Instant.now().plusSeconds(expiresIn);

            log.info("Spotify token refreshed — expires in" + expiresIn);
        } catch (Exception e) {
            log.error("Failed to fetch spotify token", e);
        }
    }

    public String getToken(){
        lock.readLock().lock();
        try{
            if(isTokenValid()){
                return cachedToken;
            }
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try{
            if(isTokenValid()){
                return cachedToken;
            }
            fetchNewToken();
            return cachedToken;
        } finally {
            lock.writeLock().unlock();
        }
    }

    @PostConstruct
    public void init(){
        log.info("Initializing Spotify Auth, fetching token");
        getToken();
    }

    @Scheduled(fixedRateString = "3000000")
    public void scheduledRefresh(){
        log.info("proactive Spotify token refresh triggered");
        lock.writeLock().lock();
        try{
            fetchNewToken();
        }finally {
            lock.writeLock().unlock();
        }
    }
}
