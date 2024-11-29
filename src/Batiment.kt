class Batiment(nom: String) {

    private var porteOuverte: Boolean = false
    private var entree = LecteurDeBadge()
    private var sortie = LecteurDeBadge()
    private var laser = FaisceauLaser()
    private var detecteur = DetecteurIncendi()
    private var alarme = Alarme()

    fun alarme() = print("Alarme!")

    fun ouvrirPorte() {
        porteOuverte = true
        print("porte ouverte")
    }

    fun fermerPorte() {
        porteOuverte = false
        print("porte fermee")
    }

    fun demarrer() = print("Démarrer")

    fun passageUsager() {
        print("passage")
        // reception de la detection du laser
        // transmettre passage à SystemGlobal
    }

    fun detecterFeu() {
        porteOuverte = true
        print("FEU FEU FEU")
    }
}
