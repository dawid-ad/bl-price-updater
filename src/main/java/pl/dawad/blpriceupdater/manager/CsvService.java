package pl.dawad.blpriceupdater.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.dawad.blpriceupdater.dao.AllegroProductRepository;
import pl.dawad.blpriceupdater.dao.BaselinkerProductRepository;
import pl.dawad.blpriceupdater.dao.entity.AllegroProduct;
import pl.dawad.blpriceupdater.dao.entity.BaselinkerProduct;
import pl.dawad.blpriceupdater.dao.entity.Product;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
public class CsvService {
    private final AllegroProductRepository allegroProductRepository;
    private final BaselinkerProductRepository baselinkerProductRepository;
    @Autowired
    public CsvService(AllegroProductRepository allegroProductRepository, BaselinkerProductRepository baselinkerProductRepository) {
        this.allegroProductRepository = allegroProductRepository;
        this.baselinkerProductRepository = baselinkerProductRepository;
    }
    public void exportAllegroDb(){
        exportDataToCsv("allegro.csv",allegroProductRepository.findAll());
    }
    public void exportBaselinkerDb(){
        exportDataToCsv("baselinker.csv",baselinkerProductRepository.findAll());
    }
    private void exportDataToCsv(String filePath, List<? extends Product> products) {
        try (FileWriter writer = new FileWriter(filePath)) {
            // Write CSV header
            if (!products.isEmpty()) {
                writer.append(getCsvHeader(products.get(0))).append("\n");
            }
            // Write data to CSV
            for (Product product : products) {
                writer.append(String.valueOf(product.getId())).append(",");
                writer.append(product.getPortalId()).append(",");
                writer.append(product.getEan()).append(",");
                writer.append(product.getSku()).append(",");
                writer.append(product.getName()).append(",");
                writer.append(String.valueOf(product.getPrice())).append("\n");
            }
            System.out.println("Data exported to CSV successfully!");

        } catch (IOException e) {
            System.out.println("Error writing to CSV file: " + e.getMessage());
        }
    }

    private String getCsvHeader(Product product) {
        return "Id,PortalId,EAN,SKU,Name,Price";
    }
}
