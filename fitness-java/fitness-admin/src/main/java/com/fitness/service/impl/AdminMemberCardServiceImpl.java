package com.fitness.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.entity.MemberCard;
import com.fitness.exception.BusinessException;
import com.fitness.exception.ErrorCode;
import com.fitness.mapper.MemberCardMapper;
import com.fitness.service.AdminMemberCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminMemberCardServiceImpl implements AdminMemberCardService {

    @Autowired
    private MemberCardMapper memberCardMapper;

    @Override
    public Page<MemberCard> listCards(int page, int size) {
        return memberCardMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<MemberCard>()
                        .eq(MemberCard::getStatus, 1)
                        .orderByDesc(MemberCard::getCreateTime));
    }

    @Override
    public MemberCard createCard(MemberCard card) {
        card.setStatus(1);
        memberCardMapper.insert(card);
        return card;
    }

    @Override
    public MemberCard updateCard(MemberCard card) {
        MemberCard exist = memberCardMapper.selectById(card.getId());
        if (exist == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        memberCardMapper.updateById(card);
        return card;
    }

    @Override
    public void deleteCard(Long id) {
        MemberCard card = memberCardMapper.selectById(id);
        if (card == null) throw new BusinessException(ErrorCode.NOT_FOUND);
        card.setStatus(0);
        memberCardMapper.updateById(card);
    }
}
