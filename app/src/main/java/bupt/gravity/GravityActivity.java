package bupt.gravity;

import android.app.ActionBar;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;

import com.sackcentury.shinebuttonlib.ShineButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import xyz.hanks.library.NumberView;

public class GravityActivity extends AppCompatActivity {
    private Sensor linearAccSensor;
    private SensorManager mSensorManager;

    double x, y, z = 0;

    Calculation calc;
    private double distance = .00000;

    void onDistanceChanged(double newValue) {
        this.distance = newValue;
        NumberView numberViewFront = this.findViewById(R.id.number1);
        NumberView numberViewEnd = this.findViewById(R.id.number2);
        int front = (int) newValue;
        int end = (int) (100 * (newValue-front));
        numberViewFront.setNumberText(front);
        numberViewFront.play();
        numberViewEnd.setNumberText(end);
        numberViewEnd.play();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_gravity);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void init() {
        initButtons();

    }

//    private void initSensor() {
//        wogeiwangle(new OnBoomListener() {
//            void onThrow(View view, Double distance) {
//                this.distance = distance;
//            }
//        })
//    }
//


    private void initButtons() {
        initRetryButtonClicked();
        initShareButtonClicked();
        initRankingButtonClicked();
    }

    private void initShareButtonClicked() {
        ShineButton shineButton = initShineButton(R.id.share_button);

        shineButton.setOnClickListener(v->{
            shineButton.setChecked(true, true);
            // TODO: replace string
                String htmlString = buildGravityShareHtml(this.distance);
            shareGravityResult(htmlString);
        });
    }

    private void initRetryButtonClicked() {
        ShineButton shineButton = initShineButton(R.id.retry_button);

        shineButton.setOnClickListener(v->{
            shineButton.setChecked(true, true);
            NumberView numberViewFront = this.findViewById(R.id.number1);
            numberViewFront.play();
            NumberView numberViewEnd = this.findViewById(R.id.number2);
            numberViewEnd.play();
            numberViewFront.setNumberText(0);
            numberViewEnd.setNumberText(0);
            startGravitySample();
        });
    }

    private void startGravitySample() {
        calc = new Calculation();

        if (mSensorManager == null) {
            mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            linearAccSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
            mSensorManager.registerListener(new SensorEventListener() {
                @Override
                public void onSensorChanged(SensorEvent event) {
                    double threshold = 0.2;

                    if (Math.abs(event.values[0] - x) > threshold)
                        x = event.values[0];
                    if (Math.abs(event.values[1] - y) > threshold)
                        y = event.values[1];
                    if (Math.abs(event.values[2] - z) > threshold)
                        z = event.values[2];

                    double distance = calc.onDataArrive(x, y, z);
                    Log.w("[LOGGGGGGGGGGGGGGGGGGG]", "DISTANCE"+distance);
                    if(distance > 0) {
                        Log.w("[LOGGGGGGGGGGGGGGGGGGG]", "DISTANCE LEGAL!!");
                        onDistanceChanged(distance);
                        mSensorManager.unregisterListener(this);
                        mSensorManager = null;
                    }
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    // omit it
                }
            },
            linearAccSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void initRankingButtonClicked() {
        ShineButton shineButton = initShineButton(R.id.ranking_button);

        FrameLayout bottomSheet = findViewById(R.id.bottom_sheet);
        BottomSheetBehavior sheetBehavior = BottomSheetBehavior.from(bottomSheet);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//        sheetBehavior.setPeekHeight(BottomSheetBehavior.PEEK_HEIGHT_AUTO);

        shineButton.setOnClickListener(v -> {
            shineButton.setChecked(true, true);
            if (sheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN
                    && sheetBehavior.getState() != BottomSheetBehavior.STATE_DRAGGING) {
                sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
            } else {
                sendNameAndDistance();
                ArrayList<NameDistanceInfo> rankList = requestRankList();
                initRankRecyclerView(rankList);
                sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });
    }

    private ShineButton initShineButton(int res_id) {
        ShineButton shineButton = findViewById(res_id);
        shineButton.init(this);
        shineButton.setChecked(true, true);
        return shineButton;
    }

    private String buildGravityShareHtml(double distance) {
        String head = "<h1>快来和我一起扔手机</h1>";
        String body = "<p>我扔了整整" + String.valueOf(distance).substring(0,4) + "米远哦！</p>";
        return  head + body;
    }

    private void shareGravityResult(String htmlString) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/html");
        shareIntent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(htmlString));
        startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.shareBy)));
    }

    private void initRankRecyclerView(ArrayList<NameDistanceInfo> nameDistanceInfos){
        RecyclerView rankRecyclerView;
        RecyclerView.Adapter rankAdapter;
        RecyclerView.LayoutManager mLayoutManager;

        rankRecyclerView = (RecyclerView) findViewById(R.id.rank_recycler_view);
        rankRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        rankRecyclerView.setLayoutManager(mLayoutManager);
        rankAdapter = new RankRecyclerViewAdapter(nameDistanceInfos);
        rankRecyclerView.setAdapter(rankAdapter);
    }

    private void sendNameAndDistance(){
        Intent intent = getIntent();
        JSONObject json = new JSONObject();
        if (intent != null) {
            String username = intent.getStringExtra("username"); // 从Intent当中根据key取得value
            Log.e("name：", username);

            json = new JSONObject();
            try {
                //TODO 接口格式
                json.put("username", username);
                json.put("distance", String.valueOf(distance).substring(0,4));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString());
        Request request = new Request.Builder()
                .url("http://192.168.0.102:8080/?????")
                .post(requestBody)
                .build();
        //发送请求获取响应
        try {
            Response response=okHttpClient.newCall(request).execute();
            //判断请求是否成功
            if(response.isSuccessful()){
                //打印服务端返回结果
                Log.e("post",response.body().string());

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private ArrayList<NameDistanceInfo> requestRankList(){
        ArrayList<NameDistanceInfo> nameDistanceInfos = new ArrayList<>();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://??????").build();
        try {
            Response response = client.newCall(request).execute();//发送请求
            JSONArray results = new JSONArray(response.body().string());
            Log.d("get", "result: " + results);

            for(int i=0;i<results.length();i++)
            {
                JSONObject jsonObject=results.getJSONObject(i);
                //TODO 接口格式
                nameDistanceInfos.add(new NameDistanceInfo(jsonObject.getString("username"), jsonObject.getString("distance")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    return nameDistanceInfos;
    }
}
