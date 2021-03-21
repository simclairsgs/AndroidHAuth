package com.example.mkce_hauth;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;
import static java.lang.System.out;

public class HomeActivity extends AppCompatActivity {

    private Button attnBtn;
    private Context context;
    private JSONObject userData,attnStat;
    private TextView reg_no,name,date,status;
    private String register_no;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        try {
            getUserData();
            initialise();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        attnBtn = (Button)findViewById(R.id.todayAttendance);
        attnBtn.setEnabled(false);

        attnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                out.println("Clicked...Attnbtn");
                AuthenticateUser();
            }
        });

    }

    @SuppressLint("SetTextI18n")
    private void initialise() {
        reg_no = (TextView)findViewById(R.id.home_reg_vw);
        date = (TextView)findViewById(R.id.home_date_vw);
        name = (TextView)findViewById(R.id.home_welcome_lbl);
        status = (TextView)findViewById(R.id.home_todat_sts_vw);

        register_no = userData.get("Register_No").toString();
        reg_no.setText(register_no);
        String nameo = userData.get("Name").toString();
        int firstSpace = nameo.indexOf(" ");
        name.setText("Welcome, "+nameo.substring(0,firstSpace));
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        date.setText(sdf.format(new Date()));

        //send request to get status
        getStatus();


    }

    private void  debug(int line)
    {
        out.println("Execution crossed "+line);
    }


    private void AuthenticateUser()
    {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(HomeActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
               /* Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT)
                        .show();*/
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);

                Thread thread = new Thread(new Runnable(){
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    public void run() {
                        Looper.prepare();
                        try {
                            MainActivity Mactivty = new MainActivity();
                            URL url = new URL("http://"+Mactivty.TargetIP+"/auth-user/");
                            HttpURLConnection con = (HttpURLConnection)url.openConnection();
                            con.setRequestMethod("POST");
                            con.setRequestProperty("Content-Type","application/json; utf-8");
                            con.setRequestProperty("Accept","application/json");
                            con.setDoOutput(true);
                            String jsonInputString = "{\"Register_No\":\""+register_no+"\",\"Key\":\""+Mactivty.ApiKey+"\"}";

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

                                out.println(response.toString());

                                        if(obj.get("Status").toString().equals("success"))
                                        {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    Toast.makeText(getApplicationContext(),
                                                            "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                                                    refresh();
                                                }
                                            });

                                        }




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

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT)
                        .show();
            }
        });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric Authentication")
                .setSubtitle("for attendance")
                .setNegativeButtonText("Cancel")
                .build();

            biometricPrompt.authenticate(promptInfo);

    }

    private void getStatus()
    {
        MainActivity Mactivty = new MainActivity();
        //Process of calling api-auth
        Thread thread = new Thread(new Runnable(){
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void run() {
        try {
            URL url = new URL("http://"+Mactivty.TargetIP+"/check-status/");
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type","application/json; utf-8");
            con.setRequestProperty("Accept","application/json");
            con.setDoOutput(true);
            String jsonInputString = "{\"Register_No\":\""+register_no+"\",\"Key\":\""+Mactivty.ApiKey+"\"}";

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
                attnStat = obj;
                out.println(response.toString());
                runOnUiThread(new Runnable() {
                    @SuppressLint("ResourceAsColor")
                    @Override
                    public void run() {
                        if(Boolean.parseBoolean(attnStat.get("Status").toString()))
                            status.setText("Authenticated");
                        else
                            status.setText("Not Authenticated");
                        if(Boolean.parseBoolean(attnStat.get("Allow_auth").toString()))
                        {
                            attnBtn.setEnabled(true);
                            attnBtn.setBackgroundColor(Color.GREEN);
                        }

                    }
                });


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

    private void getUserData() throws IOException, ParseException
    {
        Context content = getApplicationContext();
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
        out.println("Recovered >>>> "+text);
        //parse to json and test
        JSONParser parser = new JSONParser();
        userData= (JSONObject) parser.parse(text);

    }

    private void refresh()
    {
        finish();
        overridePendingTransition(0,0);
        startActivity(getIntent());
        overridePendingTransition(0,0);
    }
}