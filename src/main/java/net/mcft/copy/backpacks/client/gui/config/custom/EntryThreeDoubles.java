package net.mcft.copy.backpacks.client.gui.config.custom;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;

import net.minecraft.client.resources.I18n;

import net.mcft.copy.backpacks.WearableBackpacks;
import net.mcft.copy.backpacks.client.gui.config.BaseEntry;
import net.mcft.copy.backpacks.client.gui.control.GuiField;
import net.mcft.copy.backpacks.config.Status;
import net.mcft.copy.backpacks.config.Setting.ChangeRequiredAction;
import net.mcft.copy.backpacks.config.Status.Severity;

public class EntryThreeDoubles extends BaseEntry.Labelled {
	
	private final double _prevX, _prevY, _prevZ;
	private final GuiField _fieldX, _fieldY, _fieldZ;
	
	// TODO: Add range checking.
	public EntryThreeDoubles(String labelKey, double x, double y, double z) {
		labelKey = "config." + WearableBackpacks.MOD_ID + "." + labelKey;
		label.setText(I18n.format(labelKey));
		label.setTooltip(Arrays.asList(I18n.format(labelKey + ".tooltip").split("\\n")));
		
		_prevX = x; _prevY = y; _prevZ = z;
		
		_fieldX = new GuiField(0, ENTRY_HEIGHT);
		_fieldY = new GuiField(0, ENTRY_HEIGHT);
		_fieldZ = new GuiField(0, ENTRY_HEIGHT);
		_fieldX.setCharValidator(chr -> isCharValid(_fieldX.getText(), _fieldX.getCursorPosition(), chr));
		_fieldY.setCharValidator(chr -> isCharValid(_fieldY.getText(), _fieldY.getCursorPosition(), chr));
		_fieldZ.setCharValidator(chr -> isCharValid(_fieldZ.getText(), _fieldZ.getCursorPosition(), chr));
		
		setSpacing(4, 8, 2, 2, 6);
		addFixed(iconStatus);
		addFixed(label);
		addWeighted(_fieldX);
		addWeighted(_fieldY);
		addWeighted(_fieldZ);
		addFixed(buttonUndo);
		
		set(x, y, z);
	}
	
	private static boolean isCharValid(String text, int cursorPosition, char chr) {
		String validChars = "0123456789";
		return (validChars.contains(String.valueOf(chr)) ||
				((chr == '-') && (cursorPosition == 0) && !text.startsWith("-")) ||
				((chr == '.') && !text.contains(".")));
	}
	
	public OptionalDouble getX() { return get(_fieldX); }
	public OptionalDouble getY() { return get(_fieldY); }
	public OptionalDouble getZ() { return get(_fieldZ); }
	private static OptionalDouble get(GuiField field) {
		try { return OptionalDouble.of(Double.parseDouble(field.getText())); }
		catch (NumberFormatException ex) { return OptionalDouble.empty(); }
	}
	
	public void set(double x, double y, double z) {
		_fieldX.setText(Double.toString(x));
		_fieldY.setText(Double.toString(y));
		_fieldZ.setText(Double.toString(z));
	}
	
	@Override
	public void draw(int mouseX, int mouseY, float partialTicks) {
		_fieldX.setTextAndBorderColor(Severity.ERROR.foregroundColor, !getX().isPresent());
		_fieldY.setTextAndBorderColor(Severity.ERROR.foregroundColor, !getY().isPresent());
		_fieldZ.setTextAndBorderColor(Severity.ERROR.foregroundColor, !getZ().isPresent());
		super.draw(mouseX, mouseY, partialTicks);
	}
	
	// IConfigEntry implementation
	
	@Override
	public boolean isChanged() {
		return getX().equals(OptionalDouble.of(_prevX))
		    || getY().equals(OptionalDouble.of(_prevY))
		    || getZ().equals(OptionalDouble.of(_prevZ));
	}
	@Override
	public boolean isDefault() { return false; } // Doesn't matter.
	
	@Override
	public void undoChanges() { set(_prevX, _prevY, _prevZ); }
	@Override
	public void setToDefault() {  } // Doesn't matter.
	
	@Override
	public List<Status> getStatus() {
		return (!getX().isPresent() || !getY().isPresent() || !getZ().isPresent())
			? Arrays.asList(Status.INVALID)
			: Collections.emptyList();
	}
	
	@Override
	public ChangeRequiredAction applyChanges()
		{ return ChangeRequiredAction.None; }
	
}
