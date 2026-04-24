package com.example.project666;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class Initialize extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
