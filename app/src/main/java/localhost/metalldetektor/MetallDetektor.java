package localhost.metalldetektor;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MetallDetektor extends AppCompatActivity implements SensorEventListener {

    private ProgressBar progressBar;
    private SensorManager sensorManager;
    private Sensor magneticFieldSensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metall_detektor);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        List <Sensor> magneticFieldSensors = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);

        if (magneticFieldSensors.isEmpty()){
            throw new RuntimeException("No Sensor");
        }

        magneticFieldSensor = magneticFieldSensors.get(0);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setMax((int) magneticFieldSensor.getMaximumRange() /3);

    }


    protected void onResume(){
        super.onResume();
        if (magneticFieldSensor != null){
            sensorManager.registerListener(this, magneticFieldSensor,  SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    protected void onPause(){
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged (SensorEvent event){
        if (event.sensor == magneticFieldSensor){
            float[] mag = event.values;
            double betrag = Math.sqrt(mag[0] * mag[0] + mag[1] * mag[1] + mag[2] * mag[2]);

            progressBar.setProgress((int)betrag);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    private static final int SCAN_QR_REQUEST_CODE = 0;
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuItem menuItem = menu.add("Log");
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent( "com.google.zxing.client.android.SCAN");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, SCAN_QR_REQUEST_CODE);
                return false;
            }
        });
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        if (requestCode == SCAN_QR_REQUEST_CODE){
            if (resultCode == RESULT_OK) {
                log(intent.getStringExtra("SCAN_RESULT"));
            }
        }
    }

    private void log(String qrCode){
        Intent intent = new Intent("ch.appquest.intent.LOG");
        JSONObject log = new JSONObject();

        try {
            log.put("task","Metalldetektor");
            log.put("salution", qrCode);
        }
        catch (JSONException e){

        }
        intent.putExtra("ch.appquest.logmessage", log.toString());
        startActivity(intent);
    }
}
