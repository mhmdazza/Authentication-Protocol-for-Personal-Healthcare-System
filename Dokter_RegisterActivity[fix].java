package com.example.healthcaresystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.math.BigInteger;
import java.security.*;

import android.content.Context;
import android.Manifest;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.telephony.TelephonyManager;
import android.content.pm.PackageManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class RegisterActivity extends AppCompatActivity {

    private EditText Name, Password, C_password;
    private TextView NoIMEI;
    private Button btn_regist;
    private Button getIMEI;
    private ProgressBar loading;
    private String IMEI;
    protected Cursor cursor;
    private SQLiteDatabase db;
    private SQLiteHelper database;

    private String Penanda ="nilaidariregister";
    private static final int REQUEST_CODE = 101;
    private static String URL_REGIST = "http://192.168.43.228:1234/register";
    //private static String URL_REGIST = "http://192.168.43.60:1234/register";
    //private static String URL_REGIST = "http://192.168.43.194:1234/register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        loading = findViewById(R.id.loading);
        Name = findViewById(R.id.name);
        Password = findViewById(R.id.password);
        C_password = findViewById(R.id.c_password);
        NoIMEI = findViewById(R.id.noIMEI);
        btn_regist = findViewById(R.id.btn_regist);
        getIMEI = findViewById(R.id.getIMEI);
        database = new SQLiteHelper(this);

        getIMEI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE);
                    return;
                }
                IMEI = telephonyManager.getDeviceId();
                NoIMEI.setText(IMEI);
            }
        });

        btn_regist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Regist();
            }
        });
    }

    private void Regist() {
        loading.setVisibility(View.VISIBLE);
        btn_regist.setVisibility(View.GONE);

        final String name = this.Name.getText().toString().trim();
        final String password = this.Password.getText().toString().trim();
        final String cpassword = this.C_password.getText().toString().trim();
        final String IMEI2 = this.NoIMEI.getText().toString().trim();

        if (!name.isEmpty() || !password.isEmpty() || !cpassword.isEmpty() || !IMEI2.isEmpty()) {
            if (password.equals(cpassword)) {
                final String HPW = hpw(password, IMEI2);

//                final byte[] hpw = password.concat(IMEI2).getBytes();
//                //final byte[] password2 = this.password.getText().toString().getBytes();
//                MessageDigest sha256 = null;
//                try {
//                    sha256 = MessageDigest.getInstance("SHA-256");
//                } catch (NoSuchAlgorithmException e) {
//                    e.printStackTrace();
//                }
//                sha256.update(hpw);
//                byte[] hasil = sha256.digest();
//                StringBuffer hexhasil = new StringBuffer();
//                for (int i = 0; i < hasil.length; i++)
//                    hexhasil.append(Integer.toString((hasil[i] & 0xff) + 0x100, 16).substring(1));
//                final String HPW = hexhasil.toString();
//                Log.d("HPW", HPW);

                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_REGIST,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    String success = jsonObject.getString("sukses");
                                    String nama = jsonObject.getString("nama");
                                    String CID = jsonObject.getString("CID");
                                    Log.d("CID", CID);
                                    String GID = jsonObject.getString("GID");
                                    Log.d("GID", GID);
                                    String A1 = jsonObject.getString("A1");
                                    Log.d("A1", A1);
                                    String A2 = jsonObject.getString("A2");
                                    Log.d("A2", A2);
//                                    String A3 = jsonObject.getString("A3");
//                                    Log.d("A3", A3);

                                    String A3 = a3(nama, password, IMEI2);
                                    database.insert(A1, A2, A3, CID, GID);
                                    if (success.equals("success")) {

                                        Toast.makeText(RegisterActivity.this, "Sukses Register! \nNama : "
                                                + nama + "\ndi Gateway : " + GID, Toast.LENGTH_SHORT)
                                                .show();
                                        Log.d("nilai CID", CID);
                                        Log.d("nilai GID", GID);
                                        loading.setVisibility(View.GONE);
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        intent.putExtra(Penanda,nama);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(RegisterActivity.this, "Gagal Register! \nNama : "
                                                + nama, Toast.LENGTH_SHORT).show();
                                        loading.setVisibility(View.GONE);
                                        btn_regist.setVisibility(View.VISIBLE);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(RegisterActivity.this, "Gagal Registrasi Dokter!", Toast.LENGTH_SHORT).show();
                                    loading.setVisibility(View.GONE);
                                    btn_regist.setVisibility(View.VISIBLE);
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                error.printStackTrace();
                                Toast.makeText(RegisterActivity.this, "Gagal Registrasi Dokter!", Toast.LENGTH_SHORT).show();
                                loading.setVisibility(View.GONE);
                                btn_regist.setVisibility(View.VISIBLE);
                            }
                        }) {
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<>();
                        params.put("name", name);
                        params.put("passwordasli", password);
                        params.put("password",HPW);
                        params.put("IMEI", IMEI2);
                        return params;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(this);
                requestQueue.add(stringRequest);
            }else{
                Toast.makeText(RegisterActivity.this, "Password tidak sesuai!", Toast.LENGTH_SHORT).show();
                loading.setVisibility(View.GONE);
                btn_regist.setVisibility(View.VISIBLE);
            }
        }else{
            Name.setError("Masukkan nama!");
            Password.setError("Masukkan password!");
            C_password.setError("Masukkan password lagi!");
            NoIMEI.setError("Masukkan IMEI!");
            Toast.makeText(RegisterActivity.this, "Harus diisi semuanya!", Toast.LENGTH_SHORT).show();
            loading.setVisibility(View.GONE);
            btn_regist.setVisibility(View.VISIBLE);
        }
    }

    private String a3(String nama, String password, String IMEI2) {
        final byte[] a3hash = nama.concat(password).getBytes();
        //final byte[] password2 = this.password.getText().toString().getBytes();
        MessageDigest sha256 = null;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        sha256.update(a3hash);
        byte[] hasil = sha256.digest();
        StringBuffer hexhasil = new StringBuffer();
        for (int i = 0; i < hasil.length; i++)
            hexhasil.append(Integer.toString((hasil[i] & 0xff) + 0x100, 16).substring(1));
        String IDpass = hexhasil.toString();
//        StringBuilder sb = new StringBuilder();
//        for (char c : IDpass.toCharArray())
//            sb.append((int)c);
//
//        BigInteger IDpass_ASCII = new BigInteger(sb.toString());
//        //ubah HPW_ASCII ke string
//        String IDpass_string = IDpass_ASCII.toString();
//        IDpass_string = IDpass_string.substring(0,15);
//        long IDpass_int2 = Long.parseLong(IDpass_string);
//        //long HPW_int2 = Long.parseLong(HPW_string);
//        Log.d("IDpass", String.valueOf(IDpass_int2));
//        long IMEI = Long.parseLong(IMEI2);
//        long A3_long = IDpass_int2^IMEI;
//        String A3 = String.valueOf(A3_long);
        String A3 = xorString(IDpass,IMEI2);
        Log.d("A3",A3);
        return A3;
    }

    private String hpw(String password, String IMEI2) {
        final byte[] hpw = password.concat(IMEI2).getBytes();
        //final byte[] password2 = this.password.getText().toString().getBytes();
        MessageDigest sha256 = null;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        sha256.update(hpw);
        byte[] hasil = sha256.digest();
        StringBuffer hexhasil = new StringBuffer();
        for (int i = 0; i < hasil.length; i++)
            hexhasil.append(Integer.toString((hasil[i] & 0xff) + 0x100, 16).substring(1));
        final String HPW = hexhasil.toString();
        Log.d("HPW", HPW);
        return HPW;
    }

    private String xorString(String output, String output_2)
    {
        int panjang1 = output.length();
        int panjang2 = output_2.length();
        if (panjang1>panjang2){
            for(int j=0; j<(panjang1-panjang2);j++){
                output_2 = '0'+output_2;
            }
            //System.out.println("output : " + output_2);
        }else if (panjang1<panjang2){
            for(int j=0; j<(panjang2-panjang1);j++){
                output = '0'+output;
            }
        }
        char[] char_output = output.toCharArray();
        char[] char_output_2 = output_2.toCharArray();

        String output4 = "";
        for(int i=0; i<panjang1; i++){
            int b = Character.getNumericValue(char_output[i]);
            //System.out.print(b);
            int b_2 = Character.getNumericValue(char_output_2[i]);
            int b_hasil = b^b_2;
            String output3 = String.valueOf(b_hasil);
            if(output3.equals("10")){
                output3 = "a";
            }else if(output3.equals("11")){
                output3 = "b";
            }else if(output3.equals("12")){
                output3 = "c";
            }else if(output3.equals("13")){
                output3 = "d";
            }else if(output3.equals("14")){
                output3 = "e";
            }else if(output3.equals("15")){
                output3 = "f";
            }
            output4 += output3;
        }
        //System.out.println();
        //System.out.println("output4 : "+output4);
        return output4;
    }
}