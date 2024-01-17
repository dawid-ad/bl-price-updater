package pl.dawad.blpriceupdater.dao.entity;

public class UpdatedBaselinkerProduct extends BaselinkerProduct {
    private double priceAfterUpdate;
    public UpdatedBaselinkerProduct() {
    }
    public UpdatedBaselinkerProduct(BaselinkerProduct baselinkerProduct, double priceAfterUpdate) {
        super(baselinkerProduct.getPortalId(),
                baselinkerProduct.getEan(),
                baselinkerProduct.getSku(),
                baselinkerProduct.getName(),
                baselinkerProduct.getPrice());
        this.priceAfterUpdate = priceAfterUpdate;
    }

    public double getPriceAfterUpdate() {
        return priceAfterUpdate;
    }

    public void setPriceAfterUpdate(double priceAfterUpdate) {
        this.priceAfterUpdate = priceAfterUpdate;
    }

    @Override
    public String toString() {
        return "UpdatedBaselinkerProduct{" +
                "priceAfterUpdate=" + priceAfterUpdate +
                "} " + super.toString();
    }
}
