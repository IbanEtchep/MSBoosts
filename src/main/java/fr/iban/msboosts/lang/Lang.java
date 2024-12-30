package fr.iban.msboosts.lang;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public enum Lang {

    BOOST_LIST_GLOBAL_TITLE("boosts.list.global.title"),
    BOOST_LIST_PERSONAL_TITLE("boosts.list.personal.title"),
    BOOST_LIST_LINE_ACTIVE("boosts.list.line.active"), // params: %percentage% %duration% %remaining%
    BOOST_LIST_LINE_PAUSED("boosts.list.line.paused"), // params: %percentage% %duration% %remaining%
    BOOST_LIST_LINE_EXPIRED("boosts.list.line.expired"), // params: %percentage% %duration%
    BOOST_LIST_LINE_QUEUED("boosts.list.line.queued"), // params: %percentage% %duration%
    BOOST_LIST_NONE("boosts.list.global.none"),
    BOOST_LIST_REMOVE_BUTTON("boosts.list.remove.button"),
    BOOST_LIST_TOTAL("boosts.list.total"),

    BOOST_GLOBAL_START("boosts.global.start"), // params: %percentage% %duration%
    BOOST_GLOBAL_END("boosts.global.end"), // params: %percentage% %duration%
    BOOST_GLOBAL_ACTIVATED("boosts.global.activated"), // params: %percentage% %duration%
    BOOST_PERSONAL_START("boosts.personal.start"), // params: %percentage% %duration%
    BOOST_PERSONAL_END("boosts.personal.end"), // params: %percentage% %duration%
    BOOST_PERSONAL_RESUMED("boosts.personal.resumed"), // params: %percentage% %duration%
    BOOST_PERSONAL_ACTIVATED("boosts.personal.activated"), // params: %percentage% %duration%

    BOOST_GLOBAL_ADDED("boosts.global.added"), // params: %percentage% %duration%
    BOOST_PERSONAL_ADDED("boosts.personal.added"), // params: %player% %percentage% %duration%
    BOOST_PERSONAL_RECEIVED("boosts.personal.received"), // params: %percentage% %duration%
    BOOST_REMOVED("boosts.removed"),
    BOOST_NOT_FOUND("boosts.notfound"),
    RELOADED("reloaded");

    private final String key;
    private static YamlDocument messages;
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer.plainText();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer.legacySection();

    Lang(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static void setMessages(YamlDocument messagesFile) {
        messages = messagesFile;
    }

    private String getRaw() {
        return messages.getString(key, "Missing translation: " + key);
    }

    public Component component() {
        return MINI_MESSAGE.deserialize(getRaw());
    }

    public Component component(String... placeholders) {
        if (placeholders.length % 2 != 0) {
            throw new IllegalArgumentException("Placeholders must be paired (key, value)");
        }

        String message = getRaw();
        for (int i = 0; i < placeholders.length; i += 2) {
            String placeholder = placeholders[i + 1];
            message = message.replace("%" + placeholders[i] + "%", placeholder);
        }

        return MINI_MESSAGE.deserialize(message);
    }

    public String plainText() {
        return PLAIN_SERIALIZER.serialize(MINI_MESSAGE.deserialize(getRaw()));
    }

    public String plainText(String... placeholders) {
        return PLAIN_SERIALIZER.serialize(component(placeholders));
    }

    public String toString() {
        return LEGACY_SERIALIZER.serialize(MINI_MESSAGE.deserialize(getRaw()));
    }

    public String toString(String... placeholders) {
        return LEGACY_SERIALIZER.serialize(component(placeholders));
    }
}