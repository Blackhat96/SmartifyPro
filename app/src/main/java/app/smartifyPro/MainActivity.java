package app.smartifyPro;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.rm.rmswitch.RMSwitch;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import mx.com.quiin.contactpicker.SimpleContact;
import mx.com.quiin.contactpicker.ui.ContactPickerActivity;


public class MainActivity extends RuntimePermission implements GoogleApiClient.OnConnectionFailedListener {
    private SharedPreferences sp;
    private final List<Adapter> movieList = new ArrayList<>();
    private ListAdapter mAdapter;
    private SharedPreferences.Editor editor;
    private static final int REQUEST_PERMISSION = 10;
    private static final int CONTACT_PICKER_REQUEST = 11;
    private static final int READ_CONTACT_REQUEST = 12;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAnalytics mFirebaseAnalytics;
    DatabaseReference mDatabase;
    String userId = null;
    // private final static int PERM_REQUEST_CODE_DRAW_OVERLAYS = 1;

    @Override
    public void onPermissionsGranted(int requestCode) {
        Snackbar SB = Snackbar.make(findViewById(android.R.id.content), "Permission Granted", Snackbar.LENGTH_LONG);
        ViewGroup group = (ViewGroup) SB.getView();
        SB.setActionTextColor(Color.BLACK);
        group.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.skyblue));
        SB.show();
    }


    private void launchContactPicker() {
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            Intent contactPicker = new Intent(this, ContactPickerActivity.class);
            startActivityForResult(contactPicker, CONTACT_PICKER_REQUEST);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                        READ_CONTACT_REQUEST);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CONTACT_PICKER_REQUEST:
                if (resultCode == RESULT_OK) {
                    TreeSet<SimpleContact> selectedContacts = (TreeSet<SimpleContact>) data.getSerializableExtra(ContactPickerActivity.CP_SELECTED_CONTACTS);
                    SharedPreferences pref = this.getSharedPreferences("Numbers", Context.MODE_PRIVATE);
                    SharedPreferences.Editor edit = pref.edit();

                    Set<String> oldSet = pref.getStringSet("num", new LinkedHashSet<String>());
                    Set<String> cur = pref.getStringSet("num", new LinkedHashSet<String>());

                    String numb;

                    for (SimpleContact selectedContact : selectedContacts) {
                        numb = selectedContact.getCommunication();
                        cur.add(numb);
                    }

                    oldSet.addAll(cur);

                    edit.putStringSet("asd", oldSet);
                    edit.putStringSet("num", oldSet);
                    edit.apply();
                    edit.commit();
                    edit.clear();
                    Snackbar SB = Snackbar.make(findViewById(android.R.id.content), "Number added Successfully", Snackbar.LENGTH_LONG);
                    ViewGroup group = (ViewGroup) SB.getView();
                    group.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.skyblue));
                    SB.setActionTextColor(Color.BLACK);
                    SB.show();
                } else
                    Toast.makeText(this, "No contacts selected", Toast.LENGTH_LONG).show();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @SuppressLint("BatteryLife")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sp = this.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        final int theme = sp.getInt("Theme", 0);
        setTheme(theme);
        setContentView(R.layout.activity_main);
        final SharedPreferences settings = this.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        editor = sp.edit();
        boolean isAppInstalled = sp.getBoolean("isAppInstalled", false);
        boolean isSignedIn = sp.getBoolean("LoggedIn", false);

        if (!isSignedIn) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
            mDatabase = FirebaseDatabase.getInstance().getReference();
            userId = mDatabase.push().getKey();
            mFirebaseAnalytics.setUserProperty("Email", user.getEmail());
            mFirebaseAnalytics.setUserProperty("Package", BuildConfig.APPLICATION_ID);
            UserProperty userPro = new UserProperty(user.getEmail(), BuildConfig.APPLICATION_ID);
            mDatabase.child(userId).setValue(userPro);
            editor.putBoolean("LoggedIn", true);
            editor.apply();
        }


        if (!isAppInstalled) {
            editor.putBoolean("isAppInstalled", true);
            editor.apply();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AutoStart(MainActivity.this);
                }
            }, 9000);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    launchContactPicker();
                }
            }, 13000);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    String packageName = MainActivity.this.getPackageName();
                    PowerManager pm = (PowerManager) MainActivity.this.getSystemService(Context.POWER_SERVICE);
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (pm.isIgnoringBatteryOptimizations(packageName))
                            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        else {
                            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + packageName));
                        }
                        MainActivity.this.startActivity(intent);
                    }
                }
            }, 6000);
        }

        if (!getPackageName().equals("app.smartifyPro")) {
            Toast.makeText(this, "Please respect developer's effort and buy this app", Toast.LENGTH_SHORT).show();
            finish();
        }

        //If its version is greater than lollipop
        if (Build.VERSION.SDK_INT >= 23) {
      /*      if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, PERM_REQUEST_CODE_DRAW_OVERLAYS);
            }
*/
            //Ask for permission
            int ReadContactPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS);
            int ReadPhoneStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
            if (ReadContactPermission != PackageManager.PERMISSION_GRANTED && ReadPhoneStatePermission != PackageManager.PERMISSION_GRANTED) {
                requestAppPermissions(new String[]{
                                Manifest.permission.READ_CONTACTS, Manifest.permission.READ_PHONE_STATE},
                        R.string.msg, REQUEST_PERMISSION);
            } else if (ReadContactPermission != PackageManager.PERMISSION_GRANTED) {
                requestAppPermissions(new String[]{
                                Manifest.permission.READ_CONTACTS},
                        R.string.msg, REQUEST_PERMISSION);
            } else if (ReadPhoneStatePermission != PackageManager.PERMISSION_GRANTED) {
                requestAppPermissions(new String[]{
                                Manifest.permission.READ_PHONE_STATE},
                        R.string.msg, REQUEST_PERMISSION);
            }
        }

        Boolean wifi = settings.getBoolean("wifi", false);
        Boolean ring = settings.getBoolean("ring", false);
        Boolean profile = settings.getBoolean("profile", false);
        RMSwitch S_wifi, S_ring, S_gen_silent;

        if (!getPackageName().equals("app.smartifyPro")) {
            Toast.makeText(this, "Please respect developer's effort and buy this app", Toast.LENGTH_SHORT).show();
            finish();
        }

        S_wifi = (RMSwitch) findViewById(R.id.s_wifi);
        S_wifi.setChecked(wifi);
        S_wifi.addSwitchObserver(new RMSwitch.RMSwitchObserver() {
            @Override
            public void onCheckStateChange(RMSwitch switchView, boolean isChecked) {
                editor.putBoolean("wifi", isChecked);
                editor.apply();
            }
        });

        S_ring = (RMSwitch) findViewById(R.id.s_ring);
        S_ring.setChecked(ring);
        S_ring.addSwitchObserver(new RMSwitch.RMSwitchObserver() {
            @Override
            public void onCheckStateChange(RMSwitch switchView, boolean isChecked) {
                editor.putBoolean("ring", isChecked);
                editor.apply();
            }
        });

        S_gen_silent = (RMSwitch) findViewById(R.id.s_gen_sil);
        S_gen_silent.setChecked(profile);
        S_gen_silent.addSwitchObserver(new RMSwitch.RMSwitchObserver() {
            @Override
            public void onCheckStateChange(RMSwitch switchView, boolean isChecked) {
                editor.putBoolean("profile", isChecked);
                editor.apply();
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new ListAdapter(movieList);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(mAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                switch (position) {
                    case 0:
                        Intent i = new Intent(getBaseContext(), Help.class);
                        startActivity(i);
                        break;
                    case 1:
                        new Extras().share_app(view.getContext());
                        break;
                    case 2:
                        new Extras().rate(view.getContext());
                        break;
                    case 3:
                        Intent about = new Intent(getBaseContext(), AboutDev.class);
                        startActivity(about);
                }
            }

            @Override
            public void onLongClick(View view, int position) {
            }
        }));
        prepareMovieData();
    }

    private void AutoStart(Context context) {
        String manufacturer = "xiaomi";
        Intent intent = new Intent();
        if (manufacturer.equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
            Toast.makeText(this, "In order for app to work properly, Add Smartify to auto Start manager.", Toast.LENGTH_LONG).show();
            intent.setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            context.startActivity(intent);
        } else
            showHelp();
    }

    @Override
    public void onBackPressed() {

        Intent intCloseApp = new Intent(Intent.ACTION_MAIN);
        intCloseApp.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intCloseApp.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intCloseApp.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intCloseApp.addCategory(Intent.CATEGORY_HOME);
        intCloseApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intCloseApp);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    private void showHelp() {
        final Dialog webViewDialog;
        webViewDialog = new Dialog(MainActivity.this);
        webViewDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        webViewDialog.setCancelable(true);
        webViewDialog.setContentView(R.layout.activity_splash);
        webViewDialog.show();
        TextView btnClose = (TextView) webViewDialog.findViewById(R.id.ok);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webViewDialog.dismiss();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final SharedPreferences settings = this.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        editor = settings.edit();
        int theme = sp.getInt("Theme", 0);
        switch (item.getItemId()) {
            case R.id.cng:
                launchContactPicker();
                return true;
            case R.id.help:
                AutoStart(this);
                break;
            case R.id.theme:
                return true;
            case R.id.theme1:
                Toast.makeText(this, "Dark theme", Toast.LENGTH_SHORT).show();
                theme = R.style.AppThemeDark;
                finish();
                startActivity(getIntent());
                break;
            case R.id.theme2:
                Toast.makeText(this, "Light theme", Toast.LENGTH_SHORT).show();
                theme = R.style.AppThemeLight;
                finish();
                startActivity(getIntent());
                break;
            case R.id.signout:

                Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                        new ResultCallback<Status>() {
                            @Override
                            public void onResult(@NonNull Status status) {

                                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                mAuth.signOut();

                                Toast.makeText(getApplicationContext(), "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getBaseContext(), Login.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                startActivity(intent);
                                editor.putBoolean("LoggedIn", false);
                                editor.apply();
                            finish();
                            }
                        });
                break;
        }

        editor.putInt("Theme", theme);
        editor.apply();

        if (item.getItemId() != R.id.cng && item.getItemId() != R.id.help) {
            startActivity(getIntent());
        }
        return true;
    }

    @Override
    public void onStart() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        mGoogleApiClient.connect();
        super.onStart();
    }

    private void prepareMovieData() {
        Adapter list = new Adapter("Why Smartify", "How to use this app");
        movieList.add(list);

        list = new Adapter("Share", "Share this app");
        movieList.add(list);

        list = new Adapter("Rate this app", "Help us improve");
        movieList.add(list);

        list = new Adapter("About", "About the developer");
        movieList.add(list);

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
}