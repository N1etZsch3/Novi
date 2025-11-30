package com.n1etzsch3.novi.common.pojo.dto;

import lombok.*;

/**
 * 通用 API 结果包装类
 * <p>
 * API 响应的标准包装器，包含状态码、消息和数据。
 * </p>
 *
 * @author N1etzsch3
 * @since 2025-11-26
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    /**
     * 创建不带数据的成功结果。
     *
     * @return 成功的结果对象。
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMsg("success");
        return result;
    }

    /**
     * 创建带数据的成功结果。
     *
     * @param data 结果中包含的数据。
     * @return 带数据的成功结果对象。
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMsg("success");
        result.setData(data);
        return result;
    }

    /**
     * 创建带消息的错误结果。
     *
     * @param msg 错误消息。
     * @return 错误的结果对象。
     */
    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.msg = msg;
        result.setCode(0);
        return result;
    }

}