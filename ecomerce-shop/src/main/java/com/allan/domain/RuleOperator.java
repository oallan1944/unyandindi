package com.allan.domain;

public enum RuleOperator {
    GTE,    // measured value >= threshold
    LTE,    // measured value <= threshold
    EQ,     // measured value == threshold (exact match)
    IN,     // measured value exists in comma-separated set
    NOT_IN, // measured value does not exist in comma-separated set
    GT,
    LT
}