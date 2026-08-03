package udblib.sql;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;
import org.junit.jupiter.api.Test;

class SqlResultRowTest {
    @Test
    void looksUpColumnsIgnoringCase() {
        sqlResultRow row = new sqlResultRow(1, Locale.US);
        row.set(0, "ExampleValue", 42);
        assertEquals(42, row.get("examplevalue"));
    }
}
