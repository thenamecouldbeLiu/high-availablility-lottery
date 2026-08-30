package com.interview.lottory.service.campaign;

import com.interview.lottory.domain.LotteryCampaign;
import com.interview.lottory.domain.LotteryPrize;
import com.interview.lottory.enums.CampaignStatus;
import com.interview.lottory.enums.PrizeType;
import com.interview.lottory.infra.Constants;
import com.interview.common.exception.ErrorCode;
import com.interview.common.exception.InterviewException;
import com.interview.lottory.repository.LotteryCampaignRepository;
import com.interview.lottory.repository.LotteryPrizeRepository;
import com.interview.lottory.service.campaign.dto.CampaignBo;
import com.interview.lottory.service.campaign.dto.CreateCampaignBo;
import com.interview.lottory.service.campaign.dto.PrizeConfigBo;
import com.interview.lottory.service.campaign.dto.UpdateCampaignBo;
import com.interview.lottory.service.campaign.mapper.CampaignEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
public class CampaignService {
    private final LotteryCampaignRepository campaignRepository;
    private final LotteryPrizeRepository prizeRepository;
    private final CampaignEntityMapper mapper;

    @Transactional
    public CampaignBo create(CreateCampaignBo command) {
        if (campaignRepository.existsByCampaignCodeAndDeletedFalse(command.campaignCode())) {
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
    public List<PrizeConfigBo> addPrizes(Long campaignId, List<PrizeConfigBo> commands) {
        LotteryCampaign campaign = getCampaignEntity(campaignId);
        validateBatch(commands);
        List<LotteryPrize> entities = commands.stream()
                .map(command -> mapper.toEntity(command, campaignId))
                .toList();
        List<LotteryPrize> saved = prizeRepository.saveAll(entities);
        validateIfActive(mapper.toBo(campaign));
        return mapper.toPrizeBos(saved);
    }

    @Transactional
    public PrizeConfigBo updatePrize(Long campaignId, Long prizeId, PrizeConfigBo command) {
        LotteryCampaign campaign = getCampaignEntity(campaignId);
        LotteryPrize entity = prizeRepository.findByIdAndCampaignIdAndDeletedFalse(prizeId, campaignId)
                .orElseThrow(() -> new InterviewException(ErrorCode.PRIZE_NOT_FOUND));
        PrizeConfigBo current = mapper.toBo(entity);
        PrizeConfigBo adjusted = checkRemainAndGetCalculatedPrize(command, current);
        mapper.updatePrize(adjusted, entity);
        LotteryPrize saved = prizeRepository.save(entity);
        validateIfActive(mapper.toBo(campaign));
        return mapper.toBo(saved);
    }

    private PrizeConfigBo checkRemainAndGetCalculatedPrize(PrizeConfigBo command,
                                                                           PrizeConfigBo current) {
        long awarded = current.totalStock() - current.remainingStock();
        if (command.prizeType() == PrizeType.PRIZE && command.totalStock() < awarded) {
            throw new InterviewException(ErrorCode.INVALID_REQUEST,
                    Constants.MessageKey.STOCK_BELOW_AWARDED, awarded);
        }
        return new PrizeConfigBo(command.id(), command.campaignId(), command.prizeCode(),
                command.name(), command.prizeType(), command.probability(), command.totalStock(),
                command.prizeType() == PrizeType.NO_PRIZE ? 0 : command.totalStock() - awarded,
                command.displayOrder(), command.enabled());
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
        if (prizeRepository.softDeleteByIdAndCampaignId(prizeId, campaignId,
                java.time.Instant.now()) != 1) {
            throw new InterviewException(ErrorCode.PRIZE_NOT_FOUND);
        }
        validateIfActive(mapper.toBo(campaign));
    }

    @Transactional
    public void deleteCampaign(Long campaignId) {
        getCampaignEntity(campaignId);
        java.time.Instant deletedAt = java.time.Instant.now();
        prizeRepository.softDeleteAllByCampaignId(campaignId, deletedAt);
        if (campaignRepository.softDeleteById(campaignId, deletedAt) != 1) {
            throw new InterviewException(ErrorCode.CAMPAIGN_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public CampaignBo get(Long campaignId) {
        return withPrizes(getCampaignEntity(campaignId));
    }

    @Transactional(readOnly = true)
    public List<CampaignBo> getAvailableCampaigns() {
        return campaignRepository.findAvailableCampaigns(java.time.Instant.now()).stream()
                .map(this::withEnabledPrizes)
                .toList();
    }

    private void validateIfActive(CampaignBo campaign) {
        if (campaign.status() == CampaignStatus.ACTIVE) {
            validatePrizeConfiguration(campaign.id());
        }
    }

    private void validatePrizeConfiguration(Long campaignId) {
        List<PrizeConfigBo> prizes = mapper.toPrizeBos(
                prizeRepository.findByCampaignIdAndEnabledTrueAndDeletedFalseOrderByDisplayOrderAsc(campaignId));
        long actualPrizes = prizes.stream().filter(p -> p.prizeType() == PrizeType.PRIZE).count();
        long noPrize = prizes.stream().filter(p -> p.prizeType() == PrizeType.NO_PRIZE).count();
        BigDecimal total = prizes.stream().map(PrizeConfigBo::probability)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        boolean stockValid = prizes.stream().filter(p -> p.prizeType() == PrizeType.PRIZE)
                .allMatch(p -> p.totalStock() > 0);
        if (actualPrizes != 3 || noPrize != 1 || total.compareTo(Constants.TOTAL_PROBABILITY) != 0 || !stockValid) {
            throw new InterviewException(ErrorCode.INVALID_PRIZE_CONFIGURATION);
        }
    }

    private CampaignBo withPrizes(LotteryCampaign entity) {
        CampaignBo base = mapper.toBo(entity);
        return new CampaignBo(base.id(), base.campaignCode(), base.name(), base.status(),
                base.maxDrawsPerUser(), base.startsAt(), base.endsAt(),
                mapper.toPrizeBos(prizeRepository.findByCampaignIdAndDeletedFalseOrderByDisplayOrderAsc(
                        base.id())));
    }

    private CampaignBo withEnabledPrizes(LotteryCampaign entity) {
        CampaignBo base = mapper.toBo(entity);
        return new CampaignBo(base.id(), base.campaignCode(), base.name(), base.status(),
                base.maxDrawsPerUser(), base.startsAt(), base.endsAt(),
                mapper.toPrizeBos(
                        prizeRepository.findByCampaignIdAndEnabledTrueAndDeletedFalseOrderByDisplayOrderAsc(
                                base.id())));
    }

    private LotteryCampaign getCampaignEntity(Long id) {
        return campaignRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new InterviewException(ErrorCode.CAMPAIGN_NOT_FOUND));
    }

    private void validatePeriod(java.time.Instant startsAt, java.time.Instant endsAt) {
        if (startsAt == null || endsAt == null || !endsAt.isAfter(startsAt)) {
            throw new InterviewException(ErrorCode.INVALID_REQUEST,
                    Constants.MessageKey.INVALID_CAMPAIGN_PERIOD);
        }
    }

    private void validateBatch(List<PrizeConfigBo> commands) {
        if (commands == null || commands.isEmpty()) {
            throw new InterviewException(ErrorCode.INVALID_REQUEST);
        }
        HashSet<String> codes = new HashSet<>();
        long noPrizeCount = 0;
        for (PrizeConfigBo command : commands) {
            if (!codes.add(command.prizeCode())) {
                throw new InterviewException(ErrorCode.INVALID_REQUEST);
            }
            if (command.prizeType() == PrizeType.NO_PRIZE && ++noPrizeCount > 1) {
                throw new InterviewException(ErrorCode.INVALID_REQUEST);
            }
        }
    }
}
