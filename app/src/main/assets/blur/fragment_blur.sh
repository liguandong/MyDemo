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



void main(){
    vec4 nNormalColor = texture2D(uNormalTexture,vCoordinate);
    vec4 nBlurColor;
    float dis = distance(vec2(vPosition.x,vPosition.y / uXY),vec2(uCenter.x,uCenter.y /uXY));
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

}





