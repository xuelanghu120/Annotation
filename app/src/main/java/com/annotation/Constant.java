package com.annotation;




import com.example.SDCardRootFile;

import java.io.Serializable;

import static com.annotation.Constant.DISK_DATA;
import static com.annotation.Constant.DISK_DOWNLOAD_PATH;
import static com.annotation.Constant.DISK_IMAGE_PHOTO_PATH;
import static com.annotation.Constant.DISK_MSG_CACHE_PATH;
import static com.annotation.Constant.DISK_MSG_RECORD_VOICE_PATH;
import static com.annotation.Constant.DISK_SOUND_PATH;
import static com.annotation.Constant.DISK_TAKE_PHOTO_PATH;

/**
 * Created by walkingMen on 2016/8/1.
 */
public class Constant implements Serializable {
    //=================SD文件路径===========================================
    public static final String DISK_IMAGE_PHOTO_PATH = "img";

    public static final String DISK_DATA = "data";

    public static final String DISK_TAKE_PHOTO_PATH = "temp";

    public static final String DISK_MSG_CACHE_PATH = "msg";

    public static final String DISK_SOUND_PATH = "sound";

    public static final String DISK_MSG_RECORD_VOICE_PATH = "soundLocal";

    public static final String DISK_DOWNLOAD_PATH = "download";


    //缓存路径
    @SDCardRootFile(fileNames = {DISK_IMAGE_PHOTO_PATH, DISK_DATA, DISK_TAKE_PHOTO_PATH,
            DISK_MSG_CACHE_PATH, DISK_SOUND_PATH, DISK_MSG_RECORD_VOICE_PATH, DISK_DOWNLOAD_PATH})
    public static final String CACHE_ROOT_DIR_NAME = "Super";
}
