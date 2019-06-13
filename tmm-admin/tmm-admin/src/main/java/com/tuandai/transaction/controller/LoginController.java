package com.tuandai.transaction.controller;

import com.tuandai.transaction.service.inf.UserInfoService;
import com.tuandai.transaction.utils.MD5Utils;
import com.tuandai.transaction.utils.Response;
import com.tuandai.transaction.vo.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Author: guoguo
 * @Date: 2018/5/31 0031 16:19
 * @Description:
 */

@Controller
//@RequestMapping("/login")
public class LoginController {

    @Autowired
    private UserInfoService userInfoService;

    /*@RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public Object login(
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "passWord", required = false) String passWord,
            @RequestParam(value = "rememberMe", required = true, defaultValue = "false") boolean rememberMe
    ) {
        Map<String,Object> msg = new HashMap<String,Object>();
        if(StringUtils.isBlank(userName)||StringUtils.isBlank(passWord)){
            return Response.error("请先登陆");
        }
        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken("guoguo", "guoguo");
        token.setRememberMe(rememberMe);
        try {
            subject.login(token);
            msg.put("token", subject.getSession().getId());
            msg.put("userRole",subject.getSession().getAttribute("user"));
            msg.put("msg", "登录成功");
        } catch (IncorrectCredentialsException e) {
            msg.put("msg", "密码错误");
        } catch (LockedAccountException e) {
            msg.put("msg", "登录失败，该用户已被冻结");
        } catch (UnknownAccountException e) {
            msg.put("msg", "该用户不存在");
        } catch (Exception e) {
            e.printStackTrace();
            msg.put("msg", "系统开小差");
        }

        return Response.success(msg);
    }*/



   /* @RequestMapping(value = "/login",method = RequestMethod.GET)
    @ResponseBody
    public Response login() {
       // Subject subject = SecurityUtils.getSubject();
        HashMap<String,Object> msg = new HashMap<String,Object>();
        msg.put("token",null);
        msg.put("userRole",null);
        return Response.success(msg);
    }*/

/*    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public String login() {
        // Subject subject = SecurityUtils.getSubject();
        HashMap<String,Object> msg = new HashMap<String,Object>();
        msg.put("token",null);
        msg.put("userRole",null);
        return "login";
    }*/

   /* @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public Response fail(
            @RequestParam(FormAuthenticationFilter.DEFAULT_USERNAME_PARAM) String username,
            Map<String, Object> map, ServletRequest request) {
        Response msg = parseException(request);
        return msg;
    }*/


    @RequestMapping("/logout")
    @ResponseBody
    public String logout(HttpServletRequest req,HttpServletResponse res) {
        BaseController.removeUserInfo(req.getHeader("token"));
        return "login";
    }

    @RequestMapping("/success")
    @ResponseBody
    public String success() {
        return "login success";
    }


    @RequestMapping("/first")
    @ResponseBody
    public String first() {
        return "first";
    }


    @RequestMapping("/noPermission")
    @ResponseBody
    public String noPermission() {
        return "未授权";
    }

    /*private Response parseException(ServletRequest request) {
        String errorString = (String)request.getAttribute(FormAuthenticationFilter.DEFAULT_ERROR_KEY_ATTRIBUTE_NAME);
        Class<?> error = null;
        try {
            if (errorString != null) {
                error = Class.forName(errorString);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String msg = "其他错误！";
        if (error != null) {
            if (error.equals(UnknownAccountException.class))
                msg = "未知帐号错误！";
            else if (error.equals(IncorrectCredentialsException.class))
                msg = "密码错误！";
            else if (error.equals(AuthenticationException.class))
                msg = "认证失败！";
            else if (error.equals(DisabledAccountException.class))
                msg = "账号被冻结！";
        }

        return Response.error("登录失败，" + msg);
    }
*/

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public Response login(@RequestParam(value = "userName", required = false) String userName,
                          @RequestParam(value = "passWord", required = false) String passWord){


        if(StringUtils.isBlank(userName)||StringUtils.isBlank(passWord)){
            return Response.error("用户名或密码不能为空");
        }
        UserInfo userInfo = userInfoService.findByUsername(userName);
        Map<String, Object> msg = new HashMap<String, Object>();
        if (userInfo == null) {
            return Response.error("没找到帐号");
        }
        if (userInfo.getStatus() == 0) { //账户冻结
            return Response.error("账户冻结");
        }

        String afgerHex = MD5Utils.encryption(passWord,2);
        if (!afgerHex.equals(userInfo.getPassword())) {
            return Response.error("密码错误");
        }

        /*if(rememberMe){
            Cookie cookieName=new Cookie("userName", userName);
            Cookie cookiePassword=new Cookie("passWord", passWord);
            cookieName.setMaxAge(30*24*60*60);   //存活期为一个月 30*24*60*60
            cookieName.setPath("/");
            res.addCookie(cookieName);
            res.addCookie(cookiePassword);
        }*/
        String token = UUID.randomUUID().toString();
        BaseController.setCurrentUserInfo(token,userInfo);
        msg.put("token",token);
        msg.put("user",userInfo);
        return  Response.success(msg);
    }



}
