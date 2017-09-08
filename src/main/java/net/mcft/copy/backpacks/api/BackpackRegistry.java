package net.mcft.copy.backpacks.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import net.mcft.copy.backpacks.item.ItemBackpack;

public final class BackpackRegistry {
	
	private BackpackRegistry() {  }
	
	private static final Map<String, BackpackEntityEntry> _byID = new HashMap<>();
	private static final Map<Class<? extends EntityLivingBase>, BackpackEntityEntry> _byClass = new HashMap<>();
	static { addEntityEntry(new BackpackEntityEntry(EntityPlayer.class, RenderOptions.DEFAULT, true)); }
	
	/** Registers an entity as possible backpack carrier, meaning
	 *  they'll get constructed with an IBackpack capability.
	 *  Must be called in pre-initialization phase (or before). */
	public static void registerEntity(Class<? extends EntityLivingBase> entityClass, RenderOptions renderOptions) {
		if (Loader.instance().getLoaderState().compareTo(LoaderState.PREINITIALIZATION) > 0)
			throw new IllegalStateException("Must be called during (or before) pre-initialization phase.");
		addEntityEntry(new BackpackEntityEntry(entityClass, renderOptions, true));
	}
	/** Registers an entity as possible backpack carrier, meaning
	 *  they'll get constructed with an IBackpack capability.
	 *  Must be called in pre-initialization phase (or before). */
	public static void registerEntity(String entityID, RenderOptions renderOptions) {
		if (Loader.instance().getLoaderState().compareTo(LoaderState.PREINITIALIZATION) > 0)
			throw new IllegalStateException("Must be called during (or before) pre-initialization phase.");
		addEntityEntry(new BackpackEntityEntry(entityID, renderOptions, true));
	}
	
	/**
	 * Registers a backpack to randomly spawn on the specified entity.
	 * Must be called after registerEntity, in pre-initialization phase (or before).
	 * 
	 * @param entityID The entity to register to spawn with this backpack.
	 * 
	 * @param entryID    String uniquely identifying this entry for this entity.
	 *                   For example "wearblebackpacks:default".
	 * @param backpack   Backpack item to spawn on the entity (must implement IBackpackType).
	 * @param chance     Chance in 1 out of X. For example 100 = 1% and 1000 = 0.1%.
	 * @param lootTable  Loot table for the backpack when spawned on this mob (if any).
	 * @param colorRange A range of colors to spawn the backpack with, or null if default.
	 **/
	public static void registerBackpack(String entityID,
	                                    String entryID, ItemBackpack backpack, int chance,
	                                    String lootTable, ColorRange colorRange) {
		if (Loader.instance().getLoaderState().compareTo(LoaderState.PREINITIALIZATION) > 0)
			throw new IllegalStateException("Must be called during (or before) pre-initialization phase.");
		if (entryID == null) throw new NullPointerException("id must not be null");
		addBackpackEntry(entityID, new BackpackEntry(entryID, backpack, chance, lootTable, colorRange, true));
	}
	/**
	 * Registers a backpack to randomly spawn on the specified entity.
	 * Must be called after registerEntity, in pre-initialization phase (or before).
	 * 
	 * @param entityClass The entity to register to spawn with this backpack.
	 * 
	 * @param entityID   String uniquely identifying this entry for this entity.
	 *                   For example "wearblebackpacks:default".
	 * @param backpack   Backpack item to spawn on the entity (must implement IBackpackType).
	 * @param chance     Chance in 1 out of X. For example 100 = 1% and 1000 = 0.1%.
	 * @param lootTable  Loot table for the backpack when spawned on this mob (if any).
	 * @param colorRange A range of colors to spawn the backpack with, or null if default.
	 **/
	public static void registerBackpack(Class<? extends EntityLivingBase> entityClass,
	                                    String entityID, ItemBackpack backpack, int chance,
	                                    String lootTable, ColorRange colorRange) {
		if (Loader.instance().getLoaderState().compareTo(LoaderState.PREINITIALIZATION) > 0)
			throw new IllegalStateException("Must be called during (or before) pre-initialization phase.");
		if (entityID == null) throw new NullPointerException("id must not be null");
		addBackpackEntry(entityClass, new BackpackEntry(entityID, backpack, chance, lootTable, colorRange, true));
	}
	
	/** Returns if the specified entity should be able to wear backpacks.
	 *  This affects whether the entity will be constructed with an IBackpack capability. */
	public static boolean canEntityWearBackpacks(Entity entity)
		{ return _byClass.containsKey(entity.getClass()); }
	
	
	public static BackpackEntityEntry getEntityEntry(Class<? extends EntityLivingBase> entityClass)
		{ return _byClass.get(entityClass); }
	
	public static Collection<BackpackEntityEntry> getEntityEntries()
		{ return Collections.unmodifiableCollection(_byID.values()); }
	
	public static void addEntityEntry(BackpackEntityEntry value) {
		if (value == null) throw new NullPointerException("value must not be null");
		if (value.entityID != null) {
			if (!_byID.containsKey(value.entityID)) _byID.put(value.entityID, value);
			else BackpackHelper.LOG.warn("Attempted to add duplicate entry for ID " + value.entityID);
		}
		if (value.entityClass != null) {
			if (!_byClass.containsKey(value.entityClass)) _byClass.put(value.entityClass, value);
			else BackpackHelper.LOG.warn("Attempted to add duplicate entry for class " + value.entityClass.getName());
		}
	}
	
