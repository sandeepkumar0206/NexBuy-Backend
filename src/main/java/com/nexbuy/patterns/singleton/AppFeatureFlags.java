package com.nexbuy.patterns.singleton;

/**
 * Eager singleton for app-wide feature flags.
 * (Spring beans are also singletons by default.)
 */
public final class AppFeatureFlags {
    private static final AppFeatureFlags INSTANCE = new AppFeatureFlags();

    private boolean reviewsEnabled = true;

    private AppFeatureFlags() {}

    public static AppFeatureFlags getInstance() {
        return INSTANCE;
    }

    public boolean isReviewsEnabled() {
        return reviewsEnabled;
    }

    public void setReviewsEnabled(boolean reviewsEnabled) {
        this.reviewsEnabled = reviewsEnabled;
    }
}
