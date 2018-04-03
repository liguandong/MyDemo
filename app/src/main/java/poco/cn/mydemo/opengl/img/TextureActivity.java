package poco.cn.mydemo.opengl.img;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import poco.cn.mydemo.R;


public class TextureActivity extends AppCompatActivity
{

    protected ImgSurfaceView glView;
    private View clickView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_texture);
        initView();
    }

    private void initView()
    {
        glView = (ImgSurfaceView) findViewById(R.id.glView);
        clickView = (View) findViewById(R.id.clickView);
//        clickView.setOnClickListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        glView.onResume();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        glView.onPause();
    }
}
