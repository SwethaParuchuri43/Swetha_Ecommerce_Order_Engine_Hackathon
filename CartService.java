package ecommerce;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class CartService {

	private Map<String, Cart> userCarts;
	private ProductService productService;
	private AuditService auditService;
	private Map<String, ReentrantLock> userLocks;

	public CartService(ProductService productService) {
		this.userCarts = new ConcurrentHashMap<>();
		this.productService = productService;
		this.auditService = new AuditService();
		this.userLocks = new ConcurrentHashMap<>();
	}

	private ReentrantLock getLock(String userId) {
		return userLocks.computeIfAbsent(userId, k -> new ReentrantLock());
	}

	public void addToCart(String userId, String productId, int quantity) {
		ReentrantLock lock = getLock(userId);
		lock.lock();
		try {
			if (productService.getAvailableStock(productId) < quantity)
				throw new RuntimeException("Insufficient stock");

			Cart cart = userCarts.computeIfAbsent(userId, k -> new Cart(userId));
			if (!productService.reserveStock(productId, quantity))
				throw new RuntimeException("Failed to reserve");

			cart.addItem(productId, quantity);
			auditService.log("ADD_TO_CART", userId, productId + " x" + quantity);
		} finally {
			lock.unlock();
		}
	}

	public void removeFromCart(String userId, String productId) {
		ReentrantLock lock = getLock(userId);
		lock.lock();
		try {
			Cart cart = userCarts.get(userId);
			if (cart == null)
				throw new RuntimeException("Cart not found");

			Integer qty = cart.getItems().get(productId);
			if (qty != null) {
				productService.releaseReservedStock(productId, qty);
				cart.removeItem(productId);
				auditService.log("REMOVE_FROM_CART", userId, productId);
			}
		} finally {
			lock.unlock();
		}
	}

	public Cart getCart(String userId) {
		Cart cart = userCarts.get(userId);
		if (cart == null)
			throw new RuntimeException("Cart not found");
		return cart;
	}

	public void clearCart(String userId) {
		ReentrantLock lock = getLock(userId);
		lock.lock();
		try {
			Cart cart = userCarts.get(userId);
			if (cart != null) {
				for (Map.Entry<String, Integer> e : cart.getItems().entrySet())
					productService.releaseReservedStock(e.getKey(), e.getValue());
				cart.clear();
			}
		} finally {
			lock.unlock();
		}
	}

	public void applyCoupon(String userId, String code) {
		Cart cart = userCarts.get(userId);
		if (cart == null)
			throw new RuntimeException("Cart not found");
		cart.setCouponCode(code);
		auditService.log("COUPON_APPLIED", userId, code);
	}
}
