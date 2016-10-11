package com.example.wper_smile.sqlite;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.Map;
import java.util.StringTokenizer;


public class SearchActivity extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Intent intent=new Intent();
        intent=getIntent();
        Bundle bundle=intent.getExtras();
        ArrayList<Map<String,String>> items=(ArrayList<Map<String,String>>) bundle.getSerializable("result");

        SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.item,
                new String[]{Words.Word._ID, Words.Word.Column_name_word, Words.Word.Column_name_meaning, Words.Word.Column_name_sample},
                new int[]{R.id.text, R.id.WordView, R.id.MeaningView, R.id.SampleView});

        ListView list = (ListView) findViewById(R.id.wordSearch);

        list.setAdapter(adapter);
    }
}
