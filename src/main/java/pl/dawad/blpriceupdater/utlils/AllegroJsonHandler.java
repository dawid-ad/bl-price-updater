package pl.dawad.blpriceupdater.utlils;

import com.jayway.jsonpath.JsonPath;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import pl.dawad.blpriceupdater.dao.entity.AllegroProduct;

import java.util.ArrayList;
import java.util.List;
@Component
public class AllegroJsonHandler {
    public List<AllegroProduct> getAllegroProductsFromResponse(List<String> responses) {
        List<AllegroProduct> products = new ArrayList<>();

        for (String response : responses) {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray offersArray = jsonObject.getJSONArray("offers");

            for (int i = 0; i < offersArray.length(); i++) {
                JSONObject offerObject = offersArray.getJSONObject(i);
                String productId = offerObject.getString("id");
                String name = offerObject.getString("name");

                JSONObject sellingModeObject = offerObject.getJSONObject("sellingMode");
                JSONObject priceObject = sellingModeObject.getJSONObject("price");
                String price = priceObject.getString("amount");
                products.add(new AllegroProduct(productId,null,null,name,Double.parseDouble(price)));
            }
        }
        return products;
    }
    public String getAllegroEanFromResponse(String response) {
        String ean = JsonPath.read(response, "$..[?(@.name == 'EAN (GTIN)')].values[0]").toString();
        return ean.replaceAll("[\\[\\]\"]", "");
    }
//    public String getAllegroEanFromResponse(String response) {
//    System.out.println(response);
//        String ean = "";
//        JSONObject jsonObject = new JSONObject(response);
//
//        JSONArray parametersArray = jsonObject.getJSONArray("parameters");
//
//        for (int i = 0; i < parametersArray.length(); i++) {
//            JSONObject parameterObject = parametersArray.getJSONObject(i);
//            if (parameterObject.has("values")) {
//                JSONArray valuesArray = parameterObject.getJSONArray("values");
//                if (valuesArray.length() > 0) {
//                    String value = valuesArray.getString(0);
//                    if(value.length() == 13){
//                        ean = value;
//                    }
//                }
//            }
//        }
//        System.out.println(ean);
//        return ean;
//    }
}
