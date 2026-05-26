package vn.edu.giabao.huchitieu;

public class Pot {
    private String key; // Thêm key để định danh hũ trên Firebase
    private String name;
    private long balance;
    private int percent;

    // Constructor mặc định cần thiết cho Firebase
    public Pot() {
    }

    public Pot(String name, long balance, int percent) {
        this.name = name;
        this.balance = balance;
        this.percent = percent;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = balance;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }
}
