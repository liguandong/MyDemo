precision mediump float;
uniform float uXY;
uniform float uProgress ;
uniform sampler2D uNormalTexture;
uniform sampler2D uBlurTexture;
varying vec2 vCoordinate;
varying vec4 vPosition;
const float size = 7.0; //窗口的个数
void main(){
    float offset = 1.0 * 2.0/size;
    //(vPosition.y + 1.0)  转换成正整数运算
    int temp = int((vPosition.y + 1.0) / offset);
    float divisor = float(temp);
    float start = divisor * offset - 1.0;
    float end = start + offset * uProgress;
    if(vPosition.y >= start && vPosition.y <= end){
        gl_FragColor = texture2D(uBlurTexture,vCoordinate);
    }else{
         gl_FragColor = texture2D(uNormalTexture,vCoordinate);
    }
}





