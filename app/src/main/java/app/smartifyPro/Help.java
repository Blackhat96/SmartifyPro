package app.smartifyPro;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class Help extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        final int theme = sp.getInt("Theme", 0);
        setTheme(theme);
        setContentView(R.layout.help);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
    }
}
