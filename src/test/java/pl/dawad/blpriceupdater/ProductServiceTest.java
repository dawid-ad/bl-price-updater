package pl.dawad.blpriceupdater;

import org.junit.jupiter.api.Test;
import pl.dawad.blpriceupdater.api.AllegroApiService;
import pl.dawad.blpriceupdater.api.BaselinkerApiService;
import pl.dawad.blpriceupdater.dao.AllegroProductRepository;
import pl.dawad.blpriceupdater.dao.BaselinkerProductRepository;
import pl.dawad.blpriceupdater.dao.entity.BaselinkerProduct;
import pl.dawad.blpriceupdater.dao.entity.UpdatedBaselinkerProduct;
import pl.dawad.blpriceupdater.manager.CsvService;
import pl.dawad.blpriceupdater.manager.ProductService;
import pl.dawad.blpriceupdater.manager.external.UpdateContext;
import pl.dawad.blpriceupdater.utlils.AllegroJsonHandler;
import pl.dawad.blpriceupdater.utlils.BaselinkerJsonHandler;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductServiceTest {
	@Test
	void checkUpdatedProducts_AllPricesMatch() {
		// Arrange
		AllegroProductRepository allegroProductRepository = mock(AllegroProductRepository.class);
		BaselinkerProductRepository baselinkerProductRepository = mock(BaselinkerProductRepository.class);
		AllegroApiService allegroApiService = mock(AllegroApiService.class);
		BaselinkerApiService baseLinkerApiService = mock(BaselinkerApiService.class);
		BaselinkerJsonHandler baselinkerJsonHandler = mock(BaselinkerJsonHandler.class);
		AllegroJsonHandler allegroJsonHandler = mock(AllegroJsonHandler.class);
		CsvService csvService = mock(CsvService.class);
		UpdateContext updateContext = mock(UpdateContext.class);

		ProductService productService = new ProductService(
				allegroProductRepository, baselinkerProductRepository,
				allegroApiService, baseLinkerApiService,
				baselinkerJsonHandler, allegroJsonHandler, csvService, updateContext);

		BaselinkerProduct product1 = new BaselinkerProduct("1", "123", "SKU123", "Product A", 50.0);
		BaselinkerProduct product2 = new BaselinkerProduct("2", "456", "SKU456", "Product B", 75.0);

		UpdatedBaselinkerProduct updatedProduct1 = new UpdatedBaselinkerProduct(product1, 50.0);
		UpdatedBaselinkerProduct updatedProduct2 = new UpdatedBaselinkerProduct(product2, 75.0);

		List<BaselinkerProduct> baselinkerProductsToCheck = Arrays.asList(product1, product2);
		List<UpdatedBaselinkerProduct> updatedBaselinkerProducts = Arrays.asList(updatedProduct1, updatedProduct2);

		when(baselinkerJsonHandler.getBaselinkerProductsFromResponse(baseLinkerApiService.getAllPagesWithProducts()))
				.thenReturn(baselinkerProductsToCheck);
		when(updateContext.getUpdatedProducts()).thenReturn(updatedBaselinkerProducts);

		// Act
		boolean result = productService.checkUpdatedProducts();

		// Assert
		assertThat(result).isTrue();
	}
}
