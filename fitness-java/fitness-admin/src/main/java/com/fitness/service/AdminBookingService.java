package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.UserCourse;

public interface AdminBookingService {
    Page<UserCourse> listBookings(int page, int size);
}
