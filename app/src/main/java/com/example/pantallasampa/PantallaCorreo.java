package com.example.pantallasampa;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class PantallaCorreo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_correo);
    }

    //Metodo que cancela el correo y vuelve de vuelta a la pantalla de mensajes
    public void cancelarCorreo(View view){
        Intent intent =  new Intent(this, PantallaMensajes.class);
        startActivity(intent);
    }
}