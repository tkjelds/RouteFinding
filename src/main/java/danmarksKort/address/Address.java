package danmarksKort.address;

import danmarksKort.AlertBox;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

public class Address implements Comparable<Address>, Serializable {
    static String regex = "^ *(?<street>[a-zæøåA-ZÆØÅ ,-.]*[a-zæøåA-ZÆØÅ]+(?:[,-.])?) *(?<house>[0-9]*[a-zA-Z]*)? " +
            "*(?<floor>([0-9]{1,2})?([s]{1}[t]{1})?)? " +
            "*(?<side>[t]{1}[vh]{1})? *(?<postcode>[0-9]{4})? *(?<city>[a-zæøåA-ZÆØÅ ]*) *$";

    static Pattern pattern = Pattern.compile(regex);
    public String street, housenumber, floor, side, postcode, city, municipality;
    public int houseIntNumber;
    private float lon, lat;

    public Address(String _street, String _house, String _floor, String _side, String _postcode,
                   String _city, String _municipality) {
        municipality = _municipality;
        street = _street;
        housenumber = _house;
        floor = _floor;
        side = _side;
        postcode = _postcode;
        city = _city;
    }

    public Address() {
    }


    public static Address parse(String input) {

        var matcher = pattern.matcher(input);
        if (matcher.matches()) {
            return new Builder()
                    .street(matcher.group("street"))
                    .house(matcher.group("house"))
                    .floor(matcher.group("floor"))
                    .side(matcher.group("side"))
                    .city(matcher.group("city"))
                    .postcode(matcher.group("postcode"))
                    .municipality(matcher.group("city"))
                    .build();
        } else {

            AlertBox.display("Cannot Parse", "Cannot parse: " + input);
            throw new IllegalArgumentException("Cannot parse: " + input);
        }

    }

    private int splitHouseNumber(String housenumber) {
        if (housenumber != null) {
            String temp = housenumber.replaceAll("[^0-9]", "");
            if (!temp.isEmpty()) {
                try {
                    int number = Integer.parseInt(temp);
                    return number;
                } catch (NullPointerException e) {
                    return 0;
                }
            } else {
                return 0;
            }
        }
        return 0;
    }


    public String toString() {
        return String.format("%s %s %s %s %s %s", street, housenumber, Objects.toString(floor, ""),
                Objects.toString(side, ""), postcode, city);
    }

    public String getStreet() {
        return street;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public int compareTo(Address address) {

        if (!this.street.toLowerCase().equals(address.street.toLowerCase())) {
            return this.street.toLowerCase().compareTo(address.street.toLowerCase());
        } else {
            this.houseIntNumber = splitHouseNumber(this.housenumber);
            address.houseIntNumber = splitHouseNumber(address.housenumber);
            if (this.houseIntNumber < address.houseIntNumber) {
                return -1;
            } else if (this.houseIntNumber > address.houseIntNumber) {
                return +1;
            } else {
                String thisTemp = this.housenumber.replaceAll("[^a-zæøåA-ZÆØÅ]", "");
                String paramTemp = address.housenumber.replaceAll("[^a-zæøåA-ZÆØÅ]", "");
                return thisTemp.toLowerCase().compareTo(paramTemp.toLowerCase());
            }
        }
    }

    public static class Builder {
        private String street, house, floor, side, postcode, city, municipality;

        public Address.Builder street(String _street) {
            street = _street;
            return this;
        }

        public Address.Builder house(String _house) {
            house = _house;
            return this;
        }

        public Address.Builder floor(String _floor) {
            floor = _floor;
            return this;
        }

        public Address.Builder side(String _side) {
            side = _side;
            return this;
        }

        public Address.Builder postcode(String _postcode) {
            postcode = _postcode;
            return this;
        }

        public Address.Builder city(String _city) {
            city = _city;
            return this;
        }

        public Address.Builder municipality(String _municipality) {
            municipality = _municipality;
            return this;
        }

        public Address build() {
            return new Address(street, house, floor, side, postcode, city, municipality);
        }
    }
}