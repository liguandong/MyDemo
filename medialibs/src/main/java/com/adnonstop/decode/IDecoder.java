package com.adnonstop.decode;

/**
 * Created by: fwc
 * Date: 2018/8/9
 */
public interface IDecoder {

	void prepare(DecodeSurface surface);

	void start();

	void release();
}
