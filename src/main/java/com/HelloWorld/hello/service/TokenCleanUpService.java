package com.HelloWorld.hello.service;

import com.HelloWorld.hello.repository.InvalidatedTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanUpService {
    private final InvalidatedTokenRepository invalidatedTokenRepository;

    // Chạy ngầm định kỳ (Ví dụ: sử dụng Cron Expression để chạy vào 1 giờ sáng mỗi ngày)
     @Scheduled(cron = "0 0 1 * * ?")

    // Test 10s 1 lần xóa
//    @Scheduled(fixedRate = 10000)

    // Hoặc chạy cố định cứ sau mỗi 1 tiếng (3600000 mili-giây) để test cho nhanh:
    // @Scheduled(fixedRate = 3600000)
    public void cleanExpriedToken(){
        log.info("Bắt đầu tiến trình quét và dọn dẹp Token hết hạn...");
        Date now = new Date();
        invalidatedTokenRepository.deleteAllByExpiryTimeBefore(now);

        log.info("Dọn dẹp hoàn tất!");
    }
}
