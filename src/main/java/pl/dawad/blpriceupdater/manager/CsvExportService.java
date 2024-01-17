package pl.dawad.blpriceupdater.manager;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import pl.dawad.blpriceupdater.dao.entity.Product;
import pl.dawad.blpriceupdater.dao.entity.UpdatedBaselinkerProduct;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
@Service
public class CsvExportService {
    private String directory;
    @PostConstruct
    private void mkDir(){
        directory = "";
        directory = "csv_reports/";
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm");
        String formattedDateTime = currentDateTime.format(formatter);
        directory += formattedDateTime + "/";
        Path directoryPath = Path.of(directory);

        if (!Files.exists(directoryPath)) {
            try {
                Files.createDirectories(directoryPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void exportProductsToCsv(String fileName, List<? extends Product> products) {
        String filePath = directory + fileName;
        String header = "Id;PortalId;EAN;SKU;Name;Price";
        boolean isUpdatedBaseLinkerProduct = fileName.equals("updated.csv") || products.stream().anyMatch(p -> p instanceof UpdatedBaselinkerProduct);

        try (FileWriter writer = new FileWriter(filePath)) {
            // Write CSV header
            if(products == null){
                writer.append("Nothing to update.");
            } else {
                if(isUpdatedBaseLinkerProduct){
                    writer.append(header).append(";PriceAfterUpdate").append("\n");
                } else {
                    writer.append(header).append("\n");
                }
                // Write data to CSV
                for (Product product : products) {
                    writer.append(String.valueOf(product.getId())).append(";");
                    writer.append(product.getPortalId()).append(";");
                    writer.append(product.getEan()).append(";");
                    writer.append(product.getSku()).append(";");
                    writer.append(product.getName()).append(";");
                    if(isUpdatedBaseLinkerProduct){
                        UpdatedBaselinkerProduct updatedProduct = (UpdatedBaselinkerProduct) product;
                        writer.append(String.valueOf(product.getPrice())).append(";");
                        writer.append(String.valueOf(updatedProduct.getPriceAfterUpdate())).append("\n");
                    } else {
                        writer.append(String.valueOf(product.getPrice())).append("\n");
                    }
                }
            }
            System.out.println("Data exported to " + filePath + " successfully!");

        } catch (IOException e) {
            System.out.println("Error writing to CSV file: " + e.getMessage());
        }
    }
}
