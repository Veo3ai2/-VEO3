package com.example.languagepartner.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Partner(
    val id: String,
    val name: String,
    val language: String,
    val languageName: String,
    val flag: String,
    val voiceName: String,
    val role: String,
    val description: String,
    val welcomeMessage: String,
    val suggestedTopics: List<String>
)

object PartnerCatalog {
    val PARTNERS: List<Partner> = listOf(
        Partner(
            id = "elena",
            name = "إيلينا (Elena)",
            language = "es-ES",
            languageName = "الإسبانية",
            flag = "🇪🇸",
            voiceName = "Kore",
            role = "مرشدة ثقافية ومحلية في إشبيلية",
            description = "إيلينا متحدثة مرحة وودودة، تعشق تعريفك بتاريخ الأندلس وثقافة الفلامنكو والتاباس في إشبيلية. تتحدث إسبانية واضحة وسهلة الفهم للمتعلمين.",
            welcomeMessage = "¡Hola! Qué alegría conocerte. Me llamo Elena. ¿Te gustaría hablar sobre gastronomía, viajes o simplemente practicar tu español del día a día?",
            suggestedTopics = listOf("الفلامنكو والفنون", "التاباس والمأكولات الإسبانية", "المهرجانات الصيفية", "الروتين اليومي")
        ),
        Partner(
            id = "marie",
            name = "ماري (Marie)",
            language = "fr-FR",
            languageName = "الفرنسية",
            flag = "🇫🇷",
            voiceName = "Puck",
            role = "منظمة معارض فنية في باريس",
            description = "ماري شخصية أنيقة ومبدعة، تتحدث الفرنسية الباريسية الطبيعية بطلاقة. تعشق الفن المعاصر، والطهي الفرنسي، والسينما الكلاسيكية وتساعدك على إتقان العبارات الشائعة.",
            welcomeMessage = "Bonjour ! Enchantée de faire votre connaissance. Je m'appelle Marie. De quoi aimeriez-vous parler aujourd'hui ? On peut parler d'art, de projets littéraires ou de votre journée !",
            suggestedTopics = listOf("صالات الفنون في باريس", "فن الطهي الفرنسي", "السينما والأزياء", "السفر والثقافة")
        ),
        Partner(
            id = "yuki",
            name = "يوكي (結衣)",
            language = "ja-JP",
            languageName = "اليابانية",
            flag = "🇯🇵",
            voiceName = "Kore",
            role = "مالكة مقهى وشاي تقليدي في كيوتو",
            description = "يوكي هادئة ومتفهمة للغاية. تعشق طقوس شاي الماتشا، والأدب الياباني الكلاسيكي، وصناعة الخزف. تقدم دائماً إرشادات صوتية وكتابية مبسطة.",
            welcomeMessage = "こんにちは！お会いできて嬉しいです。京都でカフェを営んでいる結衣（ユキ）と申します。日本の文化や美味しいお茶、あるいは最近の出来事について気軽にお話ししましょう。",
            suggestedTopics = listOf("معابد وحدائق كيوتو", "الشاي والحلويات التقليدية", "الحياة اليومية في اليابان", "الطبيعة وفصول السنة")
        ),
        Partner(
            id = "lucas",
            name = "لوكاس (Lucas)",
            language = "de-DE",
            languageName = "الألمانية",
            flag = "🇩🇪",
            voiceName = "Fenrir",
            role = "مهندس معماري للطاقة المستدامة في برلين",
            description = "لوكاس منظم وودود. يحب التصميم المعماري الحديث والتقنيات البيئية وركوب الدراجات في برلين والتنزه في الطبيعة. يتحدث بنبرة واضحة ومخارج حروف متقنة.",
            welcomeMessage = "Hallo! Schön, dich kennenzulernen. Ich bin Lucas. Lass uns ein angenehmes Gespräch auf Deutsch führen. Interessierst du dich für Umwelt, Technik, oder wie läuft dein Tag?",
            suggestedTopics = listOf("العمارة البيئية الحديثة", "ثقافة الدراجات في برلين", "نصائح للألمانية المهنية", "الهوايات الخارجية")
        ),
        Partner(
            id = "giulia",
            name = "جوليا (Giulia)",
            language = "it-IT",
            languageName = "الإيطالية",
            flag = "🇮🇹",
            voiceName = "Zephyr",
            role = "مصممة أزياء وأقمشة راقية في ميلانو",
            description = "جوليا فنانة شغوفة تتحدث بإيقاع إيطالي ساحر ومفعم بالحيوية. تعشق السينما الإيطالية وأزياء ميلانو والسفر على شواطئ إيطاليا الساحرة وتساعدك على التحدث بعفوية.",
            welcomeMessage = "Ciao! Che bello essere qui con te. Mi chiamo Giulia. Parliamo un po' di moda, di viaggi in Italia o delle tue passioni. Il mio scopo è aiutarti a parlare con naturalezza!",
            suggestedTopics = listOf("الريفييرا الإيطالية", "صيحات الموضة في ميلانو", "السينما الإيطالية المعاصرة", "ثقافة القهوة الإيطالية")
        )
    )
}
