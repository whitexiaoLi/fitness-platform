package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.response.CourseVO;
import com.fitness.entity.Course;
import com.fitness.entity.User;
import com.fitness.entity.UserCourse;
import com.fitness.enums.CourseCategory;
import com.fitness.enums.CourseStatus;
import com.fitness.enums.DifficultyLevel;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.dto.response.CourseVO.CourseExerciseVO;
import com.fitness.entity.CourseExercise;
import com.fitness.mapper.CourseExerciseMapper;
import com.fitness.mapper.CourseMapper;
import com.fitness.mapper.UserCourseMapper;
import com.fitness.mapper.UserMapper;
import com.fitness.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private UserCourseMapper userCourseMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CourseExerciseMapper courseExerciseMapper;

    @Override
    public Page<CourseVO> listCourses(int page, int size, String keyword,
                                       CourseCategory category, DifficultyLevel difficulty) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getStatus, CourseStatus.APPROVED);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Course::getTitle, keyword);
        }
        if (category != null) {
            wrapper.eq(Course::getCategory, category);
        }
        if (difficulty != null) {
            wrapper.eq(Course::getDifficulty, difficulty);
        }
        wrapper.orderByDesc(Course::getCreateTime);

        Page<Course> coursePage = courseMapper.selectPage(new Page<>(page, size), wrapper);
        return (Page<CourseVO>) coursePage.convert(this::toVO);
    }

    @Override
    public CourseVO getCourse(Long id) {
        Course course = courseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }
        CourseVO vo = toVO(course);

        List<CourseExercise> exercises = courseExerciseMapper.selectList(
                new LambdaQueryWrapper<CourseExercise>()
                        .eq(CourseExercise::getCourseId, id)
                        .orderByAsc(CourseExercise::getSortOrder));
        vo.setExercises(exercises.stream().map(e -> {
            CourseExerciseVO evo = new CourseExerciseVO();
            evo.setId(e.getId());
            evo.setTitle(e.getTitle());
            evo.setVideoUrl(e.getVideoUrl());
            evo.setDescription(e.getDescription());
            evo.setSortOrder(e.getSortOrder());
            return evo;
        }).toList());

        return vo;
    }

    @Override
    @Transactional
    public void subscribe(Long userId, Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getStatus() != CourseStatus.APPROVED) {
            throw new BusinessException(ErrorCode.COURSE_NOT_FOUND);
        }

        Long count = userCourseMapper.selectCount(new LambdaQueryWrapper<UserCourse>()
                .eq(UserCourse::getUserId, userId)
                .eq(UserCourse::getCourseId, courseId));
        if (count > 0) {
            throw new BusinessException(ErrorCode.COURSE_ALREADY_SUBSCRIBED);
        }

        UserCourse uc = new UserCourse();
        uc.setUserId(userId);
        uc.setCourseId(courseId);
        uc.setProgress(0);
        uc.setPurchaseTime(LocalDateTime.now());
        userCourseMapper.insert(uc);
    }

    @Override
    public Page<CourseVO> listMyCourses(Long userId, int page, int size) {
        Page<UserCourse> ucPage = userCourseMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<UserCourse>().eq(UserCourse::getUserId, userId)
                        .orderByDesc(UserCourse::getCreateTime));

        return (Page<CourseVO>) ucPage.convert(uc -> {
            Course course = courseMapper.selectById(uc.getCourseId());
            if (course == null) return null;
            CourseVO vo = toVO(course);
            vo.setProgress(uc.getProgress());
            return vo;
        });
    }

    private CourseVO toVO(Course course) {
        CourseVO vo = new CourseVO();
        vo.setId(course.getId());
        vo.setCoachId(course.getCoachId());
        vo.setTitle(course.getTitle());
        vo.setDescription(course.getDescription());
        vo.setCoverUrl(course.getCoverUrl());
        vo.setVideoUrl(course.getVideoUrl());
        vo.setCategory(course.getCategory() != null ? course.getCategory().getDesc() : null);
        vo.setDifficulty(course.getDifficulty() != null ? course.getDifficulty().getDesc() : null);
        vo.setDuration(course.getDuration());
        vo.setPrice(course.getPrice());
        vo.setStatus(course.getStatus() != null ? course.getStatus().getDesc() : null);
        vo.setRating(course.getRating());
        vo.setRatingCount(course.getRatingCount());
        vo.setCreateTime(course.getCreateTime());

        // coach name
        if (course.getCoachId() != null) {
            User coach = userMapper.selectById(course.getCoachId());
            if (coach != null) {
                vo.setCoachName(coach.getNickname());
            }
        }
        return vo;
    }
}
