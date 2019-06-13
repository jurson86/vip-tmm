package com.tuandai.transaction.config;

import com.alibaba.fastjson.JSON;
import com.tuandai.transaction.utils.BaseException;
import com.tuandai.transaction.utils.Response;
import com.tuandai.transaction.utils.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * guoguo
 */
@ControllerAdvice
public class ExceptionHandlers {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlers.class);

    @ExceptionHandler
    void handleControllerException(HandlerMethod handlerMethod, HttpServletRequest request,
                                   HttpServletResponse response, Throwable ex) throws IOException {
        LOGGER.error("exception url: " + request.getRequestURL().toString());
        LOGGER.error("exception info: ", ex);

        //ajax请求返回json,页面请求返回错误页面
        Method method = handlerMethod.getMethod();
        Annotation responseBodyAnn = AnnotationUtils.findAnnotation(method, ResponseBody.class);
        Annotation restControllerAnn = AnnotationUtils.findAnnotation(method.getDeclaringClass(), RestController.class);
        if(!(responseBodyAnn == null && restControllerAnn == null)){
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter pw = response.getWriter();
            String errorMsg = null;
            //输入验证
            if(ex instanceof BindException){
                BindException e = (BindException) ex;
                BindingResult result = e.getBindingResult();
                if(result.hasErrors()){
                    String errMsg = result.getAllErrors().get(0).getDefaultMessage();
                    if(errMsg.toUpperCase().contains("EXCEPTION")){
                        errorMsg = "请求失败,请检查输入数据是否有误";
                    }else {
                        errorMsg = errMsg;
                    }
                }
            }else if(ex instanceof BaseException){
                errorMsg = ex.getMessage();
            }else if(ex instanceof ServiceException){
                errorMsg = ex.getMessage();
            }else {
                errorMsg = "系统开小差";
            }
            pw.write(JSON.toJSONString(Response.error(errorMsg)));
            pw.close();
        }else{
            response.sendError(getStatus(request).value());
        }

    }

    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return HttpStatus.valueOf(statusCode);
    }

}

