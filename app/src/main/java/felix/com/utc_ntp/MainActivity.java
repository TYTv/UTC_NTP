package felix.com.utc_ntp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView tv;
    private Button bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.textViewShow);
        bt = (Button) findViewById(R.id.buttonStart);

    }

    public void onClickStart(View view) {

        bt.setEnabled(false);
        new NTP(this) {

            StringBuilder sb = new StringBuilder();

            @Override
            public void onResult(final RESULT result) {

                sb.append(result.offsetTime() + "s\t" + result.responseTime() + "ms\t" + result.host + "\n");
                tv.setText(sb);

            }

            @Override
            public void onResultFinish(final List<RESULT> results) {

                Map<String, Double> msd = averageNTP(results);
                sb.append(msd.get(KEY_OFF) + "s\t" + msd.get(KEY_RSP) + "ms\t" + " <- Average(" + msd.get(KEY_TOL) + ")\n");
                tv.setText(sb);
                bt.setEnabled(true);

            }

        };

    }

}
