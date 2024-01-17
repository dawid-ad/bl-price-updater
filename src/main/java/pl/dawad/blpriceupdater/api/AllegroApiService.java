package pl.dawad.blpriceupdater.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

@Service
public class AllegroApiService {
    private final WebClient webClientAuthorization;
    private final WebClient webClientGetters;
    private final String clientId;  // Allegro API client ID
    private final String clientSecret;  // Allegro API client secret
    private final String REFRESH_TOKEN_PROPERTIES = "rt.properties";
    private String token = null;
    public AllegroApiService(@Value("${allegro.client-id}") String clientId,
                             @Value("${allegro.client-secret}") String clientSecret) {
        this.webClientAuthorization = WebClient.builder()
                .baseUrl("https://allegro.pl")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
        this.webClientGetters = WebClient.builder()
                .baseUrl("https://api.allegro.pl")
                .build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }
    public List<String> getAllPagesWithProducts(){
        List<String> responses = new ArrayList<>();
        int offset = 0;
        String response;
        do{
            response = getAllProductsFromPage(offset);
            System.out.println("Status Allegro: " + offset + " products downloaded.");
            if(response == null){
                return responses;
            }
            responses.add(response);
            offset += 100;
        } while (true);
    }
    public String getAllProductsFromPage(int offset) {
        if (token == null){
            token = getToken();
        }
        String response =  webClientGetters.get()
                .uri(uriBuilder -> uriBuilder.path("/sale/offers")
                        .queryParam("limit", 100)
                        .queryParam("offset", offset)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.ACCEPT, "application/vnd.allegro.public.v1+json")
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return isPageEmpty(response) ? null : response;
    }
    private boolean isPageEmpty(String response) {
        if(response == null) {
            return true;
        }
        return response.contains("\"count\":0");
    }
    public String getResponseForOneAllegroProduct(String allegroId) {
        if (token == null){
            token = getToken();
        }
        return webClientGetters.get()
                .uri(uriBuilder -> uriBuilder.path("/sale/product-offers/"+allegroId)
                        .build())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.ACCEPT, "application/vnd.allegro.public.v1+json")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
    public String getToken() {
        String response = webClientAuthorization.post()
                .uri("/auth/oauth/token")
                .header(HttpHeaders.AUTHORIZATION, "Basic " + getClientCredentials())
                .body(BodyInserters.fromFormData("grant_type", "refresh_token")
                        .with("refresh_token", Objects.requireNonNull(getRefreshToken())))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        saveRefreshToken(extractRefreshToken(response));
        token = extractAccessToken(response);
        return token;
    }
    private String extractAccessToken(String response) {
        return extractJson(response,"access_token");
    }

    private String extractRefreshToken(String response) {
        return extractJson(response,"refresh_token");
    }
    private String extractJson(String response, String valueKey){
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return jsonNode.get(valueKey).asText();
    }

    public void saveRefreshToken(String newRefreshToken) {
        try (InputStream input = new FileInputStream(REFRESH_TOKEN_PROPERTIES)) {
            Properties properties = new Properties();
            properties.load(input);
            properties.setProperty("allegro.refresh-token", newRefreshToken);
            try (OutputStream output = new FileOutputStream(REFRESH_TOKEN_PROPERTIES)) {
                properties.store(output, null);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private String getRefreshToken(){
        try(InputStream input = new FileInputStream(REFRESH_TOKEN_PROPERTIES)){
            Properties properties = new Properties();
            properties.load(input);
            return properties.getProperty("allegro.refresh-token");
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }

    private String getClientCredentials() {
        String credentials = clientId + ":" + clientSecret;
        return java.util.Base64.getEncoder().encodeToString(credentials.getBytes());
    }
}
