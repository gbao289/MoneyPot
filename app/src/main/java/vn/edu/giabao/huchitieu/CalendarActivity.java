package vn.edu.giabao.huchitieu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private TextView tvTotalIncome, tvTotalExpense, tvBalance, tvMonthTitle;
    private RecyclerView rvCalendarDays, rvTransactions;
    private BottomNavigationView bottomNavigationView;
    private ImageButton btnPrevMonth, btnNextMonth;
    private ImageView btnExpandCalendar;
    
    private String userKey;
    private Calendar currentCalendar; 
    private List<CalendarDay> calendarDayList;
    private CalendarDayAdapter dayAdapter;
    private boolean isExpanded = false; // Trạng thái mở rộng lịch

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        userKey = getIntent().getStringExtra("USER_KEY");
        currentCalendar = Calendar.getInstance(); 

        initViews();
        setupNavigation();
        setupCalendar();
        
        updateSummaryUI();
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
        
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        btnPrevMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            updateCalendarData();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            updateCalendarData();
        });

        // Logic Mở rộng / Thu gọn lịch
        btnExpandCalendar.setOnClickListener(v -> {
            isExpanded = !isExpanded;
            // Đổi icon tương ứng
            btnExpandCalendar.setImageResource(isExpanded ? 
                R.drawable.ic_handle_up : R.drawable.ic_handle_down);
            updateCalendarData();
        });

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupCalendar() {
        calendarDayList = new ArrayList<>();
        dayAdapter = new CalendarDayAdapter(calendarDayList);
        rvCalendarDays.setLayoutManager(new GridLayoutManager(this, 7));
        rvCalendarDays.setAdapter(dayAdapter);
        
        updateCalendarData();
    }

    private void updateCalendarData() {
        calendarDayList.clear();
        
        Calendar calendar = (Calendar) currentCalendar.clone();
        Calendar today = Calendar.getInstance();
        
        // Cập nhật tiêu đề tháng
        if (calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) && 
            calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
            tvMonthTitle.setText("Tháng này");
        } else {
            SimpleDateFormat monthSdf = new SimpleDateFormat("'Tháng 'MM, yyyy", Locale.getDefault());
            tvMonthTitle.setText(monthSdf.format(calendar.getTime()));
        }

        if (isExpanded) {
            // CHẾ ĐỘ FULL THÁNG: Lấy 42 ngày (6 tuần)
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            int daysBefore = (firstDayOfWeek == Calendar.SUNDAY) ? 6 : (firstDayOfWeek - 2);
            calendar.add(Calendar.DAY_OF_MONTH, -daysBefore);
            
            fillDays(calendar, 42);
        } else {
            // CHẾ ĐỘ 1 TUẦN: Lấy tuần chứa ngày hôm nay (nếu là tháng này) hoặc tuần 1 (tháng khác)
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
            
            calendarDayList.add(new CalendarDay(date, 0, isToday, isCurrentMonth));
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private void updateSummaryUI() {
        tvTotalIncome.setText("0₫");
        tvTotalExpense.setText("0₫");
        tvBalance.setText("0₫");
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
            }
            return id == R.id.nav_calendar;
        });
    }
}
