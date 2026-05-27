package vn.edu.giabao.huchitieu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;

public class UtilitiesActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private MaterialCardView cardStatistics, cardReminders, cardCategories, cardLogout;
    private String userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_utilities);

        userKey = getIntent().getStringExtra("USER_KEY");

        setupToolbar();
        initViews();
        setupNavigation();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Tiện ích");
        }
    }

    private void initViews() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        cardStatistics = findViewById(R.id.cardStatistics);
        cardReminders = findViewById(R.id.cardReminders);
        cardCategories = findViewById(R.id.cardCategories);
        cardLogout = findViewById(R.id.cardLogout);

        cardStatistics.setOnClickListener(v -> {
            Intent intent = new Intent(this, StatisticsActivity.class);
            intent.putExtra("USER_KEY", userKey);
            startActivity(intent);
        });

        cardReminders.setOnClickListener(v -> Toast.makeText(this, "Tính năng Nhắc nhở đang phát triển", Toast.LENGTH_SHORT).show());
        cardCategories.setOnClickListener(v -> Toast.makeText(this, "Tính năng Quản lý danh mục đang phát triển", Toast.LENGTH_SHORT).show());
        
        cardLogout.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void setupNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_utilities);
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
                Intent intent = new Intent(this, RecurringTransactionActivity.class);
                intent.putExtra("USER_KEY", userKey);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                return true;
            } else if (id == R.id.nav_utilities) {
                return true;
            }
            return false;
        });
    }
}
