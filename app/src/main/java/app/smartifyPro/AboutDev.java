package app.smartifyPro;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class AboutDev extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences sp = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        final int theme = sp.getInt("Theme", 0);
        setTheme(theme);
        setContentView(R.layout.about_dev);
        About_Builder.with(this,theme).init().loadAbout();
    }
}
