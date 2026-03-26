package com.schoolday.qa.base;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

public abstract class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected Properties config;

    private static final Path CODES_FILE = Path.of("SchoolDay_reset_codes.txt");
    private static final Path DOWNLOAD_DIR = Path.of("build/downloads").toAbsolutePath();

    @BeforeEach
    protected void setUp() throws IOException {
        config = loadProperties();

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
        if ("true".equals(config.getProperty("browser.headless"))) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--start-maximized");

        // Configure download directory for file download tests
        Files.createDirectories(DOWNLOAD_DIR);
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", DOWNLOAD_DIR.toString());
        prefs.put("download.prompt_for_download", false);
        options.setExperimentalOption("prefs", prefs);

        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    protected void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    protected String getBaseUrl() {
        return config.getProperty("base.url") + config.getProperty("login.path");
    }

    protected String getEmail() {
        return config.getProperty("test.email");
    }

    protected String getPassword() {
        return config.getProperty("test.password");
    }

    protected Path getDownloadDir() {
        return DOWNLOAD_DIR;
    }

    /**
     * Reads the next unused recovery code from SchoolDay_reset_codes.txt in the project root.
     * Marks the consumed code with USED: prefix so subsequent calls skip it.
     */
    protected String consumeRecoveryCode() throws IOException {
        List<String> lines = new ArrayList<>(Files.readAllLines(CODES_FILE));

        String code = null;
        int codeIndex = -1;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.isBlank() && !line.startsWith("USED:") && !line.startsWith("INVALIDATED:")) {
                code = line;
                codeIndex = i;
                break;
            }
        }

        if (code == null) {
            throw new IllegalStateException("No recovery codes remaining in " + CODES_FILE);
        }

        lines.set(codeIndex, "USED:" + code);
        Files.write(CODES_FILE, lines);

        return code;
    }

    /**
     * Returns a previously used recovery code (one already marked with USED: prefix).
     */
    protected String getUsedRecoveryCode() throws IOException {
        List<String> lines = Files.readAllLines(CODES_FILE);

        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("USED:")) {
                return trimmed.substring("USED:".length());
            }
        }

        throw new IllegalStateException("No USED recovery codes found in " + CODES_FILE);
    }

    /**
     * Checks Chrome performance logs for a Network.responseReceived event with the expected HTTP status.
     */
    protected boolean hasHttpStatus(int expectedStatus) {
        List<LogEntry> logs = driver.manage().logs().get(LogType.PERFORMANCE).getAll();

        for (LogEntry entry : logs) {
            JsonObject json = JsonParser.parseString(entry.getMessage()).getAsJsonObject();
            JsonObject message = json.getAsJsonObject("message");

            if (message == null) continue;

            String method = message.has("method") ? message.get("method").getAsString() : "";
            if (!"Network.responseReceived".equals(method)) continue;

            JsonObject params = message.getAsJsonObject("params");
            if (params == null) continue;

            JsonObject response = params.getAsJsonObject("response");
            if (response == null) continue;

            JsonElement statusElement = response.get("status");
            if (statusElement != null && statusElement.getAsInt() == expectedStatus) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns true if the codes file contains at least one USED: code.
     */
    protected static boolean hasUsedRecoveryCodes() throws IOException {
        return Files.readAllLines(CODES_FILE).stream()
                .anyMatch(l -> l.trim().startsWith("USED:"));
    }

    /**
     * Counts unused (not USED:-prefixed, not blank) recovery codes in the codes file.
     */
    protected static long countUnusedCodes() throws IOException {
        return Files.readAllLines(CODES_FILE).stream()
                .map(String::trim)
                .filter(l -> !l.isBlank() && !l.startsWith("USED:") && !l.startsWith("INVALIDATED:"))
                .count();
    }

    /**
     * Saves newly generated recovery codes to SchoolDay_reset_codes.txt.
     * Marks old unused codes as INVALIDATED: (since regeneration invalidates them on the server),
     * keeps USED: codes for reference, and appends new codes at the end.
     */
    protected void saveNewRecoveryCodes(List<String> codes) throws IOException {
        List<String> existingLines = Files.exists(CODES_FILE)
                ? new ArrayList<>(Files.readAllLines(CODES_FILE))
                : new ArrayList<>();

        // Collect all old codes (USED + INVALIDATED + unused) and keep only the last 6
        List<String> oldCodes = existingLines.stream()
                .map(String::trim)
                .filter(l -> !l.isBlank())
                .map(l -> {
                    if (l.startsWith("USED:")) return l;
                    if (l.startsWith("INVALIDATED:")) return "INVALIDATED:" + l.substring("INVALIDATED:".length());
                    return "INVALIDATED:" + l;  // mark old unused codes as invalidated
                })
                .toList();

        // Keep only the last 6 old codes
        List<String> kept = new ArrayList<>(
                oldCodes.subList(Math.max(0, oldCodes.size() - 6), oldCodes.size()));

        // Append new codes
        kept.addAll(codes);
        Files.write(CODES_FILE, kept);
    }

    /**
     * Returns all old codes (USED and INVALIDATED) from the codes file for comparison.
     */
    protected static List<String> getOldCodes() throws IOException {
        if (!Files.exists(CODES_FILE)) return List.of();
        return Files.readAllLines(CODES_FILE).stream()
                .map(String::trim)
                .filter(l -> l.startsWith("USED:") || l.startsWith("INVALIDATED:"))
                .map(l -> l.startsWith("USED:") ? l.substring("USED:".length()) :
                        l.substring("INVALIDATED:".length()))
                .toList();
    }

    private Properties loadProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("test.properties")) {
            if (is == null) {
                throw new IOException("test.properties not found on classpath");
            }
            props.load(is);
        }
        return props;
    }
}
