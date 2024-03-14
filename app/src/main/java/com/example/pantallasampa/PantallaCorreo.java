package com.example.pantallasampa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class PantallaCorreo extends AppCompatActivity {

    private EditText destinatarioEditText;
    private EditText asuntoEditText;
    private EditText contenidoEditText;
    private Button enviarButton;

    private DatabaseReference correosRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_correo);

        destinatarioEditText = findViewById(R.id.editTextText2);
        asuntoEditText = findViewById(R.id.editTextText3);
        contenidoEditText = findViewById(R.id.editTextTextMultiLine);
        enviarButton = findViewById(R.id.button);

        // Obtener una referencia a la base de datos de Firebase
        correosRef = FirebaseDatabase.getInstance().getReference().child("correos");

        enviarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarCorreo();
            }
        });

        // Obtén la referencia al TextView
        TextView cancelarTextView = findViewById(R.id.textView16);

        // Agrega un OnClickListener al TextView para cancelar el correo
        cancelarTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelarCorreo();
            }
        });
    }


    private void enviarCorreo() {
        String destinatario = destinatarioEditText.getText().toString();
        String asunto = asuntoEditText.getText().toString();
        String contenido = contenidoEditText.getText().toString();

        // Obtener el correo electrónico del remitente
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String remitente = currentUser.getEmail();

        // Validar que los campos no estén vacíos
        if (TextUtils.isEmpty(destinatario) || TextUtils.isEmpty(asunto) || TextUtils.isEmpty(contenido)) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear un objeto Correo con los datos ingresados
        Correo correo = new Correo(remitente, destinatario, asunto, contenido);

        // Guardar el correo en la base de datos
        correosRef.push().setValue(correo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(PantallaCorreo.this, "Correo enviado correctamente", Toast.LENGTH_SHORT).show();
                        // Limpiar los campos de texto después de enviar el correo
                        destinatarioEditText.setText("");
                        asuntoEditText.setText("");
                        contenidoEditText.setText("");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PantallaCorreo.this, "Error al enviar el correo", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Método para cancelar el correo y volver a la pantalla de mensajes
    private void cancelarCorreo() {
        Toast.makeText(this, "Correo cancelado", Toast.LENGTH_SHORT).show();
        // Vuelve a la pantalla de mensajes
        Intent intent = new Intent(this, PantallaMensajes.class);
        startActivity(intent);
    }
}
