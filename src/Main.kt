package org.example

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select
import java.util.*

fun main(): Unit = runBlocking {
    // faisceauLaser
    val detPassage = Channel<Unit>()
    val capPassage = Channel<Unit>()

    // alarme
    val activAlarme = Channel<Unit>()
    val desactivAlarme = Channel<Unit>()

    // detecteur incendie
    val detFeu = Channel<Unit>()

    //badge
    val capScanner = Channel<String>()
    val verification = Channel<Boolean>()
    val verifier = Channel<String>()
    val porteOuverte = Channel<Unit>()
    val ouvrir_porte = Channel<Unit>()

    // voyant
    val rouge = Channel<Unit>()


    launch {
        faisceauLaser(detPassage, capPassage)
    }

    launch {
        alarme(activAlarme, desactivAlarme)
    }

    launch {
        detecteurIncendie(detFeu, activAlarme)
    }
    /*
        launch {
            passageUsager(ouvrirPorte, detPassage, ajouter, timer, eteint, vert, activAlarm)
        }

     */

    launch {
        salleDeControle(detFeu, capPassage, capScanner)
    }
}

suspend fun faisceauLaser(detPassage: Channel<Unit>, capPassage: Channel<Unit>) {
    while (true) {
        capPassage.receive()
        detPassage.send(Unit)
        println("passage")
    }
}

suspend fun alarme(
    activAlarme: Channel<Unit>,
    desactivAlarme: Channel<Unit>,
) {
    var etat = "inactive"

    while (true) {
        println("Alarme: État actuel -> $etat")
        select<Unit> {
            activAlarme.onReceive {
                etat = "active"
            }

            desactivAlarme.onReceive {
                etat = "eteint"
            }
        }
    }
}

suspend fun lecteurDeBadge(
    capScanner: Channel<String>,
    verification: Channel<Boolean>,
    verifier: Channel<String>,
    porteOuverte: Channel<Unit>,
    ouvrir_porte: Channel<String>,
    rouge: Channel<Unit>,
) {
    while (true) {
        val user = capScanner.receive()
        verifier.send(user)
        val userAccepted = verification.receive()
        if(userAccepted){
            ouvrir_porte.send(user)
            porteOuverte.send(Unit)
        } else {
            rouge.send(Unit)
        }
    }
}

suspend fun detecteurIncendie(detFeu: Channel<Unit>, activAlarm: Channel<Unit>) {
    while (true) {
        detFeu.receive()
        activAlarm.send(Unit)
    }
}

suspend fun salleDeControle(detFeu: Channel<Unit>, capPassage: Channel<Unit>, capScanner: Channel<String>) {
    val userInput = Scanner(System.`in`)

    while (true) {
        print("> ")
        val input = userInput.nextLine()

        if (input.equals("feu", ignoreCase = true)) {
            detFeu.send(Unit)
        }

        if (input.equals("passage", ignoreCase = true)) {
            capPassage.send(Unit)
        }

        if (input.equals("scan", ignoreCase = true)) {
            capScanner.send("infoUser")
        }

        delay(1000)
    }
}

/*
suspend fun passageUsager(
    ouvrirPorte: Channel<String>,
    detPassage: Channel<Unit>,
    ajouter: Channel<String>,
    timer: Channel<Int>,
    eteint: Channel<Unit>,
    vert: Channel<Unit>,
    activAlarm: Channel<Unit>
) {
    while (true) {
        select<Unit> {
            // Réception d'un badge
            ouvrirPorte.onReceive { badge ->
                println("PassageUsager: Badge reçu -> $badge")
                timer.send(30) // Démarrer un timer de 30 secondes
                vert.send(Unit) // Allumer la lumière verte
                println("PassageUsager: Timer démarré pour 30 secondes")

                launch {
                    gestionTimer(timer, eteint, badge, detPassage, ajouter, activAlarm)
                }
            }
        }
    }
}

suspend fun gestionTimer(
    timer: Channel<Int>,
    eteint: Channel<Unit>,
    badge: String,
    detPassage: Channel<Unit>,
    ajouter: Channel<String>,
    activAlarm: Channel<Unit>
) {
    var passageDetecte = false
    while (true) {
        select<Unit> {
            // Timer expiré
            timer.onReceive {
                if (!passageDetecte) {
                    eteint.send(Unit)
                    println("PassageUsager: Lumière éteinte après expiration du timer")
                    return
                }
            }

            // Passage détecté
            detPassage.onReceive {
                if (!passageDetecte) {
                    passageDetecte = true
                    ajouter.send(badge) // Ajouter le badge
                    println("PassageUsager: Passage détecté, badge ajouté -> $badge")
                } else {
                    activAlarm.send(Unit) // Activer l'alarme pour un second passage
                    println("PassageUsager: Deuxième passage détecté, alarme activée !")
                }
            }
        }
    }
}
*/