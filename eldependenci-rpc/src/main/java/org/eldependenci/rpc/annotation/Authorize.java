package org.eldependenci.rpc.annotation;

import java.lang.annotation.*;

/**
 * 啟用授權請求 (僅限 Serve 服務)
 */
@Inherited
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Authorize {
}
