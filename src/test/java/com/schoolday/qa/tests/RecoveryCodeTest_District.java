package com.schoolday.qa.tests;

import com.schoolday.qa.base.BaseTest;
import com.schoolday.qa.pages.DistrictLoginPage;
import com.schoolday.qa.pages.DistrictMfaPage;
import com.schoolday.qa.pages.DistrictRecoveryCodePage;
import com.schoolday.qa.pages.MainPage;
import com.schoolday.qa.pages.RegenerateCodesDialog;
import com.schoolday.qa.pages.UserProfilePage;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RecoveryCodeTest_District extends BaseTest {

    private DistrictMfaPage mfaPage;
    private DistrictRecoveryCodePage recoveryCodePage;
    private UserProfilePage userProfilePage;
    private RegenerateCodesDialog regenerateCodesDialog;
    private List<String> newCodes;

    @Override
    protected Path getCodesFile() {
        return Path.of("district/SchoolDay_reset_codes.txt");
    }

    @Override
    protected String getBaseUrl() {
        return config.getProperty("district.base.url") + config.getProperty("district.login.path");
    }

    @Override
    protected String getEmail() {
        return config.getProperty("district.test.email");
    }

    @Override
    protected String getPassword() {
        return config.getProperty("district.test.password");
    }

    @Override
    @BeforeEach
    protected void setUp() {
        // no-op — driver lifecycle managed by @BeforeAll/@AfterAll
    }

    @Override
    @AfterEach
    protected void tearDown() {
        // no-op — driver lifecycle managed by @BeforeAll/@AfterAll
    }

    @BeforeAll
    void initDriver() throws Exception {
        super.setUp();

        // Clean download directory so findDownloadedFile won't pick up stale files
        Path dlDir = getDownloadDir();
        if (Files.exists(dlDir)) {
            try (var stream = Files.newDirectoryStream(dlDir)) {
                for (Path file : stream) {
                    Files.deleteIfExists(file);
                }
            }
        }

        districtLogin();
        mfaPage = new DistrictMfaPage(driver);
        mfaPage.waitForPage();
        recoveryCodePage = new DistrictRecoveryCodePage(driver);
        userProfilePage = new UserProfilePage(driver);
        regenerateCodesDialog = new RegenerateCodesDialog(driver);
    }

    @AfterAll
    void closeDriver() {
        super.tearDown();
    }

    private void districtLogin() {
        DistrictLoginPage loginPage = new DistrictLoginPage(driver);
        loginPage.open(getBaseUrl());
        loginPage.login(getEmail(), getEmail(), getPassword());
    }

    // === UI Tests ===

    @Test
    @Order(1)
    void testMfaPageElementsDisplayed() {
        assertTrue(mfaPage.isDisplayed(), "MFA page should be displayed");
        assertEquals("Enter code", mfaPage.getHeadingText(), "Heading should be 'Enter code'");
        assertTrue(mfaPage.isCodeInputDisplayed(), "Code input should be visible");
        assertTrue(mfaPage.isConfirmButtonDisplayed(), "Confirm button should be visible");
        assertTrue(mfaPage.isResendCodeButtonDisplayed(), "Resend Code button should be visible");
        assertTrue(mfaPage.getResendCodeButtonText().contains("Resend Code"),
                "Resend Code button should contain timer text");
        assertTrue(mfaPage.isHavingProblemsTextDisplayed(), "'Having problems?' text should be visible");
        assertTrue(mfaPage.isRecoveryCodeButtonDisplayed(), "Recovery code button should be visible");
    }

    @Test
    @Order(2)
    void testNavigateToRecoveryCodePage() {
        mfaPage.clickRecoveryCodeButton();
        recoveryCodePage.waitForPage();

        assertTrue(recoveryCodePage.isDisplayed(), "Recovery Code page should be displayed");
        assertEquals("Enter Recovery code", recoveryCodePage.getHeadingText(),
                "Heading should be 'Enter Recovery code'");
        assertTrue(recoveryCodePage.getDescriptionText().contains("recovery codes"),
                "Description should mention recovery codes");
        assertTrue(recoveryCodePage.isRecoveryCodeInputDisplayed(), "Recovery code input should be visible");
        assertTrue(recoveryCodePage.isConfirmButtonDisplayed(), "Confirm button should be visible");
    }

    // === Empty submission (no server call, won't trigger lockout) ===

    @Test
    @Order(3)
    void testRecoveryCodeEmptySubmission() {
        String urlBefore = driver.getCurrentUrl();
        recoveryCodePage.clickConfirm();

        assertTrue(recoveryCodePage.hasValidationError(),
                "Validation error should be shown for empty submission");
        assertTrue(recoveryCodePage.isDisplayed(),
                "Should remain on Recovery Code page after empty submission");
        assertEquals(urlBefore, driver.getCurrentUrl(),
                "URL should not change after empty submission");
    }

    // === Invalid code submissions ===

    @Test
    @Order(4)
    void testUsedRecoveryCodeSubmission() throws Exception {
        String usedCode = getUsedRecoveryCode();

        recoveryCodePage.enterRecoveryCode(usedCode);
        recoveryCodePage.clickConfirm();

        assertTrue(recoveryCodePage.hasSnackbarError(),
                "Snackbar error should be shown for used recovery code");

        String errorMessage = recoveryCodePage.getSnackbarErrorMessage();
        assertFalse(errorMessage.isBlank(), "Error message should not be blank");

        recoveryCodePage.dismissSnackbar();
        recoveryCodePage.waitForPage();
    }

    @Test
    @Order(5)
    void testWrongRecoveryCodeSubmission() {
        recoveryCodePage.enterRecoveryCode("fTImfE&beN");
        recoveryCodePage.clickConfirm();

        assertTrue(recoveryCodePage.hasSnackbarError(),
                "Snackbar error should be shown for wrong recovery code");

        String errorMessage = recoveryCodePage.getSnackbarErrorMessage();
        assertFalse(errorMessage.isBlank(), "Error message should not be blank");

        recoveryCodePage.dismissSnackbar();
        recoveryCodePage.waitForPage();
    }

    // === Successful login ===

    @Test
    @Order(6)
    void testSuccessfulRecoveryCodeLogin() throws Exception {
        String recoveryCode = consumeRecoveryCode();

        recoveryCodePage.enterRecoveryCode(recoveryCode);
        recoveryCodePage.clickConfirm();

        // District lands on ETX main page first
        MainPage mainPage = new MainPage(driver);
        mainPage.waitForPage();

        assertTrue(driver.getCurrentUrl().contains("etx-qa.gg4l.com"),
                "Should land on ETX main page after recovery code login, but was: " + driver.getCurrentUrl());

        // Click Connect to navigate to Connect QA
        mainPage.clickConnect();

        wait.until(d -> d.getCurrentUrl().contains("/admin/applications"));

        assertEquals("https://connect-qa.gg4l.com/admin/applications", driver.getCurrentUrl(),
                "Should redirect to /admin/applications after clicking Connect");
    }

    // === Post-Login Tests ===

    @Test
    @Order(7)
    void testNavigateToMyProfile() {
        userProfilePage.clickUserMenu();
        userProfilePage.clickMyProfile();

        assertTrue(driver.getCurrentUrl().contains("/user/profile"),
                "Should navigate to user profile page");
        assertTrue(userProfilePage.isTabGroupDisplayed(), "Tab group should be visible");
        assertTrue(userProfilePage.isProfileTabDisplayed(), "Profile tab should be visible");
        assertTrue(userProfilePage.isSecuritySettingsTabDisplayed(), "Security Settings tab should be visible");
        assertTrue(userProfilePage.isEmailNotificationsTabDisplayed(), "Email Notifications tab should be visible");
        assertTrue(userProfilePage.isEditOrganizationTabDisplayed(), "Edit Organization tab should be visible");
    }

    @Test
    @Order(8)
    void testNavigateToSecuritySettingsAndOpenDialog() {
        userProfilePage.clickSecuritySettingsTab();

        assertTrue(userProfilePage.isSecuritySettingsContentLoaded(),
                "Security settings content should be loaded");

        regenerateCodesDialog.clickRegenerateButton();

        assertTrue(regenerateCodesDialog.isDialogDisplayed(),
                "Regenerate Recovery Codes dialog should be displayed");
    }

    @Test
    @Order(9)
    void testRegenerateDialogElements() {
        assertTrue(regenerateCodesDialog.isDialogDisplayed(), "Dialog should be displayed");
        assertTrue(regenerateCodesDialog.isPasswordInputDisplayed(), "Password input should be visible");
        assertTrue(regenerateCodesDialog.isRegenerateButtonDisplayed(), "Regenerate button should be visible");
        assertTrue(regenerateCodesDialog.isCancelButtonDisplayed(), "Cancel button should be visible");

        String dialogText = regenerateCodesDialog.getDialogText();
        assertTrue(dialogText.contains("Regenerate Recovery Codes"),
                "Dialog should contain title 'Regenerate Recovery Codes'");
        assertTrue(dialogText.contains("enter your password"),
                "Dialog should contain password instruction text");
        assertTrue(dialogText.contains("Old codes won't work after generating new ones"),
                "Dialog should contain warning about old codes");
    }

    @Test
    @Order(10)
    void testRegenerateButtonDisabledWithEmptyPassword() {
        assertFalse(regenerateCodesDialog.isRegenerateButtonEnabled(),
                "Regenerate button should be disabled when password is empty");
    }

    @Test
    @Order(11)
    void testPasswordVisibilityToggle() {
        regenerateCodesDialog.enterPassword("TestPassword");

        assertEquals("password", regenerateCodesDialog.getPasswordInputType(),
                "Password input should be masked by default");

        regenerateCodesDialog.clickPasswordToggle();

        assertEquals("text", regenerateCodesDialog.getPasswordInputType(),
                "Password should be visible after clicking toggle");

        regenerateCodesDialog.clickPasswordToggle();

        assertEquals("password", regenerateCodesDialog.getPasswordInputType(),
                "Password should be masked again after clicking toggle");
    }

    @Test
    @Order(12)
    void testInvalidPasswordThenCancelOrRegenerate() throws Exception {
        regenerateCodesDialog.enterPassword("WrongPassword123!");
        regenerateCodesDialog.clickRegenerate();

        assertTrue(regenerateCodesDialog.hasSnackbarError(),
                "Error should be shown for invalid password");
        assertTrue(regenerateCodesDialog.isSnackbarAtBottom(),
                "Snackbar should appear at the bottom of the page");

        String errorMessage = regenerateCodesDialog.getSnackbarErrorMessage();
        assertFalse(errorMessage.isBlank(), "Error message should not be blank");

        regenerateCodesDialog.dismissSnackbar();

        if (countUnusedCodes() == 0) {
            // No codes left — regenerate using the already-open dialog
            regenerateCodesDialog.enterPassword(getPassword());
            regenerateCodesDialog.clickRegenerate();
            regenerateCodesDialog.waitForCodesDialog();

            newCodes = regenerateCodesDialog.extractNewCodes();

            // Save codes BEFORE asserting — regeneration already invalidated all previous codes
            if (!newCodes.isEmpty()) {
                saveNewRecoveryCodes(newCodes);
            }

            assertFalse(newCodes.isEmpty(), "New recovery codes should be generated");
            assertEquals(6, newCodes.size(),
                    "Should generate exactly 6 recovery codes, but got " + newCodes.size()
                            + ": " + newCodes);
        } else {
            // Codes still available — cancel and close dialog
            regenerateCodesDialog.clickCancel();
            regenerateCodesDialog.waitForDialogClosed();

            assertFalse(regenerateCodesDialog.isDialogDisplayed(),
                    "Dialog should be closed after clicking Cancel");
        }
    }

    @Test
    @Order(13)
    void testCodesDialogElementsDisplayed() {
        assumeTrue(newCodes != null && !newCodes.isEmpty(),
                "No regeneration occurred — skipping codes dialog tests");

        assertEquals("Recovery codes", regenerateCodesDialog.getCodesDialogTitle(),
                "Dialog title should be 'Recovery codes'");

        String description = regenerateCodesDialog.getCodesDialogDescription();
        assertTrue(description.contains("lose access to your device"),
                "Description should mention losing access to device");
        assertTrue(description.contains("Each code can be used only once"),
                "Description should mention each code can be used only once");

        assertTrue(regenerateCodesDialog.isWarningIconDisplayed(),
                "Warning icon should be displayed");
        String warningText = regenerateCodesDialog.getCodesDialogWarningText();
        assertTrue(warningText.contains("Put these in a safe spot"),
                "Warning should mention putting codes in a safe spot");

        String instructionText = regenerateCodesDialog.getCodesDialogInstructionText();
        assertTrue(instructionText.contains("Download, print, or copy"),
                "Instruction text should mention download, print, or copy");
    }

    @Test
    @Order(14)
    void testSixUniqueCodesDifferentFromOld() throws Exception {
        assumeTrue(newCodes != null && !newCodes.isEmpty(),
                "No regeneration occurred — skipping codes dialog tests");

        assertEquals(6, newCodes.size(),
                "Should have exactly 6 recovery codes");

        for (String code : newCodes) {
            assertFalse(code.isBlank(), "Each recovery code should be non-blank");
        }

        assertEquals(newCodes.size(), new HashSet<>(newCodes).size(),
                "All recovery codes should be unique");

        List<String> oldCodes = getOldCodes();
        for (String code : newCodes) {
            assertFalse(oldCodes.contains(code),
                    "New code '" + code + "' should not match any old code");
        }
    }

    @Test
    @Order(15)
    void testActionButtonsDisplayed() {
        assumeTrue(newCodes != null && !newCodes.isEmpty(),
                "No regeneration occurred — skipping codes dialog tests");

        assertTrue(regenerateCodesDialog.isDownloadButtonDisplayed(),
                "Download button should be visible");
        assertEquals("file_download", regenerateCodesDialog.getDownloadButtonIconText(),
                "Download button should have file_download icon");
        assertEquals("Download Recovery codes", regenerateCodesDialog.getDownloadButtonAriaLabel(),
                "Download button should have correct aria-label");

        assertTrue(regenerateCodesDialog.isPrintButtonDisplayed(),
                "Print button should be visible");
        assertEquals("print", regenerateCodesDialog.getPrintButtonIconText(),
                "Print button should have print icon");
        assertEquals("Print Recovery codes", regenerateCodesDialog.getPrintButtonAriaLabel(),
                "Print button should have correct aria-label");

        assertTrue(regenerateCodesDialog.isCopyButtonDisplayed(),
                "Copy button should be visible");
        assertEquals("content_copy", regenerateCodesDialog.getCopyButtonIconText(),
                "Copy button should have content_copy icon");
        assertEquals("Copy Recovery codes", regenerateCodesDialog.getCopyButtonAriaLabel(),
                "Copy button should have correct aria-label");
    }

    @Test
    @Order(16)
    void testClickOutsideDialogDoesNotClose() {
        assumeTrue(newCodes != null && !newCodes.isEmpty(),
                "No regeneration occurred — skipping codes dialog tests");

        regenerateCodesDialog.clickBackdrop();

        assertTrue(regenerateCodesDialog.isCodesDialogDisplayed(),
                "Codes dialog should remain open after clicking outside");
    }

    @Test
    @Order(17)
    void testCloseButtonDisabledBeforeSaveAction() {
        assumeTrue(newCodes != null && !newCodes.isEmpty(),
                "No regeneration occurred — skipping codes dialog tests");

        assertTrue(regenerateCodesDialog.isCloseButtonDisplayed(),
                "Close button should be visible");
        assertFalse(regenerateCodesDialog.isCloseButtonEnabled(),
                "Close button should be disabled before clicking Download/Print/Copy");
    }

    @Test
    @Order(18)
    void testDownloadEnablesCloseButton() {
        assumeTrue(newCodes != null && !newCodes.isEmpty(),
                "No regeneration occurred — skipping codes dialog tests");

        regenerateCodesDialog.clickDownloadButton();

        assertTrue(regenerateCodesDialog.isCloseButtonEnabled(),
                "Close button should be enabled after clicking Download");
    }

    @Test
    @Order(19)
    void testAllSourcesReturnSameCodes() throws IOException {
        assumeTrue(newCodes != null && !newCodes.isEmpty(),
                "No regeneration occurred — skipping codes dialog tests");

        List<String> dialogCodes = newCodes;

        Path downloadDir = getDownloadDir();
        Path downloadedFile = regenerateCodesDialog.findDownloadedFile(downloadDir);
        assertNotNull(downloadedFile, "Downloaded file should exist in " + downloadDir);
        List<String> downloadCodes = regenerateCodesDialog.extractCodesFromDownloadedFile(downloadedFile);

        regenerateCodesDialog.clickPrintSuppressed();
        assertTrue(regenerateCodesDialog.wasPrintTriggered(),
                "Print button should trigger window.print() or window.open()");

        regenerateCodesDialog.clickCopyButton();

        assertTrue(regenerateCodesDialog.hasCopySnackbar(),
                "Snackbar should appear after clicking Copy");
        String snackbarMessage = regenerateCodesDialog.getCopySnackbarMessage();
        assertTrue(snackbarMessage.contains("Copied to Clipboard"),
                "Snackbar should show 'Copied to Clipboard', but was: " + snackbarMessage);

        List<String> clipboardCodes = regenerateCodesDialog.readClipboardCodes();
        regenerateCodesDialog.dismissSnackbar();

        assertEquals(dialogCodes, downloadCodes,
                "Downloaded file codes should match dialog codes");
        assertEquals(dialogCodes.size(), clipboardCodes.size(),
                "Copied codes count should match displayed codes count");
        for (int i = 0; i < dialogCodes.size(); i++) {
            assertEquals(dialogCodes.get(i), clipboardCodes.get(i),
                    "Copied code at index " + i + " should match displayed code");
        }
    }

    @Test
    @Order(20)
    void testCloseDialogAndVerifyCodesSaved() throws Exception {
        assumeTrue(newCodes != null && !newCodes.isEmpty(),
                "No regeneration occurred — skipping codes dialog tests");

        regenerateCodesDialog.dismissSnackbar();
        regenerateCodesDialog.clickCloseButton();

        assertFalse(regenerateCodesDialog.isDialogDisplayed(),
                "Dialog should be closed after clicking Close");

        long unusedCount = countUnusedCodes();
        assertEquals(6, unusedCount,
                "File should contain 6 unused recovery codes after regeneration");
    }

}
