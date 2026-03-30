package ecommerce;

import java.util.Random;

public class PaymentService {

	 private Random random = new Random();
	    
	    public boolean processPayment(String orderId, double amount, boolean simulateFailure) {
	        try { Thread.sleep(100); } catch (InterruptedException e) {}
	        
	        boolean success = simulateFailure ? random.nextDouble() > 0.3 : random.nextDouble() > 0.1;
	        System.out.println("  💳 Payment " + (success ? "SUCCESS" : "FAILED") + " - ₹" + amount);
	        return success;
	    }
}
