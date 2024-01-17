package pl.dawad.blpriceupdater.manager.external;

import org.springframework.stereotype.Service;
import pl.dawad.blpriceupdater.dao.entity.UpdatedBaselinkerProduct;

import java.util.List;
@Service
public class UpdateContext {
    private List<UpdatedBaselinkerProduct> updatedProducts;

    public UpdateContext() {
    }

    public List<UpdatedBaselinkerProduct> getUpdatedProducts() {
        return updatedProducts;
    }
    public void setUpdatedProducts(List<UpdatedBaselinkerProduct> updatedProducts) {
        this.updatedProducts = updatedProducts;
    }
}