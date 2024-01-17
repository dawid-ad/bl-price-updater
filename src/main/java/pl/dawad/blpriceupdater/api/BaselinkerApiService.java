package pl.dawad.blpriceupdater.api;

import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.web.util.UriBuilder;
import pl.dawad.blpriceupdater.dao.entity.BaselinkerProduct;
import pl.dawad.blpriceupdater.dao.entity.UpdatedBaselinkerProduct;


import java.util.ArrayList;
import java.util.List;

@Service
public class BaselinkerApiService {
    private final WebClient webClient;
    private final String baseUrl = "https://api.baselinker.com/connector.php";
    private final String blToken;
    public BaselinkerApiService(@Value("${baselinker.token}") String blToken) {
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .defaultHeader("X-BLToken", blToken)
                .build();
        this.blToken = blToken;
    }
    public List<String> getAllPagesWithProducts(){
        List<String> responses = new ArrayList<>();
        int page = 1;
        String response;
        do{
            response = getAllProductsFromPage(page);
            System.out.println("Status BaseLinker: " + page + " pages of products downloaded.");
            if(response == null){
                return responses;
            }
            responses.add(response);
            page++;
        } while (true);
    }
    private String getAllProductsFromPage(int page) {
        String parameters = "{\"storage_id\": \"bl_1\",\"page\": \""+page+"\"}";

        String response = webClient.post()
                .uri(UriBuilder::build)
                .body(BodyInserters.fromValue("method=getProductsList&parameters=" + parameters))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return isPageEmpty(response) ? null : response;
    }
    private boolean isPageEmpty(String response) {
        if(response == null) {
            return true;
        }
        return response.contains("\"products\":[]");
    }
    public void updateProductPrices(List<BaselinkerProduct> baselinkerProductsToUpdate){
        if(baselinkerProductsToUpdate.size() == 0){
            System.out.println("Nothing to update.");
        } else {
            System.out.println("Trying to update: " + baselinkerProductsToUpdate.size() + " products.");
            List<List<BaselinkerProduct>> dividedBaselinkerProductsToUpdate = divideList(baselinkerProductsToUpdate);

            for (List<BaselinkerProduct> baselinkerProducts : dividedBaselinkerProductsToUpdate) {
                String prefixParameters = "{\n" +
                        "   \"storage_id\": \"bl_1\",\n" +
                        "   \"products\": [\n";
                String sufixParameters = " ]}";
                String insideParameters = "";
                String parameters;
                int counter = 1;
                for (BaselinkerProduct baselinkerProduct : baselinkerProducts) {
                    String productId = baselinkerProduct.getPortalId();
                    double price = baselinkerProduct.getPrice();
                    insideParameters = insideParameters + "{\"product_id\": "+ productId +", \"variant_id\": 0, \"price_brutto\": " + price + ", \"tax_rate\": 23}\n";
                    if(counter < baselinkerProducts.size()) {
                        insideParameters+=",";
                    }
                    counter++;
                }
                parameters = prefixParameters + insideParameters + sufixParameters;
                sendUpdate(parameters);
                System.out.println("Updated " + baselinkerProducts.size() + " products on BaseLinker.");
            }
        }
    }
    private List<List<BaselinkerProduct>> divideList(List<BaselinkerProduct> baselinkerProductsToUpdate){
        int productsTargetSize = 50;
        return ListUtils.partition(baselinkerProductsToUpdate, productsTargetSize);
    }
    private void sendUpdate(String parameters) {
        String response = webClient.post()
                .uri(UriBuilder::build)
                .body(BodyInserters.fromValue("method=updateProductsPrices&parameters=" + parameters))
                .retrieve()
                .bodyToMono(String.class)
                .block();
        System.out.println("BaseLinker update status: " + response);
    }

}

