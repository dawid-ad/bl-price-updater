package pl.dawad.blpriceupdater.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.dawad.blpriceupdater.api.AllegroApiService;
import pl.dawad.blpriceupdater.api.BaselinkerApiService;
import pl.dawad.blpriceupdater.dao.AllegroProductRepository;
import pl.dawad.blpriceupdater.dao.BaselinkerProductRepository;
import pl.dawad.blpriceupdater.dao.ProductRepository;
import pl.dawad.blpriceupdater.dao.entity.AllegroProduct;
import pl.dawad.blpriceupdater.dao.entity.BaselinkerProduct;
import pl.dawad.blpriceupdater.dao.entity.Product;
import pl.dawad.blpriceupdater.utlils.AllegroJsonHandler;
import pl.dawad.blpriceupdater.utlils.BaselinkerJsonHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ProductManager {
    private final AllegroProductRepository allegroProductRepository;
    private final BaselinkerProductRepository baselinkerProductRepository;
    private final AllegroApiService allegroApiService;
    private final BaselinkerApiService baseLinkerApiService;
    private final BaselinkerJsonHandler baselinkerJsonHandler;
    private final AllegroJsonHandler allegroJsonHandler;

    @Autowired
    public ProductManager(AllegroProductRepository allegroProductRepository,
                          BaselinkerProductRepository baselinkerProductRepository,
                          AllegroApiService allegroApiService,
                          BaselinkerApiService baseLinkerApiService, BaselinkerJsonHandler baselinkerJsonHandler, AllegroJsonHandler allegroJsonHandler) {
        this.allegroProductRepository = allegroProductRepository;
        this.baselinkerProductRepository = baselinkerProductRepository;
        this.allegroApiService = allegroApiService;
        this.baseLinkerApiService = baseLinkerApiService;
        this.baselinkerJsonHandler = baselinkerJsonHandler;
        this.allegroJsonHandler = allegroJsonHandler;
    }

    public List<AllegroProduct> getAllAllegroProducts() {
        return allegroProductRepository.findAll();
    }
    public List<BaselinkerProduct> getAllBaselinkerProducts() {
        return baselinkerProductRepository.findAll();
    }
    public void addAllegroProduct(AllegroProduct allegroProduct) {
        addOrUpdateProduct(allegroProduct, allegroProductRepository);
    }
    public void addAllegroProducts(List<AllegroProduct> allegroProducts) {
        for (AllegroProduct allegroProduct : allegroProducts) {
            addOrUpdateProduct(allegroProduct, allegroProductRepository);
        }
    }
    public void addBaselinkerProduct(BaselinkerProduct baselinkerProduct) {
        addOrUpdateProduct(baselinkerProduct, baselinkerProductRepository);
    }
    public void addBaselinkerProducts(List<BaselinkerProduct> baselinkerProducts) {
        for (BaselinkerProduct baselinkerProduct : baselinkerProducts) {
            addOrUpdateProduct(baselinkerProduct, baselinkerProductRepository);
        }
    }
    private <T extends Product> void addOrUpdateProduct(T product, ProductRepository<T> repository) {
        Optional<T> existingProduct = repository.findByPortalId(product.getPortalId());
        if (existingProduct.isPresent()) {
            //update price in db
            T existing = existingProduct.get();
            existing.setEan(product.getEan());
            existing.setSku(product.getSku());
            existing.setName(product.getName());
            existing.setPrice(product.getPrice());
            repository.save(existing);
        } else {
            //save new product
            if(repository == allegroProductRepository && product.getEan() == null){
                String ean = getEanFromAllegroProduct((AllegroProduct) product);
                product.setEan(ean);
            }
            repository.save(product);
        }
    }
    public void printAllAllegroProducts(){
        List<AllegroProduct> products = getAllAllegroProducts();
        for (AllegroProduct product : products) {
            System.out.println(product.toString());
        }
    }
    public void printAllBaseLinkerProducts(){
        List<BaselinkerProduct> products = getAllBaselinkerProducts();
        for (BaselinkerProduct product : products) {
            System.out.println(product.toString());
        }
    }
    public void updateAllBaseLinkerProducts(){
        addBaselinkerProducts(
                baselinkerJsonHandler.getBaselinkerProductsFromResponse(
                        baseLinkerApiService.getAllPagesWithProducts()
                ));
    }
    public void updateAllAllegroProducts(){
        addAllegroProducts(
                allegroJsonHandler.getAllegroProductsFromResponse(
                        allegroApiService.getAllPagesWithProducts()
                ));
    }
    public String getEanFromAllegroProduct(AllegroProduct allegroProduct){
        return allegroJsonHandler.getAllegroEanFromResponse(
                allegroApiService.getResponseForOneAllegroProduct(
                        allegroProduct.getPortalId()
                ));
    }
    public void updatePricesOnline(){
        baseLinkerApiService.updateProductPrices(
                getProductsToUpdate());
    }
    private List<BaselinkerProduct> getProductsToUpdate(){
        List<BaselinkerProduct> baselinkerProducts = getAllBaselinkerProducts();
        List<BaselinkerProduct> productsToUpdate = new ArrayList<>();

        for (BaselinkerProduct baselinkerProduct : baselinkerProducts) {
            AllegroProduct lowestPriceProduct = findAllegroProductWithLowestPriceByEan(baselinkerProduct.getEan());
            if(lowestPriceProduct != null){
                baselinkerProduct.setPrice(lowestPriceProduct.getPrice());
                productsToUpdate.add(baselinkerProduct);
            }
        }
        return productsToUpdate;
    }
    public AllegroProduct findAllegroProductWithLowestPriceByEan(String ean) {
        List<AllegroProduct> products = allegroProductRepository.findByEan(ean);
        return products.stream()
                .min(Comparator.comparing(AllegroProduct::getPrice))
                .orElse(null);
    }
}
