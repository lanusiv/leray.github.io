package me.better.leray.rafflewheeldemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private RaffleWheelView raffleWheelView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        raffleWheelView = (RaffleWheelView) findViewById(R.id.raffle);

    }
}
