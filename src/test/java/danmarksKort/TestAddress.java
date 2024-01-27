package danmarksKort;
import danmarksKort.Model;
import danmarksKort.address.Address;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
 public class TestAddress {
//         Model model;
//         String s1;
//         String s2;
//         String s3;
//         String s4;
//         String s5;
//         String s6;
//         String s7;
//         String s8;
//         String s9;
//         String s10;

//         Address a1;
//         Address a2;
//         Address a3;
//         Address a4;
//         Address a5;
//         Address a6;
//         Address a7;
//         Address a8;
//         Address a9;
//         Address a10;


//         @Before
//         public void setUp() {
//             model = Model.getInstance();
//             s1 = "Agerup 6";
//             s2 = "Agerup 6A";
//             s3 = "Anton Rosens Plads 1A";
//             s4 = "ANTON ROSENS PLADS 1C 8305 Samsø";
//             s5 = "Anton Rosens plads 1D"; // Anton
//             s6 = "Blåmejsevej 22";
//             s7 = "GråmejseVEJ 7";
//             s8 = "PER POulsens vej 4";
//             s9 = "per poul 8305";
//             s10 ="8305 Samsø";

//             a1 = parse(s3);
//             a2 = parse(s4);
//             a3 = parse(s5);
//             a4 = parse(s1);
//             a5 = parse(s2);
//             a6 = parse(s8);
//             a7 = parse(s6);
//             a8 = parse(s7);
//         }
//         @After
//         public void tearDown() {
//             s1 = null;
//             s2 = null;
//             s3 = null;
//             s4 = null;
//             s5 = null;
//             s6 = null;
//             s7 = null;
//             s8 = null;
//             s9 = null;
//             s10 = null;
//         }
//         @Test
//         public void testViewControlParser() {
//             assertEquals("Agerup" ,parse(s1).street);
//             assertEquals("6",parse(s1).housenumber);
//             assertEquals("6A",parse(s2).housenumber);
//             assertEquals("Anton Rosens Plads",parse(s3).street);
//             assertEquals("Anton Rosens Plads",parse(s4).street);
//             assertEquals("1C",parse(s4).housenumber);
//             assertEquals("Anton Rosens Plads",parse(s5).street);
//             assertEquals("1D",parse(s5).housenumber);
//             assertEquals("Blåmejsevej",parse(s6).street);
//             assertEquals("22",parse(s6).housenumber);
//             assertEquals("Gråmejsevej",parse(s7).street);
//             assertEquals("7",parse(s7).housenumber);
//             assertEquals("Per Poulsens Vej",parse(s8).street);
//             assertEquals("4",parse(s8).housenumber);
//             assertThrows(ExceptionInInitializerError.class, () -> parse(s10));
//             assertEquals("8305",parse(s9).postcode);



//         }
//         @Test
//         public void testSearch() {
//             Assertions.assertEquals("Anton Rosens Plads",model.addressArray.search(a1).street);
//             Assertions.assertEquals("Anton Rosens Plads",model.addressArray.search(a2).street);
//             Assertions.assertEquals("Anton Rosens Plads",model.addressArray.search(a3).street);
//             Assertions.assertEquals("1A",model.addressArray.search(a1).housenumber);
//             Assertions.assertEquals("1C",model.addressArray.search(a2).housenumber);
//             Assertions.assertEquals("1D",model.addressArray.search(a3).housenumber);
//             Assertions.assertEquals("Agerup",model.addressArray.search(a4).street);
//             Assertions.assertEquals("6",model.addressArray.search(a4).housenumber);
//             Assertions.assertEquals("6A",model.addressArray.search(a5).housenumber);
//         }
//         @Test
//         public void testSort() {
//             model.addressArray.sort();
//             Assertions.assertEquals("50A",model.addressArray.addresses[291].housenumber);
//             Assertions.assertEquals("50B",model.addressArray.addresses[292].housenumber);
//             Assertions.assertEquals("50C",model.addressArray.addresses[293].housenumber);
//             Assertions.assertEquals("50D",model.addressArray.addresses[294].housenumber);
//             Assertions.assertEquals("50E",model.addressArray.addresses[295].housenumber);
//         }
//         public Address parse(String s) {
//             Address temp = Address.parse(s);
//             return model.addressArray.search(temp);
//         }
    }


