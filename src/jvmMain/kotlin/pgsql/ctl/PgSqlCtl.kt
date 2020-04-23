package pgsql.ctl

import core.ProcessStreamGlobber
import org.apache.commons.io.FileUtils
import org.slf4j.LoggerFactory
import pgsql.DsPostgreSqlProvider
import satd.step2.assert2
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

/* Simone 08/07/2014 09:49 */

class PgSqlCtl(
    private val pgsqlBinFolder: String = "data/pgsql",
    private val pgsqlDataFolder: String = "data/db/pg",
    private val pgsqlTcpPort: Int = 1603,
    private val pgSqlConfigFix: IPgSqlConfigFix = PgSqlConfigFix()
) : IPgSqlCtl {
    companion object {
        private val log = LoggerFactory.getLogger(PgSqlCtl::class.java)
    }

    override fun status(): CtlStatus {
        val returnCode = exec(getPgBin("pg_ctl"), "status", "-D", db)
        val status: CtlStatus
        status = when (returnCode) {
            0 -> CtlStatus.RUNNING
            3 -> CtlStatus.NO_SERVER_RUNNING
            else -> CtlStatus.UNKNOWN
        }
        log.info("Status: {}({})", status.toString(), returnCode)
        return status
    }

    private val db: String get() = dbPath.toString()

    private val dbPath: Path
        get() = Paths.get(pgsqlDataFolder).normalize()

    private fun getPgBin(exe: String): String {
        val home: Path = Paths.get(pgsqlBinFolder).toAbsolutePath().normalize()
        assert2(Files.exists(home), "Postgres home directory not found: $home")
        val bin = home.resolve("bin").resolve(exe).normalize()
        assert2(Files.exists(bin), "Not found $bin")
        return bin.toString()
    }

    override fun stopFast(): StopStatus {
        return stopInternal(true)
    }

    override fun stop(): StopStatus {
        return stopInternal(false)
    }

    fun stopInternal(fast: Boolean): StopStatus {
        val cmds = mutableListOf(getPgBin("pg_ctl"), "stop", "-t", "20", "-D", db)
        if (fast) {
            cmds.add("-m")
            cmds.add("fast")
        }
        val returnCode = exec(*cmds.toTypedArray())
        val stopStatus = if (returnCode == 0) StopStatus.STOP_OK else StopStatus.STOP_FAILED
        log.info("Stop result: {}({})", stopStatus.toString(), returnCode)
        return stopStatus
    }

    override fun start() {
        val exitCode = exec(getPgBin("pg_ctl"), "start", "-w", "-t", "300", "-D", db)
        log.info(
            "Start: {}",
            if (exitCode == 0) "SUCCESSFUL" else "FAILED(exitCode:$exitCode)"
        )
        if (exitCode != 0) throw PgCtlStartFailed(exitCode)
    }

    private fun exec(vararg commandTokens: String): Int {
        val tokens = listOf(*commandTokens)
        val command = ProcessBuilder().command(tokens)
        command.environment()["LANGUAGE"] = "EN"
        log.info("Running command: {}", java.lang.String.join(" ", command.command()))
        log.info("in {}", command.directory())
        log.info("with environment [{}]", command.environment().map { "${it.key}=${it.value}" }.joinToString(", "))
        val process = command.start()
        val globber = ProcessStreamGlobber(process)
        globber.setName(File(tokens[0]).name)
        globber.startGlobber()
        val returnVal = process.waitFor()
        log.info("returned {}", returnVal)
        return returnVal
    }

    override fun initDb() {
        initDb(pgsqlTcpPort)
    }

    override fun dbExist(): Boolean {
        return dbPath.toFile().exists()
    }

    fun initDb(tcpPort: Int) {
        if (dbExist()) throw PgdataAlreadyExists(db)
        val pwFile = pwFile
        val exitValue: Int
        exitValue = try {
            exec(
                getPgBin("initdb"), "--no-locale", "-E", "UTF8", "-U", DsPostgreSqlProvider.USERNAME
                , "-A", "md5", "-D", db, "--pwfile=" + pwFile.absolutePath
            )
        } finally {
            FileUtils.deleteQuietly(pwFile)
        }
        if (exitValue != 0) throw RuntimeException("Could not initialize database, initdb returned $exitValue")
        pgSqlConfigFix.fixConfig(dbPath, tcpPort)
    }

    private val pwFile: File
        get() {
            val pwFile = File.createTempFile("tmp", "tmp")
            core.FileUtils.write(pwFile, DsPostgreSqlProvider.PASSWORD)
            return pwFile
        }

    private inner class PgdataAlreadyExists(message: String?) : RuntimeException(message)
    private inner class PgCtlStartFailed(returnCode: Int) :
        RuntimeException("return code: $returnCode")


}