	public static List<BackpackEntry> getBackpackEntries(Class<? extends EntityLivingBase> entityClass) {
		if (entityClass == null) throw new NullPointerException("entityClass must not be null");
		BackpackEntityEntry entityEntry = _byClass.get(entityClass);
		return (entityEntry != null)
			? Collections.unmodifiableList(entityEntry._backpackEntries)
			: Collections.emptyList();
	}
	
	public static void addBackpackEntry(Class<? extends EntityLivingBase> entityClass, BackpackEntry value) {
		if (entityClass == null) throw new NullPointerException("entityClass must not be null");
		if (value == null) throw new NullPointerException("value must not be null");
		BackpackEntityEntry entityEntry = _entities.get(entityClass);
		if (entityEntry == null) throw new IllegalStateException("entityClass has not been registered: " + entityClass.getName());
		entityEntry._backpackEntries.add(value);
	}
	
	public static void setToDefault() {
		_byID.entrySet().removeIf(e -> !e.getValue().isDefault);
		_byID.values().forEach(e -> e._backpackEntries.removeIf(b -> !b.isDefault));
		_byClass.entrySet().removeIf(e -> !e.getValue().isDefault);
		_byClass.values().forEach(e -> e._backpackEntries.removeIf(b -> !b.isDefault));
	}
	
	
	public static final class BackpackEntityEntry {
		
		public final String entityID;
		public final Class<? extends EntityLivingBase> entityClass;
		public final RenderOptions renderOptions;
		public final boolean isDefault;
		
		private final List<BackpackEntry> _backpackEntries = new ArrayList<>();
		
		public BackpackEntityEntry(String entityID, RenderOptions renderOptions)
			{ this(entityID, renderOptions, false); }
		private BackpackEntityEntry(String entityID, RenderOptions renderOptions, boolean isDefault) {
			this(entityID, Optional.ofNullable(ForgeRegistries.ENTITIES.getValue(new ResourceLocation(entityID)))
					.map(EntityEntry::getEntityClass)
					.filter(EntityLivingBase.class::isAssignableFrom)
					// Java is not smart enough to figure this out without the hint! :(
					.<Class<? extends EntityLivingBase>>map(c -> c.asSubclass(EntityLivingBase.class))
					.orElse(null)
				, renderOptions, isDefault);
			if (entityID == null) throw new NullPointerException("entityID must not be null");
		}
		
		public BackpackEntityEntry(Class<? extends EntityLivingBase> entityClass, RenderOptions renderOptions)
			{ this(entityClass, renderOptions, false); }
		private BackpackEntityEntry(Class<? extends EntityLivingBase> entityClass, RenderOptions renderOptions, boolean isDefault) {
			this(ForgeRegistries.ENTITIES.getValues().stream()
					.filter(e -> (e.getEntityClass() == entityClass)).findAny()
					.map(e -> e.getRegistryName().toString()).orElse(null)
				, entityClass, renderOptions, isDefault);
			if (entityClass == null) throw new NullPointerException("entityID must not be null");
		}
		
		private BackpackEntityEntry(String entityID, Class<? extends EntityLivingBase> entityClass,
		                            RenderOptions renderOptions, boolean isDefault) {
			if (renderOptions == null) throw new NullPointerException("renderOptions must not be null");
			this.entityClass   = entityClass;
			this.entityID      = entityID;
			this.renderOptions = renderOptions;
			this.isDefault     = isDefault;
		}
		
	}
	
	public static final class RenderOptions {
		public static final RenderOptions DEFAULT = new RenderOptions(12, 2.5, 0, 0, 0.8);
		
		public final double x, y, z;
		public final double rotate, scale;
		
		public RenderOptions(double x, double y, double z,
		                     double rotate, double scale) {
			this.x = x; this.y = y; this.z = z;
			this.rotate = rotate; this.scale = scale;
		}
	}
	
	public static final class BackpackEntry {
		public final String id;
		public final ItemBackpack backpack;
		public final int chance;
		public final String lootTable;
		public final ColorRange colorRange;
		public final boolean isDefault;
		// TODO: Check if color is supported?
		
		public BackpackEntry(String id, ItemBackpack backpack, int chance,
		                     String lootTable, ColorRange colorRange)
			{ this(id, backpack, chance, lootTable, colorRange, false); }
		private BackpackEntry(String id, ItemBackpack backpack, int chance,
		                      String lootTable, ColorRange colorRange, boolean isDefault) {
			if (backpack == null) throw new NullPointerException("backpack must not be null");
			if (!(backpack instanceof IBackpackType)) throw new IllegalArgumentException("backpack must be an IBackpackType");
			if (chance < 0) throw new NullPointerException("chance must not be negative");
			
			this.id         = id;
			this.backpack   = backpack;
			this.chance     = chance;
			this.lootTable  = lootTable;
			this.colorRange = colorRange;
			this.isDefault  = isDefault;
		}
	}
	
	public static final class ColorRange {
		public static final ColorRange DEFAULT = new ColorRange(0x202020, 0xD0D0D0);
		
		public final int min, max;
		
		public ColorRange(int min, int max)
			{ this.min = min; this.max = max; }
	}
	
}
