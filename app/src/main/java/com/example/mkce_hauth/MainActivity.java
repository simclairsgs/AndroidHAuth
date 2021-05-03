package com.example.mkce_hauth;
/*
Copyrights (C) 2021 , George Simclair Sam

This file is a part of AndroidHAuth project.

This file can not be copied and/or distributed without the express
permission of George Simclair Sam, simclair.sgs@gmail.com .
*/
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Base64;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText registerNoEditText,passwordEditText;
    private Button loginBtn;
    private Context content;

    public String TargetIP = "192.168.240.229:8000";        // Change the IP here
    public String ApiKey = "t(j3zi6jwui$0r6+v94bbct!u^&^krwt-!qulz3(qm^7=mgpc1";   // Change the API key here

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initialiseElements();

        try {
            CheckLoginStatus();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Process of calling api-auth
                Thread thread = new Thread(new Runnable(){
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void run() {
                        try {
                            URL url = new URL("http://"+TargetIP+"/login/");
                            HttpURLConnection con = (HttpURLConnection)url.openConnection();
                            con.setRequestMethod("POST");
                            con.setRequestProperty("Content-Type","application/json; utf-8");
                            con.setRequestProperty("Accept","application/json");
                            con.setDoOutput(true);
                            String jsonInputString = "{\"Register_No\":\""+registerNoEditText.getText()+"\",\"Otp\":\""+passwordEditText.getText()+"\",\"Key\":\""+ApiKey+"\"}";

                            try(OutputStream os = con.getOutputStream()) {
                                byte[] input = jsonInputString.getBytes("utf-8");
                                os.write(input, 0, input.length);
                            }
                            try(BufferedReader br = new BufferedReader(
                                    new InputStreamReader(con.getInputStream(), "utf-8"))) {
                                StringBuilder response = new StringBuilder();
                                String responseLine = null;
                                while ((responseLine = br.readLine()) != null) {
                                    response.append(responseLine.trim());
                                }
                                //System.out.println(response.toString());
                                JSONParser parser = new JSONParser();
                                JSONObject obj = (JSONObject)parser.parse(response.toString());

                                if(obj.containsKey("Name"))
                                {
                                    System.out.println(">>>Authentication success...");
                                    byte[] data = obj.toString().getBytes("UTF-8");
                                    String base64 = Base64.encodeToString(data, Base64.DEFAULT);
                                    File file = new File(content.getFilesDir(),"userdata.sgs");
                                    FileWriter fileWriter = new FileWriter(file);
                                    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                                    bufferedWriter.write(base64);
                                    bufferedWriter.close();
                                    //System.out.println(">>>File Written... .sgs\n\n"+base64);
                                }
                                else
                                {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(content,"Invalid Credentials...",Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }

                                //File read sequence
                                //System.out.println(obj);
                                CheckLoginStatus();


                            } catch (ParseException e) {
                                e.printStackTrace();
                            }


                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                });

                thread.start();


            }
        });

    }

    private void CheckLoginStatus() throws IOException, ParseException
    {
        File file = new File(content.getFilesDir(),"userdata.sgs");
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuilder stringBuilder = new StringBuilder();
        String line = bufferedReader.readLine();
        while (line != null){
            stringBuilder.append(line).append("\n");
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        // This responce will have Json Format String
        String fresponses = stringBuilder.toString();
        //System.out.println(fresponses);
        byte[] data = Base64.decode(fresponses, Base64.DEFAULT);
        String text = new String(data, "UTF-8");
        System.out.println("Recovered >>>> "+text);
        //parse to json and test
        JSONParser parser = new JSONParser();
        JSONObject json = (JSONObject) parser.parse(text);
        if(json.containsKey("Name"))
        {
            System.out.println("User Present--fwd to HomeActivity");
            Intent home = new Intent(this,HomeActivity.class);
            startActivity(home);
            finish();

        }

    }

    private void initialiseElements()
    {
        registerNoEditText = (EditText)findViewById(R.id.login_register_no);
        passwordEditText = (EditText)findViewById(R.id.login_password);
        loginBtn = (Button)findViewById(R.id.login_btn);

        content = getApplicationContext();
    }
}