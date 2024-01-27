package danmarksKort;

import danmarksKort.address.Address;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @source https://stackoverflow.com/questions/36861056/javafx-textfield-auto-suggestions
 * Currently the first answer, by user: "Ruslan Gabbazov"
 * Ruslan Gabbazov based his solution on: https://gist.github.com/floralvikings/10290131
 */

public class NavigationTextField extends TextField {
    private ContextMenu suggestions;


    public NavigationTextField() {
        super();
        suggestions = new ContextMenu();

    }

    /**
     * @param addresses Array af addresser til sammenligning med user input
     */
    void setListener(AddressArray addresses) {
        textProperty().addListener((observedVal, oldVal, newVal) -> {
            String input = getText();
            if (input == null || input.isEmpty()) {
                suggestions.hide();
            } else {
                Address parsed = Address.parse(input);
                List<Address> temp = Arrays.stream(addresses.addresses)
                        .filter(e -> e.street.toLowerCase().contains(parsed.street.toLowerCase()))
                        .filter(e -> e.housenumber.contains(parsed.housenumber))
                        .limit(5)
                        .collect(Collectors.toList());
                if (!temp.isEmpty()) {
                    fillMenu(temp);
                    if (!suggestions.isShowing()) {
                        suggestions.show(NavigationTextField.this, Side.BOTTOM, 0, 0);
                    }
                } else {
                    suggestions.hide();
                }
            }
        });

    }

    private void fillMenu(List<Address> found) {
        List<MenuItem> items = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (i == found.size()) {
                break;
            }

            String address = found.get(i).toString();
            Text text = new Text(address);

            CustomMenuItem menuItem = new CustomMenuItem(text, true);
            items.add(menuItem);

            menuItem.setOnAction(actionEvent -> {
                setText(address);
                positionCaret(address.length());
                suggestions.hide();
            });
        }
        suggestions.getItems().clear();
        suggestions.getItems().addAll(items);
    }
}

