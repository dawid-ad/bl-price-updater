package pl.dawad.blpriceupdater.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.dawad.blpriceupdater.dao.entity.AllegroProduct;
import pl.dawad.blpriceupdater.dao.entity.Product;

public interface AllegroProductRepository extends ProductRepository<AllegroProduct> {
}
