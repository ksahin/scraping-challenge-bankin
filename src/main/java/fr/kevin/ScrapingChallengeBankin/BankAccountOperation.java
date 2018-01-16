package fr.kevin.ScrapingChallengeBankin;

public class BankAccountOperation {
	
	
	private int amount ;
	private String account ;
	private int transactionId;

	private String currency ;
	
	public int getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}
	public int getAmount() {
		return amount;
	}
	public void setAmount(int amount) {
		this.amount = amount;
	}
	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	
	@Override
	public String toString() {
		return "BankAccountOperation [amount=" + amount + ", account=" + account + ", transaction=" + transactionId
				+ ", currency=" + currency + "]";
	}
	
	


	
}
