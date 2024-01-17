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
import pl.dawad.blpriceupdater.dao.entity.UpdatedBaselinkerProduct;
import pl.dawad.blpriceupdater.manager.external.UpdateContext;
import pl.dawad.blpriceupdater.utlils.AllegroJsonHandler;
import pl.dawad.blpriceupdater.utlils.BaselinkerJsonHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductService {
    private final AllegroProductRepository allegroProductRepository;
    private final BaselinkerProductRepository baselinkerProductRepository;
    private final AllegroApiService allegroApiService;
    private final BaselinkerApiService baseLinkerApiService;
    private final BaselinkerJsonHandler baselinkerJsonHandler;
    private final AllegroJsonHandler allegroJsonHandler;
    private final CsvService csvService;
    private final UpdateContext updateContext;


    @Autowired
    public ProductService(AllegroProductRepository allegroProductRepository,
                          BaselinkerProductRepository baselinkerProductRepository,
                          AllegroApiService allegroApiService,
                          BaselinkerApiService baseLinkerApiService, BaselinkerJsonHandler baselinkerJsonHandler, AllegroJsonHandler allegroJsonHandler, CsvService csvService, UpdateContext updateContext) {
        this.allegroProductRepository = allegroProductRepository;
        this.baselinkerProductRepository = baselinkerProductRepository;
        this.allegroApiService = allegroApiService;
        this.baseLinkerApiService = baseLinkerApiService;
        this.baselinkerJsonHandler = baselinkerJsonHandler;
        this.allegroJsonHandler = allegroJsonHandler;
        this.csvService = csvService;
        this.updateContext = updateContext;
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
        System.out.println("Downloaded: " + allegroProducts.size() + " Allegro products.");
    }
    public void addBaselinkerProduct(BaselinkerProduct baselinkerProduct) {
        addOrUpdateProduct(baselinkerProduct, baselinkerProductRepository);
    }
    public void addBaselinkerProducts(List<BaselinkerProduct> baselinkerProducts) {
        for (BaselinkerProduct baselinkerProduct : baselinkerProducts) {
            addOrUpdateProduct(baselinkerProduct, baselinkerProductRepository);
        }
        System.out.println("Downloaded: " + baselinkerProducts.size() + " BaseLinker products.");
    }
    private <T extends Product> void addOrUpdateProduct(T product, ProductRepository<T> repository) {
        Optional<T> existingProduct = repository.findByPortalId(product.getPortalId());
        if (existingProduct.isPresent()) {
            //update price in db
            T existing = existingProduct.get();
            existing.setSku(product.getSku());
            existing.setName(product.getName());
            existing.setPrice(product.getPrice());
            if(repository == allegroProductRepository){
                if(existing.getEan() == null || existing.getEan().isBlank() || existing.getEan().equalsIgnoreCase("null")){
                    System.out.println("Getting EAN for the EXISTING product: " + product.getName());
                    String ean = getEanFromAllegroProduct((AllegroProduct) product);
                    existing.setEan(ean);
                }
            } else {
                existing.setEan(product.getEan());
            }
            repository.save(existing);
        } else {
            //save new product
            if(repository == allegroProductRepository && product.getEan() == null){
                System.out.println("Getting EAN for the NEW product: " + product.getName());
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
    public void downloadAllBaseLinkerProducts(){
        addBaselinkerProducts(
                baselinkerJsonHandler.getBaselinkerProductsFromResponse(
                        baseLinkerApiService.getAllPagesWithProducts()
                ));
    }
    public void downloadAllAllegroProducts(){
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
        List<UpdatedBaselinkerProduct> updatedBaselinkerProducts = new ArrayList<>();
        double newPrice, oldPrice;

        for (BaselinkerProduct baselinkerProduct : baselinkerProducts) {
            AllegroProduct lowestPriceProduct = findAllegroProductWithLowestPriceByEan(baselinkerProduct.getEan());
            if(lowestPriceProduct != null){
                oldPrice = baselinkerProduct.getPrice();
                newPrice = lowestPriceProduct.getPrice();
                if(oldPrice != newPrice){
                    updatedBaselinkerProducts.add(
                            new UpdatedBaselinkerProduct(baselinkerProduct, newPrice));
                    baselinkerProduct.setPrice(newPrice);
                    productsToUpdate.add(baselinkerProduct);
                }
            }
        }
        updateContext.setUpdatedProducts(updatedBaselinkerProducts);
        return productsToUpdate;
    }
    public AllegroProduct findAllegroProductWithLowestPriceByEan(String ean) {
        List<AllegroProduct> products = allegroProductRepository.findByEan(ean);
        return products.stream()
                .min(Comparator.comparing(AllegroProduct::getPrice))
                .orElse(null);
    }
    public void exportUpdatedProducts(){
        csvService.exportAndSaveUpdated(updateContext.getUpdatedProducts());
    }
    public boolean checkUpdatedProducts() {
        List<UpdatedBaselinkerProduct> updatedBaselinkerProducts = updateContext.getUpdatedProducts();
        if(updatedBaselinkerProducts == null || updatedBaselinkerProducts.size() == 0){
            System.out.println("Nothing to check");
            return true;
        }
        List<BaselinkerProduct> productsToCheck = baselinkerJsonHandler
                .getBaselinkerProductsFromResponse(baseLinkerApiService.getAllPagesWithProducts());
        boolean pricesMatch = updatedBaselinkerProducts.stream()
                .allMatch(updatedProduct ->
                        productsToCheck.stream()
                                .anyMatch(baselinkerProduct ->
                                        baselinkerProduct.getEan().equals(updatedProduct.getEan()) &&
                                                baselinkerProduct.getPrice() == updatedProduct.getPriceAfterUpdate()));

        if (pricesMatch) {
            System.out.println("All prices match.");
        } else {
            System.out.println("Some prices do not match!");
        }

        return pricesMatch;
    }

    public void exportAllegroDb(){
        csvService.exportAllegroDb("allegro.csv");
    }
    public void exportBaselinkerDb(){
        csvService.exportBaselinkerDb("baselinker.csv");
    }

}
