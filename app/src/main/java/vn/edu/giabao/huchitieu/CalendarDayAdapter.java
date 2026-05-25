package vn.edu.giabao.huchitieu;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;
import java.util.List;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder> {

    private List<CalendarDay> days;
    private int selectedPosition = -1;

    public CalendarDayAdapter(List<CalendarDay> days) {
        this.days = days;
        // Tìm vị trí của ngày hôm nay để mặc định chọn
        for (int i = 0; i < days.size(); i++) {
            if (days.get(i).isToday()) {
                selectedPosition = i;
                days.get(i).setSelected(true);
                break;
            }
        }
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        CalendarDay day = days.get(position);
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(day.getDate());
        
        holder.tvDayNumber.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
        
        if (day.getAmount() > 0) {
            holder.tvDayAmount.setVisibility(View.VISIBLE);
            holder.tvDayAmount.setText(formatAmount(day.getAmount()));
        } else {
            holder.tvDayAmount.setText("0₫"); // Hoặc để trống tùy ý bạn
            holder.tvDayAmount.setVisibility(View.VISIBLE);
        }

        // Logic hiển thị Today Circle (Dấu chấm xanh hoặc nền tròn cho số ngày)
        if (day.isToday()) {
            holder.tvDayNumber.setTextColor(Color.WHITE);
            holder.tvDayNumber.setBackgroundResource(R.drawable.bg_today_circle);
        } else {
            holder.tvDayNumber.setTextColor(day.isCurrentMonth() ? Color.BLACK : Color.LTGRAY);
            holder.tvDayNumber.setBackground(null);
        }

        // Logic hiển thị viền Xanh khi được CHỌN (Selected)
        if (day.isSelected()) {
            holder.cardView.setStrokeColor(Color.parseColor("#1A237E")); // Màu Primary
            holder.cardView.setStrokeWidth(4);
        } else {
            holder.cardView.setStrokeColor(Color.parseColor("#F0F0F0"));
            holder.cardView.setStrokeWidth(2);
        }

        // Sự kiện click
        holder.itemView.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos == RecyclerView.NO_POSITION) return;

            // Bỏ chọn vị trí cũ
            if (selectedPosition != -1) {
                days.get(selectedPosition).setSelected(false);
                notifyItemChanged(selectedPosition);
            }

            // Chọn vị trí mới
            selectedPosition = currentPos;
            days.get(selectedPosition).setSelected(true);
            notifyItemChanged(selectedPosition);
        });
    }

    private String formatAmount(long amount) {
        if (amount >= 1000000) return (amount / 1000000) + "tr";
        if (amount >= 1000) return (amount / 1000) + "k";
        return String.valueOf(amount);
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView tvDayNumber, tvDayAmount;
        MaterialCardView cardView;

        public DayViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDayNumber = itemView.findViewById(R.id.tvDayNumber);
            tvDayAmount = itemView.findViewById(R.id.tvDayAmount);
            cardView = (MaterialCardView) itemView;
        }
    }
}
