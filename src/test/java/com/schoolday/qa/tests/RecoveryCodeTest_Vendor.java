package com.schoolday.qa.tests;

import com.schoolday.qa.base.BaseTest;
import com.schoolday.qa.pages.LoginPage;
import com.schoolday.qa.pages.MfaPage;
import com.schoolday.qa.pages.RecoveryCodePage;
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

@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class RecoveryCodeTest_Vendor extends BaseTest {

    @Override
    @BeforeEach
    protected void setUp() {
        // no-op — driver lifecycle managed by nested classes
    }

    @Override
    @AfterEach
    protected void tearDown() {
        // no-op — driver lifecycle managed by nested classes
    }

    protected void initializeDriver() throws Exception {
        super.setUp();
    }

    protected void destroyDriver() {
        super.tearDown();
    }

    @Nested
    @Order(1)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("UI Tests (1-4)")
    class UiTests {

        private MfaPage mfaPage;
        private RecoveryCodePage recoveryCodePage;

        @BeforeAll
        void initDriver() throws Exception {
            initializeDriver();
            LoginPage loginPage = new LoginPage(driver);
            loginPage.open(getBaseUrl());
            loginPage.login(getEmail(), getPassword());
            mfaPage = new MfaPage(driver);
            mfaPage.waitForPage();
            recoveryCodePage = new RecoveryCodePage(driver);
        }

        @AfterAll
        void closeDriver() {
            destroyDriver();
        }

        @Test
        @Order(1)
        void testMfaPageElementsDisplayed() {
            assertTrue(mfaPage.isDisplayed(), "MFA page should be displayed");
            assertEquals("Enter code", mfaPage.getHeadingText(), "Heading should be 'Enter code'");
            assertTrue(mfaPage.getSmsInfoText().contains("SMS"), "SMS info text should mention SMS");
            assertTrue(mfaPage.getSmsInfoText().contains("111"), "SMS info text should contain masked phone ending in 111");
            assertTrue(mfaPage.isCodeInputDisplayed(), "Code input should be visible");
            assertTrue(mfaPage.isConfirmButtonDisplayed(), "Confirm button should be visible");
            assertTrue(mfaPage.isSendNewCodeButtonDisplayed(), "Send New Code button should be visible");
            assertTrue(mfaPage.getSendNewCodeButtonText().contains("Send New Code"),
                    "Send New Code button should contain timer text");
            assertTrue(mfaPage.isHavingProblemsTextDisplayed(), "'Having problems?' text should be visible");
            assertTrue(mfaPage.isRecoveryCodeLinkDisplayed(), "Recovery code link should be visible");
        }

        @Test
        @Order(2)
        void testNavigateToRecoveryCodePage() {
            mfaPage.clickRecoveryCodeLink();
            recoveryCodePage.waitForPage();

            assertTrue(recoveryCodePage.isDisplayed(), "Recovery Code page should be displayed");
            assertEquals("Enter Recovery code", recoveryCodePage.getHeadingText(),
                    "Heading should be 'Enter Recovery code'");
            assertTrue(recoveryCodePage.getDescriptionText().contains("recovery codes"),
                    "Description should mention recovery codes");
            assertTrue(recoveryCodePage.isRecoveryCodeInputDisplayed(), "Recovery code input should be visible");
            assertTrue(recoveryCodePage.isConfirmButtonDisplayed(), "Confirm button should be visible");
            assertTrue(recoveryCodePage.isContactSupportLinkDisplayed(), "Contact Support link should be visible");
        }

        @Test
        @Order(3)
        void testNavigateBackToMfaFromRecovery() {
            recoveryCodePage.clickBackToMfa();

            mfaPage.waitForPage();
            assertTrue(mfaPage.isDisplayed(), "Should return to MFA page");
            assertEquals("Enter code", mfaPage.getHeadingText(), "MFA heading should be 'Enter code'");
        }

        @Test
        @Order(4)
        void testContactSupportLink() {
            mfaPage.clickRecoveryCodeLink();
            recoveryCodePage.waitForPage();

            String href = recoveryCodePage.getContactSupportHref();
            assertTrue(href.startsWith("https://share.hsforms.com/"),
                    "Contact Support href should point to hsforms.com, but was: " + href);

            recoveryCodePage.clickContactSupport();

            wait.until(d -> d.getCurrentUrl().startsWith("https://share.hsforms.com/"));
            assertTrue(driver.getCurrentUrl().startsWith("https://share.hsforms.com/"),
                    "Should navigate to hsforms.com, but URL was: " + driver.getCurrentUrl());
        }
    }

    @Nested
    @Order(2)
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @DisplayName("Validation, Login & Regenerate Tests (5-23)")
    class ValidationLoginAndRegenerateTests {

        private RecoveryCodePage recoveryCodePage;
        private UserProfilePage userProfilePage;
        private RegenerateCodesDialog regenerateCodesDialog;
        private List<String> newCodes;

        @BeforeAll
        void initDriver() throws Exception {
            initializeDriver();

            // Clean download directory so findDownloadedFile won't pick up stale files
            Path dlDir = getDownloadDir();
            if (Files.exists(dlDir)) {
                try (var stream = Files.newDirectoryStream(dlDir)) {
                    for (Path file : stream) {
                        Files.deleteIfExists(file);
                    }
                }
            }

            LoginPage loginPage = new LoginPage(driver);
            loginPage.open(getBaseUrl());
            loginPage.login(getEmail(), getPassword());
            MfaPage mfaPage = new MfaPage(driver);
            mfaPage.waitForPage();
            mfaPage.clickRecoveryCodeLink();
            recoveryCodePage = new RecoveryCodePage(driver);
            recoveryCodePage.waitForPage();

            userProfilePage = new UserProfilePage(driver);
            regenerateCodesDialog = new RegenerateCodesDialog(driver);
        }

        @AfterAll
        void closeDriver() {
            destroyDriver();
        }

        @Test
        @Order(5)
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

        @Test
        @Order(6)
        void testRecoveryCodeShortCode() {
            String urlBefore = driver.getCurrentUrl();
            recoveryCodePage.enterRecoveryCode("ABC123");
            recoveryCodePage.clickConfirm();

            assertTrue(recoveryCodePage.hasValidationError(),
                    "Validation error should be shown for short code");
            assertTrue(recoveryCodePage.isDisplayed(),
                    "Should remain on Recovery Code page after short code submission");
            assertEquals(urlBefore, driver.getCurrentUrl(),
                    "URL should not change after short code submission");
        }

        @Test
        @Order(7)
        void testRecoveryCodeInvalidCode() {
            recoveryCodePage.enterRecoveryCode("PK9P%vuMbv");
            recoveryCodePage.clickConfirm();

            assertTrue(recoveryCodePage.hasSnackbarError(),
                    "Snackbar error should be shown for invalid recovery code");

            String errorMessage = recoveryCodePage.getSnackbarErrorMessage();
            assertFalse(errorMessage.isBlank(), "Error message should not be blank");

            assertTrue(hasHttpStatus(403),
                    "Server should respond with 403 for invalid recovery code");
        }

        @Test
        @Order(8)
        void testAlreadyUsedRecoveryCode() throws Exception {
            assumeTrue(hasUsedRecoveryCodes(),
                    "No USED recovery codes in file — skipping");

            recoveryCodePage.dismissSnackbar();

            String usedCode = getUsedRecoveryCode();
            recoveryCodePage.enterRecoveryCode(usedCode);
            recoveryCodePage.clickConfirm();

            assertTrue(recoveryCodePage.hasSnackbarError(),
                    "Snackbar error should be shown for already-used recovery code");

            String errorMessage = recoveryCodePage.getSnackbarErrorMessage();
            assertFalse(errorMessage.isBlank(), "Error message should not be blank");

            assertTrue(hasHttpStatus(403),
                    "Server should respond with 403 for already-used recovery code");
        }

        @Test
        @Order(9)
        void testSuccessfulRecoveryCodeLogin() throws Exception {
            recoveryCodePage.dismissSnackbar();

            String recoveryCode = consumeRecoveryCode();

            recoveryCodePage.enterRecoveryCode(recoveryCode);
            recoveryCodePage.clickConfirm();

            wait.until(d -> d.getCurrentUrl().contains("/admin/institutions"));

            assertEquals("https://connect-qa.gg4l.com/admin/institutions", driver.getCurrentUrl(),
                    "Should redirect to /admin/institutions after successful recovery code login");
        }

        @Test
        @Order(10)
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
        @Order(11)
        void testNavigateToSecuritySettingsAndOpenDialog() {
            userProfilePage.clickSecuritySettingsTab();

            assertTrue(userProfilePage.isSecuritySettingsContentLoaded(),
                    "Security settings content should be loaded");

            regenerateCodesDialog.clickRegenerateButton();

            assertTrue(regenerateCodesDialog.isDialogDisplayed(),
                    "Regenerate Recovery Codes dialog should be displayed");
        }

        @Test
        @Order(12)
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
        @Order(13)
        void testRegenerateButtonDisabledWithEmptyPassword() {
            assertFalse(regenerateCodesDialog.isRegenerateButtonEnabled(),
                    "Regenerate button should be disabled when password is empty");
        }

        @Test
        @Order(14)
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
        @Order(15)
        void testInvalidPasswordThenCancelOrRegenerate() throws Exception {
            // Dialog is already open from test 14
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

                // Dialog stays open for tests 16-23 to verify codes dialog

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
        @Order(16)
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
        @Order(17)
        void testSixUniqueCodesDifferentFromOld() throws Exception {
            assumeTrue(newCodes != null && !newCodes.isEmpty(),
                    "No regeneration occurred — skipping codes dialog tests");

            assertEquals(6, newCodes.size(),
                    "Should have exactly 6 recovery codes");

            // All codes should be non-blank
            for (String code : newCodes) {
                assertFalse(code.isBlank(), "Each recovery code should be non-blank");
            }

            // All codes should be unique
            assertEquals(newCodes.size(), new HashSet<>(newCodes).size(),
                    "All recovery codes should be unique");

            // New codes should differ from all old codes (USED + INVALIDATED)
            List<String> oldCodes = getOldCodes();
            for (String code : newCodes) {
                assertFalse(oldCodes.contains(code),
                        "New code '" + code + "' should not match any old code");
            }
        }

        @Test
        @Order(18)
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
        @Order(19)
        void testClickOutsideDialogDoesNotClose() {
            assumeTrue(newCodes != null && !newCodes.isEmpty(),
                    "No regeneration occurred — skipping codes dialog tests");

            regenerateCodesDialog.clickBackdrop();

            assertTrue(regenerateCodesDialog.isCodesDialogDisplayed(),
                    "Codes dialog should remain open after clicking outside");
        }

        @Test
        @Order(20)
        void testCloseButtonDisabledBeforeSaveAction() {
            assumeTrue(newCodes != null && !newCodes.isEmpty(),
                    "No regeneration occurred — skipping codes dialog tests");

            assertTrue(regenerateCodesDialog.isCloseButtonDisplayed(),
                    "Close button should be visible");
            assertFalse(regenerateCodesDialog.isCloseButtonEnabled(),
                    "Close button should be disabled before clicking Download/Print/Copy");
        }

        @Test
        @Order(21)
        void testDownloadEnablesCloseButton() {
            assumeTrue(newCodes != null && !newCodes.isEmpty(),
                    "No regeneration occurred — skipping codes dialog tests");

            regenerateCodesDialog.clickDownloadButton();

            assertTrue(regenerateCodesDialog.isCloseButtonEnabled(),
                    "Close button should be enabled after clicking Download");
        }

        @Test
        @Order(22)
        void testAllSourcesReturnSameCodes() throws IOException {
            assumeTrue(newCodes != null && !newCodes.isEmpty(),
                    "No regeneration occurred — skipping codes dialog tests");

            // 1. Dialog codes (already extracted in test 15)
            List<String> dialogCodes = newCodes;

            // 2. Download — find the file that was downloaded in test 21
            Path downloadDir = getDownloadDir();
            Path downloadedFile = regenerateCodesDialog.findDownloadedFile(downloadDir);
            assertNotNull(downloadedFile, "Downloaded file should exist in " + downloadDir);
            List<String> downloadCodes = regenerateCodesDialog.extractCodesFromDownloadedFile(downloadedFile);

            // 3. Print — suppress native dialog, verify button triggers window.print()
            regenerateCodesDialog.clickPrintSuppressed();
            assertTrue(regenerateCodesDialog.wasPrintTriggered(),
                    "Print button should trigger window.print() or window.open()");

            // 4. Copy → snackbar + clipboard codes
            regenerateCodesDialog.clickCopyButton();

            assertTrue(regenerateCodesDialog.hasCopySnackbar(),
                    "Snackbar should appear after clicking Copy");
            String snackbarMessage = regenerateCodesDialog.getCopySnackbarMessage();
            assertTrue(snackbarMessage.contains("Copied to Clipboard"),
                    "Snackbar should show 'Copied to Clipboard', but was: " + snackbarMessage);

            List<String> clipboardCodes = regenerateCodesDialog.readClipboardCodes();
            regenerateCodesDialog.dismissSnackbar();

            // Assert all sources match dialog codes
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
        @Order(23)
        void testCloseDialogAndVerifyCodesSaved() throws Exception {
            assumeTrue(newCodes != null && !newCodes.isEmpty(),
                    "No regeneration occurred — skipping codes dialog tests");

            regenerateCodesDialog.dismissSnackbar();
            regenerateCodesDialog.clickCloseButton();

            assertFalse(regenerateCodesDialog.isDialogDisplayed(),
                    "Dialog should be closed after clicking Close");

            // Verify codes were persisted to file
            long unusedCount = countUnusedCodes();
            assertEquals(6, unusedCount,
                    "File should contain 6 unused recovery codes after regeneration");
        }
    }
}
