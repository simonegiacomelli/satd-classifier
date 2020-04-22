package pgsql

import core.Shutdown
import org.slf4j.LoggerFactory
import pgsql.ctl.*
import satd.utils.Config
import java.io.File
import java.util.*

/* Simone 08/07/2014 17:49 */
class PgSqlStarter(private val pgSqlCtl: IPgSqlCtl = PgSqlCtl()) {

    companion object {
        val def by lazy { PgSqlStarter() }
        @JvmStatic
        fun main(args: Array<String>) {
            //pgsqlctl.stop();
            def.start()
            println("DONE")
        }
    }

    val log = LoggerFactory.getLogger(javaClass)
    fun start(hookShutdown: Boolean = true) {
        if (!pgSqlCtl.dbExist()) {
            pgSqlCtl.initDb()
            pgSqlCtl.start()
        } else {
            val status = pgSqlCtl.status()
            if (status == CtlStatus.NO_SERVER_RUNNING) pgSqlCtl.start() else if (status == CtlStatus.RUNNING) log.warn("PgSql already running")
        }
        if (hookShutdown)
            hookShutdown()
    }

    private fun hookShutdown() {
        Shutdown.addApplicationShutdownHook {
            log.info("Stopping PgSql")
            if (smartShutdownFailed()) {
                if (pgSqlCtl.stopFast() == StopStatus.STOP_FAILED) log.error("Unable to stop PgSql")
            }
        }
    }

    private fun smartShutdownFailed(): Boolean {
        val result = pgSqlCtl.stop() == StopStatus.STOP_FAILED
        if (result) log.warn("Smart shutdown failed. Issuing fast shutdown")
        return result
    }

}