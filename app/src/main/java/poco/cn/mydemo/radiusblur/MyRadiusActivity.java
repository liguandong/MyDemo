package poco.cn.mydemo.radiusblur;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import poco.cn.mydemo.R;

public class MyRadiusActivity extends AppCompatActivity
{

    private RadiusPage surfaceView;
    private SeekBar seekbar1;
    private FrameLayout parent;
    private String mImgPath;
    private SeekBar seekbar2;
    ValueAnimator valueAnimator;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        parent = new FrameLayout(getApplicationContext());
        setContentView(parent);
        initView();

    }

    private void initView()
    {
        valueAnimator = new ValueAnimator();
        valueAnimator.setDuration(1000);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setFloatValues(0f,1f);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator animation)
            {
                surfaceView.setProcess1((Float) animation.getAnimatedValue());
                surfaceView.requestRender();
            }
        });

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        surfaceView = new RadiusPage(getApplicationContext(), 1080, 1500);
        parent.addView(surfaceView, params);

        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100);
        params.gravity = Gravity.BOTTOM;
        params.bottomMargin = 20;
        seekbar1 = new SeekBar(getApplicationContext());
        seekbar1.setPadding(0, 50, 0, 50);
//        seekbar1.setOnClickListener(this);
        seekbar1.setMax(100);
        seekbar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                surfaceView.setProcess1(progress * 1.0f / 100);
                surfaceView.requestRender();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                valueAnimator.cancel();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });
        seekbar1.setProgress(50);
        parent.addView(seekbar1, params);


        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 100);
        params.gravity = Gravity.BOTTOM;
        params.bottomMargin = 160;
        seekbar2 = new SeekBar(getApplicationContext());
        seekbar2.setPadding(0, 50, 0, 50);
//        seekbar1.setOnClickListener(this);
        seekbar2.setMax(100);
        seekbar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
//                surfaceView.setProcess2(progress * 1.0f / 100);
//                surfaceView.requestRender();
                valueAnimator.setDuration((long) (500 + progress * 1.0f/100  * 2000));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                valueAnimator.cancel();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                valueAnimator.
                        start();
            }
        });
        seekbar2.setProgress(50);
        parent.addView(seekbar2, params);


//
//        Button button = new Button(getApplicationContext());
//        button.setText("保存获取bitmap");
//        button.setOnClickListener(new View.OnClickListener()
//        {
//            @Override
//            public void onClick(View v)
//            {
//
//            }
//        });
//        params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        params.bottomMargin = 20;
//        params.leftMargin = 20;
//        params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
//        parent.addView(button, params);



    }

    public void onClick(View view)
    {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null)
        {
            Uri selectedImage = data.getData();
            String[] filePathColumns = {MediaStore.Images.Media.DATA};
            Cursor c = getContentResolver().query(selectedImage, filePathColumns, null, null, null);
            c.moveToFirst();
            int columnIndex = c.getColumnIndex(filePathColumns[0]);
            mImgPath = c.getString(columnIndex);
            Log.e("wuwang", "img->" + mImgPath);
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mImgPath,opts);
            opts.inSampleSize = calculateInSampleSize(opts, 1080, 1500);
            opts.inJustDecodeBounds = false;
            final Bitmap bmp =  BitmapFactory.decodeFile(mImgPath,opts);
            if(bmp != null)
            {
                seekbar1.setProgress(50);
//                parent.removeView(surfaceView);
//                surfaceView = new BlurPage(getApplicationContext(),1080,1500);
//                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(1080, ViewGroup.LayoutParams.WRAP_CONTENT);
//                parent.addView(surfaceView,params);
                surfaceView.setmBitmap(bmp);
            }else{
                Toast.makeText(this, "加载失败", Toast.LENGTH_SHORT).show();
            }
            c.close();
        }
    }

    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        final int inW = options.outWidth;
        final int inH = options.outHeight;
        int minW = reqWidth;
        int minH = reqHeight;
        int inSampleSize = 1;
        float scale_w_h = 1;
        if (minW < 1)
        {
            minW = inW << 1;
        }
        if (minH < 1)
        {
            minH = inH << 1;
        }
        scale_w_h = (float) inW / (float) inH;
        float w = minH * scale_w_h;
        float h = minH;
        if (w > minW)
        {
            w = minW;
            h = minW / scale_w_h;
        }
        inSampleSize = (int) (inW / w < inH / h ? inW / w : inH / h);
        if (inSampleSize < 1)
        {
            inSampleSize = 1;
        }
        return inSampleSize;
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        surfaceView.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        surfaceView.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_filter,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
//        surfaceView.queueEvent(new Runnable()
//        {
//            @Override
//            public void run()
//            {
                switch (item.getItemId()){
                    case R.id.circle_in:
                        surfaceView.getRender().setShader("blur/vertex_transition_circle_in.glsl","blur/fragment_transition_circle_in.glsl");
                        break;
                    case R.id.circle_out:
                        surfaceView.getRender().setShader("blur/vertex_transition_circle_out.glsl","blur/fragment_transition_circle_out.glsl");
                        break;
                    case R.id.rhombus:
                        surfaceView.getRender().setShader("blur/vertex_transition_rhombus.glsl","blur/fragment_transition_rhombus.glsl");
                        break;
                    case R.id.radius:
                        surfaceView.getRender().setShader("blur/vertex_transition_circle_in.glsl","blur/fragment_transition_circle_in.glsl");
                        break;
                    case R.id.slip:
                        surfaceView.getRender().setShader("blur/vertex_transition_circle_in.glsl","blur/fragment_transition_circle_in.glsl");
                        break;

                }
//                MyRadiusActivity.this.runOnUiThread(new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//                        surfaceView.requestRender();
//                    }
//                });
//            }
//        });
        surfaceView.requestRender();

        return super.onOptionsItemSelected(item);
    }

}