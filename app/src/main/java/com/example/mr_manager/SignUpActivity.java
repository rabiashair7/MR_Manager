package com.example.mr_manager;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {

    private static final String INTERNAL_EMAIL_DOMAIN = "@mrmanager.app";
    private static final String DEFAULT_ROLE = "user";

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[\\p{L}][\\p{L} '\\-]{0,39}$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_]{3,20}$");

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile(
                    "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s]).{8,64}$"
            );

    private TextInputLayout tilFirstName;
    private TextInputLayout tilLastName;
    private TextInputLayout tilUsername;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;

    private TextInputEditText etFirstName;
    private TextInputEditText etLastName;
    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    private MaterialButton btnSignUp;
    private ProgressBar progressBarSignUp;
    private TextView tvBackToLogin;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersReference;

    private boolean registrationInProgress = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initializeViews();
        initializeFirebase();
        initializeListeners();
    }

    private void initializeViews() {
        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilUsername = findViewById(R.id.tilUsername);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnSignUp = findViewById(R.id.btnSignUp);
        progressBarSignUp = findViewById(R.id.progressBarSignUp);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();

        usersReference = FirebaseDatabase
                .getInstance()
                .getReference("users");
    }

    private void initializeListeners() {
        btnSignUp.setOnClickListener(view -> attemptSignUp());

        tvBackToLogin.setOnClickListener(view -> {
            if (!registrationInProgress) {
                finish();
            }
        });
    }

    private void attemptSignUp() {
        clearFieldErrors();

        String firstName = getInputText(etFirstName);
        String lastName = getInputText(etLastName);
        String username = getInputText(etUsername);
        String password = getRawInputText(etPassword);
        String confirmPassword = getRawInputText(etConfirmPassword);

        if (!validateInput(
                firstName,
                lastName,
                username,
                password,
                confirmPassword
        )) {
            return;
        }

        String usernameLowercase = username.toLowerCase(Locale.ROOT);
        String internalEmail = usernameLowercase + INTERNAL_EMAIL_DOMAIN;

        setLoading(true);

        firebaseAuth
                .createUserWithEmailAndPassword(internalEmail, password)
                .addOnCompleteListener(this, authTask -> {
                    if (!authTask.isSuccessful()) {
                        setLoading(false);
                        handleAuthenticationError(authTask.getException());
                        return;
                    }

                    FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

                    if (firebaseUser == null) {
                        firebaseAuth.signOut();
                        setLoading(false);

                        Toast.makeText(
                                SignUpActivity.this,
                                R.string.account_creation_unknown_error,
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    saveUserProfile(
                            firebaseUser,
                            firstName,
                            lastName,
                            username,
                            usernameLowercase
                    );
                });
    }

    private boolean validateInput(
            String firstName,
            String lastName,
            String username,
            String password,
            String confirmPassword
    ) {
        boolean isValid = true;

        if (TextUtils.isEmpty(firstName)) {
            tilFirstName.setError(getString(R.string.error_first_name_required));
            isValid = false;
        } else if (!NAME_PATTERN.matcher(firstName).matches()) {
            tilFirstName.setError(getString(R.string.error_invalid_first_name));
            isValid = false;
        }

        if (TextUtils.isEmpty(lastName)) {
            tilLastName.setError(getString(R.string.error_last_name_required));
            isValid = false;
        } else if (!NAME_PATTERN.matcher(lastName).matches()) {
            tilLastName.setError(getString(R.string.error_invalid_last_name));
            isValid = false;
        }

        if (TextUtils.isEmpty(username)) {
            tilUsername.setError(getString(R.string.error_username_required));
            isValid = false;
        } else if (!USERNAME_PATTERN.matcher(username).matches()) {
            tilUsername.setError(getString(R.string.error_invalid_username));
            isValid = false;
        }

        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.error_password_required));
            isValid = false;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            tilPassword.setError(getString(R.string.error_weak_password));
            isValid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            tilConfirmPassword.setError(
                    getString(R.string.error_confirm_password_required)
            );
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError(
                    getString(R.string.error_passwords_do_not_match)
            );
            isValid = false;
        }

        return isValid;
    }

    private void saveUserProfile(
            @NonNull FirebaseUser firebaseUser,
            String firstName,
            String lastName,
            String username,
            String usernameLowercase
    ) {
        String uid = firebaseUser.getUid();

        User newUser = new User(
                uid,
                firstName,
                lastName,
                username,
                usernameLowercase,
                DEFAULT_ROLE
        );

        usersReference
                .child(uid)
                .setValue(newUser)
                .addOnCompleteListener(databaseTask -> {
                    if (databaseTask.isSuccessful()) {
                        handleRegistrationSuccess();
                    } else {
                        rollbackAuthenticationAccount(firebaseUser);
                    }
                });
    }

    private void handleRegistrationSuccess() {
        firebaseAuth.signOut();
        setLoading(false);

        Toast.makeText(
                SignUpActivity.this,
                R.string.registration_successful,
                Toast.LENGTH_LONG
        ).show();

        /*
         * LoginActivity has not been created yet, so finish() returns to
         * whichever Activity opened SignUpActivity.
         *
         * When LoginActivity exists, this can be replaced with:
         *
         * Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
         * intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
         *         | Intent.FLAG_ACTIVITY_NEW_TASK
         *         | Intent.FLAG_ACTIVITY_CLEAR_TASK);
         * startActivity(intent);
         */

        finish();
    }

    private void rollbackAuthenticationAccount(
            @NonNull FirebaseUser firebaseUser
    ) {
        firebaseUser.delete().addOnCompleteListener(deleteTask -> {
            firebaseAuth.signOut();
            setLoading(false);

            if (deleteTask.isSuccessful()) {
                Toast.makeText(
                        SignUpActivity.this,
                        R.string.profile_save_failed_account_removed,
                        Toast.LENGTH_LONG
                ).show();
            } else {
                Toast.makeText(
                        SignUpActivity.this,
                        R.string.profile_save_failed_cleanup_failed,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void handleAuthenticationError(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            tilUsername.setError(getString(R.string.error_username_taken));
            etUsername.requestFocus();
            return;
        }

        if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(
                    this,
                    R.string.error_invalid_registration_information,
                    Toast.LENGTH_LONG
            ).show();
            return;
        }

        if (exception instanceof FirebaseAuthException) {
            FirebaseAuthException authException =
                    (FirebaseAuthException) exception;

            String errorCode = authException.getErrorCode();

            if ("ERROR_WEAK_PASSWORD".equals(errorCode)) {
                tilPassword.setError(getString(R.string.error_weak_password));
                etPassword.requestFocus();
                return;
            }

            if ("ERROR_EMAIL_ALREADY_IN_USE".equals(errorCode)) {
                tilUsername.setError(getString(R.string.error_username_taken));
                etUsername.requestFocus();
                return;
            }

            if ("ERROR_NETWORK_REQUEST_FAILED".equals(errorCode)) {
                Toast.makeText(
                        this,
                        R.string.error_network,
                        Toast.LENGTH_LONG
                ).show();
                return;
            }
        }

        Toast.makeText(
                this,
                R.string.registration_failed,
                Toast.LENGTH_LONG
        ).show();
    }

    private void setLoading(boolean loading) {
        registrationInProgress = loading;

        btnSignUp.setEnabled(!loading);
        tvBackToLogin.setEnabled(!loading);

        progressBarSignUp.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );

        btnSignUp.setText(
                loading ? R.string.creating_account : R.string.sign_up
        );
    }

    private void clearFieldErrors() {
        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilUsername.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
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