package vn.edu.giabao.huchitieu;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AddPotActivity extends AppCompatActivity {

    private TextInputEditText etPotName, etInitialBalance, etPercent;
    private MaterialButton btnSavePot;
    private String userKey, potKey;
    private DatabaseReference userRef, potsRef;
    private boolean isEditMode = false;
    private long currentUserBalance = 0;
    private long oldPotBalance = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pot);

        initViews();
        setupData();
        loadUserBalance();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        etPotName = findViewById(R.id.etPotName);
        etInitialBalance = findViewById(R.id.etInitialBalance);
        etPercent = findViewById(R.id.etPercent);
        btnSavePot = findViewById(R.id.btnSavePot);

        btnSavePot.setOnClickListener(v -> savePot());
    }

    private void setupData() {
        userKey = getIntent().getStringExtra("USER_KEY");
        potKey = getIntent().getStringExtra("POT_KEY");

        if (userKey != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(userKey);
            potsRef = userRef.child("pots");
        }

        if (potKey != null) {
            isEditMode = true;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Sửa Hũ Chi Tiêu");
            }
            btnSavePot.setText("Cập Nhật Hũ");

            etPotName.setText(getIntent().getStringExtra("POT_NAME"));
            oldPotBalance = getIntent().getLongExtra("POT_BALANCE", 0);
            etInitialBalance.setText(String.valueOf(oldPotBalance));
            etPercent.setText(String.valueOf(getIntent().getIntExtra("POT_PERCENT", 0)));
        }
    }

    private void loadUserBalance() {
        if (userRef == null) return;
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

    private void savePot() {
        String name = etPotName.getText().toString().trim();
        String balanceStr = etInitialBalance.getText().toString().trim();
        String percentStr = etPercent.getText().toString().trim();

        if (name.isEmpty() || balanceStr.isEmpty() || percentStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        long newBalance = Long.parseLong(balanceStr);
        int percent = Integer.parseInt(percentStr);

        // Kiểm tra xem số dư tổng có đủ để chuyển vào hũ không (chỉ khi tạo mới hoặc tăng tiền hũ)
        long diff = isEditMode ? (newBalance - oldPotBalance) : newBalance;
        if (currentUserBalance < diff) {
            Toast.makeText(this, "Số dư không đủ để thực hiện thao tác này", Toast.LENGTH_SHORT).show();
            return;
        }

        if (potsRef != null) {
            Pot pot = new Pot(name, newBalance, percent);
            DatabaseReference targetRef = isEditMode ? potsRef.child(potKey) : potsRef.push();

            targetRef.setValue(pot).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Cập nhật số dư tổng của người dùng
                    userRef.child("balance").setValue(currentUserBalance - diff);

                    String msg = isEditMode ? "Cập nhật thành công" : "Thêm hũ thành công";
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
