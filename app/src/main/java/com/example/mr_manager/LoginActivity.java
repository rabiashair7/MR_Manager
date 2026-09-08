package com.example.mr_manager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private static final String INTERNAL_EMAIL_DOMAIN = "@mrmanager.app";

    private static final String PREFERENCES_NAME =
            "mr_manager_preferences";

    private static final String KEY_REMEMBER_USERNAME =
            "remember_username";

    private static final String KEY_SAVED_USERNAME =
            "saved_username";

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_]{3,20}$");

    private TextInputLayout tilLoginUsername;
    private TextInputLayout tilLoginPassword;

    private TextInputEditText etLoginUsername;
    private TextInputEditText etLoginPassword;

    private MaterialCheckBox checkRememberUsername;
    private MaterialButton btnLogin;
    private ProgressBar progressBarLogin;
    private TextView tvCreateAccount;

    private FirebaseAuth firebaseAuth;
    private SharedPreferences sharedPreferences;

    private boolean loginInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeViews();
        initializeFirebase();
        initializeSharedPreferences();
        loadSavedUsername();
        initializeListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (firebaseAuth.getCurrentUser() != null) {
            openMainActivity();
        }
    }

    private void initializeViews() {
        tilLoginUsername = findViewById(R.id.tilLoginUsername);
        tilLoginPassword = findViewById(R.id.tilLoginPassword);

        etLoginUsername = findViewById(R.id.etLoginUsername);
        etLoginPassword = findViewById(R.id.etLoginPassword);

        checkRememberUsername =
                findViewById(R.id.checkRememberUsername);

        btnLogin = findViewById(R.id.btnLogin);
        progressBarLogin = findViewById(R.id.progressBarLogin);
        tvCreateAccount = findViewById(R.id.tvCreateAccount);
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    private void initializeSharedPreferences() {
        sharedPreferences = getSharedPreferences(
                PREFERENCES_NAME,
                Context.MODE_PRIVATE
        );
    }

    private void loadSavedUsername() {
        boolean rememberUsername = sharedPreferences.getBoolean(
                KEY_REMEMBER_USERNAME,
                false
        );

        checkRememberUsername.setChecked(rememberUsername);

        if (rememberUsername) {
            String savedUsername = sharedPreferences.getString(
                    KEY_SAVED_USERNAME,
                    ""
            );

            etLoginUsername.setText(savedUsername);
        }
    }

    private void initializeListeners() {
        btnLogin.setOnClickListener(view -> attemptLogin());

        tvCreateAccount.setOnClickListener(view -> {
            if (!loginInProgress) {
                Intent intent = new Intent(
                        LoginActivity.this,
                        SignUpActivity.class
                );

                startActivity(intent);
            }
        });
    }

    private void attemptLogin() {
        clearErrors();

        String username = getInputText(etLoginUsername);
        String password = getRawInputText(etLoginPassword);

        if (!validateInput(username, password)) {
            return;
        }

        String usernameLowercase = username.toLowerCase(Locale.ROOT);
        String internalEmail =
                usernameLowercase + INTERNAL_EMAIL_DOMAIN;

        setLoading(true);

        firebaseAuth
                .signInWithEmailAndPassword(internalEmail, password)
                .addOnCompleteListener(this, loginTask -> {
                    setLoading(false);

                    if (loginTask.isSuccessful()
                            && firebaseAuth.getCurrentUser() != null) {

                        saveUsernamePreference(username);

                        Toast.makeText(
                                LoginActivity.this,
                                R.string.login_successful,
                                Toast.LENGTH_SHORT
                        ).show();

                        openMainActivity();
                    } else {
                        Toast.makeText(
                                LoginActivity.this,
                                R.string.error_incorrect_login,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }

    private void saveUsernamePreference(String username) {
        SharedPreferences.Editor editor =
                sharedPreferences.edit();

        if (checkRememberUsername.isChecked()) {
            editor.putBoolean(KEY_REMEMBER_USERNAME, true);
            editor.putString(KEY_SAVED_USERNAME, username);
        } else {
            editor.remove(KEY_REMEMBER_USERNAME);
            editor.remove(KEY_SAVED_USERNAME);
        }

        editor.apply();
    }

    private boolean validateInput(String username, String password) {
        boolean isValid = true;

        if (TextUtils.isEmpty(username)) {
            tilLoginUsername.setError(
                    getString(R.string.error_username_required)
            );

            isValid = false;
        } else if (!USERNAME_PATTERN.matcher(username).matches()) {
            tilLoginUsername.setError(
                    getString(R.string.error_invalid_username)
            );

            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilLoginPassword.setError(
                    getString(R.string.error_password_required)
            );

            isValid = false;
        }

        return isValid;
    }

    private void openMainActivity() {
        Intent intent = new Intent(
                LoginActivity.this,
                MainActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }

    private void setLoading(boolean loading) {
        loginInProgress = loading;

        btnLogin.setEnabled(!loading);
        tvCreateAccount.setEnabled(!loading);
        checkRememberUsername.setEnabled(!loading);

        progressBarLogin.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );

        btnLogin.setText(
                loading ? R.string.logging_in : R.string.login
        );
    }

    private void clearErrors() {
        tilLoginUsername.setError(null);
        tilLoginPassword.setError(null);
    }

    private String getInputText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }

        return editText.getText().toString().trim();
    }

    private String getRawInputText(TextInputEditText editText) {
        if (editText.getText() == null) {
            return "";
        }

        return editText.getText().toString();
    }
}