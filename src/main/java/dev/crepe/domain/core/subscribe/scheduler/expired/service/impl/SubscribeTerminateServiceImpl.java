package dev.crepe.domain.core.subscribe.scheduler.expired.service.impl;

import dev.crepe.domain.core.account.model.entity.Account;
import dev.crepe.domain.core.account.repository.AccountRepository;
import dev.crepe.domain.core.product.model.entity.Product;
import dev.crepe.domain.core.subscribe.exception.*;
import dev.crepe.domain.core.subscribe.scheduler.expired.service.SubscribeTerminateService;
import dev.crepe.domain.core.subscribe.model.SubscribeStatus;
import dev.crepe.domain.core.subscribe.model.dto.response.TerminatePreviewDto;
import dev.crepe.domain.core.subscribe.model.entity.Subscribe;
import dev.crepe.domain.core.subscribe.repository.SubscribeRepository;
import dev.crepe.domain.core.util.history.subscribe.model.SubscribeHistoryType;
import dev.crepe.domain.core.util.history.subscribe.model.entity.SubscribeHistory;
import dev.crepe.domain.core.util.history.subscribe.repository.SubscribeHistoryRepository;
import dev.crepe.global.error.exception.ExceptionDbService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class SubscribeTerminateServiceImpl implements SubscribeTerminateService {

    private final AccountRepository accountRepository;
    private final SubscribeRepository subscribeRepository;
    private final SubscribeHistoryRepository subscribeHistoryRepository;
    private final ExceptionDbService exceptionDbService;


    @Transactional
    public String terminate(String userEmail, Long subscribeId) {
        // 가입 정보 조회
        Subscribe subscribe = subscribeRepository.findById(subscribeId)
                .orElseThrow(()->exceptionDbService.getException("SUBSCRIBE_004"));

        // 이미 해지된 상품인지 검사
        if (subscribe.getStatus() == SubscribeStatus.EXPIRED) {
            throw exceptionDbService.getException("SUBSCRIBE_01");
        }


        Product product = subscribe.getProduct();
        BigDecimal balance = subscribe.getBalance();

        // 예치금 확인
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            throw exceptionDbService.getException("SUBSCRIBE_02");
        }

        // 가입 개월 계산
        LocalDate startDate = subscribe.getSubscribeDate().toLocalDate();
        LocalDate endDate = LocalDate.now();

        int totalMonths = (int) ChronoUnit.MONTHS.between(startDate, endDate) ;

        if (totalMonths <= 0) {
            throw exceptionDbService.getException("SUBSCRIBE_03");
        }

        // 이자 계산
        BigDecimal interestRate = BigDecimal.valueOf(product.getBaseInterestRate()).divide(BigDecimal.valueOf(100), 10, RoundingMode.DOWN);
        BigDecimal monthlyRate = interestRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.DOWN);
        BigDecimal preTaxInterest;

        switch (product.getType()) {
            case SAVING -> preTaxInterest = balance.multiply(monthlyRate).multiply(BigDecimal.valueOf(totalMonths));
            case INSTALLMENT -> {
                preTaxInterest = calculateInstallmentInterest(subscribe, interestRate);
            }
            default -> throw exceptionDbService.getException("PRODUCT_01");
        }

        // 세후 이자 계산
        BigDecimal taxRate = BigDecimal.valueOf(0.154);
        BigDecimal postTaxInterest = preTaxInterest.multiply(BigDecimal.ONE.subtract(taxRate));

        subscribe.setPreTaxInterest(preTaxInterest);

        // 은행 자본금 계좌에서 이자 차감
        Account bankTokenAccount = accountRepository
                .findByBankTokenIdAndActorIsNull(product.getBankToken().getId())
                .orElseThrow(()-> exceptionDbService.getException("BANK_001"));

        bankTokenAccount.reduceNonAvailableBalance(postTaxInterest);


        // 사용자 토큰 계좌에 원금 + 세후 이자 지급
        Account userTokenAccount = accountRepository.findByActor_EmailAndBankTokenId(
                subscribe.getUser().getEmail(), product.getBankToken().getId()
        ).orElseThrow(()-> exceptionDbService.getException("ACCOUNT_001"));


        BigDecimal totalPayout = balance.add(postTaxInterest);
        userTokenAccount.addAmount(totalPayout);


        // 상태 변경 (만기 처리)
        subscribe.changeExpired();
        subscribeRepository.save(subscribe);

        saveTerminationHistory(subscribe, totalPayout );

        return "해지 완료";
    }

    // 적금 이자 계산 - 단리
    private BigDecimal calculateInstallmentInterest(Subscribe subscribe, BigDecimal annualRate) {
        BigDecimal totalInterest = BigDecimal.ZERO;

        LocalDate startDate = subscribe.getSubscribeDate().toLocalDate(); // 가입일
        LocalDate endDate = subscribe.getExpiredDate().toLocalDate();     // 만기일
        int totalMonths = (int)ChronoUnit.MONTHS.between(startDate, endDate);

        // 단리용 월 이율
        BigDecimal monthlyRate = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.DOWN);


        for (int i = 0; i < totalMonths; i++) {
            LocalDate monthStart = startDate.plusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            LocalDateTime startOfMonth = monthStart.atStartOfDay();
            LocalDateTime endOfMonth = monthEnd.atTime(23, 59, 59);

            // 월별 입금합 조회
            BigDecimal deposited = subscribeHistoryRepository.sumMonthlyDeposit(
                    subscribe,
                    SubscribeHistoryType.DEPOSIT,
                    startOfMonth,
                    endOfMonth
            );


            // 입금 없는 달은 건너뜀
            if (deposited == null || deposited.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            // 이자 = 입금액 × 월이율 (단리)
            BigDecimal interest = deposited
                    .multiply(monthlyRate)
                    .setScale(10, RoundingMode.DOWN);

            totalInterest = totalInterest.add(interest);

        }

        // 일할 이자 계산
        if (!endDate.equals(endDate.withDayOfMonth(endDate.lengthOfMonth()))) {
            int daysPast = endDate.getDayOfMonth() - 1;
            int daysInMonth = endDate.lengthOfMonth();

            BigDecimal thisMonthInterest = totalInterest.multiply(monthlyRate).setScale(10, RoundingMode.DOWN);
            BigDecimal daysInterest = thisMonthInterest.multiply(
                    BigDecimal.valueOf(daysPast)
                            .divide(BigDecimal.valueOf(daysInMonth), 10, RoundingMode.DOWN)
            ).setScale(10, RoundingMode.DOWN);

            totalInterest = totalInterest.add(daysInterest);
        }

        return totalInterest;
    }

    private void saveTerminationHistory(Subscribe subscribe, BigDecimal totalPayout) {
        SubscribeHistory history = SubscribeHistory.builder()
                .subscribe(subscribe)
                .eventType(SubscribeHistoryType.TERMINATION)
                .amount(totalPayout)
                .build();
        subscribeHistoryRepository.save(history);
    }

    // 중도 해지시 값 조회
    public TerminatePreviewDto TerminationPreview(String userEmail, Long subscribeId) {
        Subscribe subscribe = subscribeRepository.findById(subscribeId)
                .orElseThrow(()->exceptionDbService.getException("SUBSCRIBE_004"));

        if (subscribe.getStatus() == SubscribeStatus.EXPIRED) {
            throw exceptionDbService.getException("SUBSCRIBE_01");
        }

        Product product = subscribe.getProduct();
        BigDecimal balance = subscribe.getBalance();

        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            throw exceptionDbService.getException("SUBSCRIBE_02");
        }

        LocalDate startDate = subscribe.getSubscribeDate().toLocalDate();
        LocalDate endDate = LocalDate.now();
        int totalMonths = (int) ChronoUnit.MONTHS.between(startDate, endDate);

        if (totalMonths <= 0) {
            throw exceptionDbService.getException("SUBSCRIBE_03");
        }

        BigDecimal interestRate = BigDecimal.valueOf(product.getBaseInterestRate())
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.DOWN);
        BigDecimal preTaxInterest;

        switch (product.getType()) {
            case SAVING -> preTaxInterest = balance
                    .multiply(interestRate)
                    .divide(BigDecimal.valueOf(12), 10, RoundingMode.DOWN)
                    .multiply(BigDecimal.valueOf(totalMonths));
            case INSTALLMENT -> preTaxInterest = calculateInstallmentInterest(subscribe, interestRate);
            default -> throw exceptionDbService.getException("PRODUCT_01");
        }

        BigDecimal postTaxInterest = preTaxInterest.multiply(BigDecimal.valueOf(0.846)); // 1 - 0.154

        return TerminatePreviewDto.builder()
                .subscribeId(subscribe.getId())
                .balance(balance)
                .preTaxInterest(preTaxInterest)
                .postTaxInterest(postTaxInterest)
                .totalPayout(balance.add(postTaxInterest))
                .interestRate(interestRate)
                .build();
    }


}
