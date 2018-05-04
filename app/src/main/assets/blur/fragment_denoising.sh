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

float compositeAlpha(float foregroundAlpha, float backgroundAlpha) {
    return 1.0f - (1.0f - backgroundAlpha) * (1.0f - foregroundAlpha);
}

float compositeComponent(float fgC, float fgA, float bgC, float bgA, float a) {
    if (a == 0.0) return 0.0;
    return (fgC * fgA) + (bgC * bgA * (1.0f - fgA)) / a;
}

vec4 compositeColors( vec4 foreground,vec4 background) {

    float a = compositeAlpha(foreground.a, background.a);
    float r = compositeComponent(foreground.r, foreground.a,
            background.r, background.a,a);
    float g = compositeComponent(foreground.g, foreground.a,
            background.g, background.a, a);
    float b = compositeComponent(foreground.b, foreground.a,
            background.b, background.a,a);
    return vec4(r, g, b, a);
    //return vec4(foreground.r,foreground.g,foreground.b,foreground.a);
}
/**
void InsertSort(int[] a, int n) {
    int tmp = 0;
    for (int i = 1; i < n; i++) {
        int j = i - 1;
        if (a[i] < a[j]) {
            tmp = a[i];
            a[i] = a[j];
            while (tmp < a[j-1]) {
                a[j] = a[j-1];
                j--;
            }
            a[j] = tmp;
        }
    }
}
**/
//http://www.docin.com/p-907098346.html
//https://www.cnblogs.com/wangguchangqing/p/6379646.html
const int MIN_W = 3;
const int MAX_W = 7;

void main(){
       /**
    //vec4 nNormalColor = texture2D(uNormalTexture,vCoordinate);
    vec3[] colors = new vec3[MIN_W * MIN_W -1 ];
    for (int i = -MIN_W/2 ; i < MIN_W/2 ; i++)
    {
       for (int j = -MIN_W/2 ; j < MIN_W/2 ; j++)
       {
         if(i != 0 && j != 0){
            vec4 color  = texture2D(uNormalTexture,vec2(vCoordinate.x + i * uOffsetX,vCoordinate.y + j * uOffsetY ));
            colors[i+j] = vec3(color.r,color.g,color.b);
          }
       }
    }
    **/

    vec3[] colors = new vec3[MIN_W * MIN_W];
    for (int i = -MIN_W/2 ; i < MIN_W/2 ; i++)
    {
       for (int j = -MIN_W/2 ; j < MIN_W/2 ; j++)
       {
            vec4 color  = texture2D(uNormalTexture,vec2(vCoordinate.x + i * uOffsetX,vCoordinate.y + j * uOffsetY ));
            colors[i+j] = vec3(color.r,color.g,color.b);
       }
    }
    vec4 nNormalColor = vec4(0.0);
    for (int i = 0 ; i < MIN_W * MIN_W ; i++)
    {
        nNormalColor += colors[i];
    }
    //nNormalColor /= MIN_W * MIN_W;

    //vec4 nNormalColor = texture2D(uNormalTexture,vCoordinate);
    vec4 nBlurColor;
    float dis = distance(vec2(vPosition.x,vPosition.y / uXY),vec2(uCenter.x + b,uCenter.y /uXY));
    if(uIsShowMask == 1){
        nBlurColor = vec4(1.0,1.0,1.0,0.8);
         if(abs(dis) < uRadius){
            float rang = uRadius/3.0;
            if(abs(dis) >= (uRadius - rang)){
                float alpha = 1.0 - (uRadius - abs(dis))/rang;
                alpha = clamp(alpha,0.0,1.0f);
                nBlurColor.a = nBlurColor.a  * alpha;
            }else{
                nBlurColor.a = 0.0;
            }
        }
        gl_FragColor = compositeColors(nBlurColor,nNormalColor);
    }else{
        nBlurColor = texture2D(uBlurTexture,vCoordinate);
        if(abs(dis) < uRadius){
            float rang = uRadius/3.0;
            if(abs(dis) >= (uRadius - rang)){
                float alpha = 1.0 - (uRadius - abs(dis))/rang;
                alpha = clamp(alpha,0.0,1.0f);
                nBlurColor.a = nBlurColor.a * uAlpha * alpha;
            }else{
                nBlurColor.a = 0.0;
            }
        }else{
            nBlurColor.a = nBlurColor.a * uAlpha;
        }
        gl_FragColor = compositeColors(nBlurColor,nNormalColor);
    }
    gl_FragColor = nNormalColor;

}





