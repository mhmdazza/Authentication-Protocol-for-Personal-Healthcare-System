package com.example.healthcaresystem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.math.BigInteger;
import java.security.*;
import java.util.Random;

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
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GantiPassword extends AppCompatActivity {
    private String mA1, mA2, mA3, mCID, mGID;
    private String B1,B2,B3,B15,mB16,mCIDbaru;
    private String mPassword,mIMEI;
    private EditText name, password, passwordbaru;
    private TextInputLayout layoutpasswordbaru;
    private TextView noIMEI;
    private Button btn_login, btn_passwordbaru;
    private Button getIMEI;
    private String IMEI;
    private int N1;
    private String Penanda ="nilaidariregister";
    //private String CID="";
    private static final int REQUEST_CODE = 101;
    //private static String URL_GANTIPASSWORD = "http://192.168.41.21:1234/login";
    //private static String URL_GANTIPASSWORD = "http://192.168.43.228:1234/login";
    //private static String URL_GANTIPASSWORD = "http://192.168.43.228:1234/ganti_password";
    private static String URL_GANTIPASSWORD = "http://192.168.43.228:1234/ganti_password";

    private ProgressBar loading;
    private SQLiteDatabase db;
    private SQLiteHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ganti_password);

        loading = findViewById(R.id.loading);
        name = findViewById(R.id.name);
        password = findViewById(R.id.password);
        noIMEI = findViewById(R.id.noIMEI);
        btn_login = findViewById(R.id.btn_login);
        passwordbaru = findViewById(R.id.passwordbaru);
        btn_passwordbaru = findViewById(R.id.btn_passwordbaru);
        layoutpasswordbaru = findViewById(R.id.layoutpasswordbaru);
        getIMEI = findViewById(R.id.getIMEI);
        database = new SQLiteHelper(this);
        db = database.getReadableDatabase();

        //CID = getIntent().getStringExtra(Penanda);
        //Log.d("Login","NILAI CID = "+CID);

        getIMEI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(GantiPassword.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(GantiPassword.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE);
                    return;
                }
                IMEI = telephonyManager.getDeviceId();
                noIMEI.setText(IMEI);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String mName = name.getText().toString().trim();
                mPassword = password.getText().toString().trim();
                final String mIMEI = noIMEI.getText().toString().trim();
                final byte[] hpw = mPassword.concat(mIMEI).getBytes();

                if (!mName.isEmpty() || !mPassword.isEmpty() || !mIMEI.isEmpty()) {
                    String query = "select * from android_tabel";
                    Cursor cursor = db.rawQuery(query, null);
                    while (cursor.moveToNext()) {
                        mA1 = cursor.getString(1);
                        mA2 = cursor.getString(2);
                        mA3 = cursor.getString(3);
                        mCID = cursor.getString(4);
                        mGID = cursor.getString(5);
                    }
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
                    final String mHPW = hexhasil.toString();
                    Log.d("HPW login", mHPW);

                    B15(mA1, mCID, mGID, mName, mHPW);

                    if (!mName.isEmpty() || !mPassword.isEmpty() || !mIMEI.isEmpty()) {
                        gantiPassword(mName, mHPW, mPassword, mIMEI, mCID, mGID);
                    } else {
                        name.setError("Masukkan nama!");
                        password.setError("Masukkan password");
                        noIMEI.setError("Masukkan IMEI");
                    }
                }else{
                    name.setError("Masukkan nama!");
                    password.setError("Masukkan password!");
                    noIMEI.setError("Masukkan IMEI!");
                    Toast.makeText(GantiPassword.this, "Harus diisi semuanya!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_passwordbaru.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String mpasswordbaru = passwordbaru.getText().toString().trim();
                if (mpasswordbaru.equals(mPassword)) {
                    Toast.makeText(GantiPassword.this, "Password harus beda!", Toast.LENGTH_SHORT).show();
                }else{
                    Log.d("pass lama", mPassword);
                    final String mIMEI = noIMEI.getText().toString().trim();
                    Log.d("IMEI", mIMEI);
                    byte[] hpw = mpasswordbaru.concat(IMEI).getBytes();
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
                    String HPWbaru = hexhasil.toString();
                    Log.d("HPW baru", HPWbaru);

                    //////////////// HITUNG A1 BARU //////////////////////

                    String N1_string = String.valueOf(N1);
                    String input1 = N1_string + mCID;
                    byte[] input11 = input1.getBytes();
                    MessageDigest sha256_1 = null;
                    try {
                        sha256_1 = MessageDigest.getInstance("SHA-256");
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    sha256_1.update(input11);
                    byte[] hasil1 = sha256_1.digest();
                    StringBuffer hexhasil1 = new StringBuffer();
                    for (int i = 0; i < hasil1.length; i++)
                        hexhasil1.append(Integer.toString((hasil1[i] & 0xff) + 0x100, 16).substring(1));
                    String out1 = hexhasil1.toString();
                    //ubah h(A2+h(ID||HPW)) ke ASCII
                    StringBuilder sb1 = new StringBuilder();
                    for (char c : out1.toCharArray())
                        sb1.append((int) c);

                    BigInteger out1_ASCII = new BigInteger(sb1.toString());
                    //ubah out1 ke string
                    String out1_string = out1_ASCII.toString();
                    out1_string = out1_string.substring(0, 15);
                    long out1_int = Long.parseLong(out1_string);
                    out1_string = String.valueOf(out1_int);
                    Log.d("h(Ru || CID)", out1_string);

                    //ubah HPW ke ASCII
                    StringBuilder sb2 = new StringBuilder();
                    for (char c : HPWbaru.toCharArray())
                        sb2.append((int) c);

                    BigInteger out2_ASCII = new BigInteger(sb2.toString());
                    //ubah out2 ke string
                    String out2_string = out2_ASCII.toString();
                    out2_string = out2_string.substring(0, 15);
                    long out2_int = Long.parseLong(out2_string);
                    String HPWbaru_string = String.valueOf(out2_int);
                    Log.d("HPW baru", HPWbaru_string);

                    long B16_long = Long.parseLong(mB16);
                    long A1baru_long = B16_long ^ out1_int ^ out2_int;
                    String A1baru = String.valueOf(A1baru_long);
                    Log.d("A1baru", A1baru);

                    ////////////// HITUNG A2 baru ////////////////
                    byte[] hpwlama = mPassword.concat(mIMEI).getBytes();
                    MessageDigest sha2563 = null;
                    try {
                        sha2563 = MessageDigest.getInstance("SHA-256");
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    sha2563.update(hpwlama);
                    byte[] hasil3 = sha2563.digest();
                    StringBuffer hexhasil3 = new StringBuffer();
                    for (int i = 0; i < hasil3.length; i++)
                        hexhasil3.append(Integer.toString((hasil3[i] & 0xff) + 0x100, 16).substring(1));
                    String HPWlama = hexhasil3.toString();

                    StringBuilder sb3 = new StringBuilder();
                    for (char c : HPWlama.toCharArray())
                        sb3.append((int) c);

                    BigInteger out3_ASCII = new BigInteger(sb3.toString());
                    String out3_string = out3_ASCII.toString();
                    HPWlama = out3_string.substring(0, 15);
                    long HPWlama_int = Long.parseLong(HPWlama);
                    long HPWbaru_int = Long.parseLong(HPWbaru_string);
                    long A2_int = Long.parseLong(mA2);
                    long A2_baru_int = A2_int ^ HPWlama_int ^ HPWbaru_int;
                    String A2baru = String.valueOf(A2_baru_int);
                    Log.d("A2baru", A2baru);

                    ////////////// HITUNG A3 baru ////////////////
                    long IMEI_int = Long.parseLong(mIMEI);
                    long A3_int = HPWbaru_int ^ IMEI_int;
                    String A3baru = String.valueOf(A3_int);
                    Log.d("A3baru", A3baru);

                    database.update2(A1baru, A2baru, A3baru, mCIDbaru);
                    Toast.makeText(GantiPassword.this, "Sukses Ganti Password!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(GantiPassword.this, LoginActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private void B15(String A1, String CID, String GID, String ID, String HPW){
        //N1 = 123;
        // create instance of Random class
        Random rand = new Random();
        // Generate random integers in range 0 to 999
        N1 = rand.nextInt(1000000);
        // Print random integers
        Log.d("Random Integers: ", String.valueOf(N1));

        //buat ASCII dari HPW
        StringBuilder sb = new StringBuilder();
        for (char c : HPW.toCharArray())
            sb.append((int)c);

        BigInteger HPW_ASCII = new BigInteger(sb.toString());
        Log.d("HPW_ASCII", String.valueOf(HPW_ASCII));
        //ubah HPW_ASCII ke string
        String HPW_string = HPW_ASCII.toString();
        Log.d("HPW_string",(HPW_string));
        HPW_string = HPW_string.substring(0,15);
        long HPW_int2 = Long.parseLong(HPW_string);
        //long HPW_int2 = Long.parseLong(HPW_string);
        Log.d("HPW_dipotong", String.valueOf(HPW_int2));

        long A1_integer = Long.parseLong(A1);
        long B1_int = A1_integer^HPW_int2;
        //dapat String B1
        B1 = String.valueOf(B1_int);
        Log.d("B1",B1);
        long B2_int = B1_int^N1;
        //dapat string B2
        B2 = String.valueOf(B2_int);
        Log.d("B2",B2);

        String n1 = String.valueOf(N1);
        //dapat input hash B3
        String b3_string = n1+B1;
        byte[] b3 = b3_string.getBytes();

        MessageDigest sha256 = null;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        sha256.update(b3);
        byte[] hasil = sha256.digest();
        StringBuffer hexhasil = new StringBuffer();
        for (int i=0; i<hasil.length; i++)
            hexhasil.append(Integer.toString((hasil[i]&0xff)+0x100,16).substring(1));
        final String b3_hash = hexhasil.toString();

        //ubah b3_hash ke ASCII
        StringBuilder sb_2 = new StringBuilder();
        for (char c : b3_hash.toCharArray())
            sb_2.append((int)c);

        BigInteger b3_ASCII = new BigInteger(sb_2.toString());

        String b3_string2 = b3_ASCII.toString();
        b3_string2 = b3_string2.substring(0,15);
        long b3_int2 = Long.parseLong(b3_string2);

        //ubah ID ke ASCII
        StringBuilder sb_3 = new StringBuilder();
        for (char c : ID.toCharArray())
            sb_3.append((int)c);

        BigInteger ID_ASCII = new BigInteger(sb_3.toString());
        String ID_string = ID_ASCII.toString();
        long ID_int = Long.parseLong(ID_string);
        long B3_int = ID_int^b3_int2;
        //dapat String B3
        B3 = String.valueOf(B3_int);
        Log.d("B3", B3);
        Log.d("ID ASCII",ID_string);

        String b15_string = CID+GID+B1+ID+n1;
        byte[] b15_inputhash = b15_string.getBytes();

        MessageDigest sha256_2 = null;
        try {
            sha256_2 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        sha256_2.update(b15_inputhash);
        byte[] hasil_2 = sha256_2.digest();
        StringBuffer hexhasil_2 = new StringBuffer();
        for (int i=0; i<hasil_2.length; i++)
            hexhasil_2.append(Integer.toString((hasil_2[i]&0xff)+0x100,16).substring(1));
        //dapat nilai B15
        B15 = hexhasil_2.toString();
        Log.d("B15", B15);
    }

    private void gantiPassword(final String name, final String HPW, final String password, final String IMEI, final String CID, final String GID) {
        loading.setVisibility(View.GONE);
        btn_passwordbaru.setVisibility(View.GONE);
        layoutpasswordbaru.setVisibility(View.GONE);
        passwordbaru.setVisibility(View.GONE);

        btn_login.setVisibility(View.VISIBLE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_GANTIPASSWORD,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("sukses");
                            String name = jsonObject.getString("name");
                            mB16 = jsonObject.getString("B16");
                            String mB17 = jsonObject.getString("B17");
                            String mB18 = jsonObject.getString("B18");
                            mCIDbaru = jsonObject.getString("CIDbaru");
                            Log.d("B16", mB16);
                            Log.d("B17", mB17);
                            Log.d("B18", mB18);
//                            Log.d("CIDbaru", mCIDbaru);
//
//                            StringBuilder sb = new StringBuilder();
//                            for (char c : mCIDbaru.toCharArray())
//                                sb.append((int)c);
//
//                            BigInteger CIDbaru_ASCII = new BigInteger(sb.toString());
//                            //ubah out1 ke string
//                            String CIDbaru_string = CIDbaru_ASCII.toString();
//                            //Log.d("CIDbaru ASCII",CIDbaru_string);

                            String B18 = verif(mB17, mA2, name, HPW,  N1, mCID, mGID, B1, mB16, mCIDbaru);
                            Log.d("B18 terima", mB18);
                            Log.d("B18 hitung",B18);

                            if (success.equals("SUKSES GANTI PASSWORD")){
                                Toast.makeText(GantiPassword.this, "Sukses Autentikasi! \nNama : "
                                        +name+"\ndi Gateway : "+mGID, Toast.LENGTH_SHORT)
                                        .show();
                                Log.d("B18 valid!",B18);
                                loading.setVisibility(View.GONE);
                                btn_passwordbaru.setVisibility(View.VISIBLE);
                                passwordbaru.setVisibility(View.VISIBLE);
                                layoutpasswordbaru.setVisibility(View.VISIBLE);
                            }else{
                                Toast.makeText(GantiPassword.this, "Gagal Autentikasi! \nNama : "
                                        +name, Toast.LENGTH_SHORT).show();
                                loading.setVisibility(View.GONE);
                            }
                        } catch (JSONException e){
                            e.printStackTrace();
                            loading.setVisibility(View.GONE);
                            btn_login.setVisibility(View.VISIBLE);
                            Toast.makeText(GantiPassword.this, "Gagal Autentikasi!", Toast.LENGTH_SHORT).show();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(GantiPassword.this, "Error"+error.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError{
                Map<String, String> params = new HashMap<>();
                //params.put("name", name);
                //params.put("HPW", HPW);
                //params.put("password", password);
                //params.put("IMEI", IMEI);
                //params.put("B1", B1);
                params.put("B2",B2);
                params.put("B3",B3);
                params.put("B15",B15);
                params.put("CID",CID);
                params.put("GID",GID);
                return params;

            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private String verif(String B17, String A2, String ID, String HPW, int N1, String CID, String GID, String B1, String B16, String CIDbaru){
        /////////////// HITUNG CIDbaru /////////////////
        long B17_long = Long.parseLong(B17);
        String N1_string = String.valueOf(N1);
        String input1 = ID+HPW;
        //dapat h(ID||HPW)
        byte[] input11 = input1.getBytes();
        MessageDigest sha256 = null;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        sha256.update(input11);
        byte[] hasil = sha256.digest();
        StringBuffer hexhasil = new StringBuffer();
        for (int i=0; i<hasil.length; i++)
            hexhasil.append(Integer.toString((hasil[i]&0xff)+0x100,16).substring(1));
        String out1 = hexhasil.toString();
        //ubah h(ID||HPW) ke ASCII
        StringBuilder sb1 = new StringBuilder();
        for (char c : out1.toCharArray())
            sb1.append((int)c);

        BigInteger out1_ASCII = new BigInteger(sb1.toString());
        //ubah out1 ke string
        String out1_string = out1_ASCII.toString();
        out1_string = out1_string.substring(0,15);
        long out1_int = Long.parseLong(out1_string);
        out1_string = String.valueOf(out1_int);
        Log.d("h(ID||HPW)",out1_string);
        long A2_int = Long.parseLong(A2);
        long A2xorIDHPW = A2_int^out1_int;
        String A2xorIDHPW_string = String.valueOf(A2xorIDHPW);

        String input2 = A2xorIDHPW_string+N1_string;
        byte[] input22 = input2.getBytes();
        MessageDigest sha256_2 = null;
        try {
            sha256_2 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        sha256_2.update(input22);
        byte[] hasil2 = sha256_2.digest();
        StringBuffer hexhasil2 = new StringBuffer();
        for (int i=0; i<hasil2.length; i++)
            hexhasil2.append(Integer.toString((hasil2[i]&0xff)+0x100,16).substring(1));
        String out2 = hexhasil2.toString();
        //ubah h(N1||N2) ke ASCII
        StringBuilder sb2 = new StringBuilder();
        for (char c : out2.toCharArray())
            sb2.append((int)c);

        BigInteger out2_ASCII = new BigInteger(sb2.toString());
        //ubah out2 ke string
        String out2_string = out2_ASCII.toString();
        out2_string = out2_string.substring(0,15);
        long out2_int = Long.parseLong(out2_string);
        out2_string = String.valueOf(out2_int);
        Log.d("h(A2+h(ID||HPW))||Ru)",out2_string);

        long B17_int = Long.parseLong(B17);
        long CIDbaru_int = B17_int^out2_int;
        String CIDbaru_string = String.valueOf(CIDbaru_int);
        Log.d("CIDbaru",CIDbaru_string);

        /////////////// HITUNG B18 /////////////////
        String input3 = ID+CID+CIDbaru+B1+B16;
        //dapat h(ID||HPW)
        byte[] input33 = input3.getBytes();
        MessageDigest sha256_3 = null;
        try {
            sha256_3 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        sha256_3.update(input33);
        byte[] hasil3 = sha256_3.digest();
        StringBuffer hexhasil3 = new StringBuffer();
        for (int i=0; i<hasil3.length; i++)
            hexhasil3.append(Integer.toString((hasil3[i]&0xff)+0x100,16).substring(1));
        String out3 = hexhasil3.toString();
        String B18 = out3;

        int panjang = CIDbaru_string.length();
        int num = 0;
        for (int i = 0; i < panjang; i++) {
            // Append the current digit
            num = num * 10 + (CIDbaru_string.charAt(i) - '0');
            // If num is within the required range
             char CIDbaru_char = 0;
            if (num >= 48 && num <= 122) {
                // Convert num to char
                CIDbaru_char = (char) num;
                System.out.print("CIDbaru_char : "+CIDbaru_char);
                // Reset num to 0
                num = 0;
            }
        }
        return B18;
    }
}

