package com.fitness.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.dto.ApiResponse;
import com.fitness.dto.response.PageResult;
import com.fitness.entity.MemberCard;
import com.fitness.service.AdminMemberCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMemberCardController {

    @Autowired
    private AdminMemberCardService adminMemberCardService;

    @GetMapping
    public ApiResponse<PageResult<MemberCard>> listCards(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MemberCard> result = adminMemberCardService.listCards(page, size);
        return ApiResponse.success(PageResult.of(result.getTotal(), page, size, result.getRecords()));
    }

    @PostMapping
    public ApiResponse<MemberCard> createCard(@RequestBody MemberCard card) {
        return ApiResponse.success(adminMemberCardService.createCard(card));
    }

    @PutMapping("/{id}")
    public ApiResponse<MemberCard> updateCard(@PathVariable Long id, @RequestBody MemberCard card) {
        card.setId(id);
        return ApiResponse.success(adminMemberCardService.updateCard(card));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCard(@PathVariable Long id) {
        adminMemberCardService.deleteCard(id);
        return ApiResponse.success();
    }
}
