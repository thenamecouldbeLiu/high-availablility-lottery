package com.interview.lottory.service.campaign;

import com.interview.common.exception.InterviewException;
import com.interview.lottory.domain.LotteryCampaign;
import com.interview.lottory.domain.LotteryPrize;
import com.interview.lottory.enums.CampaignStatus;
import com.interview.lottory.enums.PrizeType;
import com.interview.lottory.repository.LotteryCampaignRepository;
import com.interview.lottory.repository.LotteryPrizeRepository;
import com.interview.lottory.service.campaign.dto.CampaignBo;
import com.interview.lottory.service.campaign.dto.CreateCampaignBo;
import com.interview.lottory.service.campaign.dto.PrizeConfigBo;
import com.interview.lottory.service.campaign.mapper.CampaignEntityMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampaignServiceTest {
    @Mock LotteryCampaignRepository campaigns;
    @Mock LotteryPrizeRepository prizes;
    @Mock CampaignEntityMapper mapper;
    @InjectMocks CampaignService service;

    @Test
    void createsDraftCampaign() {
        var command = new CreateCampaignBo("C1", "Campaign", 3, future(1), future(2));
        var entity = campaign(1L, CampaignStatus.DRAFT);
        when(mapper.toEntity(command)).thenReturn(entity);
        when(campaigns.save(entity)).thenReturn(entity);
        when(mapper.toBo(entity)).thenReturn(campaignBo(entity));
        when(prizes.findByCampaignIdAndDeletedFalseOrderByDisplayOrderAsc(1L)).thenReturn(List.of());
        when(mapper.toPrizeBos(List.of())).thenReturn(List.of());

        assertThat(service.create(command)).satisfies(result -> {
            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.campaignCode()).isEqualTo("C1");
        });
    }

    @Test
    void rejectsDuplicateCampaignCode() {
        when(campaigns.existsByCampaignCodeAndDeletedFalse("C1")).thenReturn(true);
        var command = new CreateCampaignBo("C1", "Campaign", 3, future(1), future(2));

        assertThatThrownBy(() -> service.create(command)).isInstanceOf(InterviewException.class)
                .extracting(e -> ((InterviewException) e).getErrorCode().getCode())
                .isEqualTo("DUPLICATE_REQUEST");
        verify(campaigns, never()).save(any());
    }

    @Test
    void rejectsInvalidPeriod() {
        var command = new CreateCampaignBo("C1", "Campaign", 3, future(2), future(1));
        assertThatThrownBy(() -> service.create(command)).isInstanceOf(InterviewException.class);
    }

    @Test
    void activatesCampaignWithValidPrizeConfiguration() {
        var entity = campaign(1L, CampaignStatus.DRAFT);
        when(campaigns.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(entity));
        when(prizes.findByCampaignIdAndEnabledTrueAndDeletedFalseOrderByDisplayOrderAsc(1L))
                .thenReturn(List.of(mock(LotteryPrize.class)));
        when(mapper.toPrizeBos(any())).thenReturn(validPrizes());
        when(campaigns.save(entity)).thenReturn(entity);
        when(mapper.toBo(entity)).thenAnswer(invocation -> campaignBo(entity));
        doAnswer(invocation -> {
            entity.setStatus(invocation.getArgument(0));
            return null;
        }).when(mapper).changeStatus(any(CampaignStatus.class), same(entity));
        when(prizes.findByCampaignIdAndDeletedFalseOrderByDisplayOrderAsc(1L)).thenReturn(List.of());

        assertThat(service.activate(1L).status()).isEqualTo(CampaignStatus.ACTIVE);
        assertThat(entity.getStatus()).isEqualTo(CampaignStatus.ACTIVE);
    }

    @Test
    void rejectsActivationWithInvalidProbabilityTotal() {
        var entity = campaign(1L, CampaignStatus.DRAFT);
        when(campaigns.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(entity));
        when(mapper.toPrizeBos(any())).thenReturn(List.of(prize(PrizeType.PRIZE, "0.2", 1)));

        assertThatThrownBy(() -> service.activate(1L)).isInstanceOf(InterviewException.class)
                .extracting(e -> ((InterviewException) e).getErrorCode().getCode())
                .isEqualTo("INVALID_PRIZE_CONFIGURATION");
    }

    @Test
    void updatesPrizeStockWithoutRestoringAwardedStock() {
        var campaign = campaign(1L, CampaignStatus.DRAFT);
        var entity = mock(LotteryPrize.class);
        var current = new PrizeConfigBo(2L, 1L, "P", "Prize", PrizeType.PRIZE,
                new BigDecimal("0.1"), 10, 4, 1, true);
        var command = new PrizeConfigBo(2L, 1L, "P", "Prize", PrizeType.PRIZE,
                new BigDecimal("0.1"), 8, 8, 1, true);
        when(campaigns.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(campaign));
        when(prizes.findByIdAndCampaignIdAndDeletedFalse(2L, 1L)).thenReturn(Optional.of(entity));
        when(mapper.toBo(entity)).thenReturn(current, command);
        when(mapper.toBo(campaign)).thenReturn(campaignBo(campaign));
        when(prizes.save(entity)).thenReturn(entity);

        service.updatePrize(1L, 2L, command);

        verify(mapper).updatePrize(argThat(adjusted -> adjusted.remainingStock() == 2), same(entity));
    }

    @Test
    void rejectsPrizeStockBelowAlreadyAwardedQuantity() {
        var campaign = campaign(1L, CampaignStatus.DRAFT);
        var entity = mock(LotteryPrize.class);
        var current = new PrizeConfigBo(2L, 1L, "P", "Prize", PrizeType.PRIZE,
                new BigDecimal("0.1"), 10, 4, 1, true);
        var command = new PrizeConfigBo(2L, 1L, "P", "Prize", PrizeType.PRIZE,
                new BigDecimal("0.1"), 5, 5, 1, true);
        when(campaigns.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(campaign));
        when(prizes.findByIdAndCampaignIdAndDeletedFalse(2L, 1L)).thenReturn(Optional.of(entity));
        when(mapper.toBo(entity)).thenReturn(current);

        assertThatThrownBy(() -> service.updatePrize(1L, 2L, command))
                .isInstanceOf(InterviewException.class);
        verify(prizes, never()).save(any());
    }

    @Test
    void addsPrizeToDraftCampaign() {
        var campaign = campaign(1L, CampaignStatus.DRAFT);
        var command = prize(PrizeType.PRIZE, "0.1", 10);
        var entity = mock(LotteryPrize.class);
        when(campaigns.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(campaign));
        when(mapper.toEntity(command, 1L)).thenReturn(entity);
        when(prizes.save(entity)).thenReturn(entity);
        when(mapper.toBo(campaign)).thenReturn(campaignBo(campaign));
        when(mapper.toBo(entity)).thenReturn(command);

        assertThat(service.addPrize(1L, command)).isEqualTo(command);
    }

    @Test
    void addsAllPrizesInOneBatchAndReturnsGeneratedIds() {
        var campaign = campaign(1L, CampaignStatus.DRAFT);
        var first = new PrizeConfigBo(null, null, "P1", "First", PrizeType.PRIZE,
                new BigDecimal("0.2"), 10, 10, 1, true);
        var second = new PrizeConfigBo(null, null, "P2", "Second", PrizeType.NO_PRIZE,
                new BigDecimal("0.8"), 0, 0, 2, true);
        var firstEntity = mock(LotteryPrize.class);
        var secondEntity = mock(LotteryPrize.class);
        var savedBos = List.of(
                new PrizeConfigBo(101L, 1L, "P1", "First", PrizeType.PRIZE,
                        new BigDecimal("0.2"), 10, 10, 1, true),
                new PrizeConfigBo(102L, 1L, "P2", "Second", PrizeType.NO_PRIZE,
                        new BigDecimal("0.8"), 0, 0, 2, true));
        when(campaigns.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(campaign));
        when(mapper.toEntity(first, 1L)).thenReturn(firstEntity);
        when(mapper.toEntity(second, 1L)).thenReturn(secondEntity);
        when(prizes.saveAll(List.of(firstEntity, secondEntity))).thenReturn(List.of(firstEntity, secondEntity));
        when(mapper.toBo(campaign)).thenReturn(campaignBo(campaign));
        when(mapper.toPrizeBos(List.of(firstEntity, secondEntity))).thenReturn(savedBos);

        List<PrizeConfigBo> result = service.addPrizes(1L, List.of(first, second));

        assertThat(result).extracting(PrizeConfigBo::id).containsExactly(101L, 102L);
        verify(prizes, times(1)).saveAll(List.of(firstEntity, secondEntity));
    }

    @Test
    void rejectsDuplicatePrizeCodesWithinBatch() {
        var campaign = campaign(1L, CampaignStatus.DRAFT);
        var duplicate = new PrizeConfigBo(null, null, "P1", "Prize", PrizeType.PRIZE,
                new BigDecimal("0.5"), 10, 10, 1, true);
        when(campaigns.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(campaign));

        assertThatThrownBy(() -> service.addPrizes(1L, List.of(duplicate, duplicate)))
                .isInstanceOf(InterviewException.class);
        verify(prizes, never()).saveAll(any());
    }

    @Test
    void returnsAvailableCampaignsWithOnlyEnabledPrizes() {
        var campaign = campaign(1L, CampaignStatus.ACTIVE);
        var entityPrize = mock(LotteryPrize.class);
        var enabled = prize(PrizeType.PRIZE, "1.0", 10);
        when(campaigns.findAvailableCampaigns(any(Instant.class))).thenReturn(List.of(campaign));
        when(mapper.toBo(campaign)).thenReturn(campaignBo(campaign));
        when(prizes.findByCampaignIdAndEnabledTrueAndDeletedFalseOrderByDisplayOrderAsc(1L))
                .thenReturn(List.of(entityPrize));
        when(mapper.toPrizeBos(List.of(entityPrize))).thenReturn(List.of(enabled));

        assertThat(service.getAvailableCampaigns()).singleElement()
                .satisfies(result -> assertThat(result.prizes()).containsExactly(enabled));
    }

    @Test
    void softDeletesCampaignAndItsPrizes() {
        when(campaigns.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(campaign(1L, CampaignStatus.DRAFT)));
        when(campaigns.softDeleteById(eq(1L), any(Instant.class))).thenReturn(1);

        service.deleteCampaign(1L);

        verify(prizes).softDeleteAllByCampaignId(eq(1L), any(Instant.class));
        verify(campaigns).softDeleteById(eq(1L), any(Instant.class));
    }

    @Test
    void reportsMissingCampaign() {
        assertThatThrownBy(() -> service.get(404L)).isInstanceOf(InterviewException.class)
                .extracting(e -> ((InterviewException) e).getErrorCode().getCode())
                .isEqualTo("CAMPAIGN_NOT_FOUND");
    }

    private LotteryCampaign campaign(long id, CampaignStatus status) {
        var value = new LotteryCampaign();
        value.setId(id); value.setCampaignCode("C1"); value.setName("Campaign"); value.setStatus(status);
        value.setMaxDrawsPerUser(3); value.setStartsAt(future(1)); value.setEndsAt(future(2));
        return value;
    }

    private CampaignBo campaignBo(LotteryCampaign value) {
        return new CampaignBo(value.getId(), value.getCampaignCode(), value.getName(), value.getStatus(),
                value.getMaxDrawsPerUser(), value.getStartsAt(), value.getEndsAt(), List.of());
    }

    private List<PrizeConfigBo> validPrizes() {
        return List.of(prize(PrizeType.PRIZE, "0.1", 1), prize(PrizeType.PRIZE, "0.2", 1),
                prize(PrizeType.PRIZE, "0.3", 1), prize(PrizeType.NO_PRIZE, "0.4", 0));
    }

    private PrizeConfigBo prize(PrizeType type, String probability, long stock) {
        return new PrizeConfigBo(1L, 1L, "P", "Prize", type, new BigDecimal(probability),
                stock, stock, 1, true);
    }

    private Instant future(long days) { return Instant.now().plusSeconds(days * 86400); }
}
