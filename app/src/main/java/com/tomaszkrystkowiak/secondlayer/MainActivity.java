package com.tomaszkrystkowiak.secondlayer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private Button createButton;
    private Button explorationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createButton = findViewById(R.id.create_button);
        createButton.setOnClickListener(new CreateButtonClick());

        explorationButton = findViewById(R.id.explore_button);
        explorationButton.setOnClickListener(new ExplorationButtonClick());

    }

    private void startExplorationActivity(){
        Intent intent = new Intent(this, ExplorationActivity.class);
        startActivity(intent);
    }

    private void startCreationActivity(){
        Intent intent = new Intent(this, CreationActivity.class);
        startActivity(intent);
    }

    private class CreateButtonClick implements View.OnClickListener{

        @Override
        public void onClick(View v) { startCreationActivity();}
    }

    private class ExplorationButtonClick implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            startExplorationActivity();
        }
    }
}