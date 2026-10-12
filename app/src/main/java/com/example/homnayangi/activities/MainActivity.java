package com.example.homnayangi.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.homnayangi.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private BottomNavigationView bottomNav;
    private final HomeFragment fragmentHome = new HomeFragment();
    private final FoodListFragment fragmentFoodList = new FoodListFragment();
    private final HistoryFragment fragmentHistory = new HistoryFragment();
    private final CommunityFragment fragmentCommunity = new CommunityFragment();
    private final ProfileFragment fragmentProfile = new ProfileFragment();
    private Fragment activeFragment = fragmentHome;
    private boolean isGuest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        // Chưa đăng nhập → về LoginActivity
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        isGuest = currentUser.isAnonymous();

        setContentView(R.layout.activity_main);

        bottomNav = findViewById(R.id.bottomNavigation);
        bottomNav.setOnItemSelectedListener(navListener);

        // Ẩn tab Lịch sử và Hồ sơ khi là guest
        if (isGuest) {
            bottomNav.getMenu().findItem(R.id.nav_history).setVisible(false);
            bottomNav.getMenu().findItem(R.id.nav_profile).setVisible(false);
        }

        // Hiển thị HomeFragment lúc ban đầu
        if (savedInstanceState == null) {
            if (isGuest) {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragmentCommunity, "4").hide(fragmentCommunity)
                        .add(R.id.fragment_container, fragmentFoodList, "2").hide(fragmentFoodList)
                        .add(R.id.fragment_container, fragmentHome, "1").commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, fragmentProfile, "5").hide(fragmentProfile)
                        .add(R.id.fragment_container, fragmentCommunity, "4").hide(fragmentCommunity)
                        .add(R.id.fragment_container, fragmentHistory, "3").hide(fragmentHistory)
                        .add(R.id.fragment_container, fragmentFoodList, "2").hide(fragmentFoodList)
                        .add(R.id.fragment_container, fragmentHome, "1").commit();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isGuest) {
            // Guest thấy nút "Đăng nhập" để chuyển sang tài khoản thật
            menu.add(0, 1, 0, "Đăng nhập").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        } else {
            menu.add(0, 1, 0, "Đăng xuất").setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 1) {
            mAuth.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void navigateToTab(int itemId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(itemId);
        }
    }

    private final BottomNavigationView.OnItemSelectedListener navListener =
            new BottomNavigationView.OnItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    int itemId = item.getItemId();
                    if (itemId == R.id.nav_home) {
                        selectedFragment = fragmentHome;
                    } else if (itemId == R.id.nav_food_list) {
                        selectedFragment = fragmentFoodList;
                    } else if (itemId == R.id.nav_history && !isGuest) {
                        selectedFragment = fragmentHistory;
                    } else if (itemId == R.id.nav_community) {
                        selectedFragment = fragmentCommunity;
                    } else if (itemId == R.id.nav_profile && !isGuest) {
                        selectedFragment = fragmentProfile;
                    }

                    if (selectedFragment != null && selectedFragment != activeFragment) {
                        getSupportFragmentManager().beginTransaction()
                                .hide(activeFragment).show(selectedFragment).commit();
                        activeFragment = selectedFragment;
                        return true;
                    }
                    return false;
                }
            };
}