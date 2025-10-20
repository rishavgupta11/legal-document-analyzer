package com.legaldocanalyzer.service;

import java.util.Map;

public interface RiskAnalysisService {
    Map<String, Object> analyzeText(String text) throws Exception;
}