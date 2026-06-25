package com.fitness.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("member_card")
public class MemberCard extends BaseEntity {
    private String name;
    private Integer durationDays;
    private BigDecimal price;
    private String description;
    private Integer status;
}
