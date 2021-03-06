package com.travelassistant.controller.portal;

import com.travelassistant.common.Const;
import com.travelassistant.common.ResponseCode;
import com.travelassistant.common.ServerResponse;
import com.travelassistant.pojo.BrowsingHistory;
import com.travelassistant.pojo.User;
import com.travelassistant.common.UserWithToken;
import com.travelassistant.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;

    /**
     * 用户登录
     * @param phone
     * @param password
     * @param session
     * @return
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<UserWithToken> login(String phone, String password, HttpSession session) {
//        service -> myBatis -> dao
        ServerResponse<UserWithToken> response = iUserService.login(phone,password);
        if(response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getData());
            System.out.println(session.getAttribute(Const.CURRENT_USER));
        }
        return response;
    }

    @RequestMapping(value = "login_with_token.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<UserWithToken> loginWithToken(String token, HttpSession session){
        UserWithToken user = (UserWithToken)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMsg("token用户未登录");
        }
        String sessionToken = user.getToken();
        System.out.println(sessionToken);
        System.out.println(token);
        System.out.println(org.apache.commons.lang3.StringUtils.equals(sessionToken,token));
        if(org.apache.commons.lang3.StringUtils.equals(sessionToken,token)) {
            return ServerResponse.createBySuccess("登录成功",user);
        }
        return ServerResponse.createByErrorMsg("登录失败");
    }

    @RequestMapping(value = "logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session){
        UserWithToken currentUser = (UserWithToken)session.getAttribute(Const.CURRENT_USER);
        System.out.println(session.getAttribute(Const.CURRENT_USER));
        System.out.println(currentUser);
        if(currentUser == null){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccessMsg("注销成功");
    }

    @RequestMapping(value = "register.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user){
        return iUserService.register(user);
    }

    @RequestMapping(value = "check_valid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str,String type){
        return iUserService.checkValid(str,type);
    }

    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<UserWithToken> getUserInfo(HttpSession session){
        UserWithToken user = (UserWithToken) session.getAttribute(Const.CURRENT_USER);
        if(user != null){
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMsg("用户未登录,无法获取当前用户的信息");
    }

    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetRestPassword(String phone,String passwordNew,String forgetToken){
        return iUserService.forgetResetPassword(phone,passwordNew,forgetToken);
    }

    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew){
        UserWithToken user = (UserWithToken)session.getAttribute(Const.CURRENT_USER);
        if(user == null){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        User user1 = new User(user.getId(),user.getPhone(),user.getUsername(),user.getPassword(),user.getRole(),user.getCreateTime(),
                user.getUpdateTime());
        return iUserService.resetPassword(passwordOld,passwordNew,user1);
    }

    @RequestMapping(value = "update_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> update_information(User user,HttpSession session){
        UserWithToken currentUser = (UserWithToken)session.getAttribute(Const.CURRENT_USER);
        System.out.println(session.getAttribute(Const.CURRENT_USER));
        System.out.println(currentUser);
        if(currentUser == null){
            return ServerResponse.createByErrorMsg("用户未登录");
        }
        // 防止越权
        user.setId(currentUser.getId());
        user.setPhone(currentUser.getPhone());
        ServerResponse<User> response = iUserService.updateInformation(user, currentUser.getUsername());
        if(response.isSuccess()){
            response.getData().setPhone(currentUser.getPhone());
            UserWithToken userWithToken = new UserWithToken(response.getData().getId(),response.getData().getPhone(),response.getData().getUsername(),
                    response.getData().getPassword(),response.getData().getRole(),response.getData().getCreateTime(),
                    response.getData().getUpdateTime(),currentUser.getToken());
            session.setAttribute(Const.CURRENT_USER,userWithToken);
        }
        return response;
    }

    @RequestMapping(value = "get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> get_information(HttpSession session){
        UserWithToken currentUser = (UserWithToken)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"未登录,需要强制登录");
        }
        return iUserService.getInformation(currentUser.getId());
    }

    @RequestMapping(value = "add_browse_record.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> addBrowseRecord(BrowsingHistory browsingHistory){
        return iUserService.insertBrowseRecord(browsingHistory);
    }

    @RequestMapping(value = "get_browse_record.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<List<BrowsingHistory>> getBrowseRecord(HttpSession session){
        UserWithToken currentUser = (UserWithToken)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        return iUserService.selectBrowseRecord(currentUser.getId());
    }

    @RequestMapping(value = "delete_browse_record.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> deleteBrowseRecord(HttpSession session){
        UserWithToken currentUser = (UserWithToken)session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null){
            return ServerResponse.createByErrorCodeMsg(ResponseCode.NEED_LOGIN.getCode(),"未登录");
        }
        return iUserService.deleteBrowseRecord(currentUser.getId());
    }

}
