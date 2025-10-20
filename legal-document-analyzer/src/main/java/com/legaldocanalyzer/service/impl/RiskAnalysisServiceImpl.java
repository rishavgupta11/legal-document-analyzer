package com.legaldocanalyzer.service.impl;

import com.legaldocanalyzer.model.ClauseType;
import com.legaldocanalyzer.model.Priority;
import com.legaldocanalyzer.model.RecommendationType;
import com.legaldocanalyzer.model.RiskLevel;
import com.legaldocanalyzer.service.RiskAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RiskAnalysisServiceImpl implements RiskAnalysisService {

    private static final Logger logger = LoggerFactory.getLogger(RiskAnalysisServiceImpl.class);

    // Risk patterns for different clause types
    private static final Map<ClauseType, List<Pattern>> RISK_PATTERNS = new HashMap<>();

    static {
        // Non-compete clause patterns
        RISK_PATTERNS.put(ClauseType.NON_COMPETE, Arrays.asList(
                Pattern.compile("non[- ]compete|covenant not to compete", Pattern.CASE_INSENSITIVE),
                Pattern.compile("restrict.*compet(e|ing|ition)", Pattern.CASE_INSENSITIVE),
                Pattern.compile("prohibit.*similar business", Pattern.CASE_INSENSITIVE)
        ));

        // Indemnity clause patterns
        RISK_PATTERNS.put(ClauseType.INDEMNITY, Arrays.asList(
                Pattern.compile("indemnif(y|ication)|hold harmless", Pattern.CASE_INSENSITIVE),
                Pattern.compile("defend.*against.*claim", Pattern.CASE_INSENSITIVE),
                Pattern.compile("reimburse.*loss(es)?", Pattern.CASE_INSENSITIVE)
        ));

        // Confidentiality clause patterns
        RISK_PATTERNS.put(ClauseType.CONFIDENTIALITY, Arrays.asList(
                Pattern.compile("confidential(ity)?|proprietary information", Pattern.CASE_INSENSITIVE),
                Pattern.compile("non[- ]disclosure", Pattern.CASE_INSENSITIVE),
                Pattern.compile("trade secret", Pattern.CASE_INSENSITIVE)
        ));

        // Payment terms patterns
        RISK_PATTERNS.put(ClauseType.PAYMENT_TERMS, Arrays.asList(
                Pattern.compile("payment.*due|amount payable", Pattern.CASE_INSENSITIVE),
                Pattern.compile("late fee|interest.*overdue", Pattern.CASE_INSENSITIVE),
                Pattern.compile("invoice|billing", Pattern.CASE_INSENSITIVE)
        ));

        // Termination clause patterns
        RISK_PATTERNS.put(ClauseType.TERMINATION, Arrays.asList(
                Pattern.compile("terminat(e|ion)|cancel(lation)?", Pattern.CASE_INSENSITIVE),
                Pattern.compile("end.*agreement|conclude.*contract", Pattern.CASE_INSENSITIVE),
                Pattern.compile("notice period|advance notice", Pattern.CASE_INSENSITIVE)
        ));

        // Liability limitation patterns
        RISK_PATTERNS.put(ClauseType.LIABILITY_LIMITATION, Arrays.asList(
                Pattern.compile("limit(ation)? of liability", Pattern.CASE_INSENSITIVE),
                Pattern.compile("exclude.*liability|not liable for", Pattern.CASE_INSENSITIVE),
                Pattern.compile("maximum liability", Pattern.CASE_INSENSITIVE)
        ));
    }

    // High-risk keywords that increase risk score
    private static final List<String> HIGH_RISK_KEYWORDS = Arrays.asList(
            "unlimited liability", "perpetual", "irrevocable", "unrestricted",
            "sole discretion", "without limitation", "any and all", "in perpetuity",
            "automatically renew", "mandatory arbitration", "waive", "forfeit"
    );

    @Override
    public Map<String, Object> analyzeText(String text) throws Exception {
        logger.info("Starting risk analysis on text of length: {}", text.length());

        if (text == null || text.trim().isEmpty()) {
            throw new Exception("Text cannot be empty for analysis");
        }

        Map<String, Object> results = new HashMap<>();

        // Split text into sentences for clause analysis
        List<String> sentences = splitIntoSentences(text);

        // Detect risky clauses
        List<Map<String, Object>> riskClauses = detectRiskyClauses(sentences, text);

        // Calculate risk metrics
        int totalClauses = countTotalClauses(text);
        int riskyClauses = riskClauses.size();
        BigDecimal riskScore = calculateRiskScore(text, riskClauses);
        RiskLevel overallRiskLevel = determineRiskLevel(riskScore);
        BigDecimal complianceScore = calculateComplianceScore(riskScore);

        // Generate summary
        String summary = generateSummary(totalClauses, riskyClauses, overallRiskLevel);

        // Generate recommendations
        List<Map<String, Object>> recommendations = generateRecommendations(riskClauses, overallRiskLevel);

        // Build results
        results.put("riskScore", riskScore);
        results.put("totalClauses", totalClauses);
        results.put("riskyClauses", riskyClauses);
        results.put("complianceScore", complianceScore);
        results.put("overallRiskLevel", overallRiskLevel);
        results.put("summary", summary);
        results.put("riskClauses", riskClauses);
        results.put("recommendations", recommendations);

        logger.info("Risk analysis completed. Risk Score: {}, Risk Level: {}", riskScore, overallRiskLevel);

        return results;
    }

    private List<String> splitIntoSentences(String text) {
        // Simple sentence splitting (can be improved with NLP libraries)
        String[] sentences = text.split("[.!?]+");
        List<String> result = new ArrayList<>();

        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (!trimmed.isEmpty() && trimmed.length() > 10) {
                result.add(trimmed);
            }
        }

        return result;
    }

    private List<Map<String, Object>> detectRiskyClauses(List<String> sentences, String fullText) {
        List<Map<String, Object>> riskClauses = new ArrayList<>();
        int position = 0;

        for (String sentence : sentences) {
            // Check against each clause type pattern
            for (Map.Entry<ClauseType, List<Pattern>> entry : RISK_PATTERNS.entrySet()) {
                ClauseType clauseType = entry.getKey();
                List<Pattern> patterns = entry.getValue();

                for (Pattern pattern : patterns) {
                    Matcher matcher = pattern.matcher(sentence);
                    if (matcher.find()) {
                        // Found a potential risky clause
                        RiskLevel riskLevel = assessClauseRisk(sentence, clauseType);

                        Map<String, Object> clause = new HashMap<>();
                        clause.put("clauseType", clauseType);
                        clause.put("riskLevel", riskLevel);
                        clause.put("content", sentence);
                        clause.put("explanation", generateExplanation(clauseType, riskLevel));
                        clause.put("startPosition", position);
                        clause.put("endPosition", position + sentence.length());

                        riskClauses.add(clause);
                        break; // Found match, move to next sentence
                    }
                }
            }

            position += sentence.length();
        }

        return riskClauses;
    }

    private RiskLevel assessClauseRisk(String sentence, ClauseType clauseType) {
        String lowerSentence = sentence.toLowerCase();
        int riskPoints = 0;

        // Check for high-risk keywords
        for (String keyword : HIGH_RISK_KEYWORDS) {
            if (lowerSentence.contains(keyword.toLowerCase())) {
                riskPoints += 2;
            }
        }

        // Check for vague language
        if (lowerSentence.contains("reasonable") || lowerSentence.contains("appropriate") ||
                lowerSentence.contains("may") || lowerSentence.contains("should")) {
            riskPoints += 1;
        }

        // Clause-specific risk assessment
        switch (clauseType) {
            case NON_COMPETE:
            case INDEMNITY:
                riskPoints += 2;
                break;
            case LIABILITY_LIMITATION:
                riskPoints += 1;
                break;
            default:
                break;
        }

        // Determine risk level based on points
        if (riskPoints >= 4) return RiskLevel.CRITICAL;
        if (riskPoints >= 3) return RiskLevel.HIGH;
        if (riskPoints >= 2) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private int countTotalClauses(String text) {
        // Estimate total clauses based on numbered sections and sentence count
        int numberedClauses = 0;
        Pattern numberPattern = Pattern.compile("\\b\\d+\\.\\d*\\s");
        Matcher matcher = numberPattern.matcher(text);

        while (matcher.find()) {
            numberedClauses++;
        }

        // If no numbered clauses, estimate based on sentence count
        if (numberedClauses == 0) {
            List<String> sentences = splitIntoSentences(text);
            return Math.max(sentences.size() / 3, 5); // Rough estimate
        }

        return Math.max(numberedClauses, 5);
    }

    private BigDecimal calculateRiskScore(String text, List<Map<String, Object>> riskClauses) {
        double score = 0.0;

        // Base score from number of risky clauses
        score += riskClauses.size() * 10.0;

        // Add score based on risk levels
        for (Map<String, Object> clause : riskClauses) {
            RiskLevel level = (RiskLevel) clause.get("riskLevel");
            switch (level) {
                case CRITICAL: score += 25.0; break;
                case HIGH: score += 15.0; break;
                case MEDIUM: score += 10.0; break;
                case LOW: score += 5.0; break;
            }
        }

        // Check for high-risk keywords in entire text
        String lowerText = text.toLowerCase();
        for (String keyword : HIGH_RISK_KEYWORDS) {
            if (lowerText.contains(keyword.toLowerCase())) {
                score += 5.0;
            }
        }

        // Cap score at 100
        score = Math.min(score, 100.0);

        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    private RiskLevel determineRiskLevel(BigDecimal riskScore) {
        double score = riskScore.doubleValue();

        if (score >= 75.0) return RiskLevel.CRITICAL;
        if (score >= 50.0) return RiskLevel.HIGH;
        if (score >= 25.0) return RiskLevel.MEDIUM;
        return RiskLevel.LOW;
    }

    private BigDecimal calculateComplianceScore(BigDecimal riskScore) {
        // Compliance score is inverse of risk score
        double compliance = 100.0 - riskScore.doubleValue();
        return BigDecimal.valueOf(compliance).setScale(2, RoundingMode.HALF_UP);
    }

    private String generateSummary(int totalClauses, int riskyClauses, RiskLevel overallRiskLevel) {
        return String.format(
                "Document analysis identified %d clauses, with %d potentially risky provisions. " +
                        "The overall risk level is assessed as %s. " +
                        "Immediate legal review is %s.",
                totalClauses,
                riskyClauses,
                overallRiskLevel.toString().toLowerCase(),
                overallRiskLevel == RiskLevel.CRITICAL || overallRiskLevel == RiskLevel.HIGH
                        ? "strongly recommended" : "recommended for high-value transactions"
        );
    }

    private List<Map<String, Object>> generateRecommendations(
            List<Map<String, Object>> riskClauses, RiskLevel overallRiskLevel) {

        List<Map<String, Object>> recommendations = new ArrayList<>();

        // Generate recommendations based on risk clauses
        for (Map<String, Object> clause : riskClauses) {
            ClauseType type = (ClauseType) clause.get("clauseType");
            RiskLevel level = (RiskLevel) clause.get("riskLevel");

            if (level == RiskLevel.HIGH || level == RiskLevel.CRITICAL) {
                Map<String, Object> rec = createRecommendation(type, level);
                if (rec != null) {
                    recommendations.add(rec);
                }
            }
        }

        // Add general recommendation if overall risk is high
        if (overallRiskLevel == RiskLevel.CRITICAL || overallRiskLevel == RiskLevel.HIGH) {
            Map<String, Object> generalRec = new HashMap<>();
            generalRec.put("type", RecommendationType.LEGAL_REVIEW_REQUIRED);
            generalRec.put("priority", Priority.CRITICAL);
            generalRec.put("title", "Comprehensive Legal Review Required");
            generalRec.put("description", "This document contains multiple high-risk provisions that require immediate attention from legal counsel.");
            generalRec.put("suggestedAction", "Consult with a qualified attorney before signing or executing this agreement.");
            recommendations.add(generalRec);
        }

        return recommendations;
    }

    private Map<String, Object> createRecommendation(ClauseType clauseType, RiskLevel riskLevel) {
        Map<String, Object> rec = new HashMap<>();

        Priority priority = riskLevel == RiskLevel.CRITICAL ? Priority.CRITICAL : Priority.HIGH;

        switch (clauseType) {
            case NON_COMPETE:
                rec.put("type", RecommendationType.CLAUSE_MODIFICATION);
                rec.put("priority", priority);
                rec.put("title", "Review Non-Compete Restrictions");
                rec.put("description", "Non-compete clause may be overly restrictive.");
                rec.put("suggestedAction", "Negotiate time period, geographic scope, and industry limitations.");
                break;

            case INDEMNITY:
                rec.put("type", RecommendationType.RISK_MITIGATION);
                rec.put("priority", priority);
                rec.put("title", "Limit Indemnification Exposure");
                rec.put("description", "Indemnity clause may expose you to unlimited liability.");
                rec.put("suggestedAction", "Add caps on liability and exclude indirect damages.");
                break;

            case LIABILITY_LIMITATION:
                rec.put("type", RecommendationType.CLAUSE_MODIFICATION);
                rec.put("priority", priority);
                rec.put("title", "Clarify Liability Limitations");
                rec.put("description", "Liability limitations may be one-sided or unclear.");
                rec.put("suggestedAction", "Ensure mutual limitations and clearly define exceptions.");
                break;

            case PAYMENT_TERMS:
                rec.put("type", RecommendationType.CLAUSE_MODIFICATION);
                rec.put("priority", Priority.MEDIUM);
                rec.put("title", "Clarify Payment Terms");
                rec.put("description", "Payment terms may be ambiguous or unfavorable.");
                rec.put("suggestedAction", "Specify exact amounts, due dates, and late payment penalties.");
                break;

            default:
                return null;
        }

        return rec;
    }

    private String generateExplanation(ClauseType clauseType, RiskLevel riskLevel) {
        String riskDescription = riskLevel == RiskLevel.CRITICAL ? "critical risk" :
                riskLevel == RiskLevel.HIGH ? "high risk" :
                        riskLevel == RiskLevel.MEDIUM ? "moderate risk" : "low risk";

        return String.format("This %s clause presents %s and should be carefully reviewed by legal counsel.",
                clauseType.toString().toLowerCase().replace("_", " "),
                riskDescription);
    }
}