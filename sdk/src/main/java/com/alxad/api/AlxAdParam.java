package com.alxad.api;

/**
 * 所有广告的公共参数【方便以后可扩展】
 *
 * @author lwl
 * @date 2022-9-2
 */
public class AlxAdParam {

    private AlxAdParam(AlxAdParam.Builder builder) {
    }

    public static final class Builder {

        public Builder() {
        }

        public AlxAdParam build() {
            return new AlxAdParam(this);
        }
    }

}