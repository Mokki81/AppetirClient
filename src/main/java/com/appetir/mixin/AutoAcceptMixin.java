package com.appetir.mixin;

import com.appetir.modules.impl.AutoAccept;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Pattern;

@Mixin(ClientPlayNetworkHandler.class)
public class AutoAcceptMixin {

    private static long lastAcceptMs = 0L;
    private static final long COOLDOWN_MS = 800L;

    // Common TPA request phrasing (EN / RU). Avoid bare "tpa" / "teleport".
    private static final Pattern[] REQUEST_PATTERNS = {
            Pattern.compile(".*has requested (to )?(teleport|tpa).*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*requested to teleport to you.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*wants to teleport.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*tpa request.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*запросил(а)? телепорт.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*хочет телепортироваться.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*запрос на телепорт.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*принял(а)? бы телепорт.*", Pattern.CASE_INSENSITIVE),
    };

    // Messages that mention teleport but are NOT requests
    private static final Pattern[] REJECT_PATTERNS = {
            Pattern.compile(".*teleportation disabled.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*cannot use /tpa.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*teleport.*denied.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*teleport.*cancelled.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*already teleporting.*", Pattern.CASE_INSENSITIVE),
            Pattern.compile(".*телепорт.*(отключ|запрещ|отмен).*", Pattern.CASE_INSENSITIVE),
    };

    @Inject(method = "onGameMessage", at = @At("HEAD"))
    private void onMessage(GameMessageS2CPacket packet, CallbackInfo ci) {
        if (!AutoAccept.enabled_flag) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        long now = System.currentTimeMillis();
        if (now - lastAcceptMs < COOLDOWN_MS) return;

        String msg;
        try {
            msg = packet.getMessage().getString();
        } catch (Exception e) {
            return;
        }
        if (msg == null || msg.isEmpty()) return;

        for (Pattern p : REJECT_PATTERNS) {
            if (p.matcher(msg).matches()) return;
        }

        boolean request = false;
        for (Pattern p : REQUEST_PATTERNS) {
            if (p.matcher(msg).matches()) {
                request = true;
                break;
            }
        }
        if (!request) return;

        lastAcceptMs = now;
        try {
            mc.player.sendChatMessage("/tpaccept");
        } catch (Exception e) {
            System.err.println("[Appetir] AutoAccept failed: " + e.getMessage());
        }
    }
}
