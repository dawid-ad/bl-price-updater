package pl.dawad.blpriceupdater.utlils;

import com.google.gson.*;
import org.springframework.stereotype.Component;
import pl.dawad.blpriceupdater.dao.entity.BaselinkerProduct;

import java.util.ArrayList;
import java.util.List;

@Component
public class BaselinkerJsonHandler {
    private static final String PRODUCT_ID_KEY = "product_id";
    private static final String EAN_KEY = "ean";
    private static final String SKU_KEY = "sku";
    private static final String NAME_KEY = "name";
    private static final String PRICE_BRUTTO_KEY = "price_brutto";
    private final Gson gson = new Gson();

    public List<BaselinkerProduct> getBaselinkerProductsFromResponse(List<String> responses) {
        List<BaselinkerProduct> products = new ArrayList<>();

        for (String response : responses) {
            try {
                JsonObject jsonObjectResponse = gson.fromJson(response, JsonObject.class);

                if (jsonObjectResponse.has("products")) {
                    JsonArray productsJsonArray = jsonObjectResponse.getAsJsonArray("products");

                    for (JsonElement product : productsJsonArray) {
                        JsonObject productObject = product.getAsJsonObject();

                        JsonElement productIdElement = productObject.get(PRODUCT_ID_KEY);
                        JsonElement eanElement = productObject.get(EAN_KEY);
                        JsonElement skuElement = productObject.get(SKU_KEY);
                        JsonElement nameElement = productObject.get(NAME_KEY);
                        JsonElement priceBruttoElement = productObject.get(PRICE_BRUTTO_KEY);

                        products.add(new BaselinkerProduct(
                                    productIdElement.isJsonNull() ? null : productIdElement.getAsString(),
                                    eanElement.isJsonNull() ? null : eanElement.getAsString().replace(" ", "").replace("\t",""),
                                    skuElement.isJsonNull() ? null : skuElement.getAsString(),
                                    nameElement.isJsonNull() ? null : nameElement.getAsString(),
                                    priceBruttoElement.isJsonNull() ? 0.0 : priceBruttoElement.getAsDouble()
                            ));
                    }
                }
            } catch (JsonParseException | NullPointerException e) {
                e.printStackTrace();
            }
        }
        return products;
    }
}
