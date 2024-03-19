package fun.dokcn.util;

import com.baomidou.mybatisplus.core.mapper.Mapper;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.util.function.Function;

public class DbUtil {

    public static <T extends Mapper<?>, R> R dbAction(SqlSessionFactory sqlSessionFactory,
                                                      Class<T> clazz, Function<T, R> action) {
        try (SqlSession sqlSession = sqlSessionFactory.openSession(true)) {
            T mapper = sqlSession.getMapper(clazz);
            return action.apply(mapper);
        }
    }

}
