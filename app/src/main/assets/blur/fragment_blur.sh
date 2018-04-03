precision mediump float;
uniform vec2 uCenter;
uniform float uRadius;
uniform float uAlpha;
uniform float uXY;
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
    vec4 nBlurColor = texture2D(uBlurTexture,vCoordinate);
    //gl_FragColor = ColorBurnBlend(nNormalColor, nBlurColor,0.0f);
    nBlurColor.a = nBlurColor.a * uAlpha;
    gl_FragColor = compositeColors(nBlurColor,nNormalColor);
    //gl_FragColor = nBlurColor;
}





