package com.deutrust.deutrust.infrastructure.db

import com.deutrust.deutrust.domain.model.*
import com.deutrust.deutrust.infrastructure.persistence.*
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DatabaseInitializer(
    private val categoryRepository: CategoryRepository,
    private val serviceRepository: ServiceRepository,
    private val requirementRepository: ServiceRequirementRepository,
    private val faqRepository: ServiceFaqRepository,
    private val stepRepository: ServiceStepRepository
) : CommandLineRunner {

    @Transactional
    override fun run(vararg args: String) {
        if (categoryRepository.count() > 0) {
            return
        }

        // Initialize Categories
        val c1 = categoryRepository.save(Category(
            nameDe = "Einwohnermeldeamt",
            nameEn = "Resident Registration",
            link = "/categories/einwohnermeldeamt",
            icon = "home_work",
            descriptionDe = "Alle wichtigen Dienstleistungen rund um Meldewesen, Ausweise und Bescheinigungen im Freistaat Sachsen. Erledigen Sie viele Behördengänge digital oder bereiten Sie Ihren Termin vor Ort optimal vor.",
            descriptionEn = "All important services related to registration, identity cards, and certificates in the Free State of Saxony. Complete many administrative procedures digitally or prepare your on-site appointment optimally."
        ))
        val c2 = categoryRepository.save(Category(
            nameDe = "Familie & Kind",
            nameEn = "Family & Child",
            link = "/categories/familie-kind",
            icon = "family_restroom",
            descriptionDe = "Leistungen für Familien, Eltern und Kinder wie Geburtsanmeldungen und finanzielle Unterstützungen.",
            descriptionEn = "Services for families, parents, and children such as birth registrations and financial support."
        ))
        val c3 = categoryRepository.save(Category(
            nameDe = "Mobilität & Verkehr",
            nameEn = "Mobility & Transport",
            link = "/categories/mobilitaet-verkehr",
            icon = "car",
            descriptionDe = "Alles rund um Fahrzeuge, Führerscheine und Bewohnerparken.",
            descriptionEn = "Everything regarding vehicles, driving licenses, and resident parking."
        ))
        val c4 = categoryRepository.save(Category(
            nameDe = "Bauen & Wohnen",
            nameEn = "Building & Living",
            link = "/categories/bauen-wohnen",
            icon = "apartment",
            descriptionDe = "Genehmigungen, Wohnbauförderung und wohnungsbezogene Dienstleistungen.",
            descriptionEn = "Permits, residential construction funding, and housing-related services."
        ))
        val c5 = categoryRepository.save(Category(
            nameDe = "Arbeit, Beruf & Steuern",
            nameEn = "Work, Career & Taxes",
            link = "/categories/arbeit-beruf-steuern",
            icon = "briefcase",
            descriptionDe = "Gewerbeanmeldungen, steuerliche Angelegenheiten und beruflicher Service.",
            descriptionEn = "Business registrations, tax matters, and professional services."
        ))
        val c6 = categoryRepository.save(Category(
            nameDe = "Soziales, Gesundheit & Rente",
            nameEn = "Social, Health & Pension",
            link = "/categories/soziales-gesundheit-rente",
            icon = "health_and_safety",
            descriptionDe = "Gesundheitsvorsorge, Rentenberatung und soziale Unterstützung.",
            descriptionEn = "Healthcare, pension advice, and social support."
        ))
        val c7 = categoryRepository.save(Category(
            nameDe = "Bildung & Wissenschaft",
            nameEn = "Education & Science",
            link = "/categories/bildung-wissenschaft",
            icon = "graduation-cap",
            descriptionDe = "Schule, Studium, Ausbildungsförderung (BAföG) und Weiterbildung.",
            descriptionEn = "School, university studies, educational support (BAföG), and further education."
        ))
        val c8 = categoryRepository.save(Category(
            nameDe = "Sicherheit & Recht",
            nameEn = "Safety & Law",
            link = "/categories/sicherheit-recht",
            icon = "shield_radar",
            descriptionDe = "Führungszeugnisse, rechtliche Beglaubigungen und Sicherheitsdienste.",
            descriptionEn = "Certificates of good conduct, legal certifications, and security services."
        ))
        val c9 = categoryRepository.save(Category(
            nameDe = "Freizeit, Natur & Tiere",
            nameEn = "Leisure, Nature & Animals",
            link = "/categories/freizeit-natur-tiere",
            icon = "trees",
            descriptionDe = "Angelscheine, Haustierregistrierung und Freizeitangebote.",
            descriptionEn = "Fishing licenses, pet registration, and leisure opportunities."
        ))
        val c10 = categoryRepository.save(Category(
            nameDe = "Einwanderung & Integration",
            nameEn = "Immigration & Integration",
            link = "/categories/einwanderung-integration",
            icon = "globe",
            descriptionDe = "Dienstleistungen für ausländische Mitbürger, Aufenthaltstitel und Sprachkurse.",
            descriptionEn = "Services for foreign citizens, residence permits, and language courses."
        ))

        // Initialize Services inside Category 1 (Einwohnermeldeamt)
        val s1 = serviceRepository.save(Service(
            nameDe = "Wohnsitz anmelden",
            nameEn = "Register Address",
            link = "/services/wohnsitz-anmelden",
            descriptionDe = "In Deutschland besteht eine allgemeine Meldepflicht. Wer eine Wohnung bezieht, hat sich innerhalb von zwei Wochen nach dem Einzug bei der Meldebehörde anzumelden.",
            descriptionEn = "In Germany there is a general registration requirement. Anyone who moves into an apartment must register with the registration authority within two weeks of moving in.",
            costDe = "Gebührenfrei",
            costEn = "Free of charge",
            processingTimeDe = "10 Minuten",
            processingTimeEn = "10 Minutes",
            category = c1,
            isTop = false
        ))
        val s2 = serviceRepository.save(Service(
            nameDe = "Wohnsitz ummelden",
            nameEn = "Register Change of Address",
            link = "/services/wohnsitz-ummelden",
            descriptionDe = "Adressänderung bei Umzug innerhalb derselben Gemeinde. Schnell und oft digital möglich.",
            descriptionEn = "Address change when moving within the same municipality. Fast and often available digitally.",
            costDe = "Gebührenfrei",
            costEn = "Free of charge",
            processingTimeDe = "Sofort",
            processingTimeEn = "Immediately",
            category = c1,
            isTop = true
        ))
        val s3 = serviceRepository.save(Service(
            nameDe = "Personalausweis",
            nameEn = "Identity Card",
            link = "/services/personalausweis-beantragen",
            descriptionDe = "Neubeantragung oder Verlängerung Ihres Personalausweises. Inklusive eID-Funktion.",
            descriptionEn = "New application or renewal of your identity card. Includes eID function.",
            costDe = "37,00 €",
            costEn = "37.00 €",
            processingTimeDe = "2-4 Wochen",
            processingTimeEn = "2-4 weeks",
            category = c1,
            isTop = true
        ))
        val s4 = serviceRepository.save(Service(
            nameDe = "Reisepass",
            nameEn = "Passport",
            link = "/services/reisepass-beantragen",
            descriptionDe = "Beantragung für Reisen außerhalb der EU. Express-Optionen für Eilfälle verfügbar.",
            descriptionEn = "Application for travel outside the EU. Express options available for urgent cases.",
            costDe = "70,00 €",
            costEn = "70.00 €",
            processingTimeDe = "3-4 Wochen",
            processingTimeEn = "3-4 weeks",
            category = c1,
            isTop = false
        ))
        val s5 = serviceRepository.save(Service(
            nameDe = "Führungszeugnis",
            nameEn = "Certificate of Good Conduct",
            link = "/services/fuehrungszeugnis-beantragen",
            descriptionDe = "Einfaches oder erweitertes Zeugnis für Arbeitgeber oder Vereine.",
            descriptionEn = "Simple or extended certificate of good conduct for employers or clubs.",
            costDe = "13,00 €",
            costEn = "13.00 €",
            processingTimeDe = "1-2 Wochen",
            processingTimeEn = "1-2 weeks",
            category = c1,
            isTop = false
        ))
        val s6 = serviceRepository.save(Service(
            nameDe = "Beglaubigungen",
            nameEn = "Certifications",
            link = "/services/beglaubigungen",
            descriptionDe = "Amtliche Bestätigung der Echtheit von Kopien oder Unterschriften.",
            descriptionEn = "Official confirmation of the authenticity of copies or signatures.",
            costDe = "5,00 € - 15,00 €",
            costEn = "5.00 € - 15.00 €",
            processingTimeDe = "Sofort vor Ort",
            processingTimeEn = "On-site immediately",
            category = c1,
            isTop = false
        ))

        // Initialize rest of Top Services under their appropriate categories
        serviceRepository.save(Service(
            nameDe = "Kfz-Zulassung",
            nameEn = "Vehicle Registration",
            link = "/services/kfz-zulassung",
            descriptionDe = "Zulassung eines neuen oder gebrauchten Fahrzeugs.",
            descriptionEn = "Registration of a new or used vehicle.",
            costDe = "ca. 30,00 €",
            costEn = "approx. 30.00 €",
            processingTimeDe = "Sofort",
            processingTimeEn = "Immediately",
            category = c3,
            isTop = true
        ))
        serviceRepository.save(Service(
            nameDe = "Elterngeld",
            nameEn = "Parental Allowance",
            link = "/services/elterngeld",
            descriptionDe = "Finanzielle Unterstützung für Eltern nach der Geburt eines Kindes.",
            descriptionEn = "Financial support for parents after the birth of a child.",
            costDe = "Gebührenfrei",
            costEn = "Free of charge",
            processingTimeDe = "4-6 Wochen",
            processingTimeEn = "4-6 weeks",
            category = c2,
            isTop = true
        ))
        serviceRepository.save(Service(
            nameDe = "Gewerbeanmeldung",
            nameEn = "Business Registration",
            link = "/services/gewerbeanmeldung",
            descriptionDe = "Anmeldung einer gewerblichen Tätigkeit.",
            descriptionEn = "Registration of a commercial activity.",
            costDe = "26,00 €",
            costEn = "26.00 €",
            processingTimeDe = "3-5 Werktage",
            processingTimeEn = "3-5 business days",
            category = c5,
            isTop = true
        ))
        serviceRepository.save(Service(
            nameDe = "Führerschein",
            nameEn = "Driving License",
            link = "/services/fuehrerschein",
            descriptionDe = "Beantragung, Umtausch oder Erweiterung einer Fahrerlaubnis.",
            descriptionEn = "Application, exchange, or extension of a driving license.",
            costDe = "ca. 45,00 €",
            costEn = "approx. 45.00 €",
            processingTimeDe = "4-6 Wochen",
            processingTimeEn = "4-6 weeks",
            category = c3,
            isTop = true
        ))

        // Create requirements for Wohnsitz anmelden (s1)
        // TEXT requirements
        requirementRepository.save(ServiceRequirement(
            type = RequirementType.TEXT,
            titleDe = "Altersgrenze",
            titleEn = "Age Limit",
            descriptionDe = "Personen ab 16 Jahren können sich eigenständig anmelden. Für Kinder unter 16 Jahren muss die Anmeldung durch die gesetzlichen Vertreter erfolgen.",
            descriptionEn = "Persons aged 16 and over can register independently. For children under 16, registration must be carried out by the legal representatives.",
            service = s1
        ))
        requirementRepository.save(ServiceRequirement(
            type = RequirementType.TEXT,
            titleDe = "Frist",
            titleEn = "Deadline",
            descriptionDe = "Die Anmeldung muss zwingend innerhalb von 14 Tagen nach dem tatsächlichen Einzug in die neue Wohnung erfolgen.",
            descriptionEn = "The registration must be completed within 14 days of actually moving into the new apartment.",
            service = s1
        ))
        requirementRepository.save(ServiceRequirement(
            type = RequirementType.TEXT,
            titleDe = "Persönliches Erscheinen",
            titleEn = "Personal Appearance",
            descriptionDe = "Bei einer Anmeldung vor Ort in der Meldestelle ist in der Regel das persönliche Erscheinen oder die Vertretung durch eine bevollmächtigte Person erforderlich. Bei Online-Anmeldung ist ein Personalausweis mit eID-Funktion nötig.",
            descriptionEn = "When registering on-site at the registration office, personal appearance or representation by an authorized person is usually required. For online registration, an identity card with eID function is required.",
            service = s1
        ))

        // DOCUMENT requirements
        requirementRepository.save(ServiceRequirement(
            type = RequirementType.DOCUMENT,
            titleDe = "Personalausweis oder Reisepass",
            titleEn = "Identity Card or Passport",
            service = s1
        ))
        requirementRepository.save(ServiceRequirement(
            type = RequirementType.DOCUMENT,
            titleDe = "Wohnungsgeberbestätigung (Vom Vermieter unterschrieben)",
            titleEn = "Landlord Confirmation (Signed by the landlord)",
            service = s1
        ))
        requirementRepository.save(ServiceRequirement(
            type = RequirementType.DOCUMENT,
            titleDe = "Gegebenenfalls Geburtsurkunden (bei Zuzug aus dem Ausland oder familiären Anmeldungen)",
            titleEn = "Birth certificates if applicable (when moving from abroad or for family registrations)",
            service = s1
        ))

        // Create FAQs for Wohnsitz anmelden (s1)
        faqRepository.save(ServiceFaq(
            questionDe = "Was passiert, wenn ich die 14-Tage-Frist verpasse?",
            questionEn = "What happens if I miss the 14-day deadline?",
            answerDe = "Eine Überschreitung der Anmeldefrist stellt eine Ordnungswidrigkeit dar und kann mit einem Bußgeld geahndet werden. Bitte holen Sie die Anmeldung schnellstmöglich nach.",
            answerEn = "Exceeding the registration deadline is an administrative offense and can be punished with a fine. Please catch up on registration as soon as possible.",
            service = s1
        ))
        faqRepository.save(ServiceFaq(
            questionDe = "Brauche ich zwingend die eID-Funktion meines Ausweises?",
            questionEn = "Do I absolutely need the eID function of my identity card?",
            answerDe = "Ja, für das digitale Einreichen und die Authentifizierung mit der EUDI Wallet ist die aktivierte Online-Ausweisfunktion (eID) Ihres Personalausweises zwingend erforderlich.",
            answerEn = "Yes, for digital submission and authentication with the EUDI Wallet, the activated online identity card function (eID) of your identity card is mandatory.",
            service = s1
        ))

        // Create dynamic Steps for Wohnsitz anmelden (s1)
        stepRepository.save(ServiceStep(
            stepNumber = 1,
            type = "FORM",
            titleDe = "Neue Adressdaten",
            titleEn = "New Address Details",
            configJson = """[{"name":"street","type":"text","labelDe":"Neue Straße & Hausnummer","labelEn":"New Street & House Number","required":true},{"name":"postalCode","type":"text","labelDe":"Postleitzahl","labelEn":"Postal Code","required":true},{"name":"city","type":"text","labelDe":"Stadt","labelEn":"City","required":true},{"name":"moveInDate","type":"date","labelDe":"Einzugsdatum","labelEn":"Move-in Date","required":true}]""",
            service = s1
        ))
        stepRepository.save(ServiceStep(
            stepNumber = 2,
            type = "FILE_UPLOAD",
            titleDe = "Wohnungsgeberbestätigung hochladen",
            titleEn = "Upload Landlord Confirmation",
            configJson = """{"requiredDocuments":[{"key":"landlord_confirmation","labelDe":"Wohnungsgeberbestätigung (Vom Vermieter unterschrieben)","labelEn":"Landlord Confirmation (Signed by landlord)"}]}""",
            service = s1
        ))
    }
}
