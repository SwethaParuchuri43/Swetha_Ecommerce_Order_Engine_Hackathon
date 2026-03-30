package ecommerce;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProductService {

	private Map<String, Product> products;
	private AuditService auditService;

	public ProductService() {
		this.products = new ConcurrentHashMap<>();
		this.auditService = new AuditService();
		initSampleProducts();
	}

	private void initSampleProducts() {
		addProduct(new Product("P001", "Laptop", 55000, 10));
		addProduct(new Product("P002", "Smartphone", 25000, 15));
		addProduct(new Product("P003", "Headphones", 2500, 30));
		addProduct(new Product("P004", "Mouse", 1200, 50));
		addProduct(new Product("P005", "Keyboard", 3500, 20));
	}

	public void addProduct(Product p) {
		if (products.containsKey(p.getId())) {
			throw new RuntimeException("Product ID already exists: " + p.getId());
		}
		if (p.getStock() < 0) {
			throw new RuntimeException("Stock cannot be negative");
		}
		products.put(p.getId(), p);
		auditService.log("PRODUCT_ADDED", "SYSTEM", p.getId());
	}

	
	public List<Product> getAllProducts() {
		return new ArrayList<>(products.values());
	}

	
	public Product getProduct(String id) {
		Product p = products.get(id);
		if (p == null)
			throw new RuntimeException("Product not found: " + id);
		return p;
	}

	
	public List<Product> getLowStockProducts(int threshold) {
		List<Product> list = new ArrayList<>();
		for (Product p : products.values()) {
			if (p.getStock() <= threshold)
				list.add(p);
		}
		return list;
	}

	
	public boolean reserveStock(String id, int qty) {
		Product p = products.get(id);
		if (p == null)
			return false;
		return p.reserveStock(qty);
	}

	public void releaseReservedStock(String id, int qty) {
		Product p = products.get(id);
		if (p != null) {
			p.releaseReservedStock(qty);
			auditService.log("STOCK_RELEASED", "SYSTEM", id + " x" + qty);
		}
	}

	public void commitReservedStock(String id) {
		Product p = products.get(id);
		if (p != null) {
			p.commitReservedStock();
			auditService.log("STOCK_COMMITTED", "SYSTEM", id);
		}
	}

	public int getAvailableStock(String id) {
		Product p = products.get(id);
		return p != null ? p.getAvailableStock() : 0;
	}

	public void updateStock(String id, int qty) {
		Product p = products.get(id);
		if (p != null) {
			p.setStock(p.getStock() + qty);
			auditService.log("STOCK_UPDATED", "SYSTEM", id + " +/-" + qty);
		}
	}
}