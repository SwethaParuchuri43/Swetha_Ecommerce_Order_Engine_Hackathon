package ecommerce;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class OrderService {
    private Map<String, Order> orders;
    private ProductService productService;
    private CartService cartService;
    private PaymentService paymentService;
    private AuditService auditService;
    private FraudDetectionService fraudService;
    private Set<String> processedIds;
    
    public OrderService(ProductService productService, CartService cartService) {
        this.orders = new ConcurrentHashMap<>();
        this.productService = productService;
        this.cartService = cartService;
        this.paymentService = new PaymentService();
        this.auditService = new AuditService();
        this.fraudService = new FraudDetectionService();
        this.processedIds = Collections.synchronizedSet(new HashSet<>());
    }
    
    public Order placeOrder(String userId, boolean simulateFailure) {
        String requestId = userId + "_" + System.currentTimeMillis();
        if (processedIds.contains(requestId)) throw new RuntimeException("Duplicate order");
        processedIds.add(requestId);
        
        Order order = null;
        try {
            Cart cart = cartService.getCart(userId);
            if (cart.isEmpty()) throw new RuntimeException("Cart empty");
            
            // Calculate total
            double subtotal = 0;
            for (Map.Entry<String, Integer> entry : cart.getItems().entrySet()) {
                Product p = productService.getProduct(entry.getKey());
                subtotal += p.getPrice() * entry.getValue();
            }
            
            // Apply discounts
            double discount = calculateDiscount(subtotal, cart.getItems(), cart.getCouponCode());
            double finalAmount = subtotal - discount;
            
            // Create order
            order = new Order("ORD" + System.currentTimeMillis(), userId);
            order.setItems(new HashMap<>(cart.getItems()));
            order.setTotalAmount(subtotal);
            order.setDiscountAmount(discount);
            order.setFinalAmount(finalAmount);
            order.setCouponCode(cart.getCouponCode());
            
            orders.put(order.getOrderId(), order);
            auditService.log("ORDER_CREATED", userId, order.getOrderId());
            
            // Payment
            if (paymentService.processPayment(order.getOrderId(), finalAmount, simulateFailure)) {
                for (Map.Entry<String, Integer> entry : cart.getItems().entrySet())
                    productService.commitReservedStock(entry.getKey());
                order.setStatus(Order.OrderStatus.PAID);
                cartService.clearCart(userId);
                fraudService.checkFraudulentActivity(order);
                auditService.log("ORDER_PAID", userId, order.getOrderId());
            } else {
                throw new RuntimeException("Payment failed");
            }
            return order;
            
        } catch (Exception ex) {  // Changed from 'e' to 'ex' to avoid conflict
            if (order != null) {
                // Use a different variable name here, like 'item'
                for (Map.Entry<String, Integer> item : order.getItems().entrySet()) {
                    productService.releaseReservedStock(item.getKey(), item.getValue());
                }
                order.setStatus(Order.OrderStatus.FAILED);
                auditService.log("ORDER_FAILED", userId, ex.getMessage());
            }
            throw new RuntimeException("Order failed: " + ex.getMessage());
        } finally {
            processedIds.remove(requestId);
        }
    }
    
    private double calculateDiscount(double total, Map<String, Integer> items, String coupon) {
        double discount = 0;
        if (total > 1000) discount += total * 0.10;
        for (int qty : items.values()) {
            if (qty > 3) {
                discount += total * 0.05;
                break;
            }
        }
        if (coupon != null) {
            if (coupon.equalsIgnoreCase("SAVE10")) discount += total * 0.10;
            else if (coupon.equalsIgnoreCase("FLAT200")) discount += 200;
        }
        return Math.min(discount, total);
    }
    
    public List<Order> getAllOrders() { 
        return new ArrayList<>(orders.values()); 
    }
    
    public Order getOrder(String id) { 
        return orders.get(id); 
    }
    
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        List<Order> list = new ArrayList<>();
        for (Order order : orders.values()) {
            if (order.getStatus() == status) list.add(order);
        }
        return list;
    }
    
    public void cancelOrder(String orderId) {
        Order order = orders.get(orderId);
        if (order == null) throw new RuntimeException("Order not found");
        if (order.getStatus() == Order.OrderStatus.CANCELLED) throw new RuntimeException("Already cancelled");
        if (order.getStatus() != Order.OrderStatus.CREATED && order.getStatus() != Order.OrderStatus.PENDING_PAYMENT)
            throw new RuntimeException("Cannot cancel");
        
        for (Map.Entry<String, Integer> item : order.getItems().entrySet())
            productService.updateStock(item.getKey(), item.getValue());
        order.setStatus(Order.OrderStatus.CANCELLED);
        auditService.log("ORDER_CANCELLED", order.getUserId(), orderId);
    }
    
    public void returnOrder(String orderId, Map<String, Integer> returned) {
        Order order = orders.get(orderId);
        if (order == null) throw new RuntimeException("Order not found");
        if (order.getStatus() != Order.OrderStatus.DELIVERED) throw new RuntimeException("Not delivered");
        
        for (Map.Entry<String, Integer> item : returned.entrySet())
            productService.updateStock(item.getKey(), item.getValue());
        
        double refund = 0;
        for (Map.Entry<String, Integer> item : returned.entrySet()) {
            Product p = productService.getProduct(item.getKey());
            refund += p.getPrice() * item.getValue();
        }
        order.setFinalAmount(order.getFinalAmount() - refund);
        auditService.log("ORDER_RETURNED", order.getUserId(), orderId + " refund: ₹" + refund);
    }
}
