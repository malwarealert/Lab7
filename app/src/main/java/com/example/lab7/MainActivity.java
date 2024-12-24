package com.example.lab7;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.PixelCopy;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private TextView textInfo;
    private TextView textTemp;
    private ImageView imageView;
    protected OkHttpClient client = new OkHttpClient();
    String[] citys = { "Тагил", "Токио",  "Пекин"};
    String[] lat = { "57.913", "35.689", "39.901"};
    String[] lon = { "60.559", "139.691", "116.391"};
    String appid = "6248b3e3f9aaf2743fe49fdd284cf437";
    String lang = "ru";
    String units= "metric";
    String url = "";
    @Override
    //синхронный поток
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, citys);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); //визуализация списка
        spinner.setAdapter(adapter); //применяем адаптер к элементу spinner
        //взаимодействие со списком
        AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //String item = (String)parent.getItemAtPosition(position); //получаем выбранный объект
                url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat[position] + "&lon=" + lon[position] + "&appid=" + appid + "&lang=" + lang + "&units=" + units;
                OkHTTPHandler handler = new OkHTTPHandler();
                handler.execute();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        };
        spinner.setOnItemSelectedListener(itemSelectedListener);
        textInfo=(TextView) findViewById(R.id.text_fact); //описание
        textTemp=(TextView) findViewById(R.id.text_temp); //температура
        imageView=(ImageView) findViewById(R.id.imageView); //картинка
    }
    //ассинхронный поток
    public class OkHTTPHandler extends AsyncTask<Void,Void,ArrayList>{ //что подаём на вход, что в середине, что возвращаем
        @Override
        protected ArrayList doInBackground(Void ... voids) { //действия в побочном потоке
            Request.Builder builder = new Request.Builder(); //построитель запроса
            Request request = builder.url(url)
                    .get() //тип запроса
                    .build();
            try {
                Response response = client.newCall(request).execute();
                JSONObject object = new JSONObject(response.body().string()); //распарсили JSON
                String info = object.getJSONArray("weather").getJSONObject(0).getString("description"); //описание
                String temp = object.getJSONObject("main").getString("temp") + " C"; //температура
                String img = object.getJSONArray("weather").getJSONObject(0).getString("icon"); //картинка
                URL img_url = new URL("https://openweathermap.org/img/wn/" + img + "@4x.png");
                InputStream inputStream = img_url.openStream();
                Bitmap image = BitmapFactory.decodeStream(inputStream);
                ArrayList<Object> res = new ArrayList<>();
                res.add(info);
                res.add(temp);
                res.add(image);
                return res;
            } catch (IOException | JSONException e ) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(ArrayList o) { //действия после выполнения задач в фоне
            super.onPostExecute(o);
            textInfo.setText(o.get(0).toString()); //описание
            textTemp.setText(o.get(1).toString()); //температура
            imageView.setImageBitmap((Bitmap) o.get(2)); //картинка
        }
    }
}