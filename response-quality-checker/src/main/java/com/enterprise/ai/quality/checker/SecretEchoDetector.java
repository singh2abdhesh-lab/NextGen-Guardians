package com.enterprise.ai.quality.checker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects whether credentials, API tokens, or secrets present in the prompt
 * are reflected/echoed into the response, or if the response exposes new credentials.
 */
@Component
public class SecretEchoDetector {

    private static final Logger log = LoggerFactory.getLogger(SecretEchoDetector.class);

    private static final Pattern OPENAI_KEY = Pattern.compile("\\b(?:sk_live_|sk_test_|sk-)[A-Za-z0-9]{20,}\\b");
    private static final Pattern AWS_KEY = Pattern.compile("\\bAKIA[0-9A-Z]{16}\\b");
    private static final Pattern GITHUB_TOKEN = Pattern.compile("\\b(?:ghp_|gho_|ghu_|ghs_|ghr_)[A-Za-z0-9]{36}\\b");
    private static final Pattern SLACK_TOKEN = Pattern.compile("\\bxox[baprs]-[A-Za-z0-9-]{20,}\\b");
    private static final Pattern PRIVATE_KEY = Pattern.compile("-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----");
    private static final Pattern PASSWORD_ASSIGNMENT = Pattern.compile(
            "(?i)\\b(?:password|passwd|pwd|secret_key|api_secret)\\s*(?:is|was|=:|:=|=|:)\\s*[\"']?([^\"'\\s,;]{6,})[\"']?"
    );
    private static final Pattern JWT_TOKEN = Pattern.compile("\\beyJ[A-Za-z0-9-_]+\\.eyJ[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\b");

    /**
     * Inspects prompt and response to identify echoed secrets or exposed credentials.
     *
     * @param prompt   the input prompt submitted to the model
     * @param response the model generated completion
     * @return list of secret echo or credential exposure violation messages
     */
    public List<String> detectSecretEchoes(String prompt, String response) {
        List<String> violations = new ArrayList<>();
        if (response == null || response.isBlank()) {
            return violations;
        }

        // 1. Check prompt-to-response echo of specific secret patterns
        checkEcho(OPENAI_KEY, prompt, response, "Response echoes OpenAI API key from prompt", violations);
        checkEcho(AWS_KEY, prompt, response, "Response echoes AWS Access Key ID from prompt", violations);
        checkEcho(GITHUB_TOKEN, prompt, response, "Response echoes GitHub personal access token from prompt", violations);
        checkEcho(SLACK_TOKEN, prompt, response, "Response echoes Slack token from prompt", violations);
        checkEcho(PRIVATE_KEY, prompt, response, "Response echoes private cryptographic key header from prompt", violations);
        checkEcho(JWT_TOKEN, prompt, response, "Response echoes JWT authentication token from prompt", violations);

        // 2. Check password or secret assignment echoes
        if (prompt != null && !prompt.isBlank()) {
            Matcher pwMatcher = PASSWORD_ASSIGNMENT.matcher(prompt);
            while (pwMatcher.find()) {
                String secretValue = pwMatcher.group(1);
                if (secretValue != null && secretValue.length() >= 6 && response.contains(secretValue)) {
                    violations.add("Response echoes credential/password from prompt: [REDACTED]");
                    break;
                }
            }
        }

        // 3. Autonomous credential emission in response even if not directly in prompt
        if (PRIVATE_KEY.matcher(response).find() && !violations.contains("Response echoes private cryptographic key header from prompt")) {
            violations.add("Response contains raw private cryptographic key material");
        }
        if (AWS_KEY.matcher(response).find() && !violations.contains("Response echoes AWS Access Key ID from prompt")) {
            violations.add("Response exposes active AWS Access Key ID");
        }
        if (OPENAI_KEY.matcher(response).find() && !violations.contains("Response echoes OpenAI API key from prompt")) {
            violations.add("Response exposes live API key token");
        }

        if (!violations.isEmpty()) {
            log.warn("Secret echo / credential leakage detected: {}", violations);
        }

        return violations;
    }

    private void checkEcho(Pattern pattern, String prompt, String response, String message, List<String> violations) {
        if (prompt == null || prompt.isBlank()) {
            return;
        }

        Matcher promptMatcher = pattern.matcher(prompt);
        while (promptMatcher.find()) {
            String token = promptMatcher.group();
            if (response.contains(token)) {
                violations.add(message);
                break;
            }
        }
    }
}
