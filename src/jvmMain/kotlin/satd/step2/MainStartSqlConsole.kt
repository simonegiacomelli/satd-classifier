package satd.step2

import org.h2.tools.Server
import satd.utils.logln

fun main() {
    persistence.setupDatabase()
    logln("Connect to http://servername:61603")
    Server.createWebServer("-webPort", "61603","-webAllowOthers").start()
}