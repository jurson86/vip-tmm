package com.tuandai.transaction.config;

import com.alibaba.fastjson.JSON;
import com.tuandai.transaction.controller.BaseController;
import com.tuandai.transaction.utils.BZStatusCode;
import com.tuandai.transaction.utils.Response;
import com.tuandai.transaction.vo.UserInfo;
import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: guoguo
 * @Date: 2018/6/5 0005 10:18
 * @Description:
 */

//@WebFilter(filterName = "tokenFilter",description = "token过滤器",urlPatterns = {"/*"})
public class TokenFilter implements Filter{

    private List<String> pathArray = new ArrayList<String>();

    private List<String> staticSource = new ArrayList<String>();

    @Override
    public void init(FilterConfig config) throws ServletException {
        String cp = config.getServletContext().getContextPath();
        String ignoresParam = config.getInitParameter("exclusions");
        String[] ignoreArray = ignoresParam.split(",");
        for (String s : ignoreArray) {
            pathArray.add(cp + s);
        }

        String ingoresStaticSource = config.getInitParameter("static");
        String[] ignoreStaticArray = ingoresStaticSource.split(",");
        for (String s : ignoreStaticArray) {
            staticSource.add(cp + s);
        }
    }

    private boolean canIgnore(HttpServletRequest request) {
        String url = request.getRequestURI();
        for (String ignore : pathArray) {
            if (url.endsWith(ignore)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStaticSource(HttpServletRequest request){
        String url = request.getRequestURI();
        for (String ignore : staticSource) {
            if (url.startsWith(ignore)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        if (canIgnore(req) || isStaticSource(req)) {
            chain.doFilter(req, res);
            return;
        }

        String token = req.getHeader("token");
        UserInfo userInfo = StringUtils.isNotBlank(token)?BaseController.currentUserInfo(token):null;
        if(userInfo == null){
            OutputStream out = response.getOutputStream();
            Response result = new Response(BZStatusCode.TOKEN_UNVALID.code(),BZStatusCode.TOKEN_UNVALID.message()) ;
            //response.setContentType("text/html;charset=UTF-8");
            response.setContentType("application/json;charset=UTF-8");
            //response.getWriter().print("你咩有登录,跳转到登录页面");
            out.write(JSON.toJSONString(result).getBytes("UTF-8"));
            out.close();
            out.flush();
            return;
        }

        chain.doFilter(request,response);
    }

    @Override
    public void destroy() {
        pathArray = null;
        staticSource = null;
    }
}
