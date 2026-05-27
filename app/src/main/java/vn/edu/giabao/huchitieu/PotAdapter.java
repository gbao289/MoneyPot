package vn.edu.giabao.huchitieu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PotAdapter extends RecyclerView.Adapter<PotAdapter.PotViewHolder> {

    private List<Pot> potList;
    private OnPotClickListener listener;

    public interface OnPotClickListener {
        void onPotClick(Pot pot);
        void onPotLongClick(Pot pot);
    }

    public PotAdapter(List<Pot> potList, OnPotClickListener listener) {
        this.potList = potList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pot, parent, false);
        return new PotViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PotViewHolder holder, int position) {
        Pot pot = potList.get(position);
        holder.textViewName.setText(pot.getName());
        
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        holder.textViewBalance.setText(currencyFormat.format(pot.getBalance()));
        
        holder.progressBar.setProgress(pot.getPercent());
        holder.textViewPercent.setText(pot.getPercent() + "%");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPotClick(pot);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onPotLongClick(pot);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return potList.size();
    }

    static class PotViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName, textViewBalance, textViewPercent;
        ProgressBar progressBar;

        public PotViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewPotName);
            textViewBalance = itemView.findViewById(R.id.textViewPotBalance);
            textViewPercent = itemView.findViewById(R.id.textViewPotPercent);
            progressBar = itemView.findViewById(R.id.progressBarPot);
        }
    }
}
