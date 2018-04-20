package poco.cn.mydemo.filterView;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

import poco.cn.mydemo.R;
import poco.cn.mydemo.ShareData;

public class FilterActivity extends AppCompatActivity implements View.OnClickListener
{

    protected FrameLayout parent;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_filter);
        ShareData.InitData(this);
        initView();
    }

    private void initView()
    {
        parent = (FrameLayout) findViewById(R.id.parent);
        parent.setOnClickListener(FilterActivity.this);

        RoundAnimView roundAnimView = new RoundAnimView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(200,200);
        parent.addView(roundAnimView,params);
        ObjectAnimator objectAnimator = ObjectAnimator.ofInt(roundAnimView,"startH",0,80);
        objectAnimator.setDuration(200);
        objectAnimator.setRepeatCount(ValueAnimator.INFINITE);
        objectAnimator.start();

    }

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.parent)
        {

        }
    }
}
