package com.debuglab.reloop;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestRunner {

    private static class TestResult {
        String status;
        long durationMs;
        String error;

        TestResult(String status, long durationMs, String error) {
            this.status = status;
            this.durationMs = durationMs;
            this.error = error;
        }
    }

    @FunctionalInterface
    private interface TestCase {
        void run() throws Throwable;
    }

    public static void main(String[] args) {
        ReLoopTestSuite suite = new ReLoopTestSuite();
        Map<String, TestCase> tests = new LinkedHashMap<>();

        tests.put("test_component_compatibility_matching", suite::testComponentCompatibilityMatching);
        tests.put("test_recovery_job_prioritization", suite::testRecoveryJobPrioritization);
        tests.put("test_batch_capacity_allocation", suite::testBatchCapacityAllocation);
        tests.put("test_technician_scheduling_interval", suite::testTechnicianSchedulingInterval);
        tests.put("test_recovery_lifecycle_transition_validation", suite::testRecoveryLifecycleTransitions);
        tests.put("test_reservation_persistence_on_cancellation", suite::testReservationConsistencyOnCancellation);

        Map<String, TestResult> results = new LinkedHashMap<>();
        int passed = 0;
        int failed = 0;
        long totalDurationMs = 0;

        for (Map.Entry<String, TestCase> entry : tests.entrySet()) {
            String testName = entry.getKey();
            TestCase testCase = entry.getValue();
            long start = System.currentTimeMillis();
            try {
                testCase.run();
                long duration = System.currentTimeMillis() - start;
                totalDurationMs += duration;
                results.put(testName, new TestResult("passed", duration, null));
                passed++;
            } catch (Throwable t) {
                long duration = System.currentTimeMillis() - start;
                totalDurationMs += duration;
                String msg = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
                results.put(testName, new TestResult("failed", duration, msg));
                failed++;
            }
        }

        // Build valid JSON string
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        for (Map.Entry<String, TestResult> entry : results.entrySet()) {
            String name = entry.getKey();
            TestResult tr = entry.getValue();
            json.append("  \"").append(name).append("\": {\n");
            json.append("    \"Status\": \"").append(tr.status).append("\",\n");
            json.append("    \"Execution time\": \"").append(tr.durationMs).append("ms\"");
            if ("failed".equals(tr.status)) {
                json.append(",\n    \"Error\": \"").append(escapeJson(tr.error)).append("\"\n");
            } else {
                json.append("\n");
            }
            json.append("  },\n");
        }
        json.append("  \"Passed\": ").append(passed).append(",\n");
        json.append("  \"Failed\": ").append(failed).append(",\n");
        json.append("  \"Total bugs\": 6,\n");
        json.append("  \"Total Execution time\": \"").append(totalDurationMs).append("ms\"\n");
        json.append("}\n");

        System.out.println(json);

        if (failed > 0) {
            System.exit(1);
        } else {
            System.exit(0);
        }
    }

    private static String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
