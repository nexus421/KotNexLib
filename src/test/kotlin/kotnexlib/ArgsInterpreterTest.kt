package kotnexlib

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ArgsInterpreterTest {

    @Test
    fun testGetValue() {
        val args = arrayOf("name=Junie", "mode=auto", "quoted=\"hello world\"")
        val interpreter = ArgsInterpreter(args)

        assertEquals("Junie", interpreter.getValue("name"))
        assertEquals("auto", interpreter.getValue("mode"))
        assertEquals("\"hello world\"", interpreter.getValue("quoted"))
        assertNull(interpreter.getValue("nonexistent"))
    }

    @Test
    fun testGetValueAsTypes() {
        val args = arrayOf("count=10", "price=9.99", "enabled=true", "disabled=false")
        val interpreter = ArgsInterpreter(args)

        assertEquals(10, interpreter.getValueAsInt("count"))
        assertEquals(9.99, interpreter.getValueAsDouble("price"))
        assertEquals(true, interpreter.getValueAsBoolean("enabled"))
        assertEquals(false, interpreter.getValueAsBoolean("disabled"))

        assertNull(interpreter.getValueAsInt("price")) // Should fail to cast
        assertNull(interpreter.getValueAsBoolean("name"))
    }

    @Test
    fun testContainsParam() {
        val args = arrayOf("-h", "--verbose", "-d")
        val interpreter = ArgsInterpreter(args)

        assertTrue(interpreter.containsParam("h"))
        assertTrue(interpreter.containsParam("d"))
        assertFalse(interpreter.containsParam("verbose")) // containsParam appends one '-'
        assertFalse(interpreter.containsParam("help"))
    }

    @Test
    fun testCustomSplittingChar() {
        val args = arrayOf("name:Junie", "id:123")
        val interpreter = ArgsInterpreter(args, splittingChar = ':')

        assertEquals("Junie", interpreter.getValue("name"))
        assertEquals(123, interpreter.getValueAsInt("id"))
    }
}
