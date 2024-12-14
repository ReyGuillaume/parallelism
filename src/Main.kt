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

    // voyant
    val rouge = Channel<Unit>()

    // passage usager
    val ouvrirPorte = Channel<String>()
    val vert = Channel<Unit>()
    val afficherLogs = Channel<Unit>()

    launch {
        voyant(vert, rouge)
    }

    launch {
        systemeGlobal(verifier, verification, afficherLogs)
    }

    launch {
        faisceauLaser(detPassage, capPassage)
    }

    launch {
        alarme(activAlarme, desactivAlarme)
    }

    launch {
        detecteurIncendie(detFeu, activAlarme)
    }

    launch {
        porte(ouvrirPorte, vert, detPassage, activAlarme)
    }

    launch {
        lecteurDeBadge(capScanner,verification, verifier, ouvrirPorte, rouge)
    }

    launch {
        salleDeControle(detFeu, capPassage, capScanner, afficherLogs, desactivAlarme)
    }
}

suspend fun faisceauLaser(detPassage: Channel<Unit>, capPassage: Channel<Unit>) {
    while (true) {
        capPassage.receive()
        detPassage.send(Unit)
    }
}

suspend fun alarme(
    activAlarme: Channel<Unit>,
    desactivAlarme: Channel<Unit>,
) {
    var etat = "désactivée"

    while (true) {
        select<Unit> {
            activAlarme.onReceive {
                etat = "activée"
            }

            desactivAlarme.onReceive {
                etat = "désactivée"
            }
        }
        println("Alarme $etat")
    }
}

suspend fun lecteurDeBadge(
    capScanner: Channel<String>,
    verification: Channel<Boolean>,
    verifier: Channel<String>,
    ouvrirPorte: Channel<String>,
    rouge: Channel<Unit>,
) {
    while (true) {
        val user = capScanner.receive()
        verifier.send(user)
        val userAccepted = verification.receive()
        println("AUTH : $user $userAccepted")
        if(userAccepted){
            ouvrirPorte.send(user)
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

suspend fun salleDeControle(
    detFeu: Channel<Unit>,
    capPassage: Channel<Unit>,
    capScanner: Channel<String>,
    afficherLogs: Channel<Unit>,
    desactivAlarme: Channel<Unit>
) {
    val userInput = Scanner(System.`in`)

    while (true) {
        print("> ")
        val input = userInput.nextLine()

        if (input.equals("feu", ignoreCase = true)) {
            detFeu.send(Unit)
        }

        if (input.equals("eteindre alarme", ignoreCase = true)) {
            desactivAlarme.send(Unit)
        }

        if (input.equals("passage", ignoreCase = true)) {
            capPassage.send(Unit)
        }

        if (input.startsWith("scan ", ignoreCase = true)) {
            val user = input.drop(5)
            capScanner.send(user)
        }

        if (input.equals("logs", ignoreCase = true)) {
            afficherLogs.send(Unit)
        }

        delay(1000)
    }
}

suspend fun porte(
    ouvrirPorte: Channel<String>,
    vert: Channel<Unit>,
    detPassage: Channel<Unit>,
    activAlarm: Channel<Unit>
) {
    withContext(Dispatchers.IO) {
        val finTimer = Channel<Unit>()

        launch {
            while (true) {
                ouvrirPorte.receive()
                vert.send(Unit)
                launch { timer(30, finTimer) } // timer avant fermeture de la porte
                println("porte ouverte")
            }
        }

        launch {
            while (true) {
                finTimer.receive()
                println("porte fermée")
            }
        }

        launch {
            while (true) {
                detPassage.receive()
                select<Unit> {
                    detPassage.onReceive {
                        activAlarm.send(Unit)
                    }
                    finTimer.onReceive {
                        println("porte fermée")
                    }
                }
            }
        }
    }
}

suspend fun timer(
    timeout: Long,
    finTimer: Channel<Unit>,
) {
    delay(timeout * 1000)
    finTimer.send(Unit)
}

suspend fun systemeGlobal(verifier: Channel<String>, verification: Channel<Boolean>, afficherLogs: Channel<Unit>) {
    val authorisation = listOf("guigui", "leo")
    val logs: MutableList<String> = mutableListOf()

    while (true) {
        select<Unit> {
            verifier.onReceive {
                val estAutorise = authorisation.contains(it)
                verification.send(estAutorise)
                logs.add((if(estAutorise) "\uD83D\uDFE2" else "\uD83D\uDD34") + it)
            }
            afficherLogs.onReceive {
                println("--- Logs ---")
                logs.forEach { println(it) }
            }
        }
    }
}

suspend fun voyant(
    vert: Channel<Unit>,
    rouge: Channel<Unit>
) {
    withContext(Dispatchers.IO) {
        val finTimer = Channel<Unit>()
        var etat: String

        launch {
            while (true) {
                vert.receive()
                etat = "\uD83D\uDFE2"
                launch { timer(5, finTimer) }
                println("Voyant $etat")
            }
        }

        launch {
            while (true) {
                rouge.receive()
                etat = "\uD83D\uDD34"
                launch { timer(10, finTimer) }
                println("Voyant $etat")
            }
        }

        launch {
            while (true) {
                finTimer.receive()
                etat = "eteint"
                println("Voyant $etat")
            }
        }
    }
}