precision mediump float;
uniform vec2 uCenter;
uniform float uRadius;
uniform float uAlpha;
uniform float uXY;
uniform int uIsShowMask;
varying vec2 vCoordinate;
varying vec4 vPosition;
uniform sampler2D uNormalTexture;
uniform sampler2D uBlurTexture;

uniform float uOffsetX;
uniform float uOffsetY;

const float PI = 3.14159265;

varying vec2 uv;
const float max_sampleDist = 1.0;
const float max_sampleStrength = 2.2;
uniform float uProgress ;

const float zoomTime = 2.0f;
const int pixelSize = 4;

vec4 getRadiusColor(sampler2D texture,float progress,float zoom)
{
    float samples[10];
    samples[0] = -0.08;
    samples[1] = -0.05;
    samples[2] = -0.03;
    samples[3] = -0.02;
    samples[4] = -0.01;
    samples[5] =  0.01;
    samples[6] =  0.02;
    samples[7] =  0.03;
    samples[8] =  0.05;
    samples[9] =  0.08;

    float sampleDist = progress * max_sampleDist / zoom;
    float sampleStrength = 1.0f + progress * max_sampleStrength;

    //距离越远，纹理zhi

// 图像放大效果  http://www.icodelogic.com/?p=588
//				int src_x = (int)((float)(i - centerX) / xishu + centerX);
//				int src_y = (int)((float)(j - centerY) / xishu + centerY);

    //放大，取样逐渐向中间靠拢，这个2效果可以
//    vec2 uv1 = vec2(abs(uv.x * (1.0 - progress/zoom) + progress/zoom/2.0),abs(uv.y * (1.0 - progress/zoom) + progress/zoom/2.0));
    float xishu = (1.0 + (zoom - 1.0) * progress);
    vec2 uv1 = vec2( (uv.x - 0.5f ) /xishu + 0.5f,(uv.y - 0.5f ) / xishu + 0.5f);
    vec2 dir = vec2(0.5 - uv1.x,0.5 - uv1.y) * zoom;
    float dist = sqrt(dir.x*dir.x + dir.y*dir.y);
    dir = dir/dist;

    //距离中心点越远，取样间隔越大
    vec4 color = texture2D(texture,uv1);
    vec4 sum = color;


     sum += texture2D( texture, uv1 + dir * samples[0] * sampleDist );
     sum += texture2D( texture, uv1 + dir * samples[1] * sampleDist );
     sum += texture2D( texture, uv1 + dir * samples[2] * sampleDist );
     sum += texture2D( texture, uv1 + dir * samples[3] * sampleDist );
     sum += texture2D( texture, uv1 + dir * samples[4] * sampleDist );
     sum += texture2D( texture, uv1 + dir * samples[5] * sampleDist );
     sum += texture2D( texture, uv1 + dir * samples[6] * sampleDist );
     sum += texture2D( texture, uv1 + dir * samples[7] * sampleDist );
     sum += texture2D( texture, uv1 + dir * samples[8] * sampleDist );
     sum += texture2D( texture, uv1 + dir * samples[9] * sampleDist );
    sum *= 1.0/11.0;
    //取样强度，距离中心点越远，模糊越大
    float t = dist * sampleStrength;
    t = clamp( t ,0.0,1.0);
    return mix( color, sum, t );
}

//http://www.zealfilter.com/thread-264-1-1.html
//vec4 getConcaveColor(sampler2D texture,float progress){
//    vec4 color = vec4(0.0);
//    float originX = 0.5f;
//    float originY = 0.5f;
//    float dx = uv.x - originX;
//    float dy = uv.x - originY;
//    float distance = sqrt(dx * dx + dy * dy);
//    if (distance <= 0.5f)
//    {
//        float theta = tan(dy, dx);
//        float mapR = sin(PI * distance / (2 * 1.0)) * 1.0;
//
//        dx = originX + cos(theta);
//        dy = originY + sin(theta);
//
//        if (dx < 0 || dx >= width || dy < 0 || dy >= height)
//        {
//            ptr[0] = ptr[1] = ptr[2] = ptr[3] = 0;
//        }
//        else
//        {
//
//            ptr[0] = source[dy * bmpData.Stride + dx * pixelSize];
//            ptr[1] = source[dy * bmpData.Stride + dx * pixelSize + 1];
//            ptr[2] = source[dy * bmpData.Stride + dx * pixelSize + 2];
//            ptr[3] = source[dy * bmpData.Stride + dx * pixelSize + 3];
//
//        }
//    }
//    return color;
//}



void main(){
    vec4 color1 = getRadiusColor(uNormalTexture,uProgress,zoomTime * 2.0f);
    if(uProgress <= 0.25f){
        gl_FragColor = color1;
    }else{
        vec4 color2 = getRadiusColor(uBlurTexture,(1.0 - uProgress) * 2.0f,1.0 / zoomTime);
        float alpha = (uProgress - 0.75) * 2.0f;
        alpha = clamp(alpha,0.0,1.0);
        gl_FragColor = mix(color1,color2,alpha);
    }
//    if(uProgress <= 0.5f){
//        vec4 color1 = getRadiusColor(uNormalTexture,uProgress * 2.0f,zoomTime);
//        gl_FragColor = color1;
//    }else{
//           gl_FragColor = getRadiusColor(uBlurTexture,(1.0 - uProgress) * 2.0f,1.0 /zoomTime);
//    }

}





