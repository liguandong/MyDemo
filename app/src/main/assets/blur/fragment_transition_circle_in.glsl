precision mediump float;
uniform float uXY;
uniform float uProgress ;
uniform sampler2D uNormalTexture;
uniform sampler2D uBlurTexture;
varying vec2 vCoordinate;
varying vec4 vPosition;
void main(){
//    float dis = distance(vec2(vPosition.x,vPosition.y / uXY),vec2(0.0));
//    float radiusMax = sqrt(1.05 + 1.0 /(uXY * uXY)) * uProgress;
//    if(dis <= radiusMax){
//        gl_FragColor = texture2D(uBlurTexture,vCoordinate);
//     }else{
//        gl_FragColor = texture2D(uNormalTexture,vCoordinate);
//     }
    float dis = distance(vec2(vPosition.x,vPosition.y / uXY),vec2(0.0));
    float radiusMax = sqrt(1.05 + 1.0 /(uXY * uXY)) * (1.0-uProgress);
    float edge = 0.02f;
    if(dis >= radiusMax && dis <= radiusMax + edge){
        //这里是边缘模糊
        float alpha = (dis - radiusMax)/edge;
        vec4 color1 = texture2D(uNormalTexture,vCoordinate);
        vec4 color2 = texture2D(uBlurTexture,vCoordinate);
        gl_FragColor = mix(color1,color2,alpha);
    }else if(dis <= radiusMax){
        gl_FragColor = texture2D(uNormalTexture,vCoordinate);
     }else{
        gl_FragColor = texture2D(uBlurTexture,vCoordinate);
     }

}





