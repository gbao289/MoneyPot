package vn.edu.giabao.huchitieu;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewPots;
    private PotAdapter potAdapter;
    private List<Pot> potList;
    private TextView textViewTotalBalance;
    private MaterialCardView btnCreatePot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textViewTotalBalance = findViewById(R.id.textViewTotalBalance);
        btnCreatePot = findViewById(R.id.btnCreatePot);
        recyclerViewPots = findViewById(R.id.recyclerViewPots);

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");
        myRef.setValue("Hello, World!");


        // Bắt đầu với danh sách trống
        potList = new ArrayList<>();
        potAdapter = new PotAdapter(potList);
        recyclerViewPots.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPots.setAdapter(potAdapter);

        updateTotalBalance();

        // Sự kiện click nút Tạo hũ
    }


    private void updateTotalBalance() {
        long total = 0;
        for (Pot pot : potList) {
            total += pot.getBalance();
        }
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        textViewTotalBalance.setText("Số dư: " + currencyFormat.format(total));
    }
}