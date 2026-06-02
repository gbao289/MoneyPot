package vn.edu.giabao.huchitieu;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
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
    private TextInputLayout tilSource;
    private MaterialButtonToggleGroup toggleGroupType;
    private MaterialButton btnSave;
    private String userKey;
    private Calendar calendar = Calendar.getInstance();
    private List<Pot> potList = new ArrayList<>();
    private List<String> potNames = new ArrayList<>();
    private long currentUserBalance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        userKey = getIntent().getStringExtra("USER_KEY");

        initViews();
        setupDropdowns();
        setupDatePicker();
        loadPotsData();
        loadUserData();
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
        tilSource = findViewById(R.id.tilSource);
        toggleGroupType = findViewById(R.id.toggleGroupType);
        btnSave = findViewById(R.id.btnSaveTransaction);

        updateLabel();

        toggleGroupType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btnIncome) {
                    tilSource.setVisibility(View.GONE);
                } else {
                    tilSource.setVisibility(View.VISIBLE);
                }
            }
        });

        // Set default state based on initial selection
        if (toggleGroupType.getCheckedButtonId() == R.id.btnIncome) {
            tilSource.setVisibility(View.GONE);
        } else {
            tilSource.setVisibility(View.VISIBLE);
        }

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

    private void loadUserData() {
        if (userKey == null) return;
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userKey);
        userRef.child("balance").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUserBalance = snapshot.getValue(Long.class);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
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

        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
            return;
        }

        long amount = Long.parseLong(amountStr);
        String type = toggleGroupType.getCheckedButtonId() == R.id.btnIncome ? "Income" : "Expense";

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("users").child(userKey);
        String transKey = dbRef.child("transactions").push().getKey();

        if (type.equals("Income")) {
            Transaction transaction = new Transaction(amount, dateStr, type, frequency, "GeneralBalance", note, calendar.getTimeInMillis());
            if (transKey != null) {
                dbRef.child("transactions").child(transKey).setValue(transaction);
                dbRef.child("balance").setValue(currentUserBalance + amount);
                Toast.makeText(this, "Lưu thu nhập thành công", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            if (potName.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn hũ chi tiêu", Toast.LENGTH_SHORT).show();
                return;
            }

            Pot selectedPot = null;
            for (Pot pot : potList) {
                if (pot.getName().equals(potName)) {
                    selectedPot = pot;
                    break;
                }
            }

            if (selectedPot == null) return;

            // Kiểm tra số dư trong hũ
            if (selectedPot.getBalance() < amount) {
                Toast.makeText(this, "Số dư trong hũ không đủ", Toast.LENGTH_SHORT).show();
                return;
            }

            Transaction transaction = new Transaction(amount, dateStr, type, frequency, selectedPot.getKey(), note, calendar.getTimeInMillis());
            if (transKey != null) {
                dbRef.child("transactions").child(transKey).setValue(transaction);
                long newBalance = selectedPot.getBalance() - amount;
                dbRef.child("pots").child(selectedPot.getKey()).child("balance").setValue(newBalance);
                Toast.makeText(this, "Lưu chi tiêu thành công", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
