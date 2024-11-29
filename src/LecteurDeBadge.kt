class LecteurDeBadge {
    enum class EtatVoyant {
        VERT,
        ROUGE,
        ETEINT
    }

    private var voyant: EtatVoyant = EtatVoyant.ETEINT

    fun demarrer() = print("Démarrer")

    fun scannerBadge() {
        // verification usager
            // emettre usager
            // timeout 30s avant fermeture
            // envoyer message après timeout
            voyant = EtatVoyant.ETEINT

        // verification echouée
            voyant = EtatVoyant.ROUGE
            // timeout 10s
            voyant = EtatVoyant.ETEINT

    }
}