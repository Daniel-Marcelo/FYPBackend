package mckee.daniel.model;

public class User implements Comparable<User> {

	private String firstName;
	private String lastName;
	private String email;
	private String password;
	private String country;

	/*
	 * Not field in DB
	 */
	private String retypedPassword;
	private double balance;
	private double curAccVal;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String name) {
		this.firstName = name;
	}

	public String getRetypedPassword() {
		return retypedPassword;
	}

	public void setRetypedPassword(String retypedPassword) {
		this.retypedPassword = retypedPassword;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public double getBalance() {
		return balance;
	}

	public void setBalance(double balance) {
		this.balance = balance;
	}

	public double getCurAccVal() {
		return curAccVal;
	}

	public void setCurAccVal(double curAccVal) {
		this.curAccVal = curAccVal;
	}

	@Override
	public int compareTo(User o) {

		if (this.curAccVal < o.getCurAccVal())
			return -1;
		if (this.curAccVal > o.getCurAccVal())
			return 1;
		else
			return 0;
	}
	
	public void print(){
		System.out.println("\n\nFIRST NAME: "+firstName);
		System.out.println("LAST NAME: "+lastName);
		System.out.println("EMAIL: "+email);
		System.out.println("COUNTRY: "+country);
		System.out.println("ACC VAL: "+curAccVal);
		System.out.println("BALANCE: "+balance);


	}

}
