package satd.step2


import kotlin.test.Test
import kotlin.test.assertEquals


internal class FindMethodWithSatdTest {
    val class1 by lazy { load("Class1.java") }
    val class2 by lazy { load("Class2.java") }
    val class2b by lazy { load("Class2b.java") }
    val class3 by lazy { load("Class3.java") }
    val class4 by lazy { load("Class4.java") }
    val class5line by lazy { load("Class5line.java") }
    val fixmethod by lazy { load("Fixmethod.java") }
    val throwsInJavaDoc by lazy { load("SatdInJavaDocThrows.java") }


    private fun load(s: String) = this::class.java.classLoader.getResource("satd/step2/SourceTest/$s")!!.readText()

    @Test
    fun `method with satd should be correctly detected`() {
        val target = findMethodsWithSatd(class1)
        assertEquals(1, target.size)
    }

    @Test
    fun `normal comment should be ignored`() {
        val target = findMethodsWithSatd(class2)
        assertEquals(0, target.size)
    }

    @Test
    fun `normal comment should be ignored b`() {
        val target = findMethodsWithSatd(class2b)
        assertEquals(0, target.size)
    }

    @Test
    fun `bait comment on field should be ignored`() {
        val target = findMethodsWithSatd(class3)
        assertEquals(0, target.size)
    }

    @Test
    fun `double satd comment in one method should be accounted as one`() {
        val target = findMethodsWithSatd(class4)
        assertEquals(1, target.size)
    }

    @Test
    fun `comment fixmethod should not be matched`() {
        val target = findMethodsWithSatd(fixmethod)
        assertEquals(0, target.size)
    }

    @Test
    fun `some patterns must not be detected if they are in javadoc throws clause`() {
        val target = findMethodsWithSatd(throwsInJavaDoc)
        assertEquals(0, target.size)
    }

    @Test
    fun `no satd detection in JavaDoc`() {
        val target = findMethodsWithSatd(noSatdDetectionInJavaDoc)
        assertEquals(0, target.size)
    }

    @Test
    fun `no satd detection in method comment`() {
        val target = findMethodsWithSatd(noSatdDetectionInMethodComment)
        assertEquals(0, target.size)
    }

    //todo should I include this?
    //@Test
    fun `method line comment`() {
        val target = findMethodsWithSatd(class5line)
        assertEquals(1, target.size)
    }

    //TODO detect failed parsing of incorrect java source
    //TODO add method name correctenss check
}


val noSatdDetectionInJavaDoc = """
    class Class1 {
        /**
         * some comment fixme, it is hacky and ugly
         *
         * @throws IOException If there is a problem opening the file representing
         *                     the bookmark.
         */
        public void loadAutoBookmark() throws IOException {
            autoBookmark = Bookmark.getInstance(book.getPath());
            audioOffset = autoBookmark.getPosition();
            book.setCurrentIndex(autoBookmark.getNccIndex());
            book.goTo(book.current());
        }
    }
    
""".trimIndent()
val noSatdDetectionInMethodComment = """
    class Class1 {
        /*
         * some comment fixme, it is hacky and ugly
         *
         */
        public void loadAutoBookmark() throws IOException {
            autoBookmark = Bookmark.getInstance(book.getPath());
            audioOffset = autoBookmark.getPosition();
            book.setCurrentIndex(autoBookmark.getNccIndex());
            book.goTo(book.current());
        }
    }
""".trimIndent()