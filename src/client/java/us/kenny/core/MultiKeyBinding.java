package us.kenny.core;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import us.kenny.ModifierManager;
import us.kenny.ToggleManager;

import java.util.UUID;
import net.minecraft.client.KeyMapping.Category;

/**
 * Need to create this class to mimic a KeyBinding because the native KeyBinding
 * constructor will overwrite existing keys bound to that action.
 */
public class MultiKeyBinding {

    private final UUID id;
    private final String action;
    private Category category;
    private InputConstants.Key key;

    private boolean pressed;
    private int timesPressed;
    private boolean directToggle;

    public MultiKeyBinding(UUID id, String action, Category category, InputConstants.Key key) {
        this.id = id;
        this.action = action;
        this.category = category;
        this.key = key;
    }

    public boolean isUnbound() {
        return this.key.equals(InputConstants.UNKNOWN);
    }

    public void release() {
        this.timesPressed = 0;
        this.forceSetPressed(false);
    }

    public boolean shouldSetOnIngameFocus() {
        return this.key.getType() == InputConstants.Type.KEYSYM
                && this.key.getValue() != InputConstants.UNKNOWN.getValue();
    }

    public UUID getId() {
        return this.id;
    }

    public String getAction() {
        return this.action;
    }

    public Category getCategory() {
        return this.category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public InputConstants.Key getKey() {
        return this.key;
    }

    public void setKey(InputConstants.Key key) {
        this.key = key;
    }

    public Component getDisplayName() {
        return ModifierManager.getDisplayName(this.id.toString(), this.key.getDisplayName());
    }

    public int getTimesPressed() {
        return this.timesPressed;
    }

    public void incrementTimesPressed() {
        this.timesPressed++;
    }

    public void decrementTimesPressed() {
        this.timesPressed--;
    }

    public boolean getPressed() {
        return this.pressed;
    }

    public void setPressed(boolean pressed) {
        this.pressed = pressed;
    }

    /**
     * Handle a physical input event for this alternative binding. A direct-toggle
     * binding flips only on a fresh press and deliberately ignores the matching
     * key release; normal bindings retain vanilla hold behavior.
     */
    public void handleInput(boolean pressed, boolean repeat) {
        if (this.isDirectToggleActive()) {
            if (pressed && !repeat) {
                this.forceSetPressed(!this.getPressed());
            }
            return;
        }
        this.setPressed(pressed);
    }

    public boolean isDirectToggleActive() {
        return this.directToggle && us.kenny.config.FeatureConfig.get().farming.toggles.enabled;
    }

    public boolean isDirectToggle() {
        return this.directToggle;
    }

    public void setDirectToggle(boolean directToggle) {
        if (this.directToggle != directToggle) {
            this.forceSetPressed(false);
        }
        this.directToggle = directToggle;
    }

    /**
     * Bypass sticky/toggle input semantics when state must be reset or restored.
     */
    public void forceSetPressed(boolean pressed) {
        this.pressed = pressed;
    }

    /**
     * The translation key to use when displaying this binding's name in the UI
     * (collision tooltips, etc.). Mod-defined actions like the toggles group
     * are their own translation keys; bindings under a vanilla key strip the
     * "multi." prefix to share the vanilla translation.
     */
    public String getTranslationKey() {
        return ToggleManager.isToggleAction(this.action)
                ? this.action
                : this.action.replaceFirst("^multi.", "");
    }
}
