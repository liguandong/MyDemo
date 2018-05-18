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

varying vec2 uv;
uniform float uProgress ;
uniform float uSampleStrength ;
const float max_sampleDist = 1.0;
const float max_sampleStrength = 2.2;

vec4 DifferenceBlend(vec4 secondSourceColor, vec4 sourceColor, float opacity)
{
    if(sourceColor.a > 0.0){

        sourceColor.rgb = clamp(sourceColor.rgb / sourceColor.a, 0.0, 1.0);
        highp vec3 result = abs(sourceColor.rgb - secondSourceColor.rgb);

        result = clamp(result, 0.0, 1.0);

        return vec4(mix(secondSourceColor.rgb, result, sourceColor.a * opacity), 1.0);
    }else{
 		return secondSourceColor;
    }

}


vec4 getBlurColor(vec2 coordinate,sampler2D texture,float sampleDist)
{
//    vec4 color = texture2D(texture,coordinate);
//    vec4 sum = color;
//    const int size = 10;
//    float samples[size];
//    samples[0] = -0.05;
//    samples[1] = -0.04;
//    samples[2] = -0.03;
//    samples[3] = -0.02;
//    samples[4] = -0.01;
//    samples[5] =  0.01;
//    samples[6] =  0.02;
//    samples[7] =  0.03;
//    samples[8] =  0.04;
//    samples[9] =  0.05;
//    for (int i = 0; i < size; i++){
//        sum += texture2D( texture, vec2(coordinate.x + samples[i] * sampleDist/3.0,coordinate.y));
//    }
//    sum *= 1.0/(float(size)+1.0);
//    return sum;

//    vec4 color = texture2D(texture,coordinate);
//    vec4 sum = color;
//    const int size = 9;
//    float samples[size];
//    samples[0] = -0.04;
//    samples[1] = -0.03;
//    samples[2] = -0.02;
//    samples[3] = -0.01;
//    samples[4] = 0.0;
//    samples[5] =  0.01;
//    samples[7] =  0.02;
//    samples[7] =  0.03;
//    samples[8] =  0.04;
//
//    float kernel[size];
//    kernel[0] = 1.0;
//    kernel[1] = 1.0;
//    kernel[2] = 2.0;
//    kernel[3] = 2.0;
//    kernel[4] = 4.0;
//    kernel[5] =  2.0;
//    kernel[7] =  2.0;
//    kernel[7] =  1.0;
//    kernel[8] =  1.0;
//    for (int i = 0; i < size; i++){
//        vec4 currentColor = texture2D( texture, vec2(coordinate.x + samples[i] * sampleDist,coordinate.y));
//        sum += currentColor*kernel[i];
//    }
//    sum *= 1.0/16.0;
//    return sum;

 vec4 color = texture2D(texture,coordinate);
    vec4 sum = color;
    const int size = 9;
    float samples[size];
    samples[0] = 0.0;
    samples[1] = 0.01;
    samples[2] = 0.02;
    samples[3] = 0.03;
    samples[4] = 0.04;
    samples[5] =  0.05;
    samples[7] =  0.06;
    samples[7] =  0.07;
    samples[8] =  0.08;

    float kernel[size];
//    kernel[0] = 4.0;
//    kernel[1] = 4.0;
//    kernel[2] = 2.0;
//    kernel[3] = 2.0;
//    kernel[4] = 2.0;
//    kernel[5] =  2.0;
//    kernel[7] =  2.0;
//    kernel[7] =  1.0;
//    kernel[8] =  1.0;
    kernel[0] = 1.0;
    kernel[1] = 1.0;
    kernel[2] = 1.0;
    kernel[3] = 1.0;
    kernel[4] = 1.0;
    kernel[5] =  1.0;
    kernel[7] =  2.0;
    kernel[7] =  1.0;
    kernel[8] =  1.0;
    for (int i = 0; i < size; i++){
        vec4 currentColor = texture2D( texture, vec2(coordinate.x + samples[i] * sampleDist,coordinate.y));
        sum += currentColor*kernel[i];
    }
    sum *= 1.0/16.0;
    return sum;
}

