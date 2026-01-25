package com.personal.planner.domain.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.function.Supplier;

/**
 * Utility class for performance-optimized logging.
 * Debug logs are guarded to prevent expensive string operations when disabled.
 *
 * Usage example:
 * <pre>
 * if (LogUtil.isDebugEnabled()) {
 *     LOG.debug("[ServiceName] Processing: {}", expensiveOperation());
 * }
 * </pre>
 */
public final class LogUtil {

    private static final Logger LOG = LoggerFactory.getLogger(LogUtil.class);

    private LogUtil() {} // Prevent instantiation

    /**
     * Check if debug logging is enabled before expensive operations.
     * Use this to guard debug logs that involve string concatenation,
     * JSON serialization, or other expensive formatting.
     *
     * @return true if debug level is enabled
     */
    public static boolean isDebugEnabled() {
        return LOG.isDebugEnabled();
    }

    /**
     * Log debug message with lazy evaluation.
     * The message supplier is only evaluated if debug is enabled.
     *
     * @param logger the logger to use
     * @param messageSupplier supplier for the message (only evaluated if debug enabled)
     */
    public static void debug(Logger logger, Supplier<String> messageSupplier) {
        if (logger.isDebugEnabled()) {
            logger.debug(messageSupplier.get());
        }
    }
}
