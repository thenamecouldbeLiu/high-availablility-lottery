package com.interview.lottory.util;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class I18nUtil {
    private final MessageSource messageSource;

    public String getMessage(String messageKey, Object... arguments) {
        return getMessage(messageKey, LocaleContextHolder.getLocale(), arguments);
    }

    public String getMessage(String messageKey, Locale locale, Object... arguments) {
        return messageSource.getMessage(messageKey, arguments, messageKey, locale);
    }
}
