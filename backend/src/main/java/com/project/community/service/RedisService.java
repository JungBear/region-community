package com.project.community.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {

    private final RedisTemplate<String, String>redisTemplate;

    /**
     * {key, value}로 저장
     * @param key
     * @param value
     */
    @Transactional
    public void setValue(String key, String value){
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 만료시간을 설정해 자동으로 삭제 될 수 있게 한다
     * 시간 단위는 토큰의 유효기간과 같은 milliseconds이다
     * @param key
     * @param value
     * @param timeout
     */
    @Transactional
    public void setValuesWithTimeout(String key, String value, long timeout){
        redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 키값으로 value값 리턴
     * @param key
     * @return value
     */
    public String getValues(String key){
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * key로 데이터 삭제
     * @param key
     */
    @Transactional
    public void deleteValues(String key){
        redisTemplate.delete(key);
    }


}
