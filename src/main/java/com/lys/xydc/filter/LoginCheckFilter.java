package com.lys.xydc.filter;

import com.alibaba.fastjson.JSON;
import com.lys.xydc.common.BaseContext;
import com.lys.xydc.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 陆玉升
 * date: 2023/04/2505/11
 * Description:
 * 检测用户是否已经完成登录
 */
@WebFilter(filterName = "loginCheckFilter", urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

  // 路径匹配器,支持通配符
  private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;

    // 1.获取本次请求的URI
    String requestURI = request.getRequestURI();

    log.info("拦截到请求：{}", requestURI);

    // 定义不需要处理的请求路径
    String[] urls = new String[]{
            "/employee/login",
            "/employee/logout",
            "/backend/**",
            "/front/**",
            "/common/**",
            "/user/login", //移动端登录
            "/user/sendMsg" // 移动端发送短信
    };
    // 2.判断本次请求是否需要处理
    boolean check = check(urls,requestURI);

    // 3.如果不需要处理，则直接放行
    if (check) {
      log.info("本次请求不需要处理:{}", requestURI);
      filterChain.doFilter(request, response);
      return;
    }

    // 4-1.判断登录状态，如果已登录，则直接放行
    if (request.getSession().getAttribute("employee") != null) {
      Long empId = (Long) request.getSession().getAttribute("employee");
      log.info("用户已登录,用户id为:{}", empId);

      // 存放用户id到当前线程 方便字段填充
      BaseContext.setCurrentId(empId);

      filterChain.doFilter(request, response);
      return;
    }

    // 4-2.判断移动端登录状态，如果已登录，则直接放行
    if (request.getSession().getAttribute("user") != null) {
      Long userId = (Long) request.getSession().getAttribute("user");
      log.info("用户已登录,用户id为:{}", userId);

      // 存放用户id到当前线程 方便字段填充
      BaseContext.setCurrentId(userId);

      filterChain.doFilter(request, response);
      return;
    }
    log.info("用户未登录");
    // 5.如果未登录则返回未登录结果
    response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    return;
  }

  public boolean check(String[] urls, String requestURI) {
    for (String url : urls) {
      boolean match = ANT_PATH_MATCHER.match(url, requestURI);
      if (match) return true;
    }
    return false;
  }
}
