package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fitness.client.UsdaClient;
import com.fitness.entity.FoodItem;
import com.fitness.mapper.FoodItemMapper;
import com.fitness.service.FoodService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FoodServiceImpl implements FoodService {

    @Autowired
    private FoodItemMapper foodItemMapper;

    @Autowired
    private UsdaClient usdaClient;

    @Override
    public List<FoodItem> searchFoods(String keyword, int pageSize) {
        List<FoodItem> results = new ArrayList<>();

        // 1. Search local DB
        if (StringUtils.hasText(keyword)) {
            LambdaQueryWrapper<FoodItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(FoodItem::getName, keyword)
                    .or().like(FoodItem::getNameEn, keyword)
                    .orderByDesc(FoodItem::getIsCommon)
                    .last("LIMIT " + Math.min(pageSize, 50));
            results.addAll(foodItemMapper.selectList(wrapper));
        }

        // 2. Supplement with USDA if results < pageSize
        if (results.size() < pageSize && usdaClient.isConfigured()) {
            List<UsdaClient.UsdaFoodSummary> usdaResults = usdaClient.searchFoods(keyword, pageSize - results.size());
            for (UsdaClient.UsdaFoodSummary uf : usdaResults) {
                // Check if already cached locally
                Long count = foodItemMapper.selectCount(
                        new LambdaQueryWrapper<FoodItem>().eq(FoodItem::getFdcId, uf.getFdcId()));
                if (count > 0) continue;

                // Cache to local DB
                FoodItem cached = new FoodItem();
                cached.setFdcId(uf.getFdcId());
                cached.setName(uf.getDescription());
                cached.setNameEn(uf.getDescription());
                cached.setCalories(uf.getCalories());
                cached.setProtein(uf.getProtein());
                cached.setCarbs(uf.getCarbs());
                cached.setFat(uf.getFat());
                cached.setSource("USDA");
                cached.setIsCommon(0);
                try {
                    foodItemMapper.insert(cached);
                    results.add(cached);
                } catch (Exception e) {
                    log.debug("USDA food already cached: fdcId={}", uf.getFdcId());
                    // Fetch existing instead
                    FoodItem existing = foodItemMapper.selectOne(
                            new LambdaQueryWrapper<FoodItem>().eq(FoodItem::getFdcId, uf.getFdcId()));
                    if (existing != null) results.add(existing);
                }
            }
        }

        return results;
    }

    @Override
    public FoodItem getById(Long id) {
        return foodItemMapper.selectById(id);
    }

    @Override
    public FoodItem createCustom(FoodItem foodItem) {
        foodItem.setId(null);
        foodItem.setSource("CUSTOM");
        foodItem.setIsCommon(0);
        foodItemMapper.insert(foodItem);
        return foodItem;
    }
}
