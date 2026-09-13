package com.example.rental.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * {@code @MapperScan} をメインの {@code @SpringBootApplication} クラスから独立させるための設定クラス。
 * メインクラスに直接付けると、{@code @WebMvcTest} 等のスライステストでも
 * MapperScannerConfigurer が動作し、DataSource/SqlSessionFactory が存在しない
 * テストコンテキストで Mapper Bean の生成に失敗するため、ここに切り出している。
 */
@Configuration
@MapperScan("com.example.rental.mapper")
public class MyBatisConfig {

}
