package com.example.mr_manager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference usersReference;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar mainToolbar;
    private TextView textHeaderUsername;

    private ActionBarDrawerToggle drawerToggle;

    // Remains false unless Firebase confirms that the user is an admin.
    private boolean isAdmin = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            goToLoginActivity();
            return;
        }

        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        mainToolbar = findViewById(R.id.mainToolbar);

        View headerView = navigationView.getHeaderView(0);
        textHeaderUsername = headerView.findViewById(
                R.id.textHeaderUsername
        );

        setSupportActionBar(mainToolbar);

        drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                mainToolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Admin starts hidden for every account.
        navigationView.getMenu()
                .findItem(R.id.nav_admin)
                .setVisible(false);

        if (savedInstanceState == null) {
            openFragment(new HomeFragment());
            navigationView.setCheckedItem(R.id.nav_home);
            mainToolbar.setTitle("Home");
        }

        configureBackButton();
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        String currentUserId =
                firebaseAuth.getCurrentUser().getUid();

        usersReference = FirebaseDatabase.getInstance()
                .getReference("users");

        usersReference.child(currentUserId)
                .addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot
                            ) {
                                User user =
                                        snapshot.getValue(User.class);

                                if (user == null) {
                                    keepRegularUserAccess();
                                    return;
                                }

                                displayUsername(user.getUsername());

                                String role = user.getRole();

                                isAdmin = role != null
                                        && role.equalsIgnoreCase("admin");

                                navigationView.getMenu()
                                        .findItem(R.id.nav_admin)
                                        .setVisible(isAdmin);
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error
                            ) {
                                keepRegularUserAccess();

                                Toast.makeText(
                                        MainActivity.this,
                                        "Could not load account details.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }

    private void displayUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            textHeaderUsername.setText(
                    "Task Management Application"
            );
        } else {
            textHeaderUsername.setText(
                    "Signed in as " + username
            );
        }
    }

    private void keepRegularUserAccess() {
        isAdmin = false;

        navigationView.getMenu()
                .findItem(R.id.nav_admin)
                .setVisible(false);

        textHeaderUsername.setText(
                "Task Management Application"
        );
    }

    @Override
    public boolean onNavigationItemSelected(
            @NonNull MenuItem item
    ) {
        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            openFragment(new HomeFragment());
            mainToolbar.setTitle("Home");

        } else if (itemId == R.id.nav_tasks) {
            openFragment(new TasksFragment());
            mainToolbar.setTitle("Tasks");

        } else if (itemId == R.id.nav_admin) {
            if (isAdmin) {
                Intent adminIntent = new Intent(
                        MainActivity.this,
                        AdminActivity.class
                );

                startActivity(adminIntent);
            } else {
                Toast.makeText(
                        this,
                        "Administrator access is required.",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } else if (itemId == R.id.nav_logout) {
            firebaseAuth.signOut();
            goToLoginActivity();
            return true;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    private void configureBackButton() {
        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (drawerLayout.isDrawerOpen(
                                GravityCompat.START
                        )) {
                            drawerLayout.closeDrawer(
                                    GravityCompat.START
                            );
                        } else {
                            setEnabled(false);
                            getOnBackPressedDispatcher()
                                    .onBackPressed();
                        }
                    }
                }
        );
    }

    private void goToLoginActivity() {
        Intent intent = new Intent(
                MainActivity.this,
                LoginActivity.class
        );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);
        finish();
    }
}