void main(){
//    float sampleDist = uProgress * max_sampleDist;
//    float sampleStrength = uSampleStrength * max_sampleStrength;
//
//    float samples[5];
//    samples[0] = -0.01;
//    samples[1] = -0.02;
//    samples[2] = -0.03;
//    samples[3] = -0.02;
//    samples[4] = -0.01;
//    samples[5] =  0.01;
//    samples[6] =  0.02;
//    samples[7] =  0.03;
//    samples[8] =  0.05;
//    samples[9] =  0.08;
//    vec2 dir = 0.5 - uv;
//    vec4 color = texture2D(uNormalTexture,uv);
//    vec4 sum = color;
//    //距离中心点越远，取样间隔越大
//    for (int i = 0; i < 3; i++){
//        sum += texture2D( uNormalTexture, vec2(uv.x + samples[i] * sampleDist,uv.y));
//    }
//    sum *= 1.0/4.0;
    //取样强度，距离中心点越远，模糊越大
//    float t = dist * sampleStrength;
//    t = clamp( t ,0.0,1.0);
//    gl_FragColor = mix( color, sum, t );
//    gl_FragColor = sum;


//    vec2 MotionVector = texture(gMotionTexture, TexCoord0).xy / 2.0;
//    vec2 MotionVector = vec2(0.05f * uProgress ,0);
//        vec4 Color = vec4(0.0);
//        vec2 TexCoord = vCoordinate;
//        Color += texture2D(uNormalTexture, TexCoord) * 0.4;
//        TexCoord -= MotionVector;
//        Color += texture2D(uNormalTexture, TexCoord) * 0.3;
//        TexCoord -= MotionVector;
//        Color += texture2D(uNormalTexture, TexCoord) * 0.2;
//        TexCoord -= MotionVector;
//        Color += texture2D(uNormalTexture, TexCoord) * 0.1;
//        gl_FragColor = Color;

//    vec2 reverse = vec2 - vCoordinate;
//    reverse +=
    // 画面分成3段, 画面向右移动，
    // (1)第一画面1纹理逐渐模糊， (2)第二画面 1纹理逐渐模糊和渐隐 ，2 纹理逐渐模糊和渐显 (3)  2 纹理逐渐清晰

    // 时间轴分成两个， 第一个画面向第二画面过度， 第二画面向第三画面过度

    //    //平移的方向
//    if(uProgress <= 0.5){
//        float progress = uProgress / 0.5f; // 归一化处理 范围 0  - 1
//        //第一画面正面和镜面逐渐模糊
//        vec2 coord = vec2(abs(progress - vCoordinate.x),vCoordinate.y);
//        float blur = clamp(progress * 2.0 ,0.0,1.0); //快速模糊
//        vec4 color = getBlurColor(coord,uNormalTexture,blur);
////        if(progress >= 0.5f && vCoordinate.x <= progress){
//        if(progress >= 0.5f ){
//             //第二面画面模糊混合
//            float alpha = (progress-0.5f);
//             vec4 secondColor = getBlurColor(coord,uBlurTexture,1.0);
//            color = mix(color,secondColor,alpha);
//        }
//        gl_FragColor = color;
//    }else{
//        float progress = (uProgress - 0.5f) /0.5f ; // 归一化处理 范围 0  - 1
//        //第二画面正面和镜面逐渐清晰
//        vec2 coord = vec2((1.0 - abs(progress - vCoordinate.x)),vCoordinate.y);
//        float blur = clamp((1.0 - progress) * 2.0 ,0.0,1.0); //快速模糊
//        vec4 color = getBlurColor(coord,uBlurTexture,blur);
//         if(progress <= 0.5f){
//            //和第一面画面模糊混合
//            float alpha = (0.5 - progress) ;
//           vec4 secondColor = getBlurColor(coord,uNormalTexture,1.0);
//            color = mix(color,secondColor,alpha);
//         }
//        gl_FragColor = color;
//    }

//    float progress = uProgress;
//    vec2 coord1 = vec2(abs(progress - vCoordinate.x),vCoordinate.y);
//    vec2 coord2 = vec2((1.0 - abs(progress - vCoordinate.x)),vCoordinate.y);
//    vec4 color1 = getBlurColor(coord,uNormalTexture,progress);
//

    float p = uProgress;
    if(p <= 0.5){
        float progress = uProgress / 0.5f; // 归一化处理 范围 0  - 1
        //第一画面正面和镜面逐渐模糊
        vec2 coord = vec2(abs(progress - vCoordinate.x),vCoordinate.y);
        float blur = clamp(progress * 2.0 ,0.0,1.0); //快速模糊
        vec4 color = getBlurColor(coord,uNormalTexture,blur);
//        if(progress >= 0.5f && vCoordinate.x <= progress){
        if(progress >= 0.5f ){
             //第二面画面模糊混合
            float alpha = (progress-0.5f);
             vec4 secondColor = getBlurColor(coord,uBlurTexture,1.0);
            color = mix(color,secondColor,alpha);
        }
        gl_FragColor = color;
    }else{
        float progress = (uProgress - 0.5f) /0.5f ; // 归一化处理 范围 0  - 1
        //第二画面正面和镜面逐渐清晰
        vec2 coord = vec2((1.0 - abs(progress - vCoordinate.x)),vCoordinate.y);
        float blur = clamp((1.0 - progress) * 2.0 ,0.0,1.0); //快速模糊
        vec4 color = getBlurColor(coord,uBlurTexture,blur);
//         if(progress <= 0.5f){
            //和第一面画面模糊混合
            float alpha = (0.5 - progress) ;
           vec4 secondColor = getBlurColor(coord,uNormalTexture,1.0);
            color = mix(color,secondColor,alpha);
//         }
        gl_FragColor = color;
    }


//    if(uProgress <= 0.25f){
//        //第一画面正面和镜面逐渐模糊
//        float progress = uProgress / 0.5;
//        float x = abs(progress - vCoordinate.x);
//        gl_FragColor = texture2D( , vec2(x ,vCoordinate.y));
//    }else if(uProgress >=0.75f){
//        //第二画面正面和镜面逐渐清晰
//        float progress = (uProgress - 0.75) / 0.25f;
//        // 2 纹理渐清晰， 向右平移
//        gl_FragColor = texture2D(uBlurTexture, vec2(1.0 + vCoordinate.x - progress ,vCoordinate.y));
//    }else{
//        //第一画面和第二画面 镜面 透明度交替
//         float progress = (uProgress - 0.25) / 0.5;
//         vec4 blur1
//         if(uProgress <= 0.5f){
//            //第一画面模糊右移
//            float progress = uProgress - 0.25f/ 0.25;
//            float x = abs(progress - vCoordinate.x);
//            gl_FragColor = texture2D(uNormalTexture, vec2(x ,vCoordinate.y));
//         }else{
//            //第二画面模糊左移
//            progress = uProgress-0.5 / 0.25;
//
//         }
//    }

//    //平移的方向
//    if(uProgress <= 0.5f){
//        //第一段时间轴处理 0 到 1
//        float progress = uProgress / 0.5;
//        if(vCoordinate .x < progress){
//            //1纹理反转渐隐模糊，2 纹理反转渐显模糊， 向右平移
//            //progress - vCoordinate.x 代表镜像取样
//            vec4 v1 = texture2D(uNormalTexture, vec2(progress - vCoordinate.x,vCoordinate.y));
//            vec4 v2 = texture2D(uBlurTexture, vec2(progress - vCoordinate.x,vCoordinate.y));
//            gl_FragColor = mix(v1, v2, progress/2.0);
////            gl_FragColor = texture2D(uBlurTexture, vec2(progress - vCoordinate.x,vCoordinate.y));
//        }else{
//            //1 纹理向右平移和模糊
//            gl_FragColor = texture2D(uNormalTexture, vec2(vCoordinate.x - progress ,vCoordinate.y));
//        }
//    }else{
//        //第二段时间轴处理 0 到 1
//        float progress = (uProgress - 0.5) / 0.5;
//        if(vCoordinate .x < progress){
//            // 2 纹理渐清晰， 向右平移
//            gl_FragColor = texture2D(uBlurTexture, vec2(1.0 + vCoordinate.x - progress ,vCoordinate.y));
////            gl_FragColor = texture2D(uBlurTexture, vec2(progress - vCoordinate.x,vCoordinate.y));
//        }else{
//            // 2 纹理反转渐清晰， 向右平移
//            vec4 v1 = texture2D(uNormalTexture, vec2(1.0 - vCoordinate.x + progress,vCoordinate.y));
//            vec4 v2 = texture2D(uBlurTexture, vec2(1.0 - vCoordinate.x + progress,vCoordinate.y));
//            gl_FragColor = mix(v1,v2, 0.5f + progress/2.0);
//
//        }
//    }


//    vec2 reverse = vec2(1.0,0)-vCoordinate;
//    if(vCoordinate .x < uProgress){
//        gl_FragColor = texture2D(uNormalTexture, vec2(uProgress - vCoordinate.x,vCoordinate.y));
//    }else{
//        gl_FragColor = texture2D(uNormalTexture, vec2(vCoordinate.x - uProgress ,vCoordinate.y));
//    }
//    if(vCoordinate .x <= 1.0/3.0){
//
//    }else if(vCoordinate.x > 1.0/3.0 && vCoordinate.x < 2.0/3.0){
//
//    }else{
//
//    }


}





