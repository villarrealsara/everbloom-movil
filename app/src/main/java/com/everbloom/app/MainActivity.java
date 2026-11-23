package com.everbloom.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.everbloom.app.datos.SesionManager;
import com.everbloom.app.ui.CatalogoActivity;
import com.everbloom.app.ui.LoginActivity;

/** Pantalla de entrada: si ya hay sesión va al catálogo, si no, al inicio de sesión. */
public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean haySesion = new SesionManager(this).haySesion();
        startActivity(new Intent(this, haySesion ? CatalogoActivity.class : LoginActivity.class));
        finish();
    }
}
