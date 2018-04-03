package poco.cn.mydemo.imgprocess;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import poco.cn.mydemo.R;


public class EmptyActivity extends AppCompatActivity
{

    protected MyView text;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
//        super.setContentView(R.layout.activity_empty);
        FrameLayout frameLayout = new FrameLayout(getApplicationContext());
        setContentView(frameLayout);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
//        ImageView imageView = new ImageView(getApplicationContext());
//        frameLayout.addView(imageView,params);
//        ImageView imageView1 = new ImageView(getApplicationContext());
//        frameLayout.addView(imageView1,params);

        ImageView view = new ImageView(getApplicationContext());
        params = new FrameLayout.LayoutParams(300,300);
        view.setBackgroundColor(Color.RED);
        frameLayout.addView(view,params);

        ImageView view1 = new ImageView(getApplicationContext());
        view1.setBackgroundColor(0x4f00ffff);
        params = new FrameLayout.LayoutParams(300,300);
        params.leftMargin = 150;
        params.topMargin = 150;
        frameLayout.addView(view1,params);

        ImageView view2 = new ImageView(getApplicationContext());
        view2.setBackgroundColor(ColorUtils.compositeColors(0x4f00ffff,Color.RED));
        params = new FrameLayout.LayoutParams(300,300);
        params.leftMargin = 300;
        params.topMargin = 0;
        frameLayout.addView(view2,params);

//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.homepage_img1);
//        Bitmap blur = BlurBitmapUtil.blurBitmap(getApplicationContext(),bitmap,10);
//        imageView.setImageBitmap(bitmap);
//        imageView1.setImageBitmap(blur);
//        imageView1.setAlpha(0.5f);

//        initView();
    }

    private void initView()
    {
        text = (MyView) findViewById(R.id.text);
    }
}
