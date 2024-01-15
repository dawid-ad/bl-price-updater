package pl.dawad.blpriceupdater.dao.entity;

import jakarta.persistence.*;

@Entity
public class AllegroProduct extends Product{
    public AllegroProduct() {
    }
    public AllegroProduct(String portalId, String ean, String sku, String name, double price) {
        super(portalId, ean, sku, name, price);
    }

    @Override
    public String toString() {
        return "Allegro "
                + super.toString();
    }
}