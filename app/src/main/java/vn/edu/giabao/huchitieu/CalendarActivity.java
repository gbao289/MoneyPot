package vn.edu.giabao.huchitieu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarActivity extends AppCompatActivity {

    private TextView tvTotalIncome, tvTotalExpense, tvBalance, tvMonthTitle;
    private RecyclerView rvCalendarDays, rvTransactions;
    private BottomNavigationView bottomNavigationView;
    private ImageButton btnPrevMonth, btnNextMonth;
    private ImageView btnExpandCalendar;
    private MaterialButton btnAddTransaction;
    
    private String userKey;
    private Calendar currentCalendar; 
    private List<CalendarDay> calendarDayList;
    private CalendarDayAdapter dayAdapter;
    private boolean isExpanded = false;

    private DatabaseReference transRef;
    private List<Transaction> allTransactions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        userKey = getIntent().getStringExtra("USER_KEY");
        currentCalendar = Calendar.getInstance(); 

        if (userKey != null) {
            transRef = FirebaseDatabase.getInstance().getReference("users").child(userKey).child("transactions");
        }

        initViews();
        setupNavigation();
        setupCalendar();
        
        loadTransactionsFromFirebase();
    }

    private void initViews() {
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvBalance = findViewById(R.id.tvBalance);
        tvMonthTitle = findViewById(R.id.tvMonthTitle);
        
        rvCalendarDays = findViewById(R.id.rvCalendarDays);
        rvTransactions = findViewById(R.id.rvTransactions);
        
        btnPrevMonth = findViewById(R.id.btnPrevMonth);
        btnNextMonth = findViewById(R.id.btnNextMonth);
        btnExpandCalendar = findViewById(R.id.btnExpandCalendar);
        btnAddTransaction = findViewById(R.id.btnAddTransaction);
        
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendarData();
            calculateSummary();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendarData();
            calculateSummary();
        });

        btnExpandCalendar.setOnClickListener(v -> {
            isExpanded = !isExpanded;
            btnExpandCalendar.setImageResource(isExpanded ? 
                R.drawable.ic_handle_up : R.drawable.ic_handle_down);
            updateCalendarData();
        });

        btnAddTransaction.setOnClickListener(v -> {
            Intent intent = new Intent(CalendarActivity.this, AddTransactionActivity.class);
            intent.putExtra("USER_KEY", userKey);
            startActivity(intent);
        });

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupCalendar() {
        calendarDayList = new ArrayList<>();
        dayAdapter = new CalendarDayAdapter(calendarDayList, this::filterTransactionsByDay);
        rvCalendarDays.setLayoutManager(new GridLayoutManager(this, 7));
        rvCalendarDays.setAdapter(dayAdapter);
    }

    private void filterTransactionsByDay(CalendarDay day) {
        List<Transaction> filteredList = new ArrayList<>();
        Calendar dayCal = Calendar.getInstance();
        dayCal.setTime(day.getDate());

        for (Transaction trans : allTransactions) {
            Calendar transCal = Calendar.getInstance();
            transCal.setTimeInMillis(trans.getTimestamp());
            if (isSameDay(dayCal, transCal)) {
                filteredList.add(trans);
            }
        }
        rvTransactions.setAdapter(new TransactionAdapter(filteredList));
    }

    private void loadTransactionsFromFirebase() {
        if (transRef == null) return;

        transRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allTransactions.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Transaction trans = data.getValue(Transaction.class);
                    if (trans != null) {
                        allTransactions.add(trans);
                    }
                }
                updateCalendarData();
                calculateSummary();
                
                // Hiển thị giao dịch của ngày đang chọn (mặc định là hôm nay)
                for (CalendarDay day : calendarDayList) {
                    if (day.isSelected()) {
                        filterTransactionsByDay(day);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CalendarActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateSummary() {
        long totalIncome = 0;
        long totalExpense = 0;
        int currentMonth = currentCalendar.get(Calendar.MONTH);
        int currentYear = currentCalendar.get(Calendar.YEAR);

        for (Transaction trans : allTransactions) {
            Calendar transCal = Calendar.getInstance();
            transCal.setTimeInMillis(trans.getTimestamp());
            
            if (transCal.get(Calendar.MONTH) == currentMonth && transCal.get(Calendar.YEAR) == currentYear) {
                if ("Income".equals(trans.getType())) {
                    totalIncome += trans.getAmount();
                } else {
                    totalExpense += trans.getAmount();
                }
            }
        }
        updateSummaryUI(totalIncome, totalExpense);
    }

    private void updateCalendarData() {
        calendarDayList.clear();
        Calendar calendar = (Calendar) currentCalendar.clone();
        Calendar today = Calendar.getInstance();
        
        if (calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) && 
            calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            tvMonthTitle.setText("Tháng này");
        } else {
            SimpleDateFormat monthSdf = new SimpleDateFormat("'Tháng 'MM, yyyy", Locale.getDefault());
            tvMonthTitle.setText(monthSdf.format(calendar.getTime()));
        }

        if (isExpanded) {
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int daysBefore = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : (firstDayOfWeek - 2);
            calendar.add(Calendar.DAY_OF_MONTH, -daysBefore);
            fillDays(calendar, 42);
        } else {
            if (calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) && 
                calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
                calendar.setTime(today.getTime());
            } else {
                calendar.set(Calendar.DAY_OF_MONTH, 1);
            }
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int daysToSubtract = (dayOfWeek == Calendar.SUNDAY) ? 6 : (dayOfWeek - 2);
            calendar.add(Calendar.DAY_OF_MONTH, -daysToSubtract);
            fillDays(calendar, 7);
        }

        dayAdapter.notifyDataSetChanged();
    }

    private void fillDays(Calendar calendar, int count) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String todayStr = sdf.format(Calendar.getInstance().getTime());

        for (int i = 0; i < count; i++) {
            Date date = calendar.getTime();
            boolean isToday = sdf.format(date).equals(todayStr);
            boolean isCurrentMonth = calendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH);
            
            // Tính tổng giao dịch cho ngày này
            long dayAmount = 0;
            for (Transaction trans : allTransactions) {
                Calendar transCal = Calendar.getInstance();
                transCal.setTimeInMillis(trans.getTimestamp());
                if (isSameDay(calendar, transCal)) {
                    // Trong mẫu bạn gửi, thường hiển thị số dư thay đổi trong ngày (Thu - Chi)
                    if ("Income".equals(trans.getType())) dayAmount += trans.getAmount();
                    else dayAmount -= trans.getAmount();
                }
            }

            CalendarDay day = new CalendarDay(date, dayAmount, isToday, isCurrentMonth);
            // Nếu là hôm nay và chưa có ngày nào được chọn, mặc định chọn hôm nay
            if (isToday) {
                day.setSelected(true);
            }
            calendarDayList.add(day);
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }

    private void updateSummaryUI(long income, long expense) {
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        tvTotalIncome.setText(currencyFormat.format(income));
        tvTotalExpense.setText(currencyFormat.format(expense));
        tvBalance.setText(currencyFormat.format(income - expense));
    }

    private void setupNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_calendar);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_overview) {
                Intent intent = new Intent(this, MainActivity.class);
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
            }
            return id == R.id.nav_calendar;
        });
    }
}
