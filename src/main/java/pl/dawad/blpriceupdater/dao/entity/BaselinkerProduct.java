package pl.dawad.blpriceupdater.dao.entity;

import jakarta.persistence.*;

@Entity
public class BaselinkerProduct extends Product{
    public BaselinkerProduct() {
    }
    public BaselinkerProduct(String portalId, String ean, String sku, String name, double price) {
        super(portalId, ean, sku, name, price);
    }
    @Override
    public String toString() {
        return "Baselinker "
                + super.toString();
    }
}
