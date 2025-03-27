package com.mygo.interceptor;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygo.constant.HeaderConstant;
import com.mygo.constant.RedisConstant;
import com.mygo.context.UserContext;
import com.mygo.domain.entity.User;
import com.mygo.utils.JwtTool;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RefreshInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JwtTool jwtTool;

    /**
     *获取请求头中的token,把信息保存在UserContext中,并刷新在redis中的该记录的有效时间.<br>
     * 该拦截器无论怎样都会放行
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        //1.获取请求头中的token
        String token = request.getHeader(HeaderConstant.Admin_Token);
        //如果token为空,放行
        if (StrUtil.isBlank(token)) {
            return true;
        }
        //2.基于token获取用户
        Long id = jwtTool.parseJWT(token);
        //3.基于id在redis中查找用户
        String userinfo = stringRedisTemplate.opsForValue()
                .get(RedisConstant.JWT_KEY + id);
        //如果info为空,放行
        if (StrUtil.isBlank(userinfo)) {
            return true;
        }
        //4.把用户信息保存在UserContext中
        User user = objectMapper.readValue(userinfo, User.class);
        UserContext.saveUser(user);
        //5.刷新redis中该用户的有效时间
        stringRedisTemplate.expire(RedisConstant.JWT_KEY + id,RedisConstant.JWT_EXPIRE,RedisConstant.JWT_EXPIRE_UNIT);
        return true;
    }

    /**
     * 移除UserContext中的信息
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.removeUser();
    }
}
