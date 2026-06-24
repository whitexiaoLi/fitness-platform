package com.fitness.tool;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.entity.Course;
import com.fitness.enums.CourseCategory;
import com.fitness.enums.CourseStatus;
import com.fitness.enums.DifficultyLevel;
import com.fitness.mapper.CourseMapper;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
public class CourseTool {

    @Autowired
    private CourseMapper courseMapper;

    @Tool("搜索课程库，按关键词、分类、难度筛选")
    public List<Course> queryCourses(String keyword, String category, String difficulty) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, CourseStatus.APPROVED);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Course::getTitle, keyword);
        }
        if (StringUtils.hasText(category)) {
            try {
                wrapper.eq(Course::getCategory, CourseCategory.valueOf(category));
            } catch (IllegalArgumentException ignored) {}
        }
        if (StringUtils.hasText(difficulty)) {
            try {
                wrapper.eq(Course::getDifficulty, DifficultyLevel.valueOf(difficulty));
            } catch (IllegalArgumentException ignored) {}
        }
        wrapper.orderByDesc(Course::getCreateTime);
        wrapper.last("LIMIT 10");
        return courseMapper.selectList(wrapper);
    }
}
