package vn.edu.giabao.huchitieu;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddPotActivity extends AppCompatActivity {

    private TextInputEditText etPotName, etInitialBalance, etPercent;
    private MaterialButton btnSavePot;
    private String userKey, potKey;
    private DatabaseReference potsRef;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pot);

        initViews();
        setupData();
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
            potsRef = FirebaseDatabase.getInstance().getReference("users").child(userKey).child("pots");
        }

        // Kiểm tra nếu có potKey thì là chế độ Sửa
        if (potKey != null) {
            isEditMode = true;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Sửa Hũ Chi Tiêu");
            }
            btnSavePot.setText("Cập Nhật Hũ");

            // Đổ dữ liệu cũ vào các ô nhập
            etPotName.setText(getIntent().getStringExtra("POT_NAME"));
            etInitialBalance.setText(String.valueOf(getIntent().getLongExtra("POT_BALANCE", 0)));
            etPercent.setText(String.valueOf(getIntent().getIntExtra("POT_PERCENT", 0)));
        }
    }

    private void savePot() {
        String name = etPotName.getText().toString().trim();
        String balanceStr = etInitialBalance.getText().toString().trim();
        String percentStr = etPercent.getText().toString().trim();

        if (name.isEmpty() || balanceStr.isEmpty() || percentStr.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        long balance = Long.parseLong(balanceStr);
        int percent = Integer.parseInt(percentStr);

        if (potsRef != null) {
            Pot pot = new Pot(name, balance, percent);
            
            DatabaseReference targetRef;
            if (isEditMode) {
                targetRef = potsRef.child(potKey);
            } else {
                targetRef = potsRef.push();
            }

            targetRef.setValue(pot).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
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
