package com.mds.cache.factory;

import com.mds.cache.config.properties.LoggerConfigProperties;
import com.mds.error.handler.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.mds.cache.enumerator.LogLevel.INFO;
import static com.mds.cache.enumerator.LogLevel.DEBUG;
import static com.mds.cache.enumerator.LogLevel.WARN;
import static com.mds.cache.enumerator.LogLevel.ERROR;
import static com.mds.cache.enumerator.LogLevel.TRACE;
import static com.mds.error.handler.enumerator.Action.RETRY_ON_STATE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
public class LoggerFactoryTest {

    private static final String TEST_MESSAGE = "Test message";
    private static final String TEST_CODE = "Test code";
    private static final String ARG_1 = "arg1";
    private static final String ARG_2 = "arg2";
    private static final String MESSAGE = "message";
    private static final String MESSAGE_ARRAY = "message {}, {}";

    @Test
    void should_instantiate_class(){
        LoggerConfigProperties properties = new LoggerConfigProperties();
        assertDoesNotThrow(() -> new LoggerFactory(properties));
    }

    @Test
    void should_invoke_log_methods(){
        LoggerConfigProperties properties = new LoggerConfigProperties();
        properties.setNotShowLog(false);
        LoggerFactory loggerFactory = new LoggerFactory(properties);

        loggerFactory.log(INFO, MESSAGE);
        loggerFactory.log(DEBUG, MESSAGE);
        loggerFactory.log(WARN, MESSAGE);
        loggerFactory.log(ERROR, MESSAGE);
        loggerFactory.log(TRACE, MESSAGE);

        loggerFactory.log(INFO, MESSAGE, new GeneralException(TEST_CODE, TEST_MESSAGE, RETRY_ON_STATE));
        loggerFactory.log(DEBUG, MESSAGE, new GeneralException(TEST_CODE, TEST_MESSAGE, RETRY_ON_STATE));
        loggerFactory.log(WARN, MESSAGE, new GeneralException(TEST_CODE, TEST_MESSAGE, RETRY_ON_STATE));
        loggerFactory.log(ERROR, MESSAGE, new GeneralException(TEST_CODE, TEST_MESSAGE, RETRY_ON_STATE));
        loggerFactory.log(TRACE, MESSAGE, new GeneralException(TEST_CODE, TEST_MESSAGE, RETRY_ON_STATE));

        loggerFactory.log(INFO, MESSAGE_ARRAY, ARG_1, ARG_2);
        loggerFactory.log(DEBUG, MESSAGE_ARRAY, ARG_1, ARG_2);
        loggerFactory.log(WARN, MESSAGE_ARRAY, ARG_1, ARG_2);
        loggerFactory.log(ERROR, MESSAGE_ARRAY, ARG_1, ARG_2);
        loggerFactory.log(TRACE, MESSAGE_ARRAY, ARG_1, ARG_2);

        loggerFactory.isNotShowLog();
    }

    @Test
    void should_not_invoke_log_methods(){
        LoggerConfigProperties properties = new LoggerConfigProperties();
        properties.setNotShowLog(true);
        LoggerFactory loggerFactory = new LoggerFactory(properties);

        loggerFactory.log(INFO, MESSAGE);

        loggerFactory.log(INFO, MESSAGE, new GeneralException(TEST_CODE, TEST_MESSAGE, RETRY_ON_STATE));

        loggerFactory.log(INFO, MESSAGE_ARRAY, ARG_1, ARG_2);
    }
}
