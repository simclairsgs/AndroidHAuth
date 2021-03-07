package com.example.mkce_hauth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class HomeActivity extends AppCompatActivity {

    private Button attnBtn;
    private Context context;
    private JSONObject userData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        try {
            getUserData();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        attnBtn = (Button)findViewById(R.id.todayAttendance);
        attnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity main = new MainActivity();
                Toast.makeText(HomeActivity.this, "Hello World"+userData.toString(), Toast.LENGTH_SHORT).show();
            }
        });

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
        System.out.println("Recovered >>>> "+text);
        //parse to json and test
        JSONParser parser = new JSONParser();
        userData= (JSONObject) parser.parse(text);

    }
}