package li.cil.oc2.client.audio;

import li.cil.oc2.common.Constants;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class LoopingTileEntitySound extends AbstractTickableSoundInstance {
    private static final float FADE_IN_DURATION_IN_SECONDS = 2.0f;
    private static final float FADE_IN_DURATION_IN_TICKS = FADE_IN_DURATION_IN_SECONDS * Constants.SECONDS_TO_TICKS;
    private static final float FADE_IN_PER_TICK = 1f / FADE_IN_DURATION_IN_TICKS;

    ///////////////////////////////////////////////////////////////////

    private final BlockEntity tileEntity;

    ///////////////////////////////////////////////////////////////////

    public LoopingTileEntitySound(final BlockEntity tileEntity, final SoundEvent sound) {
        super(sound, SoundSource.BLOCKS);
        this.tileEntity = tileEntity;
        this.volume = 0;

        final Vec3 position = Vec3.atCenterOf(tileEntity.getBlockPos());
        x = position.x;
        y = position.y;
        z = position.z;

        looping = true;
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    public void tick() {
        volume = Mth.clamp(volume + FADE_IN_PER_TICK, 0, 1);
        final ChunkPos chunkPos = new ChunkPos(tileEntity.getBlockPos());
        if (tileEntity.isRemoved() || !tileEntity.getLevel().hasChunk(chunkPos.x, chunkPos.z)) {
            stop();
        }
    }
}
