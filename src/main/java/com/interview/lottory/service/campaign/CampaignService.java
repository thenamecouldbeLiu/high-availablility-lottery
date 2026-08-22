package com.interview.lottory.service.campaign;

import com.interview.lottory.domain.*;
import com.interview.lottory.enums.CampaignStatus;
import com.interview.lottory.enums.PrizeType;
import com.interview.lottory.infra.exception.ErrorCode;
import com.interview.lottory.infra.exception.InterviewException;
import com.interview.lottory.infra.Constants;
import com.interview.lottory.repository.LotteryCampaignRepository;
import com.interview.lottory.repository.LotteryPrizeRepository;
import com.interview.lottory.service.campaign.dto.*;
import com.interview.lottory.service.campaign.mapper.CampaignEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CampaignService {
    private static final BigDecimal TOTAL_PROBABILITY = new BigDecimal("1.0000000");
    private final LotteryCampaignRepository campaignRepository;
    private final LotteryPrizeRepository prizeRepository;
    private final CampaignEntityMapper mapper;

    @Transactional
    public CampaignBo create(CreateCampaignBo command) {
        if (campaignRepository.existsByCampaignCode(command.campaignCode())) {
            throw new InterviewException(ErrorCode.DUPLICATE_REQUEST,
                    Constants.MessageKey.CAMPAIGN_CODE_EXISTS, command.campaignCode());
        }
        validatePeriod(command.startsAt(), command.endsAt());
        return withPrizes(campaignRepository.save(mapper.toEntity(command)));
    }

    @Transactional
    public CampaignBo update(Long campaignId, UpdateCampaignBo command) {
        validatePeriod(command.startsAt(), command.endsAt());
        LotteryCampaign entity = getCampaignEntity(campaignId);
        mapper.updateCampaign(command, entity);
        return withPrizes(campaignRepository.save(entity));
    }

    @Transactional
    public PrizeConfigBo addPrize(Long campaignId, PrizeConfigBo command) {
        LotteryCampaign campaign = getCampaignEntity(campaignId);
        LotteryPrize saved = prizeRepository.save(mapper.toEntity(command, campaignId));
        validateIfActive(mapper.toBo(campaign));
        return mapper.toBo(saved);
    }

    @Transactional
    public PrizeConfigBo updatePrize(Long campaignId, Long prizeId, PrizeConfigBo command) {
        LotteryCampaign campaign = getCampaignEntity(campaignId);
        LotteryPrize entity = prizeRepository.findById(prizeId)
                .filter(prize -> mapper.toBo(prize).campaignId().equals(campaignId))
                .orElseThrow(() -> new InterviewException(ErrorCode.PRIZE_NOT_FOUND));
        PrizeConfigBo current = mapper.toBo(entity);
        long awarded = current.totalStock() - current.remainingStock();
        if (command.prizeType() == PrizeType.PRIZE && command.totalStock() < awarded) {
            throw new InterviewException(ErrorCode.INVALID_REQUEST,
                    Constants.MessageKey.STOCK_BELOW_AWARDED, awarded);
        }
        PrizeConfigBo adjusted = new PrizeConfigBo(command.id(), command.campaignId(), command.prizeCode(),
                command.name(), command.prizeType(), command.probability(), command.totalStock(),
                command.prizeType() == PrizeType.NO_PRIZE ? 0 : command.totalStock() - awarded,
                command.displayOrder(), command.enabled());
        mapper.updatePrize(adjusted, entity);
        LotteryPrize saved = prizeRepository.save(entity);
        validateIfActive(mapper.toBo(campaign));
        return mapper.toBo(saved);
    }

    @Transactional
    public CampaignBo activate(Long campaignId) {
        LotteryCampaign campaign = getCampaignEntity(campaignId);
        validatePrizeConfiguration(campaignId);
        mapper.changeStatus(CampaignStatus.ACTIVE, campaign);
        return withPrizes(campaignRepository.save(campaign));
    }

    @Transactional
    public CampaignBo changeStatus(Long campaignId, CampaignStatus status) {
        LotteryCampaign campaign = getCampaignEntity(campaignId);
        if (status == CampaignStatus.ACTIVE) {
            validatePrizeConfiguration(campaignId);
        }
        mapper.changeStatus(status, campaign);
        return withPrizes(campaignRepository.save(campaign));
    }

    @Transactional
    public void deletePrize(Long campaignId, Long prizeId) {
        LotteryCampaign campaign = getCampaignEntity(campaignId);
        LotteryPrize prize = prizeRepository.findById(prizeId)
                .filter(item -> mapper.toBo(item).campaignId().equals(campaignId))
                .orElseThrow(() -> new InterviewException(ErrorCode.PRIZE_NOT_FOUND));
        prizeRepository.delete(prize);
        prizeRepository.flush();
        validateIfActive(mapper.toBo(campaign));
    }

    @Transactional(readOnly = true)
    public CampaignBo get(Long campaignId) {
        return withPrizes(getCampaignEntity(campaignId));
    }

    private void validateIfActive(CampaignBo campaign) {
        if (campaign.status() == CampaignStatus.ACTIVE) {
            validatePrizeConfiguration(campaign.id());
        }
    }

    private void validatePrizeConfiguration(Long campaignId) {
        List<PrizeConfigBo> prizes = mapper.toPrizeBos(
                prizeRepository.findByCampaignIdAndEnabledTrueOrderByDisplayOrderAsc(campaignId));
        long actualPrizes = prizes.stream().filter(p -> p.prizeType() == PrizeType.PRIZE).count();
        long noPrize = prizes.stream().filter(p -> p.prizeType() == PrizeType.NO_PRIZE).count();
        BigDecimal total = prizes.stream().map(PrizeConfigBo::probability)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean stockValid = prizes.stream().filter(p -> p.prizeType() == PrizeType.PRIZE)
                .allMatch(p -> p.totalStock() > 0);
        if (actualPrizes != 3 || noPrize != 1 || total.compareTo(TOTAL_PROBABILITY) != 0 || !stockValid) {
            throw new InterviewException(ErrorCode.INVALID_PRIZE_CONFIGURATION);
        }
    }

    private CampaignBo withPrizes(LotteryCampaign entity) {
        CampaignBo base = mapper.toBo(entity);
        return new CampaignBo(base.id(), base.campaignCode(), base.name(), base.status(),
                base.maxDrawsPerUser(), base.startsAt(), base.endsAt(),
                mapper.toPrizeBos(prizeRepository.findByCampaignIdOrderByDisplayOrderAsc(entity.getId())));
    }

    private LotteryCampaign getCampaignEntity(Long id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new InterviewException(ErrorCode.CAMPAIGN_NOT_FOUND));
    }

    private void validatePeriod(java.time.Instant startsAt, java.time.Instant endsAt) {
        if (startsAt == null || endsAt == null || !endsAt.isAfter(startsAt)) {
            throw new InterviewException(ErrorCode.INVALID_REQUEST,
                    Constants.MessageKey.INVALID_CAMPAIGN_PERIOD);
        }
    }
}
