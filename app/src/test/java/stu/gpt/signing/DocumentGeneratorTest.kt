package stu.gpt.signing

import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.junit.Test
import java.io.FileOutputStream
import java.io.File

class DocumentGeneratorTest {

    @Test
    fun generateDocx() {
        val docxFile = File("Sign-In_v4.0_User_Guide.docx")
        XWPFDocument().use { doc ->
            val p1 = doc.createParagraph()
            val r1 = p1.createRun()
            r1.setText("Sign-In v4.0 – Professional User & Technical Guide")
            r1.isBold = true
            r1.fontSize = 20
            r1.color = "1F4E78"

            val pIntro = doc.createParagraph()
            pIntro.createRun().setText("This comprehensive guide covers installation, configuration, themes, data management, tablet auto-start troubleshooting, and sign-in safeguards for Sign-In v4.0.")

            addSection(doc, "1. Main Screen & Sign-In Safeguards", listOf(
                "Name entry with predictive dropdown from the Members database.",
                "Duplicate prevention safeguard: pops up an 'Already Signed In' warning and resets the name box if someone tries to check in twice.",
                "Guest validation rule (AA AAA format).",
                "Role and Location selection."
            ))

            addSection(doc, "2. Theme Modes & RGB Color Engine", listOf(
                "Four mode-driven themes from left to right: Theatre (#2BBCF2), Rehearsals (#75FDB4), Youth (#85EFC8), and Dark (#71A0F1).",
                "Built-in RGB color selection engine with real-time sliders and live color preview.",
                "Automatic high-contrast text coloring based on perceptual luminance (white on dark backgrounds, black on light backgrounds)."
            ))

            addSection(doc, "3. Tablet Auto-Start & Battery Optimization", listOf(
                "App automatically starts up and shows over the lock screen on device reboot.",
                "Tablet Note: On certain tablets (Samsung, Lenovo, etc.), OEM background process managers may terminate apps launched automatically via boot receivers. To ensure reliability, go to Settings > Apps > Sign-In v4.0 > Battery, set it to 'Unrestricted', and enable Auto-start / Allow background activity in device care."
            ))

            addSection(doc, "4. Data Management & Warehouse Mode", listOf(
                "Excel (.xlsx) integration stored in Documents/Sign-In Data/.",
                "Members (Cols A & B), Cast (Row 1 Title, Row 2 Date, Row 3+ Names), Roles, Locations, Titles, and Questions.",
                "Warehouse mode switchable in Admin for dedicated Warehouse worksheets (GPT/YT toggle)."
            ))

            FileOutputStream(docxFile).use { out ->
                doc.write(out)
            }
        }
    }

    private fun addSection(doc: XWPFDocument, title: String, bullets: List<String>) {
        val pTitle = doc.createParagraph()
        pTitle.setSpacingBefore(200)
        val rTitle = pTitle.createRun()
        rTitle.setText(title)
        rTitle.isBold = true
        rTitle.fontSize = 14
        rTitle.color = "2F5597"

        for (bullet in bullets) {
            val pB = doc.createParagraph()
            pB.style = "ListBullet"
            val rB = pB.createRun()
            rB.setText(bullet)
            rB.fontSize = 11
        }
    }
}
