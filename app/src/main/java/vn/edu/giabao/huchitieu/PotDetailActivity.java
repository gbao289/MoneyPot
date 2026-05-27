package vn.edu.giabao.huchitieu;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class PotDetailActivity extends AppCompatActivity {

    private TextView tvPotName, tvPotBalance, tvPotPercent, tvEmptyHistory;
    private RecyclerView rvTransactions;
    private TransactionAdapter adapter;
    private List<Transaction> transactionList = new ArrayList<>();
    
    private String userKey, potKey;
    private DatabaseReference transRef, potRef;
    private Pot currentPot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pot_detail);

        userKey = getIntent().getStringExtra("USER_KEY");
        potKey = getIntent().getStringExtra("POT_KEY");

        initViews();
        loadPotDetails();
        loadTransactions();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Chi tiết hũ");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        tvPotName = findViewById(R.id.tvDetailPotName);
        tvPotBalance = findViewById(R.id.tvDetailPotBalance);
        tvPotPercent = findViewById(R.id.tvDetailPotPercent);
        tvEmptyHistory = findViewById(R.id.tvEmptyHistory);
        rvTransactions = findViewById(R.id.rvPotTransactions);

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new TransactionAdapter(transactionList);
        rvTransactions.setAdapter(adapter);
    }

    private void loadPotDetails() {
        if (userKey == null || potKey == null) return;
        potRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userKey).child("pots").child(potKey);

        potRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                currentPot = snapshot.getValue(Pot.class);
                if (currentPot != null) {
                    currentPot.setKey(snapshot.getKey());
                    updatePotUI();
                } else {
                    // Hũ đã bị xóa
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updatePotUI() {
        tvPotName.setText(currentPot.getName());
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvPotBalance.setText(currencyFormat.format(currentPot.getBalance()));
        tvPotPercent.setText(String.format(Locale.getDefault(), "Tỉ lệ: %d%%", currentPot.getPercent()));
    }

    private void loadTransactions() {
        if (userKey == null || potKey == null) return;

        transRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userKey).child("transactions");

        transRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Transaction trans = data.getValue(Transaction.class);
                    if (trans != null && potKey.equals(trans.getSourcePotKey())) {
                        transactionList.add(trans);
                    }
                }
                
                Collections.sort(transactionList, (t1, t2) -> Long.compare(t2.getTimestamp(), t1.getTimestamp()));
                
                adapter.notifyDataSetChanged();
                tvEmptyHistory.setVisibility(transactionList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PotDetailActivity.this, "Lỗi tải lịch sử", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
