package com.example.deliverymoviles;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.AuthResult;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword, editTextName;
    private TextView textViewError;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextName = findViewById(R.id.editTextName);
        Button buttonRegister = findViewById(R.id.buttonRegister);
        textViewError = findViewById(R.id.textViewError);

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                String name = editTextName.getText().toString().trim();

                if (validateInput(email, password, name)) {
                    registerUser(email, password, name);
                }
            }
        });
    }

    private boolean validateInput(String email, String password, String name) {
        if (email.isEmpty() || password.isEmpty() || name.isEmpty()) {
            textViewError.setVisibility(View.VISIBLE);
            textViewError.setText("Please fill in all fields.");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            textViewError.setVisibility(View.VISIBLE);
            textViewError.setText("Invalid email address.");
            return false;
        }

        if (password.length() < 6) {
            textViewError.setVisibility(View.VISIBLE);
            textViewError.setText("Password must be at least 6 characters long.");
            return false;
        }

        return true;
    }

    private void registerUser(String email, String password, String name) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String userId = mAuth.getCurrentUser().getUid();
                            Map<String, Object> user = new HashMap<>();
                            user.put("email", email);
                            user.put("password", password);
                            user.put("name", name);

                            // Accede a la instancia de FirebaseFirestore
                            FirebaseFirestore db = FirebaseFirestore.getInstance();

                            // Guarda los datos del usuario en Firestore
                            db.collection("deliverym").document("usuarios").collection("usuarios").document(userId)
                                    .set(user)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this, "Registration successful.",
                                                        Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                                finish();
                                            } else {
                                                textViewError.setVisibility(View.VISIBLE);
                                                textViewError.setText("Firestore error: " + task.getException().getMessage());
                                            }
                                        }
                                    });
                        } else {
                            textViewError.setVisibility(View.VISIBLE);
                            textViewError.setText("Authentication failed: " + task.getException().getMessage());
                        }
                    }
                });
    }


}
