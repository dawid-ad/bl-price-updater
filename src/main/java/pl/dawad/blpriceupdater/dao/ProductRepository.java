package pl.dawad.blpriceupdater.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface ProductRepository<T> extends JpaRepository<T, Long> {
    List<T> findByEan(String ean);
    Optional<T> findByPortalId(String portalId);
}