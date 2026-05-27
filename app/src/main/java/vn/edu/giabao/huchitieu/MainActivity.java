package vn.edu.giabao.huchitieu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements PotAdapter.OnPotClickListener {

    private RecyclerView recyclerViewPots;
    private PotAdapter potAdapter;
    private List<Pot> potList;
    private TextView textViewTotalBalance, textViewUserName;
    private ImageView imageViewAvatar;
    private MaterialButton btnCreatePot; 
    private BottomNavigationView bottomNavigationView;
    private DatabaseReference userRef, potsRef;
    private String userKey;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        saveImageToDatabase(imageUri);
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        textViewTotalBalance = findViewById(R.id.textViewTotalBalance);
        textViewUserName = findViewById(R.id.textViewUserName);
        imageViewAvatar = findViewById(R.id.imageViewAvatar);
        btnCreatePot = findViewById(R.id.btnCreatePot);
        recyclerViewPots = findViewById(R.id.recyclerViewPots);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        userKey = getIntent().getStringExtra("USER_KEY");
        if (userKey != null) {
            userRef = FirebaseDatabase.getInstance().getReference("users").child(userKey);
            potsRef = userRef.child("pots"); 
            
            loadUserData();
            loadPotsData();
        }

        imageViewAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        });

        potList = new ArrayList<>();
        potAdapter = new PotAdapter(potList, this);
        recyclerViewPots.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewPots.setAdapter(potAdapter);

        setupNavigation();
        
        if (btnCreatePot != null) {
            btnCreatePot.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, AddPotActivity.class);
                intent.putExtra("USER_KEY", userKey);
                startActivity(intent);
            });
        }
    }

    private void setupNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_overview);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_overview) {
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
                Intent intent = new Intent(this, UtilitiesActivity.class);
                intent.putExtra("USER_KEY", userKey);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    @Override
    public void onPotClick(Pot pot) {
        // Nhấn bình thường: Vào chi tiết
        Intent intent = new Intent(this, PotDetailActivity.class);
        intent.putExtra("USER_KEY", userKey);
        intent.putExtra("POT_KEY", pot.getKey());
        intent.putExtra("POT_NAME", pot.getName());
        intent.putExtra("POT_BALANCE", pot.getBalance());
        intent.putExtra("POT_PERCENT", pot.getPercent());
        startActivity(intent);
    }

    @Override
    public void onPotLongClick(Pot pot) {
        // Nhấn giữ: Hiện Dialog Sửa/Xóa (Chức năng cũ)
        showAdjustPotDialog(pot);
    }

    private void showAdjustPotDialog(Pot pot) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_pot, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        MaterialButton btnEdit = view.findViewById(R.id.btnEdit);
        MaterialButton btnDelete = view.findViewById(R.id.btnDelete);

        btnEdit.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(MainActivity.this, AddPotActivity.class);
            intent.putExtra("USER_KEY", userKey);
            intent.putExtra("POT_KEY", pot.getKey());
            intent.putExtra("POT_NAME", pot.getName());
            intent.putExtra("POT_BALANCE", pot.getBalance());
            intent.putExtra("POT_PERCENT", pot.getPercent());
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> {
            dialog.dismiss();
            new AlertDialog.Builder(this)
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa hũ \"" + pot.getName() + "\"?")
                    .setPositiveButton("Xóa", (d, which) -> {
                        if (potsRef != null && pot.getKey() != null) {
                            potsRef.child(pot.getKey()).removeValue().addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Đã xóa hũ", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Lỗi khi xóa hũ", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .setNegativeButton("Hủy", null)
                    .show();
        });

        dialog.show();
    }

    private void loadUserData() {
        if (userRef == null) return;
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        textViewUserName.setText(user.getName());
                        
                        // Cập nhật số dư tổng hiển thị (User balance)
                        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
                        textViewTotalBalance.setText(currencyFormat.format(user.getBalance()));

                        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isEmpty()) {
                            try {
                                byte[] decodedString = Base64.decode(user.getAvatarUrl(), Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                Glide.with(MainActivity.this).load(decodedByte).circleCrop().into(imageViewAvatar);
                            } catch (Exception e) { e.printStackTrace(); }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void loadPotsData() {
        if (potsRef == null) return;
        potsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                potList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Pot pot = data.getValue(Pot.class);
                    if (pot != null) {
                        pot.setKey(data.getKey());
                        potList.add(pot);
                    }
                }
                Collections.sort(potList, (p1, p2) -> Integer.compare(p2.getPercent(), p1.getPercent()));
                potAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Lỗi tải dữ liệu hũ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveImageToDatabase(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 200, 200, true);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            String encodedImage = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
            if (userRef != null) {
                userRef.child("avatarUrl").setValue(encodedImage);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
