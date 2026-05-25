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
    private String userKey;
    private DatabaseReference potsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pot);

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

        userKey = getIntent().getStringExtra("USER_KEY");
        if (userKey != null) {
            potsRef = FirebaseDatabase.getInstance().getReference("users").child(userKey).child("pots");
        }

        btnSavePot.setOnClickListener(v -> savePot());
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
            String potId = potsRef.push().getKey();
            Pot pot = new Pot(name, balance, percent);
            if (potId != null) {
                potsRef.child(potId).setValue(pot).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Thêm hũ thành công", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(this, "Lỗi khi thêm hũ", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }
}
