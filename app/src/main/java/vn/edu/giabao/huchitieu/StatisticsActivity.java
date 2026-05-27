package vn.edu.giabao.huchitieu;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
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

public class StatisticsActivity extends AppCompatActivity {

    private BarChart barChart;
    private TextView tvTotalIncome, tvTotalExpense;
    private String userKey;
    private DatabaseReference transRef;
    private List<Transaction> transactionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        userKey = getIntent().getStringExtra("USER_KEY");
        
        initViews();
        loadTransactions();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Thống kê thu chi");
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        barChart = findViewById(R.id.barChart);

        setupChart();
    }

    private void setupChart() {
        barChart.getDescription().setEnabled(false);
        barChart.setPinchZoom(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawGridBackground(false);

        // X-Axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setCenterAxisLabels(true);
        xAxis.setLabelCount(7);
        xAxis.setTextColor(Color.DKGRAY);

        // Y-Axis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setSpaceTop(35f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (value >= 1000000) {
                    return String.format(Locale.getDefault(), "%.1fM", value / 1000000);
                } else if (value >= 1000) {
                    return String.format(Locale.getDefault(), "%.0fK", value / 1000);
                }
                return String.valueOf((int) value);
            }
        });

        barChart.getAxisRight().setEnabled(false);

        // Legend
        Legend l = barChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setYOffset(0f);
        l.setXOffset(10f);
        l.setYEntrySpace(0f);
        l.setTextSize(12f);

        barChart.animateY(1000);
    }

    private void loadTransactions() {
        if (userKey == null) return;

        transRef = FirebaseDatabase.getInstance().getReference("users")
                .child(userKey).child("transactions");

        transRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                transactionList.clear();
                long totalIncome = 0;
                long totalExpense = 0;

                for (DataSnapshot data : snapshot.getChildren()) {
                    Transaction trans = data.getValue(Transaction.class);
                    if (trans != null) {
                        transactionList.add(trans);
                        if ("Income".equalsIgnoreCase(trans.getType())) {
                            totalIncome += trans.getAmount();
                        } else if ("Expense".equalsIgnoreCase(trans.getType())) {
                            totalExpense += trans.getAmount();
                        }
                    }
                }

                NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                tvTotalIncome.setText(currencyFormat.format(totalIncome));
                tvTotalExpense.setText(currencyFormat.format(totalExpense));

                updateChartData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StatisticsActivity.this, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateChartData() {
        List<BarEntry> incomeEntries = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        Map<String, Long> incomeMap = new HashMap<>();
        Map<String, Long> expenseMap = new HashMap<>();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        
        // Tạo danh sách 7 ngày gần nhất
        List<String> last7Days = new ArrayList<>();
        Calendar tempCal = Calendar.getInstance();
        tempCal.add(Calendar.DAY_OF_YEAR, -6);
        for (int i = 0; i < 7; i++) {
            String day = sdf.format(tempCal.getTime());
            last7Days.add(day);
            labels.add(day);
            incomeMap.put(day, 0L);
            expenseMap.put(day, 0L);
            tempCal.add(Calendar.DAY_OF_YEAR, 1);
        }

        SimpleDateFormat fullSdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat labelSdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

        for (Transaction trans : transactionList) {
            try {
                if (trans.getDate() == null) continue;
                Date transDate = fullSdf.parse(trans.getDate());
                if (transDate != null) {
                    String label = labelSdf.format(transDate);
                    if (incomeMap.containsKey(label)) {
                        if ("Income".equalsIgnoreCase(trans.getType())) {
                            incomeMap.put(label, incomeMap.get(label) + trans.getAmount());
                        } else if ("Expense".equalsIgnoreCase(trans.getType())) {
                            expenseMap.put(label, expenseMap.get(label) + trans.getAmount());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (int i = 0; i < 7; i++) {
            String day = last7Days.get(i);
            incomeEntries.add(new BarEntry(i, incomeMap.get(day)));
            expenseEntries.add(new BarEntry(i, expenseMap.get(day)));
        }

        BarDataSet incomeSet = new BarDataSet(incomeEntries, "Thu nhập");
        incomeSet.setColor(Color.parseColor("#4CAF50"));
        incomeSet.setDrawValues(false);

        BarDataSet expenseSet = new BarDataSet(expenseEntries, "Chi tiêu");
        expenseSet.setColor(Color.parseColor("#F44336"));
        expenseSet.setDrawValues(false);

        float groupSpace = 0.08f;
        float barSpace = 0.03f;
        float barWidth = 0.43f;
        // (0.43 + 0.03) * 2 + 0.08 = 1.00

        BarData data = new BarData(incomeSet, expenseSet);
        data.setBarWidth(barWidth);
        
        barChart.setData(data);
        
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setAxisMinimum(0);
        xAxis.setAxisMaximum(7);
        
        barChart.groupBars(0, groupSpace, barSpace);
        barChart.invalidate();
    }
}
