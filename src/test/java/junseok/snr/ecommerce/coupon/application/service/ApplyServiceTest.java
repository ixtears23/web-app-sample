package junseok.snr.ecommerce.coupon.application.service;

import junseok.snr.ecommerce.coupon.domain.repository.CouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ApplyServiceTest {

    @Autowired
    private ApplyService applyService;
    @Autowired
    private CouponRepository couponRepository;

    @DisplayName("쿠폰을 딱 한번 생성했을 때 쿠폰 조회 시 쿠폰 갯수는 1개여야 한다")
    @Test
    void apply_only_once_test() {
        applyService.apply(1L);

        final long count = couponRepository.count();

        assertThat(count).isEqualTo(1L);
    }

    @DisplayName("100장만 발급가능한 쿠폰이 100장 보다 더 많이 발급되어야 한다")
    @Test
    void apply_multiple_times_test() throws InterruptedException {
        int threadCount = 1_000;
        final ExecutorService executorService = Executors.newFixedThreadPool(32);
        final CountDownLatch countDownLatch = new CountDownLatch(threadCount);


        for (int i = 0; i < threadCount; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    applyService.apply(userId);
                } finally {
                    countDownLatch.countDown();
                }

            });
        }

        countDownLatch.await();

        final long count = couponRepository.count();
        assertThat(count).isGreaterThan(100);
    }
}