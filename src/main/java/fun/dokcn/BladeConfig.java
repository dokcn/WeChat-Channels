package fun.dokcn;

import com.hellokaton.blade.Blade;
import com.hellokaton.blade.ioc.annotation.Bean;
import com.hellokaton.blade.loader.BladeLoader;
import com.hellokaton.blade.template.FreeMarkerTemplateEngine;

@Bean
public class BladeConfig implements BladeLoader {

    @Override
    public void load(Blade blade) {
        blade.templateEngine(new FreeMarkerTemplateEngine());
    }

}
