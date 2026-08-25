package com.nexbuy.patterns;

import com.nexbuy.patterns.chainofresponsibility.AddressValidationHandler;
import com.nexbuy.patterns.chainofresponsibility.StockValidationHandler;
import com.nexbuy.patterns.observer.AuditOrderEventListener;
import com.nexbuy.patterns.observer.OrderEventPublisher;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PatternWiringConfig {

    private final OrderEventPublisher orderEventPublisher;
    private final AuditOrderEventListener auditOrderEventListener;
    private final StockValidationHandler stockValidationHandler;
    private final AddressValidationHandler addressValidationHandler;

    @PostConstruct
    void wirePatterns() {
        orderEventPublisher.subscribe(auditOrderEventListener);
        stockValidationHandler.linkWith(addressValidationHandler);
    }
}
