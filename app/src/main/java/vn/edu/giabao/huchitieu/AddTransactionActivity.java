package vn.edu.giabao.huchitieu;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private TextInputEditText etAmount, etDate, etNote;
    private AutoCompleteTextView atvFrequency, atvSource;
    private MaterialButtonToggleGroup toggleGroupType;
    private MaterialButton btnSave;
    private String userKey;
    private Calendar calendar = Calendar.getInstance();
    private List<Pot> potList = new ArrayList<>();
    private List<String> potNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        userKey = getIntent().getStringExtra("USER_KEY");

        initViews();
        setupDropdowns();
        setupDatePicker();
        loadPotsData();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        etAmount = findViewById(R.id.etAmount);
        etDate = findViewById(R.id.etDate);
        etNote = findViewById(R.id.etNote);
        atvFrequency = findViewById(R.id.atvFrequency);
        atvSource = findViewById(R.id.atvSource);
        toggleGroupType = findViewById(R.id.toggleGroupType);
        btnSave = findViewById(R.id.btnSaveTransaction);

        updateLabel();

        btnSave.setOnClickListener(v -> saveTransaction());
    }

    private void setupDropdowns() {
        String[] frequencies = {"Không lặp lại", "Hàng ngày", "Hàng tuần", "Hàng tháng", "Hàng năm"};
        ArrayAdapter<String> adapterFreq = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, frequencies);
        atvFrequency.setAdapter(adapterFreq);
        atvFrequency.setText(frequencies[0], false);
    }

    private void setupDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, day) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, day);
            updateLabel();
        };

        etDate.setOnClickListener(v -> new DatePickerDialog(AddTransactionActivity.this, dateSetListener,
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)).show());
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        etDate.setText(sdf.format(calendar.getTime()));
    }

    private void loadPotsData() {
        if (userKey == null) return;
        DatabaseReference potsRef = FirebaseDatabase.getInstance().getReference("users").child(userKey).child("pots");
        potsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                potList.clear();
                potNames.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Pot pot = data.getValue(Pot.class);
                    if (pot != null) {
                        pot.setKey(data.getKey());
                        potList.add(pot);
                        potNames.add(pot.getName());
                    }
                }
                ArrayAdapter<String> adapterSource = new ArrayAdapter<>(AddTransactionActivity.this,
                        android.R.layout.simple_list_item_1, potNames);
                atvSource.setAdapter(adapterSource);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void saveTransaction() {
        String amountStr = etAmount.getText().toString().trim();
        String dateStr = etDate.getText().toString().trim();
        String note = etNote.getText().toString().trim();
        String frequency = atvFrequency.getText().toString();
        String potName = atvSource.getText().toString();

        if (amountStr.isEmpty() || potName.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền và chọn nguồn tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        long amount = Long.parseLong(amountStr);
        String type = toggleGroupType.getCheckedButtonId() == R.id.btnIncome ? "Income" : "Expense";

        // Tìm Pot được chọn để lấy key và cập nhật số dư
        Pot selectedPot = null;
        for (Pot pot : potList) {
            if (pot.getName().equals(potName)) {
                selectedPot = pot;
                break;
            }
        }

        if (selectedPot == null) return;

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(userKey);
        String transKey = dbRef.child("transactions").push().getKey();

        Transaction transaction = new Transaction(amount, dateStr, type, frequency, selectedPot.getKey(), note, calendar.getTimeInMillis());
        
        if (transKey != null) {
            // 1. Lưu giao dịch
            dbRef.child("transactions").child(transKey).setValue(transaction);

            // 2. Cập nhật số dư trong Hũ
            long newBalance = type.equals("Income") ? selectedPot.getBalance() + amount : selectedPot.getBalance() - amount;
            dbRef.child("pots").child(selectedPot.getKey()).child("balance").setValue(newBalance);

            Toast.makeText(this, "Lưu giao dịch thành công", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
