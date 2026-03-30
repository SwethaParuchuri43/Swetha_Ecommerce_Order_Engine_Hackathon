package ecommerce;

import java.time.LocalDateTime;

public class AuditLog {

	private String timestamp;
	private String action;
	private String userId;
	private String details;

	public AuditLog(String action, String userId, String details) {
		this.timestamp = LocalDateTime.now().toString();
		this.action = action;
		this.userId = userId;
		this.details = details;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public String getAction() {
		return action;
	}

	public String getUserId() {
		return userId;
	}

	public String getDetails() {
		return details;
	}
}
