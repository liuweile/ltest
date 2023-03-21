package com.alxad.api;

public class AlxAdError {

    //异常错误 try-catch异常错误
    public static final int ERR_EXCEPTION = 1018;

    // 请求网络错误
    public static final int ERR_NETWORK = 1101;
    // 没填充错误
    public static final int ERR_NO_FILL = 1102;
    // 解析错误
    public static final int ERR_PARSE_AD = 1103;
    // 服务器错误
    public static final int ERR_SERVER = 1104;
    // 未知错误
    public static final int ERR_UNKNOWN = 1105;
    // vast 解析错误
    public static final int ERR_VAST_ERROR = 1106;
    // 播放视频错误，请检查网络是否打开或者可用
    public static final int ERR_VIDEO_PLAY_FAIL = 1107;
    //视频下载失败
    public static final int ERR_VIDEO_DOWNLOAD = 1108;
    //视频格式暂不支持
    public static final int ERR_VIDEO_TYPE_NO_SUPPORT = 1109;
    //视频地址为空
    public static final int ERR_VIDEO_URL_EMPTY = 1110;
    //参数错误
    public static final int ERR_PARAMS_ERROR = 1111;
    //渲染失败
    public static final int ERR_RENDER_ERROR = 1112;
    //sdk未初始化
    public static final int ERR_SDK_NO_INIT = 1113;
    //广告加载超时
    public static final int ERR_LOAD_TIMEOUT = 1114;
    //广告正在加载中，请勿重复加载
    public static final int ERR_REPEATED_LOADING_ERROR = 1115;
    //返回对象为空
    public static final int ERR_RESPONSE_EMPTY_OBJECT = 1116;


    public static final String MSG_SDK_NO_INIT = "sdk is not initialized";

    //广告数据格式返回错误
    public static final String MSG_AD_DATA_FORMAT_ERROR = "data in wrong format";//数据格式错误

    public static final String ERROR_MSG = "请查看SDK文档查询错误信息或者联系对接人员!";

}