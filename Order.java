package ecommerce;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Order {

	public enum OrderStatus {
		CREATED, PENDING_PAYMENT, PAID, SHIPPED, DELIVERED, FAILED, CANCELLED
	}

	private String orderId;
	private String userId;
	private Map<String, Integer> items;
	private double totalAmount;
	private double discountAmount;
	private double finalAmount;
	private OrderStatus status;
	private String couponCode;
	private LocalDateTime createdAt;

	public Order(String orderId, String userId) {
		this.orderId = orderId;
		this.userId = userId;
		this.items = new HashMap<>();
		this.status = OrderStatus.CREATED;
		this.createdAt = LocalDateTime.now();
	}

	public String getOrderId() {
		return orderId;
	}

	public String getUserId() {
		return userId;
	}

	public Map<String, Integer> getItems() {
		return items;
	}

	public double getTotalAmount() {
		return totalAmount;
	}

	public double getDiscountAmount() {
		return discountAmount;
	}

	public double getFinalAmount() {
		return finalAmount;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public String getCouponCode() {
		return couponCode;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setItems(Map<String, Integer> items) {
		this.items = items;
	}

	public void setTotalAmount(double total) {
		this.totalAmount = total;
	}

	public void setDiscountAmount(double discount) {
		this.discountAmount = discount;
	}

	public void setFinalAmount(double finalAmt) {
		this.finalAmount = finalAmt;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public void setCouponCode(String code) {
		this.couponCode = code;
	}
}
