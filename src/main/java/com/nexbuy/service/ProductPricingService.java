package com.nexbuy.service;

import com.nexbuy.patterns.decorator.BasePriceCalculator;
import com.nexbuy.patterns.decorator.DiscountPriceDecorator;
import com.nexbuy.patterns.decorator.PriceCalculator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Builds the price pipeline with the Decorator pattern:
 * base price, optionally wrapped by a store-wide discount.
 */
@Service
public class ProductPricingService {

    private final PriceCalculator calculator;

    public ProductPricingService(@Value("${app.catalog.sale-percent:0}") double salePercent) {
        if (salePercent < 0 || salePercent >= 100) {
            throw new IllegalArgumentException("app.catalog.sale-percent must be between 0 and 99.99");
        }
        PriceCalculator pipeline = new BasePriceCalculator();
        if (salePercent > 0) {
            pipeline = new DiscountPriceDecorator(pipeline, salePercent);
        }
        this.calculator = pipeline;
    }

    public BigDecimal finalPrice(BigDecimal price) {
        double result = calculator.calculate(price.doubleValue());
        return BigDecimal.valueOf(result).setScale(2, RoundingMode.HALF_UP);
    }
}
