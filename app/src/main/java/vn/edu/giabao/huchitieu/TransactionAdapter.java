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

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactionList;

    public TransactionAdapter(List<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction trans = transactionList.get(position);
        
        holder.tvNote.setText(trans.getNote().isEmpty() ? "Không có ghi chú" : trans.getNote());
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        String amountStr = currencyFormat.format(trans.getAmount());
        
        if ("Income".equals(trans.getType())) {
            holder.tvAmount.setText("+" + amountStr);
            holder.tvAmount.setTextColor(Color.parseColor("#4CAF50")); // Green
            holder.imgIcon.setImageResource(android.R.drawable.ic_input_add);
            holder.imgIcon.setColorFilter(Color.parseColor("#4CAF50"));
        } else {
            holder.tvAmount.setText("-" + amountStr);
            holder.tvAmount.setTextColor(Color.parseColor("#F44336")); // Red
            holder.imgIcon.setImageResource(android.R.drawable.ic_delete);
            holder.imgIcon.setColorFilter(Color.parseColor("#F44336"));
        }
        
        // Hiển thị tên Hũ (Cần truyền Map hoặc nạp từ database nếu muốn hiện tên)
        // Hiện tại tạm thời hiển thị ID Hũ hoặc logic nạp tên hũ
        holder.tvPotName.setText(trans.getType().equals("Income") ? "Thu nhập" : "Chi tiêu");
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvPotName, tvNote, tvAmount;
        ImageView imgIcon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPotName = itemView.findViewById(R.id.tvTransPotName);
            tvNote = itemView.findViewById(R.id.tvTransNote);
            tvAmount = itemView.findViewById(R.id.tvTransAmount);
            imgIcon = itemView.findViewById(R.id.imgTransIcon);
        }
    }
}
