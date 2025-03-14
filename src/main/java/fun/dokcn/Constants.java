package fun.dokcn;

public interface Constants {

    String COOKIES_FILE_PATH = "g:/cookies.txt";
    String COOKIE_PARTS_DELIMITER = "---:::";

    String QR_CODE_FILE_PATH = "g:/qrcode.png";

    // urls
    String HOMEPAGE_URL = "https://channels.weixin.qq.com/platform";
    String STREAMING_HOME_URL = "https://channels.weixin.qq.com/platform/live/home";
    String STREAMING_CONTROL_URL = "https://channels.weixin.qq.com/platform/live/liveBuild";
    String INCOME_INFO_URL = "https://channels.weixin.qq.com/platform/statistic/live?mode=history";

    String LOGIN_URL = "https://channels.weixin.qq.com/login.html";
    String MAIN_URL = STREAMING_HOME_URL;

    // system property names
    String BROWSER_BINARY_LOCATION_PROPERTY_NAME = "browserBinaryLocation";
    String DRIVER_BINARY_LOCATION_PROPERTY_NAME = "driverBinaryLocation";
    String HEADLESS_PROPERTY_NAME = "headless";
    String WEB_DRIVER_LOG_LEVEL_PROPERTY_NAME = "webDriverLogLevel";

    // scheduling related
    String CLOSE_STREAMING_JOB_NAME = "closeStreamingJobDetail";
    String STREAMING_GROUP = "streamingGroup";

}
