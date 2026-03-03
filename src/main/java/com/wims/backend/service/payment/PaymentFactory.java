package com.wims.backend.service.payment;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class PaymentFactory {

    private final Map<String, PaymentStrategy> strategies;

    public PaymentFactory(List<PaymentStrategy> paymentStrategies) {
        strategies = paymentStrategies.stream()
                .collect(Collectors.toMap(s -> s.getMethodName().toUpperCase(), Function.identity()));
    }

    public PaymentStrategy getStrategy(String methodName) {
        PaymentStrategy strategy = strategies.get(methodName.toUpperCase());
        if (strategy == null) {
            throw new IllegalArgumentException("Unsupported payment method: " + methodName);
        }
        return strategy;
    }
}
