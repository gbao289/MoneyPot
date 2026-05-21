package vn.edu.giabao.huchitieu;

public class Pot {
    private String name;
    private long balance;
    private int percent;

    public Pot(String name, long balance, int percent) {
        this.name = name;
        this.balance = balance;
        this.percent = percent;
    }

    public String getName() {
        return name;
    }

    public long getBalance() {
        return balance;
    }

    public int getPercent() {
        return percent;
    }
}