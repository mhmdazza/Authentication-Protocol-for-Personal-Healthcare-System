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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    private String mA1, mA2, mA3, mCID, mGID;
    private EditText name, password, pasien;
    private TextView noIMEI;
    private Button btn_login;
    private TextView register, registerSensor, gantipassword;
    private Button getIMEI;
    private String IMEI;
    private long N1;
    private String tandadatabase = "Kepemilikan Database";

    private String Penanda ="nilaidariregister";
    //private String CID="";
    private static final int REQUEST_CODE = 101;
    //private static String URL_LOGIN = "http://192.168.43.228:1234/login";
    private static String URL_LOGIN = "http://192.168.43.228:1234/login";

    private ProgressBar loading;
    private SQLiteDatabase db;
    private SQLiteHelper database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loading = findViewById(R.id.loading);
        name = findViewById(R.id.name);
        password = findViewById(R.id.password);
        pasien = findViewById(R.id.pasien);
        noIMEI = findViewById(R.id.noIMEI);
        btn_login = findViewById(R.id.btn_login);
        register = findViewById(R.id.link_regist);
        registerSensor =  findViewById(R.id.link_registSensor);
        gantipassword = findViewById(R.id.gantipassword);
        getIMEI = findViewById(R.id.getIMEI);
        database = new SQLiteHelper(this);
        db = database.getReadableDatabase();

        //CID = getIntent().getStringExtra(Penanda);
        //Log.d("Login","NILAI CID = "+CID);

        getIMEI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                if (ActivityCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE);
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
                final String mPassword = password.getText().toString().trim();
                final String mPasien = pasien.getText().toString().trim();
                final String mIMEI = noIMEI.getText().toString().trim();

                if (!mName.isEmpty() || !mPassword.isEmpty() || !mPasien.isEmpty() || !mIMEI.isEmpty()) {
                    String query = "select * from android_tabel";
                    Cursor cursor = db.rawQuery(query, null);
                    while (cursor.moveToNext()) {
                        mA1 = cursor.getString(1);
                        mA2 = cursor.getString(2);
                        mA3 = cursor.getString(3);
                        mCID = cursor.getString(4);
                        mGID = cursor.getString(5);
                    }
                    String HPW = hpw(mPassword, mIMEI);
                    String B1 = b1(mA1, HPW);
                    final String Ru = ru();
                    String B2 = b2(B1,Ru);
                    String B3 = b3(mName, Ru, B1);
                    String B4 = b4(mCID, mGID, B1, mName, Ru);
                    //B4(mA1, mCID, mGID, mName, HPW, mPasien);
                    Login(HPW, Ru, mA2, mCID, mGID, mPasien, B2, B3, B4);
                }else{
                    name.setError("Masukkan nama!");
                    password.setError("Masukkan password!");
                    pasien.setError("Masukkan pasien!");
                    noIMEI.setError("Masukkan IMEI!");
                    Toast.makeText(LoginActivity.this, "Harus diisi semuanya!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

        registerSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,DaftarSensorActivity.class));
            }
        });

        gantipassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,GantiPassword.class));
            }
        });
    }

    private String b4(String CID, String GID, String B1, String ID, String Ru){
        String b4_string = CID+GID+B1+ID+Ru;
        byte[] b4_inputhash = b4_string.getBytes();

        MessageDigest sha256_2 = null;
        try {
            sha256_2 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        sha256_2.update(b4_inputhash);
        byte[] hasil_2 = sha256_2.digest();
        StringBuffer hexhasil_2 = new StringBuffer();
        for (int i=0; i<hasil_2.length; i++)
            hexhasil_2.append(Integer.toString((hasil_2[i]&0xff)+0x100,16).substring(1));
        //dapat nilai B4
        String B4 = hexhasil_2.toString();
        Log.d("B4",B4);
        return B4;
    }

    private String b3(String ID, String Ru, String B1){
        String n1 = String.valueOf(Ru);
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
        String B3 = String.valueOf(B3_int);
        Log.d("B3", B3);
        return B3;
    }

    private String b2(String B1, String Ru) {
        long B1_long = Long.parseLong(B1);
        long Ru_long = Long.parseLong(Ru);
        long B2_int = B1_long^Ru_long;
        //dapat string B2
        String B2 = String.valueOf(B2_int);
        Log.d("B2",B2);
        return B2;
    }

    private String ru() {
        Random rand = new Random();
        // Generate random integers in range 0 to 999
        N1 = rand.nextInt(1000000);
        // Print random integers
        final String Ru = String.valueOf(N1);
        Log.d("Ru", String.valueOf(N1));
        return Ru;
    }

    private String b1(String A1, String HPW) {
        StringBuilder sb = new StringBuilder();
        for (char c : HPW.toCharArray())
            sb.append((int)c);

        BigInteger HPW_ASCII = new BigInteger(sb.toString());
        //ubah HPW_ASCII ke string
        String HPW_string = HPW_ASCII.toString();
        HPW_string = HPW_string.substring(0,15);
        long HPW_int2 = Long.parseLong(HPW_string);
        //long HPW_int2 = Long.parseLong(HPW_string);
        long A1_integer = Long.parseLong(A1);
        long B1_int = A1_integer^HPW_int2;
        //dapat String B1
        String B1 = String.valueOf(B1_int);
        Log.d("B1",B1);
        return B1;
    }

    private String hpw(String mPassword, String mIMEI) {
        final byte[] hpw = mPassword.concat(mIMEI).getBytes();
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
        return mHPW;
    }

    private void Login(final String HPW, final String Ru, final String A2, final String CID, final String GID, final String SID, final String B2, final String B3, final String B4) {
        loading.setVisibility(View.GONE);
        btn_login.setVisibility(View.VISIBLE);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            String success = jsonObject.getString("sukses");
                            String ID = jsonObject.getString("name");
                            String B10 = jsonObject.getString("B10");
                            String B11 = jsonObject.getString("B11");
                            String B12 = jsonObject.getString("B12");
                            String B13 = jsonObject.getString("B13");
                            String B14terima = jsonObject.getString("B14");
                            String CIDbaru = jsonObject.getString("CIDbaru");
                            Log.d("B10 terima", B10);
                            Log.d("B11 terima", B11);
                            Log.d("B12 terima", B12);
                            Log.d("B13 terima", B13);
                            Log.d("B14 terima", B14terima);
                            Log.d("CID terima", CIDbaru);

                            String Rg = rg(B11, Ru, ID);
                            String Rs = rs(B12, Ru, Rg);
                            String SKu = sku(Ru, Rg, Rs);
                            String CIDnew = cidnew(B13, A2, ID, HPW, Rs);
                            String B14 = b14(SKu, ID, B10, CIDbaru);
                            //String B14 = verif(name, HPW, mA1, mA2, mB10, mB11, mB12, mB13, mCID, mCIDbaru);
                            Log.d("B14 terima", B14terima);
                            Log.d("B14 hitung",B14);

                            if (success.equals("SUKSES LOGIN") && B14terima.equals(B14)){
                                String A1baru = a1baru(B10, Ru, CID, HPW);
                                database.update(A1baru,CIDbaru);

                                Toast.makeText(LoginActivity.this, "Sukses Login! \nNama : "
                                            +ID+"\ndi Gateway : "+mGID, Toast.LENGTH_SHORT)
                                            .show();
                                    Log.d("B14 valid!",B14);
                                    loading.setVisibility(View.GONE);
                                    Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                                    intent.putExtra(tandadatabase, SID);
                                    startActivity(intent);
                            }else{
                                Toast.makeText(LoginActivity.this, "Gagal Login!", Toast.LENGTH_SHORT).show();
                                loading.setVisibility(View.GONE);
                            }
                        } catch (JSONException e){
                            e.printStackTrace();
                            loading.setVisibility(View.GONE);
                            btn_login.setVisibility(View.VISIBLE);
                            Toast.makeText(LoginActivity.this, "Gagal Login!", Toast.LENGTH_SHORT).show();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, "Error"+error.toString(), Toast.LENGTH_SHORT).show();
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
                params.put("B4",B4);
                params.put("CID",CID);
                params.put("GID",GID);
                params.put("SID",SID);
                return params;

            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private String a1baru(String B10, String Ru, String CID, String HPW){
        String input5 = Ru+CID;
        byte[] input55 = input5.getBytes();
        MessageDigest sha256_5 = null;
        try {
            sha256_5 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        sha256_5.update(input55);
        byte[] hasil5 = sha256_5.digest();
        StringBuffer hexhasil5 = new StringBuffer();
        for (int i=0; i<hasil5.length; i++)
            hexhasil5.append(Integer.toString((hasil5[i]&0xff)+0x100,16).substring(1));
        String out5 = hexhasil5.toString();
        //ubah h(A2+h(ID||HPW)) ke ASCII
        StringBuilder sb5 = new StringBuilder();
        for (char c : out5.toCharArray())
            sb5.append((int)c);

        BigInteger out5_ASCII = new BigInteger(sb5.toString());
        //ubah out5 ke string
        String out5_string = out5_ASCII.toString();
        out5_string = out5_string.substring(0,15);
        long out5_int = Long.parseLong(out5_string);
        out5_string = String.valueOf(out5_int);
        //Log.d("h(N1 || CID)",out5_string);

        //ubah HPW ke ASCII
        StringBuilder sb6 = new StringBuilder();
        for (char c : HPW.toCharArray())
            sb6.append((int)c);

        BigInteger out6_ASCII = new BigInteger(sb6.toString());
        //ubah out6 ke string
        String out6_string = out6_ASCII.toString();
        out6_string = out6_string.substring(0,15);
        long out6_int = Long.parseLong(out6_string);
        out6_string = String.valueOf(out6_int);
        //Log.d("HPW",out6_string);

        long B10_long = Long.parseLong(B10);
        long A1baru_long = B10_long^out5_int^out6_int;
        String A1baru = String.valueOf(A1baru_long);
        //Log.d("A1baru", A1baru);
        return A1baru;
    }

    private String b14(String SKu, String ID, String B10, String CIDbaru) {
        String input7 = SKu+ID+B10+CIDbaru;
        byte[] input77 = input7.getBytes();
        MessageDigest sha256_7 = null;
        try {
            sha256_7 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        sha256_7.update(input77);
        byte[] hasil7 = sha256_7.digest();
        StringBuffer hexhasil7 = new StringBuffer();
        for (int i=0; i<hasil7.length; i++)
            hexhasil7.append(Integer.toString((hasil7[i]&0xff)+0x100,16).substring(1));
        String B14 = hexhasil7.toString();
        return B14;
    }

    private String cidnew(String B13, String A2, String ID, String HPW, String Rs){
        long A2_long = Long.parseLong(A2);
        String input3 = ID+HPW;
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
        //ubah h(N1||N2) ke ASCII
        StringBuilder sb3 = new StringBuilder();
        for (char c : out3.toCharArray())
            sb3.append((int)c);

        BigInteger out3_ASCII = new BigInteger(sb3.toString());
        //ubah out3 ke string
        String out3_string = out3_ASCII.toString();
        out3_string = out3_string.substring(0,15);
        long out3_int = Long.parseLong(out3_string);
        out3_string = String.valueOf(out3_int);
        Log.d("h(ID||HPW)",out3_string);

        long input4_long = A2_long^out3_int;
        String input4 = String.valueOf(input4_long);
        Log.d("A2+h(ID||HPW)",input4);

        input4 = input4+Rs;
        byte[] input44 = input4.getBytes();
        MessageDigest sha256_4 = null;
        try {
            sha256_4 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        sha256_4.update(input44);
        byte[] hasil4 = sha256_4.digest();
        StringBuffer hexhasil4 = new StringBuffer();
        for (int i=0; i<hasil4.length; i++)
            hexhasil4.append(Integer.toString((hasil4[i]&0xff)+0x100,16).substring(1));
        String out4 = hexhasil4.toString();
        //ubah h(A2+h(ID||HPW)) ke ASCII
        StringBuilder sb4 = new StringBuilder();
        for (char c : out4.toCharArray())
            sb4.append((int)c);

        BigInteger out4_ASCII = new BigInteger(sb4.toString());
        //ubah out4 ke string
        String out4_string = out4_ASCII.toString();
        out4_string = out4_string.substring(0,15);
        long out4_int = Long.parseLong(out4_string);
        out4_string = String.valueOf(out4_int);
        Log.d("h(A2+h(ID||HPW))",out4_string);

        int pB13 = B13.length();
        String B13_1 = B13.substring(0,(pB13-15));
        String B13_2 = B13.substring((pB13-15),pB13);
        Log.d("B13_1", B13_1);
        Log.d("B13_2", B13_2);
        long B13_2_long = Long.parseLong(B13_2);
        long CIDnew_2_long = B13_2_long^out4_int;
        String CIDnew_2 = String.valueOf(CIDnew_2_long);
        Log.d("CIDbaru_2", CIDnew_2);
        String CIDnew = B13_1+CIDnew_2;
        Log.d("CIDbaru_ascii", CIDnew);
        int len = CIDnew.length();
        String CIDbaru = asciiToString(CIDnew,len);
        Log.d("CIDbaru",CIDbaru);
        return CIDbaru;
    }

    private String sku(String Ru, String Rg, String Rs){
        String input6 = Ru+Rg+Rs;
        byte[] input66 = input6.getBytes();
        MessageDigest sha256_6 = null;
        try {
            sha256_6 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        sha256_6.update(input66);
        byte[] hasil6 = sha256_6.digest();
        StringBuffer hexhasil6 = new StringBuffer();
        for (int i=0; i<hasil6.length; i++)
            hexhasil6.append(Integer.toString((hasil6[i]&0xff)+0x100,16).substring(1));
        String out6 = hexhasil6.toString();
        String SKu = out6;
        Log.d("Kunci Sesi Dokter (SKu)",SKu);
        return SKu;
    }

    private String rs(String B12, String Ru, String Rg){
        long B12_long = Long.parseLong(B12);
        String input_2 = Ru+Rg;
        //dapat h(N1||N2)
        byte[] input_22 = input_2.getBytes();
        MessageDigest sha256_2 = null;
        try {
            sha256_2 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        sha256_2.update(input_22);
        byte[] hasil_2 = sha256_2.digest();
        StringBuffer hexhasil_2 = new StringBuffer();
        for (int i=0; i<hasil_2.length; i++)
            hexhasil_2.append(Integer.toString((hasil_2[i]&0xff)+0x100,16).substring(1));
        final String out_2 = hexhasil_2.toString();
        //ubah h(N1||N2) ke ASCII
        StringBuilder sb_2 = new StringBuilder();
        for (char c : out_2.toCharArray())
            sb_2.append((int)c);

        BigInteger out2_ASCII = new BigInteger(sb_2.toString());
        //ubah out2 ke string
        String out2_string = out2_ASCII.toString();
        out2_string = out2_string.substring(0,15);
        long out2_int = Long.parseLong(out2_string);
        out2_string = String.valueOf(out2_int);

        long N3_long = B12_long^out2_int;
        String N3 = String.valueOf(N3_long);
        Log.d("N3",N3);
        return N3;
    }

    private String rg(String B11, String Ru, String ID){
        final byte[] inputhash1 = Ru.concat(ID).getBytes();
        MessageDigest sha256 = null;
        try {
            sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        sha256.update(inputhash1);
        byte[] hasil = sha256.digest();
        StringBuffer hexhasil = new StringBuffer();
        for (int i=0; i<hasil.length; i++)
            hexhasil.append(Integer.toString((hasil[i]&0xff)+0x100,16).substring(1));
        final String outputhash1 = hexhasil.toString();

        //ubah h(N1||ID) ke ASCII
        StringBuilder sb = new StringBuilder();
        for (char c : outputhash1.toCharArray())
            sb.append((int)c);

        BigInteger outputhash1_ASCII = new BigInteger(sb.toString());
        //ubah HPW_ASCII ke string
        String outputhash1_string = outputhash1_ASCII.toString();
        outputhash1_string = outputhash1_string.substring(0,15);
        long outputhash1_int2 = Long.parseLong(outputhash1_string);
        //long HPW_int2 = Long.parseLong(HPW_string);

        long B11_integer = Long.parseLong(B11);
        long N2_int = B11_integer^outputhash1_int2;
        //dapat String B1
        String N2 = String.valueOf(N2_int);
        Log.d("N2",N2);
        return N2;
    }

    static String xorString(String output, String output_2)
    {
        int panjang1 = output.length();
        int panjang2 = output_2.length();
        if (panjang1>panjang2){
            for(int j=0; j<(panjang1-panjang2);j++){
                output_2 = output_2+'0';
            }
            System.out.println("output : " + output_2);
        }
        char[] char_output = output.toCharArray();
        char[] char_output_2 = output_2.toCharArray();

        String output4 = "";
        for(int i=0; i<panjang1; i++){
            int b = Character.getNumericValue(char_output[i]);
            System.out.print(b);
            int b_2 = Character.getNumericValue(char_output_2[i]);
            int b_hasil = b^b_2;
            String output3 = String.valueOf(b_hasil);
            output4 += output3;
        }
        System.out.println();
        System.out.println("output4 : "+output4);
        return output4;
    }

    private String asciiToString(String ciDnew, int len) {
        String CIDnewstring="";
        int num = 0;
        for (int i = 0; i < len; i++) {
            // Append the current digit
            num = num * 10 + (ciDnew.charAt(i) - '0');
            // If num is within the required range
            if (num >= 97 && num <= 122) {
                // Convert num to char
                char CIDbaru = (char) num;
                //System.out.print(CIDbaru);
                CIDnewstring += CIDbaru;
                // Reset num to 0
                num = 0;
            }
        }
        return CIDnewstring;
    }

}
