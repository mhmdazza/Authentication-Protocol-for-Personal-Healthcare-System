package com.example.healthcaresystem;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.healthcaresystem.Adapter.DenyutAdapter;
import com.example.healthcaresystem.Model.Denyut;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DenyutActivity extends AppCompatActivity {

    ListView listView;
    TextView coba;
    List<Denyut> denyutList;
    private String tandadatabase = "KepemilikanDatabase";
    private String namadatabase = "";

    static {
        System.loadLibrary("native-lib");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_denyut);
        listView = (ListView) findViewById(R.id.list_denyut);
        //coba = (TextView) findViewById(R.id.coba);
        //coba.setText(stringFromJNI());
        denyutList = new ArrayList<>();
        namadatabase = getIntent().getStringExtra(tandadatabase);
        Log.d("Nama Rekam Medis","Nama Pasien : "+namadatabase);
        showList();
    }

    //public native String stringFromJNI();

    private void showList() {
        StringRequest stringRequest = new StringRequest
                                        //(Request.Method.GET, "http://192.168.43.228:1234/denyut",
                                                (Request.Method.GET, "http://192.168.43.228:1234/denyut",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONArray array = obj.getJSONArray("semuaDenyut");
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject denyutObj = array.getJSONObject(i);
                                Denyut d = new Denyut(denyutObj.getString("denyut"), denyutObj.getString("waktu"));
                                String denyut = denyutObj.getString("denyut").trim();
                                String waktu = denyutObj.getString("waktu").trim();
                                Log.d("denyut", denyut);
                                Log.d("waktu", waktu);
                                denyutList.add(d);
                            }
                            DenyutAdapter adapter = new DenyutAdapter(denyutList, getApplicationContext());
                            listView.setAdapter(adapter);
                            Log.d("cek", String.valueOf(adapter));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        }) {

        };
        Handler.getInstance(getApplicationContext()).addToRequestQueue(stringRequest);
    }
}
