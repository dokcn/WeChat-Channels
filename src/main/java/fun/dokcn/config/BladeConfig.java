package fun.dokcn.config;

import com.hellokaton.blade.Blade;
import com.hellokaton.blade.ioc.annotation.Bean;
import com.hellokaton.blade.loader.BladeLoader;
import com.hellokaton.blade.template.FreeMarkerTemplateEngine;
import freemarker.template.Configuration;
import no.api.freemarker.java8.Java8ObjectWrapper;

@Bean
public class BladeConfig implements BladeLoader {

    @Override
    public void load(Blade blade) {
        FreeMarkerTemplateEngine templateEngine = new FreeMarkerTemplateEngine();
        templateEngine.getConfiguration().setObjectWrapper(new Java8ObjectWrapper(Configuration.VERSION_2_3_28));
        blade.templateEngine(templateEngine);
    }

}
