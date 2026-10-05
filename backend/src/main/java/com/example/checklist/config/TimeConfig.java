package com.example.checklist.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * 系統時區固定為 Asia/Taipei。
 *
 * <p>檢點日、今日待檢點都依台北時間計算。伺服器放到雲端時系統時區通常是 UTC，
 * 直接用 {@code LocalDate.now()} 的話，台灣早上 7 點會被算成前一天。</p>
 *
 * <p>需要「現在」的地方注入 {@link Clock}，用 {@code LocalDate.now(clock)} 取得；
 * 寫測試時可以換成 {@code Clock.fixed(...)}，固定在想測的日期與時間。</p>
 */
@Configuration
public class TimeConfig {

    /** 系統使用的時區。 */
    public static final ZoneId ZONE = ZoneId.of("Asia/Taipei");

    @Bean
    public Clock clock() {
        return Clock.system(ZONE);
    }
}
