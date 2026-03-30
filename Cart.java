package ecommerce;

import java.util.HashMap;
import java.util.Map;

public class Cart {

	private String userId;
	private Map<String, Integer> items;
	private String couponCode;

	public Cart(String userId) {
		this.userId = userId;
		this.items = new HashMap<>();
	}

	public String getUserId() {
		return userId;
	}

	public Map<String, Integer> getItems() {
		return items;
	}

	public String getCouponCode() {
		return couponCode;
	}

	public void setCouponCode(String couponCode) {
		this.couponCode = couponCode;
	}

	public void addItem(String productId, int qty) {
		items.put(productId, items.getOrDefault(productId, 0) + qty);
	}

	public void removeItem(String productId) {
		items.remove(productId);
	}

	public void clear() {
		items.clear();
		couponCode = null;
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}
}
