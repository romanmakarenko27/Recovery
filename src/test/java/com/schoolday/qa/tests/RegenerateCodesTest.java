package com.schoolday.qa.tests;

import com.schoolday.qa.base.BaseTest;
import com.schoolday.qa.pages.LoginPage;
import com.schoolday.qa.pages.MfaPage;
import com.schoolday.qa.pages.RecoveryCodePage;
import com.schoolday.qa.pages.RegenerateCodesDialog;
import com.schoolday.qa.pages.UserProfilePage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RegenerateCodesTest extends BaseTest {

    private UserProfilePage userProfilePage;
    private RegenerateCodesDialog regenerateCodesDialog;

    @BeforeAll
    void setUpOnce() throws IOException {
        // Only run when exactly 1 unused code remains — that last code is used to log in,
        // then new codes are regenerated. Skip if there are still plenty of codes left.
        long unusedCount = Files.readAllLines(Path.of("SchoolDay_reset_codes.txt")).stream()
                .map(String::trim)
                .filter(l -> !l.isBlank() && !l.startsWith("USED:"))
                .count();
        assumeTrue(unusedCount == 1,
                "Skipping regeneration: " + unusedCount + " codes remain (runs only when 1 left)");

        super.setUp();

        // Login → MFA → Recovery Code → /admin/institutions
        LoginPage loginPage = new LoginPage(driver);
        MfaPage mfaPage = new MfaPage(driver);
        RecoveryCodePage recoveryCodePage = new RecoveryCodePage(driver);

        loginPage.open(getBaseUrl());
        loginPage.login(getEmail(), getPassword());
        mfaPage.waitForPage();
        mfaPage.clickRecoveryCodeLink();
        recoveryCodePage.waitForPage();

        String code = consumeRecoveryCode();
        System.out.println("Using recovery code: " + code);
        recoveryCodePage.enterRecoveryCode(code);
        recoveryCodePage.clickConfirm();

        try {
            wait.until(d -> d.getCurrentUrl().contains("/admin/institutions"));
        } catch (Exception e) {
            try {
                Files.writeString(Path.of("build/login-failure.html"), driver.getPageSource());
                System.err.println("Login failed. Current URL: " + driver.getCurrentUrl());
                System.err.println("Page source dumped to build/login-failure.html");
            } catch (IOException ioe) {
                System.err.println("Failed to dump page: " + ioe.getMessage());
            }
            throw e;
        }

        userProfilePage = new UserProfilePage(driver);
        regenerateCodesDialog = new RegenerateCodesDialog(driver);
    }

    @Override
    protected void setUp() {
        // No-op: single browser session managed by @BeforeAll / @AfterAll
    }

    @Override
    protected void tearDown() {
        // No-op: single browser session managed by @BeforeAll / @AfterAll
    }

    @AfterAll
    void tearDownOnce() {
        super.tearDown();
    }

    @Test
    @Order(1)
    void testInstitutionsPageDisplayed() {
        assertEquals("https://connect-qa.gg4l.com/admin/institutions", driver.getCurrentUrl(),
                "Should be on /admin/institutions after login");
        wait.until(d -> userProfilePage.isUserMenuButtonDisplayed());
        assertTrue(userProfilePage.isUserMenuButtonDisplayed(),
                "User menu button should be visible in toolbar");
    }

    @Test
    @Order(2)
    void testNavigateToUserProfile() {
        userProfilePage.navigateTo(config.getProperty("base.url"));

        assertTrue(driver.getCurrentUrl().contains("/user/profile"),
                "Should be on /user/profile page");
        assertTrue(userProfilePage.isTabGroupDisplayed(),
                "Tab group should be visible on profile page");
        assertTrue(userProfilePage.isProfileTabDisplayed(),
                "Profile tab should be visible");
        assertTrue(userProfilePage.isSecuritySettingsTabDisplayed(),
                "Security Settings tab should be visible");
        assertTrue(userProfilePage.isEmailNotificationsTabDisplayed(),
                "Email Notifications tab should be visible");
        assertTrue(userProfilePage.isEditOrganizationTabDisplayed(),
                "Edit Organization tab should be visible");
    }

    @Test
    @Order(3)
    void testSecuritySettingsTab() {
        userProfilePage.clickSecuritySettingsTab();

        assertTrue(userProfilePage.isSecuritySettingsContentLoaded(),
                "Security Settings content should be loaded after clicking tab");
    }

    @Test
    @Order(4)
    void testRegenerateCodesDialogElements() {
        regenerateCodesDialog.clickRegenerateButton();

        assertTrue(regenerateCodesDialog.isDialogDisplayed(),
                "Regenerate dialog should be displayed");
        assertTrue(regenerateCodesDialog.isPasswordInputDisplayed(),
                "Password input should be visible in dialog");
        assertTrue(regenerateCodesDialog.isRegenerateButtonDisplayed(),
                "Regenerate button should be visible in dialog");
        assertTrue(regenerateCodesDialog.isCancelButtonDisplayed(),
                "Cancel button should be visible in dialog");
    }

    @Test
    @Order(5)
    void testEnterPasswordAndRegenerate() throws Exception {
        regenerateCodesDialog.enterPassword(getPassword());
        regenerateCodesDialog.clickRegenerate();

        // Wait for dialog to transition from password step to codes display
        regenerateCodesDialog.waitForCodesDialog();

        assertTrue(regenerateCodesDialog.isCodesDialogDisplayed(),
                "Recovery codes dialog should be displayed");
        assertTrue(regenerateCodesDialog.isCloseButtonDisplayed(),
                "Close button should be visible");
        assertTrue(regenerateCodesDialog.isDownloadButtonDisplayed(),
                "Download button should be visible");

        // Dump dialog HTML for debugging
        try {
            String dialogHtml = regenerateCodesDialog.getDialogHtml();
            Files.writeString(Path.of("build/codes-dialog.html"), dialogHtml);
        } catch (IOException e) {
            System.err.println("Failed to dump dialog HTML: " + e.getMessage());
        }

        List<String> newCodes = regenerateCodesDialog.extractNewCodes();

        System.out.println("Extracted " + newCodes.size() + " recovery codes:");
        for (String code : newCodes) {
            System.out.println("  Code: " + code);
        }

        // IMPORTANT: Save codes BEFORE asserting — regeneration already invalidated
        // all previous codes on the server, so we must persist whatever we extracted.
        if (!newCodes.isEmpty()) {
            saveNewRecoveryCodes(newCodes);
        }

        // Download codes (enables Close button) and close the dialog
        regenerateCodesDialog.downloadAndClose();

        assertFalse(newCodes.isEmpty(),
                "New recovery codes should be generated");
        assertEquals(6, newCodes.size(),
                "Should generate exactly 6 recovery codes, but got " + newCodes.size()
                        + ": " + newCodes);
    }

    @Test
    @Order(6)
    void testSaveNewRecoveryCodes() throws Exception {
        List<String> allLines = Files.readAllLines(Path.of("SchoolDay_reset_codes.txt"));

        long codeCount = allLines.stream().filter(l -> !l.isBlank()).count();

        assertEquals(6, codeCount,
                "Should have exactly 6 new codes in file, but found " + codeCount);

        // Verify none are marked as USED (all should be fresh)
        long usedCount = allLines.stream().filter(l -> l.startsWith("USED:")).count();
        assertEquals(0, usedCount,
                "No codes should be marked USED after regeneration");

        System.out.println("Recovery codes file has " + codeCount + " fresh codes");
    }
}
