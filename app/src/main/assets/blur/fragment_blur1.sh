precision mediump float;
uniform vec2 uCenter;
uniform float uRadius;
uniform float uAlpha;
uniform float uXY;
varying vec2 vCoordinate;
varying vec4 vPosition;
uniform sampler2D uNormalTexture;
uniform sampler2D uBlurTexture;

varying vec2 textureCoordinate[13];

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
    //vec4 nBlurColor = texture2D(uBlurTexture,vCoordinate);

     vec3 sum = vec3(0.0);

    sum += texture2D(uNormalTexture, textureCoordinate[0]).rgb * 0.018544;
    sum += texture2D(uNormalTexture, textureCoordinate[1]).rgb * 0.034167;
    sum += texture2D(uNormalTexture, textureCoordinate[2]).rgb * 0.056332;
    sum += texture2D(uNormalTexture, textureCoordinate[3]).rgb * 0.083109;
    sum += texture2D(uNormalTexture, textureCoordinate[4]).rgb * 0.109719;
    sum += texture2D(uNormalTexture, textureCoordinate[5]).rgb * 0.129618;
    sum += texture2D(uNormalTexture, textureCoordinate[6]).rgb * 0.137023;
    sum += texture2D(uNormalTexture, textureCoordinate[7]).rgb * 0.129618;
    sum += texture2D(uNormalTexture, textureCoordinate[8]).rgb * 0.109719;
    sum += texture2D(uNormalTexture, textureCoordinate[9]).rgb * 0.083109;
    sum += texture2D(uNormalTexture, textureCoordinate[10]).rgb * 0.056332;
    sum += texture2D(uNormalTexture, textureCoordinate[11]).rgb * 0.034167;
    sum += texture2D(uNormalTexture, textureCoordinate[12]).rgb * 0.018544;

    vec4 nBlurColor = vec4(sum, 1.0);


    //gl_FragColor = ColorBurnBlend(nNormalColor, nBlurColor,0.0f);
     float dis = distance(vec2(vPosition.x,vPosition.y / uXY),vec2(uCenter.x,uCenter.y /uXY));
    if(abs(dis) <= uRadius){
        float rang = uRadius/2.0;
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
    //gl_FragColor = compositeColors(nBlurColor,nNormalColor);
    gl_FragColor = nBlurColor;
}





