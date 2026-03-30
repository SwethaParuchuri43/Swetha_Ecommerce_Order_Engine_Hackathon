package ecommerce;

import java.util.concurrent.locks.ReentrantLock;

public class Product {

	private String id;
	private String name;
	private double price;
	private int stock;
	private int reservedStock;
	private ReentrantLock lock;

	public Product(String id, String name, double price, int stock) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.stock = stock;
		this.reservedStock = 0;
		this.lock = new ReentrantLock();
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public double getPrice() {
		return price;
	}

	public int getStock() {
		return stock;
	}

	public void setStock(int stock) {
		this.stock = stock;
	}

	public int getReservedStock() {
		return reservedStock;
	}

	public ReentrantLock getLock() {
		return lock;
	}

	public boolean reserveStock(int quantity) {
		lock.lock();
		try {
			if (stock - reservedStock >= quantity) {
				reservedStock += quantity;
				return true;
			}
			return false;
		} finally {
			lock.unlock();
		}
	}

	public void releaseReservedStock(int quantity) {
		lock.lock();
		try {
			reservedStock -= quantity;
		} finally {
			lock.unlock();
		}
	}

	public void commitReservedStock() {
		lock.lock();
		try {
			stock -= reservedStock;
			reservedStock = 0;
		} finally {
			lock.unlock();
		}
	}

	public int getAvailableStock() {
		lock.lock();
		try {
			return stock - reservedStock;
		} finally {
			lock.unlock();
		}
	}
}
