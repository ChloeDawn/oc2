package li.cil.oc2.common.tileentity;

import li.cil.oc2.api.bus.device.object.Callback;
import li.cil.oc2.api.bus.device.object.NamedDevice;
import li.cil.oc2.common.Config;
import li.cil.oc2.common.Constants;
import li.cil.oc2.common.capabilities.Capabilities;
import li.cil.oc2.common.energy.FixedEnergyStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Collections.singletonList;

public final class ChargerTileEntity extends AbstractTileEntity implements NamedDevice {
    private static final Predicate<Entity> ENTITY_PREDICATE =
        EntitySelector.NO_SPECTATORS
            .and(EntitySelector.ENTITY_STILL_ALIVE);

    ///////////////////////////////////////////////////////////////////

    private final FixedEnergyStorage energy = new FixedEnergyStorage(Config.chargerEnergyStorage);
    private boolean isCharging;

    ///////////////////////////////////////////////////////////////////

    ChargerTileEntity(final BlockPos pos, final BlockState state) {
        super(TileEntities.CHARGER_TILE_ENTITY.get(), pos, state);
    }

    ///////////////////////////////////////////////////////////////////

    public static void tick(final Level level, final BlockPos pos, final BlockState state, final ChargerTileEntity tileEntity) {
        tileEntity.tick();
    }

    private void tick() {
        isCharging = false;
        chargeBlock();
        chargeEntities();
    }

    @Override
    protected void saveAdditional(final CompoundTag tag) {
        super.saveAdditional(tag);

        tag.put(Constants.ENERGY_TAG_NAME, energy.serializeNBT());
    }

    @Override
    public void load(final CompoundTag tag) {
        super.load(tag);

        energy.deserializeNBT(tag.getCompound(Constants.ENERGY_TAG_NAME));
    }

    @Callback
    public boolean isCharging() {
        return isCharging;
    }

    @Override
    public Collection<String> getDeviceTypeNames() {
        return singletonList("charger");
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    protected void collectCapabilities(final CapabilityCollector collector, @Nullable final Direction direction) {
        collector.offer(Capabilities.ENERGY_STORAGE, energy);
    }

    ///////////////////////////////////////////////////////////////////

    private void chargeBlock() {
        if (energy.getEnergyStored() == 0) {
            return;
        }

        final BlockEntity tileEntity = level.getBlockEntity(getBlockPos().above());
        if (tileEntity != null) {
            chargeCapabilityProvider(tileEntity);
        }
    }

    private void chargeEntities() {
        if (energy.getEnergyStored() == 0) {
            return;
        }

        final List<Entity> entities = level.getEntities((Entity) null, new AABB(getBlockPos().above()), ENTITY_PREDICATE);
        for (final Entity entity : entities) {
            chargeCapabilityProvider(entity);
        }
    }

    private void chargeCapabilityProvider(final ICapabilityProvider capabilityProvider) {
        capabilityProvider.getCapability(Capabilities.ENERGY_STORAGE, Direction.DOWN).ifPresent(this::charge);
        capabilityProvider.getCapability(Capabilities.ITEM_HANDLER, Direction.DOWN).ifPresent(this::chargeItems);
    }

    private void chargeItems(final IItemHandler itemHandler) {
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            final ItemStack stack = itemHandler.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                stack.getCapability(Capabilities.ENERGY_STORAGE).ifPresent(this::charge);
            }
        }
    }

    private void charge(final IEnergyStorage energyStorage) {
        final int amount = Math.min(energy.getEnergyStored(), Config.chargerEnergyPerTick);
        final boolean simulate = level.isClientSide;
        if (energy.extractEnergy(energyStorage.receiveEnergy(amount, simulate), simulate) > 0) {
            isCharging = true;
        }
    }
}
