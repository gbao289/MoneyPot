package vn.edu.giabao.huchitieu;

public class Transaction {
    private String key;
    private long amount;
    private String date; // dd/MM/yyyy
    private String type; // "Expense" or "Income"
    private String frequency;
    private String sourcePotKey;
    private String note;
    private long timestamp;

    public Transaction() {
    }

    public Transaction(long amount, String date, String type, String frequency, String sourcePotKey, String note, long timestamp) {
        this.amount = amount;
        this.date = date;
        this.type = type;
        this.frequency = frequency;
        this.sourcePotKey = sourcePotKey;
        this.note = note;
        this.timestamp = timestamp;
    }

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public String getSourcePotKey() { return sourcePotKey; }
    public void setSourcePotKey(String sourcePotKey) { this.sourcePotKey = sourcePotKey; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
