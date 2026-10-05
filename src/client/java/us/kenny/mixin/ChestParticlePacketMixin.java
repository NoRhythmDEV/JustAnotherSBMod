package us.kenny.mixin;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import us.kenny.mining.PowderChestHelper;

@Mixin(ClientPacketListener.class)
public class ChestParticlePacketMixin {
    @Inject(method = "handleParticleEvent", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V",
            shift = At.Shift.AFTER), cancellable = true)
    private void farmthingy$particle(ClientboundLevelParticlesPacket packet, CallbackInfo ci) {
        if (PowderChestHelper.onParticle(packet)) ci.cancel();
    }
}
