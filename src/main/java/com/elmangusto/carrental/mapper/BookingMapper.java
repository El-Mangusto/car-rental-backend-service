package com.elmangusto.carrental.mapper;

import com.elmangusto.carrental.dto.response.BookingResponse;
import com.elmangusto.carrental.entity.Booking;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CarMapper.class})
public interface BookingMapper {

    BookingResponse toResponse(Booking booking);
}
