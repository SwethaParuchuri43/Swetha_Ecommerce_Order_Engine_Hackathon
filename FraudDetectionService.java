package ecommerce;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FraudDetectionService {
    private Map<String, List<LocalDateTime>> userOrders = new ConcurrentHashMap<>();
    
    public void checkFraudulentActivity(Order order) {
        String userId = order.getUserId();
        LocalDateTime now = LocalDateTime.now();
        userOrders.computeIfAbsent(userId, k -> new ArrayList<>()).add(now);
        
        // Check 3 orders in 1 minute
        List<LocalDateTime> times = userOrders.get(userId);
        times.removeIf(t -> t.isBefore(now.minusMinutes(1)));
        
        if (times.size() >= 3) {
            System.out.println("⚠️ FRAUD ALERT: User " + userId + " placed 3+ orders in 1 minute!");
        }
        
        // Check high value
        if (order.getFinalAmount() > 50000) {
            System.out.println("⚠️ HIGH VALUE ALERT: Order " + order.getOrderId() + " - ₹" + order.getFinalAmount());
        }
    }
}
