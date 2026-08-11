package com.bankhub.transaction.base;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Base class for unit tests that test business logic in isolation.
 * Uses Mockito for mocking dependencies.
 *
 * Unit tests should:
 * - Test a single class in isolation
 * - Mock all external dependencies
 * - Be fast (no Spring context, no containers)
 * - Focus on business logic and edge cases
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseUnitTest {
    // Common test utilities can be added here
}
