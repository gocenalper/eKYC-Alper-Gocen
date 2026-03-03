package org.example.ekyc.application;

import org.example.ekyc.domain.Customer;
import org.example.ekyc.domain.KycDecision;
import org.example.ekyc.domain.KycDecisionResponse;
import org.example.ekyc.domain.VerificationRequest;
import org.example.ekyc.domain.VerificationResult;
import org.example.ekyc.domain.VerificationType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class KycOrchestrator {

    private final DocumentVerificationClient documentClient;
    private final BiometricVerificationClient biometricClient;
    private final AddressVerificationClient addressClient;
    private final SanctionsScreeningClient sanctionsClient;
    private final DecisionEngine decisionEngine;

    public KycOrchestrator(
            DocumentVerificationClient documentClient,
            BiometricVerificationClient biometricClient,
            AddressVerificationClient addressClient,
            SanctionsScreeningClient sanctionsClient,
            DecisionEngine decisionEngine
    ) {
        this.documentClient = documentClient;
        this.biometricClient = biometricClient;
        this.addressClient = addressClient;
        this.sanctionsClient = sanctionsClient;
        this.decisionEngine = decisionEngine;
    }

    public KycDecision orchestrate(
            Customer customer,
            List<VerificationType> verificationTypes,
            VerificationRequest request
    ) {
        String decision = orchestrateWithResults(customer, verificationTypes, request).decision();
        return KycDecision.valueOf(decision);
    }

    public KycDecisionResponse orchestrateWithResults(
            Customer customer,
            List<VerificationType> verificationTypes,
            VerificationRequest request
    ) {
        if (customer == null || verificationTypes == null || verificationTypes.isEmpty() || request == null) {
            return new KycDecisionResponse(KycDecision.REJECTED.name(), List.of());
        }

        List<VerificationResult> results = new ArrayList<>();

        for (VerificationType type : verificationTypes) {
            switch (type) {
                case ID_DOCUMENT -> results.add(safeVerify("ID_DOCUMENT", () -> documentClient.verify(request)));
                case FACE_MATCH -> results.add(safeVerify("FACE_MATCH", () -> biometricClient.verify(request)));
                case ADDRESS -> results.add(safeVerify("ADDRESS", () -> addressClient.verify(request)));
                case SANCTIONS -> results.add(safeVerify("SANCTIONS", () -> sanctionsClient.verify(request)));
            }
        }

        KycDecision decision = decisionEngine.decide(results);
        return new KycDecisionResponse(decision.name(), results);
    }

    private VerificationResult safeVerify(String serviceName, VerificationCall call) {
        try {
            VerificationResult result = call.execute();
            return Objects.requireNonNullElseGet(result, () -> failedResult(serviceName, "null_result"));
        } catch (Exception exception) {
            return failedResult(serviceName, exception.getMessage());
        }
    }

    private VerificationResult failedResult(String serviceName, String reason) {
        String safeReason = reason == null ? "unknown_error" : reason;
        return new VerificationResult(serviceName, "ERROR", null, null, null, List.of(safeReason), false);
    }

    @FunctionalInterface
    private interface VerificationCall {
        VerificationResult execute();
    }
}
