precision mediump float;
varying vec2 vCoordinate;
varying vec4 vPosition;
uniform sampler2D uNormalTexture;
uniform sampler2D uBlurTexture;
//varying vec2 uv;
uniform float uProgress ;

void main(){
    float width = 0.3f;
    //左边的条带
    //范围 - 1 - width  到 0 - width 到 -1- width
    float leftMin = - abs((uProgress - 0.5f) * 2.0) - width;
    //范围 -1 到 0 到 0;
    float leftMax = clamp(abs(uProgress * 2.0 ) -1.0,-1.0,0.0);

    //右边的条带
    //范围 1 到 0 到 0
    float rightmin = clamp( - uProgress * 2.0  + 1.0,0.0,1.0);
    //范围 1+width  到 width 到 1
    float rightMax = abs(0.5f - uProgress) * 2.0 + width;

    //在范围内显示第二个纹理
    if((vPosition.x >= leftMin && vPosition.x <= leftMax) || (vPosition.x >= rightmin && vPosition.x <= rightMax)){
        //两个彩条合并期间进度，显示中间两边的固定纹理
        //范围 0.5 到 0 到 0
        float offset = clamp((0.5f - uProgress),0.0,0.5);
        gl_FragColor = texture2D(uBlurTexture,vec2(abs(vCoordinate.x - 0.5f) + 0.5f - offset,vCoordinate.y));
//        gl_FragColor = texture2D(uBlurTexture,vCoordinate);
    }else{
        gl_FragColor = texture2D(uNormalTexture,vCoordinate);
    }
}





