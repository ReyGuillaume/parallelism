import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

// Enum pour représenter l'état de la porte
enum class PorteEtat {
    OUVERTE, FERMEE
}

// Fonction principale
fun main() = runBlocking {
    // Canal pour communiquer l'état de la porte
    val canalPorte = Channel<PorteEtat>()

    // Canal pour détecter le passage
    val canalPassage = Channel<String>()

    // Lancement des coroutines pour simuler le système
    val gestionPorte = launch { gestionPorte(canalPorte, canalPassage) }
    val detecteurPassage = launch { detecteurPassage(canalPorte, canalPassage) }

    // Simule des actions utilisateur (ouverture/fermeture de la porte)
    val actions = listOf(
        PorteEtat.OUVERTE, // Ouvrir la porte
        PorteEtat.FERMEE,  // Fermer la porte
        PorteEtat.OUVERTE, // Ouvrir à nouveau
    )

    // Envoie des actions au canal
    for (action in actions) {
        delay(2000L) // Simule un délai entre les actions
        println("Action utilisateur: $action")
        canalPorte.send(action)
    }

    // Arrêter les coroutines
    gestionPorte.cancelAndJoin()
    detecteurPassage.cancelAndJoin()
}

// Coroutine pour gérer la porte
suspend fun gestionPorte(canalPorte: Channel<PorteEtat>, canalPassage: Channel<String>) {
    var etatPorte = PorteEtat.FERMEE

    for (etat in canalPorte) {
        etatPorte = etat
        println("La porte est maintenant $etatPorte")
        // Si la porte est ouverte, on notifie les détecteurs
        if (etatPorte == PorteEtat.OUVERTE) {
            canalPassage.send("DETECTEUR_ACTIVE")
        }
    }
}

// Coroutine pour le détecteur de passage
suspend fun detecteurPassage(canalPorte: Channel<PorteEtat>, canalPassage: Channel<String>) {
    for (message in canalPassage) {
        if (message == "DETECTEUR_ACTIVE") {
            println("Détecteur: Passage détecté !")
        }
    }
}
