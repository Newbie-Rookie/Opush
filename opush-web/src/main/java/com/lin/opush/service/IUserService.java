package com.lin.opush.service;

import com.lin.opush.domain.User;
import com.lin.opush.dto.UserDTO;
import com.lin.opush.vo.BasicResultVO;

public interface IUserService {

    BasicResultVO sendCode(String phone);

    BasicResultVO login(UserDTO userDTO);

    BasicResultVO logout(String token);
}
