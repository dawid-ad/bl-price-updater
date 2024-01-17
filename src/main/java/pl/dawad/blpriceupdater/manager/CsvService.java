package pl.dawad.blpriceupdater.manager;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.dawad.blpriceupdater.dao.AllegroProductRepository;
import pl.dawad.blpriceupdater.dao.BaselinkerProductRepository;
import pl.dawad.blpriceupdater.dao.entity.UpdatedBaselinkerProduct;

import java.util.List;

@Service
public class CsvService {
    private final AllegroProductRepository allegroProductRepository;
    private final BaselinkerProductRepository baselinkerProductRepository;
    private final CsvExportService csvExportService;
    @Autowired
    public CsvService(AllegroProductRepository allegroProductRepository, BaselinkerProductRepository baselinkerProductRepository, CsvExportService csvExportService) {
        this.allegroProductRepository = allegroProductRepository;
        this.baselinkerProductRepository = baselinkerProductRepository;
        this.csvExportService = csvExportService;
    }

    public void exportAllegroDb(String fileName){
        csvExportService.exportProductsToCsv(fileName, allegroProductRepository.findAll());
    }
    public void exportBaselinkerDb(String fileName){
        csvExportService.exportProductsToCsv(fileName, baselinkerProductRepository.findAll());
    }
    public void exportAndSaveUpdated(List<UpdatedBaselinkerProduct> updatedBaselinkerProducts) {
        csvExportService.exportProductsToCsv("updated.csv",updatedBaselinkerProducts);
    }
}
