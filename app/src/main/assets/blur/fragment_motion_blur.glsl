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
uniform float uSampleDist ;
uniform float uSampleStrength ;
const float max_sampleDist = 1.0;
const float max_sampleStrength = 2.2;
void main(){
    float sampleDist = uSampleDist * max_sampleDist;
    float sampleStrength = uSampleStrength * max_sampleStrength;

    float samples[5];
    samples[0] = -0.01;
    samples[1] = -0.02;
    samples[2] = -0.03;
//    samples[3] = -0.02;
//    samples[4] = -0.01;
//    samples[5] =  0.01;
//    samples[6] =  0.02;
//    samples[7] =  0.03;
//    samples[8] =  0.05;
//    samples[9] =  0.08;
//    vec2 dir = 0.5 - uv;
    vec4 color = texture2D(uNormalTexture,uv);
    vec4 sum = color;
    //距离中心点越远，取样间隔越大
    for (int i = 0; i < 3; i++){
        sum += texture2D( uNormalTexture, vec2(uv.x + samples[i] * sampleDist,uv.y));
    }
    sum *= 1.0/4.0;
    //取样强度，距离中心点越远，模糊越大
//    float t = dist * sampleStrength;
//    t = clamp( t ,0.0,1.0);
//    gl_FragColor = mix( color, sum, t );
    gl_FragColor = sum;

}





