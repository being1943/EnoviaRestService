package com.being1943.enoviamcp;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MqlSafetyTest {
 @Test void allowsReadOnlyBusQuery(){assertEquals("temp query bus Part * * limit 10;",MqlSafety.validate("temp query bus Part * * limit 10"));}
 @Test void allowsSchemaListing(){assertEquals("list relationship;",MqlSafety.validate("list relationship"));}
 @Test void blocksMutation(){assertThrows(IllegalArgumentException.class,()->MqlSafety.validate("modify bus Part P-1 A name P-2"));}
 @Test void blocksStatementChaining(){assertThrows(IllegalArgumentException.class,()->MqlSafety.validate("print bus Part P-1 A; delete bus P-1"));}
 @Test void blocksMultiline(){assertThrows(IllegalArgumentException.class,()->MqlSafety.validate("print bus Part P-1 A\nlist type"));}
 @Test void blocksUnlistedCommands(){assertThrows(IllegalArgumentException.class,()->MqlSafety.validate("execute program SomeJPO"));}
 @Test void quotesNamesWithSpaces(){assertEquals("\"Drawing of\"",MqlSafety.quoted("Drawing of","relationship"));}
}