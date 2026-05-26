package vn.edu.giabao.huchitieu;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RecurringTransactionAdapter extends RecyclerView.Adapter<RecurringTransactionAdapter.ViewHolder> {

    private List<Transaction> recurringList;

    public RecurringTransactionAdapter(List<Transaction> recurringList) {
        this.recurringList = recurringList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recurring_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction trans = recurringList.get(position);

        holder.tvNote.setText(trans.getNote().isEmpty() ? "Không có ghi chú" : trans.getNote());
        holder.tvFrequency.setText(trans.getFrequency());

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String amountStr = currencyFormat.format(trans.getAmount());

        if ("Income".equals(trans.getType())) {
            holder.tvAmount.setText("+" + amountStr);
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50")); // Xanh
            holder.imgIcon.setImageResource(android.R.drawable.ic_input_add);
            holder.imgIcon.setColorFilter(Color.parseColor("#4CAF50"));
        } else {
            holder.tvAmount.setText("-" + amountStr);
            holder.tvAmount.setTextColor(Color.parseColor("#F44336")); // Đỏ
            holder.imgIcon.setImageResource(android.R.drawable.ic_delete);
            holder.imgIcon.setColorFilter(Color.parseColor("#F44336"));
        }
    }

    @Override
    public int getItemCount() {
        return recurringList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNote, tvFrequency, tvAmount;
        ImageView imgIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNote = itemView.findViewById(R.id.tvTransNote);
            tvFrequency = itemView.findViewById(R.id.tvFrequency);
            tvAmount = itemView.findViewById(R.id.tvTransAmount);
            imgIcon = itemView.findViewById(R.id.imgTransIcon);
        }
    }
}
