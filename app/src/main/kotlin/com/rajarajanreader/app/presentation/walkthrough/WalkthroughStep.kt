package com.rajarajanreader.app.presentation.walkthrough

data class WalkthroughStep(
    val anchorKey     : String?,
    val title         : String,
    val body          : String,
    val tooltipBelow  : Boolean        = true,
    val highlightShape: HighlightShape = HighlightShape.ROUND_RECT
)

enum class HighlightShape { CIRCLE, ROUND_RECT }

val indexWalkthroughSteps = listOf(
    WalkthroughStep(
        anchorKey = null,
        title     = "வணக்கம்! ♛",
        body      = "இராசராச சோழனின் வரலாற்றில் உங்களை வரவேற்கிறோம். இந்த சுற்றுப்பயணம் ஐந்து நொடிகளில் முடியும்."
    ),
    WalkthroughStep(
        anchorKey      = "search_icon",
        title          = "தேடல்",
        body           = "தலைப்பு அல்லது வார்த்தையால் எந்த அத்தியாயத்தையும் உடனே கண்டுபிடியுங்கள்.",
        tooltipBelow   = true,
        highlightShape = HighlightShape.CIRCLE
    ),
    WalkthroughStep(
        anchorKey      = "theme_icon",
        title          = "கோலம் மாற்றம்",
        body           = "Imperial · Parchment · Palace — மூன்று அரச கோலங்களில் படிக்கலாம். இந்த பொத்தானை தொடுங்கள்.",
        tooltipBelow   = true,
        highlightShape = HighlightShape.CIRCLE
    ),
    WalkthroughStep(
        anchorKey      = "first_chapter",
        title          = "படிக்கத் தொடங்குங்கள்! ✦",
        body           = "ஒரு அத்தியாயத்தை தொட்டு இராசராச சோழனின் வீர வரலாற்றில் மூழ்குங்கள்.",
        tooltipBelow   = false,
        highlightShape = HighlightShape.ROUND_RECT
    )
)

val readerWalkthroughSteps = listOf(
    WalkthroughStep(
        anchorKey      = "content_center",
        title          = "ஸ்வைப் செய்யுங்கள்!",
        body           = "இடது அல்லது வலது பக்கம் ஸ்வைப் செய்தால் அடுத்த / முந்தைய அத்தியாயம் வரும்.",
        tooltipBelow   = false,
        highlightShape = HighlightShape.ROUND_RECT
    ),
    WalkthroughStep(
        anchorKey      = "content_center",
        title          = "திரை தொடல்",
        body           = "ஒரு முறை தட்டினால் கட்டளைகள் மறையும் — முழு வாசிப்பு அனுபவம் கிடைக்கும்.",
        tooltipBelow   = false,
        highlightShape = HighlightShape.ROUND_RECT
    ),
    WalkthroughStep(
        anchorKey      = "font_icon",
        title          = "எழுத்து அளவு",
        body           = "இந்த பொத்தானை தொட்டு உங்கள் கண்களுக்கு ஏற்ற எழுத்து அளவை தேர்ந்தெடுங்கள்.",
        tooltipBelow   = true,
        highlightShape = HighlightShape.CIRCLE
    ),
    WalkthroughStep(
        anchorKey      = "paragraph_area",
        title          = "பகிர்வு ✦",
        body           = "எந்த பத்தியையும் நீண்ட நேரம் அழுத்தி நண்பர்களுடன் பகிரலாம்! வைரல் தகவலைப் பரப்புங்கள்.",
        tooltipBelow   = false,
        highlightShape = HighlightShape.ROUND_RECT
    )
)
