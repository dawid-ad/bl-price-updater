package pl.dawad.blpriceupdater;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import pl.dawad.blpriceupdater.dao.entity.AllegroProduct;
import pl.dawad.blpriceupdater.manager.CsvService;
import pl.dawad.blpriceupdater.manager.ProductManager;

@SpringBootApplication
public class BlPriceUpdaterApplication {
	private final ProductManager productManager;
	private final CsvService csvService;
	@Autowired
	public BlPriceUpdaterApplication(ProductManager productManager, CsvService csvService) {
		this.productManager = productManager;
		this.csvService = csvService;
	}

	public static void main(String[] args) {
		SpringApplication.run(BlPriceUpdaterApplication.class, args);
	}
	@PostConstruct
	public void init() {
    		productManager.updateAllBaseLinkerProducts();
			productManager.updateAllAllegroProducts();

    //    		productManager.printAllAllegroProducts();
    //			productManager.printAllBaseLinkerProducts();

    		productManager.addAllegroProduct(new AllegroProduct("123","5905514135031",null,"GŁOŚNIK PRZENOŚNY BLUETOOTH BOSE SOUNDLINK FLEX NIEBIESKI",100000.00));
    		productManager.addAllegroProduct(new AllegroProduct("34234","5905514135031",null,"GŁOŚNIK PRZENOŚNY BLUETOOTH BOSE SOUNDLINK FLEX NIEBIESKI",500.00));
    		productManager.addAllegroProduct(new AllegroProduct("1423422","5905514135031",null,"GŁOŚNIK PRZENOŚNY BLUETOOTH BOSE SOUNDLINK FLEX NIEBIESKI",45000.67));
    		productManager.addAllegroProduct(new AllegroProduct("1423423","5905514135031",null,"GŁOŚNIK PRZENOŚNY BLUETOOTH BOSE SOUNDLINK FLEX NIEBIESKI",15505.00));

			csvService.exportAllegroDb();
			csvService.exportBaselinkerDb();
//			productManager.updatePricesOnline();
	}
}
