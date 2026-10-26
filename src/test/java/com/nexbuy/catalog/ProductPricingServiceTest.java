package com.nexbuy.catalog;

import com.nexbuy.service.ProductPricingService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductPricingServiceTest {

    @Test
    void leavesPriceUnchangedWithoutASale() {
        ProductPricingService pricing = new ProductPricingService(0);

        assertEquals(0, new BigDecimal("129.99").compareTo(pricing.finalPrice(new BigDecimal("129.99"))));
    }

    @Test
    void appliesTheDiscountDecorator() {
        ProductPricingService pricing = new ProductPricingService(20);

        assertEquals(0, new BigDecimal("80.00").compareTo(pricing.finalPrice(new BigDecimal("100.00"))));
        assertEquals(0, new BigDecimal("103.99").compareTo(pricing.finalPrice(new BigDecimal("129.99"))));
    }

    @Test
    void rejectsOutOfRangeSalePercent() {
        assertThrows(IllegalArgumentException.class, () -> new ProductPricingService(-5));
        assertThrows(IllegalArgumentException.class, () -> new ProductPricingService(100));
    }
}
