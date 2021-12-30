package li.cil.oc2.common.network.message;

import li.cil.oc2.common.network.MessageUtils;
import li.cil.oc2.common.tileentity.BusCableTileEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraftforge.network.NetworkEvent;

public abstract class BusInterfaceNameMessage extends AbstractMessage {
    protected BlockPos pos;
    protected Direction side;
    protected String value;

    ///////////////////////////////////////////////////////////////////

    protected BusInterfaceNameMessage(final BusCableTileEntity tileEntity, final Direction side, final String value) {
        this.pos = tileEntity.getBlockPos();
        this.side = side;
        this.value = value;
    }

    protected BusInterfaceNameMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    public void fromBytes(final FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        side = buffer.readEnum(Direction.class);
        value = buffer.readUtf(32);
    }

    @Override
    public void toBytes(final FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeEnum(side);
        buffer.writeUtf(value, 32);
    }

    ///////////////////////////////////////////////////////////////////

    public static final class ToClient extends BusInterfaceNameMessage {
        public ToClient(final BusCableTileEntity tileEntity, final Direction side, final String value) {
            super(tileEntity, side, value);
        }

        public ToClient(final FriendlyByteBuf buffer) {
            super(buffer);
        }

        @Override
        protected void handleMessage(final NetworkEvent.Context context) {
            MessageUtils.withClientTileEntityAt(pos, BusCableTileEntity.class,
                    (tileEntity) -> tileEntity.setInterfaceName(side, value));
        }
    }

    public static final class ToServer extends BusInterfaceNameMessage {
        public ToServer(final BusCableTileEntity tileEntity, final Direction side, final String value) {
            super(tileEntity, side, value);
        }

        public ToServer(final FriendlyByteBuf buffer) {
            super(buffer);
        }

        @Override
        protected void handleMessage(final NetworkEvent.Context context) {
            MessageUtils.withNearbyServerTileEntityAt(context, pos, BusCableTileEntity.class,
                    (tileEntity) -> tileEntity.setInterfaceName(side, value));
        }
    }
}
