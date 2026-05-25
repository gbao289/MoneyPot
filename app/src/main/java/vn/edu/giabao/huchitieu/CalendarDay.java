package vn.edu.giabao.huchitieu;

import java.util.Date;

public class CalendarDay {
    private Date date;
    private long amount;
    private boolean isToday;
    private boolean isCurrentMonth;
    private boolean isSelected;

    public CalendarDay(Date date, long amount, boolean isToday, boolean isCurrentMonth) {
        this.date = date;
        this.amount = amount;
        this.isToday = isToday;
        this.isCurrentMonth = isCurrentMonth;
        // Mặc định ngày hôm nay sẽ được chọn khi mới mở
        this.isSelected = isToday;
    }

    public Date getDate() { return date; }
    public long getAmount() { return amount; }
    public boolean isToday() { return isToday; }
    public boolean isCurrentMonth() { return isCurrentMonth; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}
