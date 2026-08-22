package com.interview.lottory.util;

import org.junit.jupiter.api.Test;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class I18nUtilTest {
    private final I18nUtil i18nUtil = new I18nUtil(messageSource());

    @Test
    void shouldResolveEnglishMessageWithArguments() {
        assertEquals("Each request may contain between 1 and 100 draws",
                i18nUtil.getMessage("validation.invalid-draw-count", Locale.ENGLISH, 1, 100));
    }

    @Test
    void shouldFallbackToMessageKeyWhenMissing() {
        assertEquals("missing.message", i18nUtil.getMessage("missing.message", Locale.TAIWAN));
    }

    private ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasename("i18n/messages");
        source.setDefaultEncoding("UTF-8");
        source.setFallbackToSystemLocale(false);
        return source;
    }
}
