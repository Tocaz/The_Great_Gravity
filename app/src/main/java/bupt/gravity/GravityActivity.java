package bupt.gravity;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.sackcentury.shinebuttonlib.ShineButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import xyz.hanks.library.NumberView;

public class GravityActivity extends AppCompatActivity {
    private Sensor linearAccSensor;
    private SensorManager mSensorManager;
    private ArrayList<NameDistanceInfo> arrayList = new ArrayList<>();

    double x, y, z = 0;

    Calculation calc;
    private double distance = .0;

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
        arrayList.add(new NameDistanceInfo("Luoop","100.128"));
        arrayList.add(new NameDistanceInfo("黄金鱼","41.88"));

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
//                sendNameAndDistance();
//                requestRankList();
                Intent intent = getIntent();
                String username = intent.getStringExtra("username"); // 从Intent当中根据key取得value
                if (this.distance < 0.1) {
                    arrayList.add(new NameDistanceInfo(username, "不敢扔 >w<"));
                } else {
                    String dis = String.valueOf(this.distance);
                    if (dis.length() > 5) dis = dis.substring(0,4);
                    arrayList.add(new NameDistanceInfo(username, dis));
                }
                initRankRecyclerView(arrayList);
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
        String body;
        if (distance < 0.1) {
            body = "<p>我根本不敢扔，我很怂的！</p>";
        }
        else if (String.valueOf(distance).length() > 5){
            body = "<p>我扔了整整" + String.valueOf(distance).substring(0,4) + "米远哦！</p>";
        }
        else {
            body = "<p>我扔了整整" + String.valueOf(distance) + "米远哦！</p>";
        }

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
                if (String.valueOf(distance).length() > 5){
                    json.put("distance", String.valueOf(distance).substring(0,4));
                }
                else {
                    json.put("distance", String.valueOf(distance));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            post("http://10.128.198.127:8080/gravityServer/RankServlet", json, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Toast.makeText(GravityActivity.this, "上传成绩失败", Toast.LENGTH_SHORT).show();
                    Log.e("[POST]","onFailure");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.e("[POST]","success");
                }
            });
        }
    }

    private Call post(String url, JSONObject json, Callback callback) {
        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient okHttpClient = new OkHttpClient();

        RequestBody body = RequestBody.create(JSON, json.toString());
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        //发送请求获取响应
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
        return call;
    }


    private void requestRankList(){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("http://10.128.198.127:8080/gravityServer/RankServlet").build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("[Request]","onFailure" + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                JSONArray results = null;
                try {
                    String str = response.body().string();
                    Log.e("[Request]", str);
                    results = new JSONArray(str);
                    for(int i=0;i<results.length();i++)
                    {
                        JSONObject jsonObject=results.getJSONObject(i);
                        //TODO 接口格式
                        ArrayList<NameDistanceInfo> nameDistanceInfos = new ArrayList<>();
                        nameDistanceInfos.add(new NameDistanceInfo(jsonObject.getString("username"), jsonObject.getString("distance")));
                        onReceivedRank(nameDistanceInfos);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.d("get", "result: " + results);
            }
        });//发送请求
    }

    private void onReceivedRank(ArrayList<NameDistanceInfo> nameDistanceInfos) {
        initRankRecyclerView(nameDistanceInfos);
    }

}
