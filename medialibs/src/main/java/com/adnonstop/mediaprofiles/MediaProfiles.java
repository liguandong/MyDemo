package com.adnonstop.mediaprofiles;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by zwq on 2018/08/02 20:23.<br/><br/>
 * 读取系统Media Profiles信息
 */
public class MediaProfiles extends DefaultHandler {
    public static class VideoEncoderCap {
        public String name;
        public boolean enabled;
        public int minBitRate;
        public int maxBitRate;
        public int minFrameWidth;
        public int maxFrameWidth;
        public int minFrameHeight;
        public int maxFrameHeight;
        public int minFrameRate;
        public int maxFrameRate;
        public int maxHFRFrameWidth;
        public int maxHFRFrameHeight;
        public int maxHFRMode;

        @Override
        public String toString() {
            return "{" +
                    "name='" + name + '\'' +
                    ", enabled=" + enabled +
                    ", minBitRate=" + minBitRate +
                    ", maxBitRate=" + maxBitRate +
                    ", minFrameWidth=" + minFrameWidth +
                    ", minFrameHeight=" + minFrameHeight +
                    ", maxFrameWidth=" + maxFrameWidth +
                    ", maxFrameHeight=" + maxFrameHeight +
                    ", minFrameRate=" + minFrameRate +
                    ", maxFrameRate=" + maxFrameRate +
                    ", maxHFRFrameWidth=" + maxHFRFrameWidth +
                    ", maxHFRFrameHeight=" + maxHFRFrameHeight +
                    ", maxHFRMode=" + maxHFRMode +
                    '}';
        }
    }

    public static class AudioEncoderCap {
        public String name;
        public boolean enabled;
        public int minBitRate;
        public int maxBitRate;
        public int minSampleRate;
        public int maxSampleRate;
        public int minChannels;
        public int maxChannels;

        @Override
        public String toString() {
            return "{" +
                    "name='" + name + '\'' +
                    ", enabled=" + enabled +
                    ", minBitRate=" + minBitRate +
                    ", maxBitRate=" + maxBitRate +
                    ", minSampleRate=" + minSampleRate +
                    ", maxSampleRate=" + maxSampleRate +
                    ", minChannels=" + minChannels +
                    ", maxChannels=" + maxChannels +
                    '}';
        }
    }

    private VideoEncoderCap mVideoEncoderCap;
    private AudioEncoderCap mAudioEncoderCap;

    public VideoEncoderCap getVideoEncoderCap() {
        return mVideoEncoderCap;
    }

    public AudioEncoderCap getAudioEncoderCap() {
        return mAudioEncoderCap;
    }

    @Override
    public String toString() {
        return "MediaProfiles{" +
                "mVideoEncoderCap=" + mVideoEncoderCap +
                ", mAudioEncoderCap=" + mAudioEncoderCap +
                '}';
    }

    public void readMediaProfiles() throws IOException, SAXException {
        // 读取media_profiles信息
        Process pp = Runtime.getRuntime().exec("cat /system/etc/media_profiles.xml");
        InputStream is = null;
        if (pp != null) {
            is = pp.getInputStream();
        }
        readMediaProfiles(is);
    }

    protected void readMediaProfiles(InputStream is) throws IOException, SAXException {
        if (is == null) {
            return;
        }

        SAXParserFactory spf = SAXParserFactory.newInstance();
        //初始化Sax解析器
        SAXParser sp = null;
        try {
            sp = spf.newSAXParser();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        if (sp != null) {
            sp.parse(is, this);
            is.close();
        }
    }

    //################# parse xml ##################

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
    }
    /*
 <VideoEncoderCap name="h264" enabled="true"
        minBitRate="64000" maxBitRate="100000000"
        minFrameWidth="176" maxFrameWidth="3840"
        minFrameHeight="144" maxFrameHeight="2160"
        minFrameRate="15" maxFrameRate="30"
        maxHFRFrameWidth="1920" maxHFRFrameHeight="1080"
        maxHFRMode="60"  />

 <AudioEncoderCap name="aac" enabled="true"
        minBitRate="8000" maxBitRate="128000"
        minSampleRate="8000" maxSampleRate="48000"
        minChannels="1" maxChannels="6" />
     */

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String name = attributes.getValue("name");
        if ("VideoEncoderCap".equals(qName) && "h264".equals(name)) {
            try {
                mVideoEncoderCap = new MediaProfiles.VideoEncoderCap();
                mVideoEncoderCap.name = name;
                mVideoEncoderCap.enabled = Boolean.parseBoolean(attributes.getValue("enabled"));
                mVideoEncoderCap.minBitRate = Integer.parseInt(attributes.getValue("minBitRate"));
                mVideoEncoderCap.maxBitRate = Integer.parseInt(attributes.getValue("maxBitRate"));
                mVideoEncoderCap.minFrameWidth = Integer.parseInt(attributes.getValue("minFrameWidth"));
                mVideoEncoderCap.maxFrameWidth = Integer.parseInt(attributes.getValue("maxFrameWidth"));
                mVideoEncoderCap.minFrameHeight = Integer.parseInt(attributes.getValue("minFrameHeight"));
                mVideoEncoderCap.maxFrameHeight = Integer.parseInt(attributes.getValue("maxFrameHeight"));
                mVideoEncoderCap.minFrameRate = Integer.parseInt(attributes.getValue("minFrameRate"));
                mVideoEncoderCap.maxFrameRate = Integer.parseInt(attributes.getValue("maxFrameRate"));
                mVideoEncoderCap.maxHFRFrameWidth = Integer.parseInt(attributes.getValue("maxHFRFrameWidth"));
                mVideoEncoderCap.maxHFRFrameHeight = Integer.parseInt(attributes.getValue("maxHFRFrameHeight"));
                mVideoEncoderCap.maxHFRMode = Integer.parseInt(attributes.getValue("maxHFRMode"));
            } catch (RuntimeException e) {
                e.printStackTrace();
            }

        } else if ("AudioEncoderCap".equals(qName) && "aac".equals(name)) {
            try {
                mAudioEncoderCap = new MediaProfiles.AudioEncoderCap();
                mAudioEncoderCap.name = name;
                mAudioEncoderCap.enabled = Boolean.parseBoolean(attributes.getValue("enabled"));
                mAudioEncoderCap.minBitRate = Integer.parseInt(attributes.getValue("minBitRate"));
                mAudioEncoderCap.maxBitRate = Integer.parseInt(attributes.getValue("maxBitRate"));
                mAudioEncoderCap.minSampleRate = Integer.parseInt(attributes.getValue("minSampleRate"));
                mAudioEncoderCap.maxSampleRate = Integer.parseInt(attributes.getValue("maxSampleRate"));
                mAudioEncoderCap.minChannels = Integer.parseInt(attributes.getValue("minChannels"));
                mAudioEncoderCap.maxChannels = Integer.parseInt(attributes.getValue("maxChannels"));
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }
        super.startElement(uri, localName, qName, attributes);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        super.endElement(uri, localName, qName);
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
    }
}
