package li.cil.oc2.common.network.message;

import li.cil.oc2.common.bus.device.item.FileImportExportCardItemDevice;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class ServerCanceledImportFileMessage extends AbstractMessage {
    private int id;

    ///////////////////////////////////////////////////////////////////

    public ServerCanceledImportFileMessage(final int id) {
        this.id = id;
    }

    public ServerCanceledImportFileMessage(final FriendlyByteBuf buffer) {
        super(buffer);
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    public void fromBytes(final FriendlyByteBuf buffer) {
        id = buffer.readVarInt();
    }

    @Override
    public void toBytes(final FriendlyByteBuf buffer) {
        buffer.writeVarInt(id);
    }

    ///////////////////////////////////////////////////////////////////

    @Override
    protected void handleMessage(final Supplier<NetworkEvent.Context> context) {
        FileImportExportCardItemDevice.cancelImport(context.get().getSender(), id);
    }
}
