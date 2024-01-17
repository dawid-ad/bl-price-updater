package pl.dawad.blpriceupdater;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import pl.dawad.blpriceupdater.manager.ProductService;
import pl.dawad.blpriceupdater.utlils.EmailNotificationService;

@SpringBootApplication
public class BlPriceUpdaterApplication {
	private final ProductService productService;
	private final EmailNotificationService emailNotificationService;
	@Autowired
	public BlPriceUpdaterApplication(ProductService productService, EmailNotificationService emailNotificationService, ConfigurableApplicationContext context) {
		this.productService = productService;
		this.emailNotificationService = emailNotificationService;
	}

	public static void main(String[] args) {
		SpringApplication.run(BlPriceUpdaterApplication.class, args);
		System.exit(0);
	}
	@PostConstruct
	public void init() {
		try{
      			productService.downloadAllBaseLinkerProducts();
      			productService.exportBaselinkerDb();

      			productService.downloadAllAllegroProducts();
      			productService.exportAllegroDb();

      			productService.updatePricesOnline();
      			productService.exportUpdatedProducts();
      			productService.checkUpdatedProducts();
		} catch (Exception e) {
			handleFailure(e);
		}

	}
	public void handleFailure(Exception e) {
		String errorMessage = "Application failed. Error: " + e.getMessage();
		emailNotificationService.sendErrorNotification("BaseLinker price updater error.", errorMessage);
		System.err.println(errorMessage); // Print error details to console
		e.printStackTrace();
		System.exit(1);
	}
}
