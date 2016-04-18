package me.better.leray.rafflewheeldemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private RaffleWheelView raffleWheelView;
    private Button btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        raffleWheelView = (RaffleWheelView) findViewById(R.id.raffle);
        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(this);

    }

    public void onClick(View view) {
        Log.i("000", "onClick: ");
        if (raffleWheelView.isRunning()) {
            raffleWheelView.stop();
        } else {
            raffleWheelView.start();
        }
    }
}
