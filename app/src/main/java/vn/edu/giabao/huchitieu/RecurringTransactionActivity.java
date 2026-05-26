package vn.edu.giabao.huchitieu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RecurringTransactionActivity extends AppCompatActivity {

    private RecyclerView rvRecurringTransactions;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabAddRecurring;
    private String userKey;
    private RecurringTransactionAdapter adapter;
    private List<Transaction> recurringList = new ArrayList<>();
    private DatabaseReference transRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recurring_transaction);

        userKey = getIntent().getStringExtra("USER_KEY");

        if (userKey != null) {
            transRef = FirebaseDatabase.getInstance().getReference("users").child(userKey).child("transactions");
        }

        setupToolbar();
        initViews();
        setupNavigation();
        loadRecurringTransactions();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Giao dịch định kỳ");
        }
    }

    private void initViews() {
        rvRecurringTransactions = findViewById(R.id.rvRecurringTransactions);
        fabAddRecurring = findViewById(R.id.fabAddRecurring);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        rvRecurringTransactions.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecurringTransactionAdapter(recurringList);
        rvRecurringTransactions.setAdapter(adapter);

        fabAddRecurring.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTransactionActivity.class);
            intent.putExtra("USER_KEY", userKey);
            startActivity(intent);
        });
    }

    private void loadRecurringTransactions() {
        if (transRef == null) return;

        transRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recurringList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Transaction trans = data.getValue(Transaction.class);
                    if (trans != null && trans.getFrequency() != null && !trans.getFrequency().equals("Không lặp lại")) {
                        trans.setKey(data.getKey());
                        recurringList.add(trans);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RecurringTransactionActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_recurring);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_overview) {
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("USER_KEY", userKey);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_calendar) {
                Intent intent = new Intent(this, CalendarActivity.class);
                intent.putExtra("USER_KEY", userKey);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_recurring) {
                return true;
            } else if (id == R.id.nav_utilities) {
                Toast.makeText(this, "Tính năng Tiện ích đang phát triển", Toast.LENGTH_SHORT).show();
                return false;
            }
            return false;
        });
    }
}
