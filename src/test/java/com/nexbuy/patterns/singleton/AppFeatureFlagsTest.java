package com.nexbuy.patterns.singleton;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

/** Unit tests for AppFeatureFlags. */
class AppFeatureFlagsTest {

    @Test
    @DisplayName("getInstance always returns the same object")
    void getinstance_always_returns_the_same_object() throws Exception {
        // Arrange
        // nothing to set up
        AppFeatureFlags first = AppFeatureFlags.getInstance();

        // Act
        // fetch again
        AppFeatureFlags second = AppFeatureFlags.getInstance();

        // Assert
        // identity
        assertNotNull(first);
        assertSame(first, second);
        assertEquals(first.isReviewsEnabled(), second.isReviewsEnabled());
    }

    @Test
    @DisplayName("setReviewsEnabled flips the shared flag and is visible through the instance")
    void setreviewsenabled_flips_the_shared_flag_and_is_visible_through_the_instance() throws Exception {
        // Arrange
        // reviews enabled by default
        AppFeatureFlags flags = AppFeatureFlags.getInstance();
        boolean original = flags.isReviewsEnabled();

        // Act
        // disable, read back through a fresh getInstance, then restore
        flags.setReviewsEnabled(false);
        boolean seenAfterDisable = AppFeatureFlags.getInstance().isReviewsEnabled();
        flags.setReviewsEnabled(original);

        // Assert
        // the toggle was shared and restored
        assertTrue(original);
        assertFalse(seenAfterDisable);
        assertEquals(original, AppFeatureFlags.getInstance().isReviewsEnabled());
    }
}
