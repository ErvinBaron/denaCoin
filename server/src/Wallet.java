package Hakaton;

public class Wallet {

	private String id;
	private double coinBalance;
	
	private String getId() {
		return id;
	}
	
	private void setId(String id) {
		this.id = id;
	}
	
	private double getCoinBalance() {
		return coinBalance;
	}
	
	private void setCoinBalance(double coinBalance) {
		this.coinBalance = coinBalance;
	}
}
