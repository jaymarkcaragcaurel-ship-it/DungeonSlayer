import Systems.Core.ClassSystem;
import Systems.UI.ClassSelectionMenu;
import Systems.UI.LoadingScreen;

import javax.swing.*;
import java.util.Optional;

public class Main {
    public static void main(String[] args) throws Exception {
        LoadingScreen loading = new LoadingScreen();

        // Touching ClassSystem here runs its table validation behind the loading screen.
        loading.addStep("Reading class tables", () -> ClassSystem.count());
        loading.addStep("Stocking item registry", () -> {});
        loading.addStep("Brewing potion catalog", () -> {});
        loading.addStep("Summoning monsters", () -> {});
        loading.addStep("Carving dungeons", () -> {});

        // Blocks until the player clicks "Begin your descent".
        // Returns false if a step failed or the window was closed.
        if (!loading.run()) {
            return;
        }

        // Blocks until the player confirms a class and name, or closes the window.
        Optional<ClassSelectionMenu.Choice> choice = ClassSelectionMenu.show();
        if (choice.isEmpty()) {
            return;
        }

        // From here on, all Swing work belongs on the event thread.
        SwingUtilities.invokeLater(() -> openGameWindow(choice.get()));
    }

    /** Placeholder: replace with your real main menu / game window. */
    private static void openGameWindow(ClassSelectionMenu.Choice choice) {
        // Once I can see Player, this is where it gets built:
        //   Player player = new Player(...);
        //   ClassSystem.applyToPlayer(player, choice.classIndex());
        //   player.setName(choice.name());
        //   wallet.add(ClassSystem.startingGold(choice.classIndex()), GoldReason.START);
        String text = choice.name() + " the " + ClassSystem.name(choice.classIndex());

        JFrame game = new JFrame("Dungeon Crawler");
        game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        game.add(new JLabel(text, SwingConstants.CENTER));
        game.setSize(900, 600);
        game.setLocationRelativeTo(null);
        game.setVisible(true);
    }
}