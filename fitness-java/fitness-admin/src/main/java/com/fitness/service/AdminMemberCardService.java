package com.fitness.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.MemberCard;

public interface AdminMemberCardService {
    Page<MemberCard> listCards(int page, int size);
    MemberCard createCard(MemberCard card);
    MemberCard updateCard(MemberCard card);
    void deleteCard(Long id);
}
