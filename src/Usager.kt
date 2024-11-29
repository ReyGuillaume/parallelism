class Usager {
    private var badge = Badge()

    fun demarrer() = print("Démarrer")

    fun scannerBadge() {
        print("Je scanne")
        // reception etat porte
    }

    fun passerPorte() {
        print("Je passe porte")
        // envoyer message à laser
    }
}