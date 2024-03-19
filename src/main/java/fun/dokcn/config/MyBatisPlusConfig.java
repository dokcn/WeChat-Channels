package fun.dokcn.config;

import com.baomidou.mybatisplus.core.MybatisSqlSessionFactoryBuilder;
import com.hellokaton.blade.ioc.annotation.Bean;
import com.hellokaton.blade.ioc.annotation.Configuration;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;

import java.io.InputStream;

@Configuration
public class MyBatisPlusConfig {

    @Bean
    public SqlSessionFactory sqlSessionFactory() throws Exception {
        InputStream config = Resources.getResourceAsStream("mybatis.cfg.xml");
        SqlSessionFactory sqlSessionFactory = new MybatisSqlSessionFactoryBuilder()
                .build(config);
        return sqlSessionFactory;
    }

}
