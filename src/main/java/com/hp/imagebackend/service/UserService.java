package com.hp.imagebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hp.imagebackend.model.dto.user.UserQueryRequest;
import com.hp.imagebackend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hp.imagebackend.model.vo.LoginUserVO;
import com.hp.imagebackend.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author hp
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-09-21 14:28:52
*/
public interface UserService extends IService<User> {

    long userRegister(String userAccount, String userPassword, String checkPassword);

    String getEncryptPassword(String userPassword);
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    LoginUserVO getLoginUserVO(User user);

    User getLoginUser(HttpServletRequest request);

    boolean userLogout(HttpServletRequest request);


    UserVO getUserVO(User user);

    List<UserVO> getUserVOList(List<User> userList);

    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    boolean isAdmin(User user);

}
