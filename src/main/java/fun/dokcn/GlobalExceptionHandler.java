package fun.dokcn;

import com.hellokaton.blade.ioc.annotation.Bean;
import com.hellokaton.blade.mvc.handler.DefaultExceptionHandler;
import com.hellokaton.blade.mvc.http.Request;
import com.hellokaton.blade.mvc.http.Response;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Bean
public class GlobalExceptionHandler extends DefaultExceptionHandler {

    @Override
    protected void handleException(Exception e, Request request, Response response) {

        String message = URLEncoder.encode(e.toString(), StandardCharsets.UTF_8);
        response.redirect("/?exception=%s".formatted(message));

    }

}
