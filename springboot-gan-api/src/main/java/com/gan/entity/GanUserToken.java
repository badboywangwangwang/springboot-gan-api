
package com.gan.entity;

import lombok.Data;

import java.util.Date;

@Data
public class GanUserToken {
    private Long userId;

    private String token;

    private Date updateTime;

    private Date expireTime;
}