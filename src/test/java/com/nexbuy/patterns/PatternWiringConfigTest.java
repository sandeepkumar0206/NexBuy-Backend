package com.nexbuy.patterns;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import com.nexbuy.patterns.chainofresponsibility.*;
import com.nexbuy.patterns.observer.*;
import org.junit.jupiter.api.DisplayName;

/** Unit tests for PatternWiringConfig. */
class PatternWiringConfigTest {

    @Test
    @DisplayName("wirePatterns subscribes the audit listener and links the validation chain")
    void wirepatterns_subscribes_the_audit_listener_and_links_the_validation_chain() throws Exception {
        // Arrange
        // mocked collaborators
        OrderEventPublisher publisher = mock(OrderEventPublisher.class);
        AuditOrderEventListener audit = mock(AuditOrderEventListener.class);
        StockValidationHandler stock = mock(StockValidationHandler.class);
        AddressValidationHandler address = mock(AddressValidationHandler.class);
        PatternWiringConfig config = new PatternWiringConfig(publisher, audit, stock, address);

        // Act
        // run the post-construct hook
        config.wirePatterns();

        // Assert
        // wiring calls
        verify(publisher, times(1)).subscribe(audit);
        verify(stock, times(1)).linkWith(address);
        verifyNoMoreInteractions(publisher, stock);
        verifyNoInteractions(address);
    }
}
