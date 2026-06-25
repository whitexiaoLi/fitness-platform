package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.GroupClass;

public interface AdminGroupClassService {
    Page<GroupClass> listClasses(int page, int size, String status);
    GroupClass createClass(GroupClass groupClass);
    GroupClass updateClass(GroupClass groupClass);
    void cancelClass(Long id);
}
