package com.elmangusto.carrental.mapper;

import com.elmangusto.carrental.dto.request.RegisterUserRequest;
import com.elmangusto.carrental.dto.response.UserAdminResponse;
import com.elmangusto.carrental.dto.response.UserSummaryResponse;
import com.elmangusto.carrental.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(RegisterUserRequest request);

    UserAdminResponse toResponse(User user);

    UserSummaryResponse toSummary(User user);
}
