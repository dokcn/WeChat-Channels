package fun.dokcn;

import java.time.LocalTime;

public interface Constants {

    String COOKIES_FILE_PATH = "g:/cookies.txt";
    String COOKIE_PARTS_DELIMITER = ":";

    String QR_CODE_FILE_PATH = "g:/qrcode.png";

    // urls
    String STREAMING_HOME_URL = "https://channels.weixin.qq.com/platform/live/home";

    String LOGIN_URL = "https://channels.weixin.qq.com/login.html";
    String HOMEPAGE_URL = STREAMING_HOME_URL;

    // system property names
    String BROWSER_BINARY_LOCATION_PROPERTY_NAME = "browserBinaryLocation";
    String DRIVER_BINARY_LOCATION_PROPERTY_NAME = "driverBinaryLocation";
    String HEADLESS_PROPERTY_NAME = "headless";
    String WEB_DRIVER_LOG_LEVEL_PROPERTY_NAME = "webDriverLogLevel";

    // scheduling related
    LocalTime CLOSE_STREAMING_TRIGGER_TIME = LocalTime.of(22, 30, 0);

    String CLOSE_STREAMING_JOB_KEY = "closeStreamingJobDetail";
    String CLOSE_STREAMING_TRIGGER_KEY = "closeStreamingTrigger";
    String CLOSE_STREAMING_ONCE_TRIGGER_KEY = "closeStreamingOnceTrigger";

}
