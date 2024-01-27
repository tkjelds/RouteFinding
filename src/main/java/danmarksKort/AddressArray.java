package danmarksKort;

import danmarksKort.address.Address;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class AddressArray implements Serializable {
    Address[] addresses;
    boolean sorted;

    public AddressArray(List<Address> list) {
        addresses = list.toArray(new Address[0]);
        sorted = false;
    }

    public void sort() {
        Arrays.sort(addresses);
        sorted = true;
    }

    /**
     * @param address Bruger binary search, til at søge igennem alle adresser udefra den adresse vi har fået indtastet
     * @return
     */
    public Address search(Address address) {
        if (!sorted) sort();
        int lo = 0, hi = addresses.length - 1;
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            int compare = address.compareTo(addresses[mid]);
            if (compare < 0) hi = mid - 1;
            else if (compare > 0) lo = mid + 1;
            else {
                return addresses[mid];
            }
        }
        return addresses[lo];
    }

}