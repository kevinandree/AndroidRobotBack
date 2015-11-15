package com.angel.robotblack;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Locale;

public class Principal extends AppCompatActivity implements TextToSpeech.OnInitListener {
    /*********BD********/

    /*******************/
    private EditText _txtPregunta, _txtRespuesta;
    private TextToSpeech textToSpeech;

    public void Inicializar(){

        //Boton Registrar
        final Button _btnGuardar = (Button) findViewById(R.id.btnGuardar);
        _btnGuardar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                registrarMensaje();
            }
        });

        //Boton Resolver
        final Button _btnResolver = (Button) findViewById(R.id.btnResolver);
        _btnResolver.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                consultarRespuesta();
            }
        });

        //Boton Limpiar
        final Button _btnCancelar = (Button) findViewById(R.id.btnCancelar);
        _btnCancelar.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Limpiar();
            }
        });
        //
        _txtPregunta = (EditText)findViewById(R.id.txtPregunta);
        _txtRespuesta = (EditText)findViewById(R.id.txtRespuesta);
        //
        textToSpeech = new TextToSpeech( this, this );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        Inicializar();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInit( int status )
    {
        if ( status == TextToSpeech.LANG_MISSING_DATA | status == TextToSpeech.LANG_NOT_SUPPORTED )
        {
            Toast.makeText( this, "ERROR LANG_MISSING_DATA | LANG_NOT_SUPPORTED", Toast.LENGTH_SHORT ).show();
        }
    }

    private void speak( String str )
    {
        //if (Build.VERSION.RELEASE.startsWith("5")) {
            //textToSpeech.speak(str, TextToSpeech.QUEUE_FLUSH, null, null);
        //}
        //else {
            textToSpeech.speak(str, TextToSpeech.QUEUE_FLUSH, null);
        //}
        textToSpeech.setSpeechRate(0.0f);
        textToSpeech.setPitch(0.0f);
    }

    @Override
    protected void onDestroy()
    {
        if ( textToSpeech != null )
        {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    //METODOS PROPIOS

    public void registrarMensaje(){
        String pregunta = _txtPregunta.getText().toString();
        String respuesta = _txtRespuesta.getText().toString();

        if(ValidarRegistro(pregunta, respuesta))
        {
            String mensajeSalida;

            AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this, "administracion", null, 1);
            SQLiteDatabase bd = admin.getWritableDatabase();
            Cursor fila = bd.rawQuery(
                    "select codPregunta from Pregunta where pregunta='" + pregunta + "'", null);
            ContentValues registro = new ContentValues();
            if (fila.moveToFirst()) {
                String  codPregunta= fila.getString(0);
                //Actualizar
                registro.put("respuesta", respuesta);
                bd.update("Pregunta", registro,"codPregunta="+codPregunta, null);
                bd.close();
                _txtPregunta.setText("");
                _txtRespuesta.setText("");
                mensajeSalida = "Se actualizó correctamente la pregunta: ";
            }
            else
            {
                //Registrar
                registro.put("pregunta", pregunta);
                registro.put("respuesta", respuesta);
                bd.insert("Pregunta", null, registro);
                bd.close();
                _txtPregunta.setText("");
                _txtRespuesta.setText("");
                mensajeSalida = "Se registró correctamente la pregunta: ";
            }

            Toast toast = Toast.makeText(getApplicationContext(), mensajeSalida + pregunta, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM | Gravity.RIGHT, 0, 0);
            toast.show();
        }
    }

    public void consultarRespuesta() {
        String pregunta = _txtPregunta.getText().toString();

        AdminSQLiteOpenHelper admin = new AdminSQLiteOpenHelper(this,"administracion", null, 1);
        SQLiteDatabase bd = admin.getWritableDatabase();
        Cursor fila = bd.rawQuery(
                "select respuesta from Pregunta where pregunta='" + pregunta + "'", null);
        if (fila.moveToFirst()) {
            _txtRespuesta.setText(fila.getString(0));
        } else
            Toast.makeText(this, "No existe una respuesta con dicha pregunta", Toast.LENGTH_SHORT).show();
        bd.close();

        textToSpeech.setLanguage(new Locale("spa", "ESP"));
        speak(fila.getString(0));
    }

    public void Limpiar(){
        _txtPregunta.setText("");
        _txtRespuesta.setText("");
    }

    public boolean ValidarRegistro(String _pregunta, String _respuesta){
        if(_pregunta.length()>0 && _respuesta.length()>0){
            return true;
        }
        else {
            Toast.makeText(this, "Debe ingresar una pregunta y una respuesta", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

}
