package com.nexbuy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;

/** Unit tests for ProductPricingService. */
class ProductPricingServiceTest {

    @Test
    @DisplayName("finalPrice returns the base price at scale 2 when no sale is configured")
    void finalprice_returns_the_base_price_at_scale_2_when_no_sale_is_configured() throws Exception {
        // Arrange
        // 0% sale
        ProductPricingService pricing = new ProductPricingService(0);

        // Act
        // price a 29.9 product
        BigDecimal result = pricing.finalPrice(new BigDecimal("29.9"));

        // Assert
        // unchanged value, scale 2
        assertEquals(new BigDecimal("29.90"), result);
        assertEquals(2, result.scale());
        assertEquals(0, new BigDecimal("29.9").compareTo(result));
    }

    @Test
    @DisplayName("finalPrice applies the configured percentage discount")
    void finalprice_applies_the_configured_percentage_discount() throws Exception {
        // Arrange
        // 10% sale
        ProductPricingService pricing = new ProductPricingService(10);

        // Act
        // price a 100.00 product
        BigDecimal result = pricing.finalPrice(new BigDecimal("100.00"));

        // Assert
        // 90.00
        assertEquals(new BigDecimal("90.00"), result);
        assertEquals(2, result.scale());
        assertTrue(result.compareTo(new BigDecimal("100.00")) < 0);
    }

    @Test
    @DisplayName("finalPrice rounds half up to two decimals")
    void finalprice_rounds_half_up_to_two_decimals() throws Exception {
        // Arrange
        // 12.5% of 1.00 is exactly 0.125 so 0.875 is a true half
        ProductPricingService pricing = new ProductPricingService(12.5);

        // Act
        // price a 1.00 product
        BigDecimal result = pricing.finalPrice(new BigDecimal("1.00"));

        // Assert
        // 0.875 rounds up to 0.88
        assertEquals(new BigDecimal("0.88"), result);
        assertEquals(2, result.scale());
        assertNotEquals(new BigDecimal("0.87"), result);
    }

    @Test
    @DisplayName("the constructor rejects a negative sale percent")
    void the_constructor_rejects_a_negative_sale_percent() throws Exception {
        // Arrange
        // invalid percent
        double percent = -1;

        // Act
        // construct
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new ProductPricingService(percent));

        // Assert
        // message states the valid range
        assertEquals("app.catalog.sale-percent must be between 0 and 99.99", error.getMessage());
        assertTrue(error.getMessage().contains("99.99"));
        assertNotNull(error);
    }

    @Test
    @DisplayName("the constructor rejects a sale percent of 100 or more")
    void the_constructor_rejects_a_sale_percent_of_100_or_more() throws Exception {
        // Arrange
        // invalid percent
        double percent = 100;

        // Act
        // construct
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> new ProductPricingService(percent));

        // Assert
        // message states the valid range; 99.99 is still accepted
        assertEquals("app.catalog.sale-percent must be between 0 and 99.99", error.getMessage());
        assertNotNull(new ProductPricingService(99.99));
        assertTrue(new ProductPricingService(99.99).finalPrice(new BigDecimal("100.00")).compareTo(new BigDecimal("1")) < 0);
    }
}
