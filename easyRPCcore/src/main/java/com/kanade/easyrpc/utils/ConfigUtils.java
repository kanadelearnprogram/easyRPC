package com.kanade.easyrpc.utils;

import cn.hutool.core.util.StrUtil;
import cn.hutool.setting.dialect.Props;
import io.netty.util.internal.StringUtil;

public class ConfigUtils {


    public static <T> T loadConfig(Class<T> tClass, String prefix){
        return loadConfig(tClass, prefix,"");
    }

    public static <T> T loadConfig(Class<T> tClass, String prefix, String env){
        StringBuilder stringBuilder = new StringBuilder("application");
        if (StrUtil.isNotBlank(env)){
            stringBuilder.append("-").append(env);
        }
        stringBuilder.append(".properties");
        Props props = new Props(stringBuilder.toString());
        return props.toBean(tClass,prefix);
    }
}
