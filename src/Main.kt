package org.example

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select
import java.lang.Thread.sleep
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
    val ouvrir_porte = Channel<String>()

    // voyant
    val rouge = Channel<Unit>()

    // passage usager
    val ouvrirPorte = Channel<String>()
    val vert = Channel<Unit>()
    val ajouterAuxLogs = Channel<String>()
    val afficherLogs = Channel<Unit>()


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
        passageUsager(ouvrirPorte, vert, detPassage, ajouterAuxLogs, activAlarme)
    }

    launch {
        lecteurDeBadge(capScanner,verification, verifier, porteOuverte, ouvrir_porte, rouge)
    }

    launch {
        salleDeControle(detFeu, capPassage, capScanner, afficherLogs)
    }

    launch {
        systemeGlobal(verifier, verification, ajouterAuxLogs, afficherLogs)
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

suspend fun salleDeControle(detFeu: Channel<Unit>, capPassage: Channel<Unit>, capScanner: Channel<String>, afficherLogs: Channel<Unit>) {
    val userInput = Scanner(System.`in`)

    while (true) {
        print("> ")
        val input = userInput.nextLine()

        if (input.equals("feu", ignoreCase = true)) {
            detFeu.send(Unit)
        }

        if (input.equals("eteindre alarme", ignoreCase = true)) {
            detFeu.send(Unit)
        }

        if (input.equals("passage", ignoreCase = true)) {
            capPassage.send(Unit)
        }

        if (input.equals("scan", ignoreCase = true)) {
            capScanner.send("guigui")
        }

        if (input.equals("logs", ignoreCase = true)) {
            afficherLogs.send(Unit)
        }

        delay(1000)
    }
}

suspend fun passageUsager(
    ouvrirPorte: Channel<String>,
    vert: Channel<Unit>,
    detPassage: Channel<Unit>,
    ajouterAuxLogs: Channel<String>,
    activAlarm: Channel<Unit>
) = runBlocking {
    val finTimer = Channel<Unit>()

    while (true) {
        val badge = ouvrirPorte.receive()
        launch {
            timer(30, finTimer)
        }
        vert.send(Unit)
        select<Unit> {
            finTimer.onReceive {}
            detPassage.onReceive {
                ajouterAuxLogs.send(badge)
                select<Unit> {
                    finTimer.onReceive {}
                    detPassage.onReceive {
                        activAlarm.send(Unit)
                    }
                }
            }
        }
    }
}

fun timer(
    timeout: Long,
    finTimer: Channel<Unit>,
) = runBlocking {
    sleep(timeout * 1000)
    finTimer.send(Unit)
}

suspend fun systemeGlobal(verifier: Channel<String>, verification: Channel<Boolean>, ajouterAuxLogs: Channel<String>, afficherLogs: Channel<Unit>) {
    val authorisation = listOf("guigui", "leo")
    val logs: MutableList<String> = mutableListOf()

    while (true) {
        select<Unit> {
            verifier.onReceive { badge ->
                val auth = authorisation.contains(badge)
                verification.send(auth)
                val usager = ajouterAuxLogs.receive()
                logs.add(usager)
            }
            afficherLogs.onReceive {
                println("--- Logs ---")
                logs.forEach { println(it) }
            }
        }
    }
}