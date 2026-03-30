package ecommerce;

import java.util.ArrayList;
import java.util.List;

public class AuditService {

	 private List<AuditLog> logs = new ArrayList<>();
	    
	    public void log(String action, String userId, String details) {
	        AuditLog log = new AuditLog(action, userId, details);
	        logs.add(log);
	        System.out.println("  📝 [AUDIT] " + action + ": " + details);
	    }
	    
	    public List<AuditLog> getAllLogs() { return new ArrayList<>(logs); }
}
