package com.csehub.app.dashboard.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.csehub.app.R;
import com.csehub.app.auth.data.AuthRepository;
import com.csehub.app.auth.ui.LoginActivity;
import com.csehub.app.core.base.BaseActivity;
import com.csehub.app.databinding.ActivityMainBinding;

import java.io.File;

/**
 * MainActivity orchestration role-based layouts (Bottom Navigation vs Navigation Drawer)
 * and deep link parsing
 */
public class MainActivity extends BaseActivity {

    private ActivityMainBinding binding;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set Toolbar
        setSupportActionBar(binding.toolbar);

        // Retrieve NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        setupRoleBasedNavigation();
        handleIntentExtras(getIntent());
        checkNotificationPermission();
        checkDeviceSecurity();
    }

    private void checkDeviceSecurity() {
        boolean isRooted = checkRootMethod1() || checkRootMethod2();
        if (isRooted) {
            android.widget.Toast.makeText(this, "WARNING: Rooted device detected! Sensitive operations may be restricted.", android.widget.Toast.LENGTH_LONG).show();
            // Restrict features if necessary based on user requirements.
        }
    }

    private boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private boolean checkRootMethod2() {
        String[] paths = {
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
            "/system/bin/failsafe/su", "/data/local/su"
        };
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private void checkNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void setupRoleBasedNavigation() {
        // 1. Everyone gets the Navigation Drawer now!
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        binding.navigationView.setVisibility(View.VISIBLE);

        // 2. Setup dynamic bottom navigation
        if (tokenManager.isAdmin()) {
            binding.bottomNavView.setVisibility(View.GONE);
        } else if (tokenManager.isFaculty()) {
            binding.bottomNavView.setVisibility(View.VISIBLE);
            binding.bottomNavView.getMenu().clear();
            binding.bottomNavView.inflateMenu(R.menu.bottom_nav_faculty);
        } else {
            binding.bottomNavView.setVisibility(View.VISIBLE);
            binding.bottomNavView.getMenu().clear();
            binding.bottomNavView.inflateMenu(R.menu.bottom_nav_student);
        }

        // 3. Define AppBar Configuration with all possible top-level destinations
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_notifications,
                R.id.navigation_events, R.id.navigation_timetable,
                R.id.navigation_files, R.id.navigation_gallery,
                R.id.navigation_search_student, R.id.navigation_admin,
                R.id.navigation_profile
        ).setOpenableLayout(binding.drawerLayout).build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navigationView, navController);
        
        if (!tokenManager.isAdmin()) {
            NavigationUI.setupWithNavController(binding.bottomNavView, navController);
            
            // Fix: If a user navigates to a fragment not in the bottom nav (e.g., Events for Students),
            // the bottom nav retains its previous selected state. Clicking it again triggers a "reselect".
            // We override the default empty reselect listener to force it to pop back to the selected destination.
            binding.bottomNavView.setOnItemReselectedListener(item -> {
                NavigationUI.onNavDestinationSelected(item, navController);
            });
        }

        // 4. Hide restricted items from Drawer based on role
        Menu drawerMenu = binding.navigationView.getMenu();
        if (tokenManager.isStudent()) {
            drawerMenu.findItem(R.id.navigation_admin).setVisible(false);
            drawerMenu.findItem(R.id.navigation_search_student).setVisible(false);
            // Rename "Files" to "Study Materials" in Drawer for students
            drawerMenu.findItem(R.id.navigation_files).setTitle("Study Materials");
        } else if (tokenManager.isFaculty()) {
            drawerMenu.findItem(R.id.navigation_admin).setVisible(false);
        }

        setupDrawerHeader();
        setupLogoutMenuListener();
    }

    private void setupDrawerHeader() {
        View headerView = binding.navigationView.getHeaderView(0);
        TextView nameTextView = headerView.findViewById(R.id.userNameTextView);
        TextView emailTextView = headerView.findViewById(R.id.userEmailTextView);

        nameTextView.setText(tokenManager.getUserName());
        emailTextView.setText(tokenManager.getUserEmail());
    }

    private void setupLogoutMenuListener() {
        binding.navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_logout) {
                performLogout();
                return true;
            }
            // Delegate routing back to navigation controller
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
            if (handled) {
                binding.drawerLayout.closeDrawers();
            }
            return handled;
        });
    }

    private void performLogout() {
        binding.drawerLayout.closeDrawers();
        new AuthRepository(this).logout(() -> {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void handleIntentExtras(Intent intent) {
        if (intent == null || navController == null) return;
        String navigateTo = intent.getStringExtra("navigate_to");
        if (navigateTo == null) return;

        switch (navigateTo) {
            case "notifications":
            case "notification_detail":
                navController.navigate(R.id.navigation_notifications);
                break;
            case "timetable":
                navController.navigate(R.id.navigation_timetable);
                break;
            case "events":
            case "event_detail":
                navController.navigate(R.id.navigation_events);
                break;
        }
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntentExtras(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

}
