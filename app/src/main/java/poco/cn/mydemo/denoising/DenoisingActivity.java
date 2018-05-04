package poco.cn.mydemo.denoising;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;

import poco.cn.mydemo.R;

public class DenoisingActivity extends AppCompatActivity
{

    private GLSurfaceView surfaceView;
    private SeekBar seekbar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blur);
        initView();

    }

    private void initView()
    {
        surfaceView = (GLSurfaceView) findViewById(R.id.surfaceView);
        surfaceView.setEGLContextClientVersion(2);
        final DenoisingRender render = new DenoisingRender(this);
        surfaceView.setRenderer(render);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        seekbar = (SeekBar) findViewById(R.id.seekbar);
//        seekbar.setOnClickListener(this);
        seekbar.setMax(100);
        seekbar.setProgress(100);
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                render.setAlpha(progress * 1.0f /100 );
                surfaceView.requestRender();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
    }


}
