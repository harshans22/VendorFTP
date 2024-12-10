
public class Main {
    public static void main(String[] args) {
        Vendor vendor = new VendorImpl();
        vendor.readProductData();
        vendor.readInventoryData();
    }
}
