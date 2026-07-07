package com.elmangusto.carrental.mapper;

import com.elmangusto.carrental.dto.request.CarRequest;
import com.elmangusto.carrental.dto.request.UserAuthRequest;
import com.elmangusto.carrental.dto.response.CarResponse;
import com.elmangusto.carrental.dto.response.UserResponse;
import com.elmangusto.carrental.entity.Car;
import com.elmangusto.carrental.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(UserAuthRequest request);

    UserResponse toResponse(User user);
}
