package com.bkap.aispark.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "uploadExecutor")
    public Executor uploadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //Tạo một thread pool — tức là chuồng chứa các luồng xử lý.
        executor.setCorePoolSize(10);      // Số thread tối thiểu cho upload (Tức là lúc nào cũng có 10 thằng worker đứng chờ upload.)
        executor.setMaxPoolSize(30);       // Tối đa
        executor.setQueueCapacity(100);    // Hàng đợi request upload (Tối đa 100 request có thể xếp hàng chờ xử lý)
        executor.setThreadNamePrefix("upload-");
        executor.initialize(); // Khởi tạo executor và mở các thread. (khởi chạy pool, tạo sẵn số lượng thread core để sẵn sàng chạy Async ngay lập tức)
        return executor;
    }
    
    @Bean(name = "openAI")
    public Executor openAI() {
    	ThreadPoolTaskExecutor open = new ThreadPoolTaskExecutor();
    	open.setCorePoolSize(10);
    	open.setMaxPoolSize(20);
    	open.setQueueCapacity(50);
        open.setThreadNamePrefix("open-");
        open.initialize();
        return open;
    }
    
    
    @Bean(name = "imageExecutor")
    public Executor imageExecutor() {
        ThreadPoolTaskExecutor img = new ThreadPoolTaskExecutor();
        img.setCorePoolSize(20);       // 20 thread chạy POST + GET poll
        img.setMaxPoolSize(60);        // không cần quá 100
        img.setQueueCapacity(200);     // hàng chờ vừa đủ
        img.setThreadNamePrefix("leonardo-img-");

        // tránh reject khi user vào đông
        img.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        img.initialize();
        return img;
    }

    @Bean(name = "email")
    public Executor email() {
    	ThreadPoolTaskExecutor em = new ThreadPoolTaskExecutor();
    	em.setCorePoolSize(20);
    	em.setMaxPoolSize(40);
    	em.setQueueCapacity(100);
    	em.setThreadNamePrefix("email-");
    	em.initialize();
    	return em;
    }
    
    
    @Bean(name = "Jsonvideo2")
    public Executor Jsonvideo() {
        ThreadPoolTaskExecutor video = new ThreadPoolTaskExecutor();
        video.setCorePoolSize(50);
        video.setMaxPoolSize(100);
        video.setQueueCapacity(300);
        video.setThreadNamePrefix("video-");
        video.initialize();
        return video;
    }

    @Bean(name = "streamExecutor")
    public Executor streamExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();

        exec.setCorePoolSize(50);      // 50 thread chạy song song (OK cho 350 user)
        exec.setMaxPoolSize(200);      // tối đa 200 thread
        exec.setQueueCapacity(500);    // 500 request chờ
        exec.setThreadNamePrefix("stream-");

        // Quan trọng để tránh reject
        exec.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        exec.initialize();
        return exec;
    }

    
    
}
