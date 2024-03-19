package com.example.pantallasampa;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PantallaPerfil extends AppCompatActivity {

    private ImageView home, email, event, news;
    private boolean rol;
    private Button out1;
    private Button out2;

    //Declaramos la variables sobre las que vamos a trabajar
    TextView nombreUsuario;

    TextView apellidoUsuario;

    TextView correoUsuario;

    TextView telefonoUsuario;

    TextView tVNumeroUsuarios;
    TextView tVNinos;
    TextView tVNinas;
    TextView tVTotal;

    ListView listaHijos;

    ScrollView scrollUsuario;
    ScrollView scrollAdmin;

    //Variable donde guardamos lo enviado a través del intent
    String userEmail;

    //Variable donde guardaremos todos los hijos del usuario
    ArrayList <Hijo> hijos = new ArrayList<>();

    //Key del usuario logeado
    String keyUsuario;

    //LineaChart - Grafico de linea
    LineChart graficoLinea;
    int numeroUsuarios;
    ArrayList<Integer> progresionUsuarios = new ArrayList<>();

    //PieChart - Gráfico de circulo
    PieChart graficoCirculo;

    int contadorH = 0;
    int contadorF = 0;
    int contadorTotal = 0;

    //Barchart - Grafico Barras
    BarChart graficoBarras;

    String titularNoticia;
    int numeroNoticia;
    int numeroClicks;

    //Arraylist donde guardamos los datos del barchart
    ArrayList <BarEntry> entries = new ArrayList<>();
    ArrayList <String> label = new ArrayList<>();


    //Usamos la clave recibida a través deñ intent para acceder a los datos de esa key
    DatabaseReference dr = FirebaseDatabase.getInstance().getReference();//Aqui estamos diciendo que apunte a los usuarios


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_perfil);

        //Variables para la barra de navegación inferior
        home = findViewById(R.id.goHomeProfile);
        email = findViewById(R.id.goEmailProfile);
        event = findViewById(R.id.goEventProfile);
        news = findViewById(R.id.goNewsProfile);
        rol = getIntent().getBooleanExtra("rol", false);
        if(rol)//si roll es true significa es admin
            configurarAccionesBarraInferiorAdmin();
        else//si es false es que es usuario
            configurarAccionesBarraInferiorUsuario();

        //Incializamos boton para cerrar sesión
        out1 = findViewById(R.id.logOut);
        out2 = findViewById(R.id.logOut2);

        //Vicnulamos las variables  los elementos del xml
        nombreUsuario = findViewById(R.id.tVNombre);
        apellidoUsuario = findViewById(R.id.tVApellidos);
        correoUsuario = findViewById(R.id.tVCorreo);
        telefonoUsuario = findViewById(R.id.tVTelefono);
        listaHijos = findViewById(R.id.listViewHijos);
        scrollUsuario = findViewById(R.id.scrollUsuario);
        scrollAdmin = findViewById(R.id.scrollAdmin);
        graficoLinea = findViewById(R.id.graficoLineal);
        tVNumeroUsuarios = findViewById(R.id.tVNumeroUsuarios);
        graficoCirculo = findViewById(R.id.graficoCirculo);
        tVNinos = findViewById(R.id.tVNinos);
        tVNinas = findViewById(R.id.tVNinas);
        tVTotal = findViewById(R.id.tvTotal);
        graficoBarras = findViewById(R.id.graficoBarras);

        //Recibimos los datos del intent
        userEmail = getIntent().getStringExtra("emailUser");
        keyUsuario = getIntent().getStringExtra("keyUsuario");

        //comprobamos el rol de usuario
        comprobarRol();

        //Llamada a los metodos de los graficos
        graficoLineal();
        graficoCirculo();
        graficoBarras();

        //Obtenemos la key del usuario logeado
        keyUser();

        //Le damos un listener a los botones que nos permieten hacer Log out
        FirebaseAuth fa = FirebaseAuth.getInstance();
        out1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fa.signOut();
                Intent intent = new Intent(PantallaPerfil.this, PantallaInicial.class);
                startActivity(intent);
            }
        });
        out2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fa.signOut();
                Intent intent = new Intent(PantallaPerfil.this, PantallaInicial.class);
                startActivity(intent);
            }
        });

        ImageView imageViewCloseSession = findViewById(R.id.imageViewCloseSession);
        imageViewCloseSession.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Iniciar la nueva actividad aquí
                Intent intent = new Intent(PantallaPerfil.this, PaginaSorteo.class);
                startActivity(intent);
            }
        });


        //ordenamos por email de usuarios y comparamos si hay igualdad para extraer sus datos
        dr.child("usuarios").orderByChild("email").equalTo(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                // Iteramos sobre los resultados
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    //Variables de lo extraido del database
                    String nombre = childSnapshot.child("nombre").getValue(String.class);
                    String apellidos = childSnapshot.child("apellidos").getValue(String.class);
                    String email = childSnapshot.child("email").getValue(String.class);
                    String telefono = childSnapshot.child("telf").getValue(String.class);

                    nombreUsuario.setText(nombre);
                    apellidoUsuario.setText(apellidos);
                    correoUsuario.setText(email);
                    telefonoUsuario.setText(telefono);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }



    //Metodo por el que sacamos la key del usuario que esta logeado
    public void keyUser(){
        Log.d("pantallaPerfil", "entra en keyuser");
        dr.child("usuarios").addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Log.d("pantallaPerfil", "antes del for");
                //Recorremos todos los usuarios que teiene la base de datos
                for (DataSnapshot usuarioSnapshot : snapshot.getChildren()){
                    Log.d("pantallaPerfil", "entra en el for");

                    //Extraemos las key de los usuarios
                    String usuarioKey = usuarioSnapshot.getKey();
                    Log.d("pantallaPerfil", usuarioKey);

                    //Ahora debemos verificar que coincide el email logeado con el del usuario, asi podremos coger la key del usuarios que tenemos logueado
                    String emailUsuario = usuarioSnapshot.child("email").getValue(String.class);

                    if (emailUsuario.equals(userEmail))  {

                        keyUsuario = usuarioKey;

                        //Apuntamos referencia a los hijos del usuario logeado a través de la key
                        DatabaseReference hijosRef = FirebaseDatabase.getInstance().getReference().child("usuarios").child(keyUsuario).child("hijos");

                        Log.d("pantallaPerfil", "apunta a la referencia");

                        hijosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                //Iteramos sobre los hijos
                                for (DataSnapshot hijoSnapshot : snapshot.getChildren()){

                                    //Obtenemos los datos del hijo
                                    String nombre = hijoSnapshot.child("nombre").getValue(String.class);
                                    String apellidos = hijoSnapshot.child("apellidos").getValue(String.class);
                                    String curso = hijoSnapshot.child("curso").getValue(String.class);
                                    String edad = hijoSnapshot.child("edad").getValue(String.class);
                                    String sexo = hijoSnapshot.child("sexo").getValue(String.class);

                                    //Creamos el objeto del hijo con los datos obtenidos y lo añadimos al arraylist
                                    Hijo hijo = new Hijo(nombre, apellidos, edad, curso,sexo);
                                    hijos.add(hijo);

                                    //Le pasamos los datos al adpter
                                    CrearHijoAdapter adapater = new CrearHijoAdapter(PantallaPerfil.this,hijos);

                                    listaHijos.setAdapter(adapater);

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });



                    }



                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {


            }
        });
    }

    //Metodo LineChart
    public void graficoLineal(){


        //Creamos un Map donde guardaremos los claves vlaor que queremos guardar
        Map<String,Object> dataMap = new HashMap<>();



        //Obtenemos el numero total de usuarios
        dr.child("usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                numeroUsuarios = (int) snapshot.getChildrenCount();
                Log.d("numUser", String.valueOf(numeroUsuarios));
                tVNumeroUsuarios.setText(String.valueOf(numeroUsuarios));

                //Sacamos el arraylist de la base de datos lo actualizamos con el nuevo numero de usuarios y lo volvemos a guardar con el map
                dr.child("stats").child("line").child("progresionUsuarios").addListenerForSingleValueEvent(new ValueEventListener() {

                    //Al prinicio al ver solo un elemento que es 0 nos devolverá ese mismo elemento y añadiremos ese al arraylist donde iremos añadiendo los nuevos numeros de
                    //usuarios para asi posteriormente poder guardarlo en el base de datos. La proxima vez que se haga recuenta además de encontrarse el 0 se encontrará ek numero añadido en el anterior recuento
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot listaUser : snapshot.getChildren()){
                            //Extaremos los valores dentro del nodo
                            Integer valor = listaUser.getValue(Integer.class);
                            //Los guardamos dentro del arraylist
                            progresionUsuarios.add(valor);
                        }

                        //Añadimos el nuevo numero
                        progresionUsuarios.add(numeroUsuarios);

                        //Añadimos al map los datos actualizados
                        dataMap.put("numeroUsuarios",numeroUsuarios);
                        dataMap.put("progresionUsuarios",progresionUsuarios);

                        //Guardamos el map en la ruta referenciada
                        dr.child("stats").child("line").setValue(dataMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {

                                Log.d("stats", "Insertado exitosamente");

                            }
                        });

                        // Configurar el eje X
                        XAxis xAxis = graficoLinea.getXAxis();
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

                        // Configurar el eje Y
                        YAxis yAxisRight = graficoLinea.getAxisRight();
                        yAxisRight.setEnabled(false);

                        // Agregar datos al LineChart
                        ArrayList<Entry> entries = new ArrayList<>();
                        // Obtener datos de progresionUsuarios y agregarlos a entries
                        for (int i = 0; i < progresionUsuarios.size(); i++) {
                            entries.add(new Entry(i, progresionUsuarios.get(i))); // Suponiendo que i representa el índice del usuario y el valor en la lista es su progreso
                        }


                        LineDataSet dataSet = new LineDataSet(entries, "Nº de usuarios"); // Agregar etiqueta opcional
                        dataSet.setColor(Color.parseColor("#003E77"));//Color de la linea
                        dataSet.setLineWidth(2f);
                        LineData lineData = new LineData(dataSet);
                        graficoLinea.setData(lineData);
                        graficoLinea.invalidate(); // Actualizar el gráfico

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    //Método piechart
    public void graficoCirculo(){

        //Creamos un Map donde guardaremos los claves vlaor que queremos guardar
        Map<String,Object> dataMap = new HashMap<>();

        dr.child("usuarios").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for(DataSnapshot snapshotUser : snapshot.getChildren()){

                   //Acceder a los hijos de cada usuario
                    DataSnapshot snapshotHijo = snapshotUser.child("hijos");

                    for(DataSnapshot hijoSnapshot : snapshotHijo.getChildren()){

                        //Obtenemos el sexo de cada hijo
                        String sexoHijo = hijoSnapshot.child("sexo").getValue(String.class);

                        Log.d("SEXO", sexoHijo);


                        if (sexoHijo.equals("Masculino")){

                            contadorH = contadorH + 1;
                            contadorTotal = contadorTotal + 1;

                        }else if (sexoHijo.equals("Femenino")){

                            contadorF = contadorF + 1;
                            contadorTotal = contadorTotal + 1;
                        }
                    }
                }

                //Mostramos los valores obtenidos en la interfaz
                tVNinos.setText(String.valueOf(contadorH));
                tVNinas.setText(String.valueOf(contadorF));
                tVTotal.setText(String.valueOf(contadorTotal));

                //Una vez que tenemos el numero de masculino y femenino de los hijos guardaremos los datos al igual que el linechart en la base de datos para mantenerlos persistentes
                dataMap.put("contadorTotal", contadorTotal);
                dataMap.put("contadorMasculino", contadorH);
                dataMap.put("contadorFemenino", contadorF);

                //referenciamos al nodo donde queremos meter el Map
                dr.child("stats").child("pie").setValue(dataMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        Log.d("stats", "pie insertado exitosamente");
                    }
                });

                //Añadimos al arraylist los datos que hemos obtenido del recuentio
                ArrayList<PieEntry> contadorSexo = new ArrayList<>();
                contadorSexo.add(new PieEntry(contadorH,"Niños"));
                contadorSexo.add(new PieEntry(contadorF,"Niñas"));

                // Crear un conjunto de datos para el gráfico circular
                PieDataSet dataSet = new PieDataSet(contadorSexo,"");

                // Configurar colores para cada segmento del gráfico
                dataSet.setColors(Color.parseColor("#003E77"),Color.parseColor("#F36EF5")); // Por ejemplo, azul para hombres, magenta para mujeres y verde para total

                // Crear una instancia de PieData que contiene el conjunto de datos
                PieData data = new PieData(dataSet);

                // Obtener la referencia al gráfico circular
                PieChart pieChart = findViewById(R.id.graficoCirculo); // Asegúrate de que el ID sea el correcto

                // Configurar el gráfico con los datos
                pieChart.setData(data);

                // Actualizar el gráfico
                pieChart.invalidate();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    //Metodo contador de clicks / grafica de barras
    public void graficoBarras(){


        dr.child("noticias").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                //Recorremos todas las noticias registradas y extraemos su titular y su numero de clicks
                for(DataSnapshot noticiaSnapshot : snapshot.getChildren()){

                    titularNoticia = noticiaSnapshot.child("titular").getValue(String.class);
                    numeroClicks = noticiaSnapshot.child("clicks").getValue(Integer.class);

                    //Guardamos los datos en el nodo de stats
                    dr.child("stats").child("barchart").child(titularNoticia).child("numeroClicks").setValue(numeroClicks).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Log.d ("stats", "Insertado");
                        }
                    });

                    //Guradamos en arraylist los titulos y clicks para poder meterlos posteriormetne en el grafico
                    entries.add(new BarEntry(entries.size(),Math.max(numeroClicks,0)));

                    label.add(titularNoticia);

                }


                //Creamos conjunto de datos para crear grafico
                BarDataSet dataSet= new BarDataSet(entries,"");
                dataSet.setColor(Color.parseColor("#003E77"));

                // Crear un objeto BarData y asignar el conjunto de datos a él
                BarData barData = new BarData(dataSet);

                // Configurar el BarChart con los datos y otras opciones
                graficoBarras.setData(barData);
                graficoBarras.setFitBars(true); // Ajustar el ancho de las barras para que se ajusten al contenedor

                // Configurar las etiquetas en el eje X
                XAxis xAxis = graficoBarras.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(label)); // Establecer las etiquetas personalizadas
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM); // Colocar el eje X en la parte inferior
                xAxis.setGranularity(1f); // Espaciar las etiquetas por unidad
                xAxis.setTextColor(Color.WHITE);

                // Configurar el eje y del gráfico
                YAxis yAxis = graficoBarras.getAxisLeft(); // Obtener el eje y izquierdo
                yAxis.setAxisMinimum(0f); // Establecer el mínimo en 0
                yAxis.setAxisMaximum(100f); // Establecer el máximo en 100
                yAxis.setTextColor(Color.WHITE);

                graficoBarras.invalidate(); // Actualizar el gráfico


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }

    //Metodo comprobar rol para poder publicar noticia
    //Método para comprobar el rol del usuario que se introduce
    public void comprobarRol(){


        //Apuntamos el dr a usuarios
        dr.child("usuarios").child(keyUsuario).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String rol = snapshot.child("roll").getValue(String.class);

                if(rol.equals("usuario")){
                    scrollAdmin.setVisibility(View.GONE);
                    scrollUsuario.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    //Metodo para navegar a crear hijo
    public void navCrearHijo(View view){

        Intent intent = new Intent(this, PantallaCrearHijo.class);
        intent.putExtra("emailUser",userEmail);//Enviamos el email con el que estamos trabajando a la pantalla de crear Hijo
        intent.putExtra("keyUsuario", keyUsuario);
        startActivity(intent);

    }


    //Metodo para navegar a pagina correos
    public void navCorreos (View view){

        Intent intent = new Intent(this, PantallaMensajes.class);
        intent.putExtra("keyUsuario", keyUsuario);
        intent.putExtra("emailUser",userEmail);
        startActivity(intent);

    }

    //Metodo navegar navCarnetSocio
    public void navCarnetSocio (View view){

        Intent intent = new Intent(this, PantallaCarnetSocio.class);
        intent.putExtra("keyUsuario", keyUsuario);
        intent.putExtra("emailUser",userEmail);
        startActivity(intent);
    }

    private void configurarAccionesBarraInferiorAdmin() {
        email.setOnClickListener(v -> navigateTo(PantallaMensajesAdmin.class));
        event.setOnClickListener(v -> navigateTo(PantallaCrearEventos.class));
        news.setOnClickListener(v -> navigateTo(PantallaNoticias.class));
        home.setOnClickListener(v -> navigateTo(PantallaEventos.class));
    }
    private void configurarAccionesBarraInferiorUsuario() {
        email.setOnClickListener(v -> navigateTo(PantallaMensajes.class));
        event.setOnClickListener(v -> navigateTo(PantallaCatalogo.class));
        news.setOnClickListener(v -> navigateTo(PantallaNoticias.class));
        home.setOnClickListener(v -> navigateTo(PantallaEventos.class));
    }

    //Método complementario de configurarAccionesBarraInferior() el cual realizar el Intent y envía el emailUser [Sujeto a cambios]
    private void navigateTo(Class<?> destination) {
        Intent intent = new Intent(PantallaPerfil.this, destination);
        intent.putExtra("emailUser", userEmail);
        intent.putExtra("keyUsuario", keyUsuario);
        intent.putExtra("rol", rol);
        startActivity(intent);
    }

}